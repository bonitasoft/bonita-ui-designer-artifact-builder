/** 
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.common.repository;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.bonitasoft.web.designer.SimpleDesignerArtifact;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.SimpleObjectBuilder;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonFileBasedPersisterTest {

    private static final String DESIGNER_VERSION = "1.0.0";
    private static final String MODEL_VERSION = "2.0";

    private Path repoDirectory;
    private JsonHandler jsonHandler;
    private JsonFileBasedPersister<SimpleDesignerArtifact> jsonFileBasedPersister;

    @Mock
    private BeanValidator validator;

    @TempDir
    Path temporaryFolder;

    @BeforeEach
    void setUp() throws IOException {
        repoDirectory = createDirectory(temporaryFolder.resolve("jsonrepository"));
        jsonHandler = spy(new JsonHandlerFactory().create());
        jsonHandler = spy(new JsonHandlerFactory().create());
        jsonFileBasedPersister = new JsonFileBasedPersister<>(jsonHandler, validator, DESIGNER_VERSION, MODEL_VERSION);
    }

    private SimpleDesignerArtifact getFromRepository(String id) throws IOException {
        byte[] json = readAllBytes(repoDirectory.resolve(id + ".json"));
        return jsonHandler.fromJson(json, SimpleDesignerArtifact.class);
    }

    @Test
    void should_serialize_an_object_and_save_it_to_a_file() throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);

        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject).isEqualTo(expectedObject);
    }

    @Test
    void should_not_set_model_version_while_saving_if_uid_version_does_not_support_model_version()
            throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);

        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getModelVersion()).isNull();
    }

    @Test
    void should_set_model_version_while_saving_if_not_already_set() throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);
        jsonFileBasedPersister.version = "1.12.0";
        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getModelVersion()).isEqualTo(MODEL_VERSION);

    }

    @Test
    void should_not_set_model_version_while_saving_if_already_set() throws Exception {
        jsonFileBasedPersister.version = "1.12.0";

        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);
        expectedObject.setModelVersion("alreadySetModelVersion");

        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getModelVersion()).isEqualTo("alreadySetModelVersion");

    }

    @Test
    void should_throw_IOException_when_error_occurs_while_saving_a_object() throws Exception {
        Mockito.doThrow(new RuntimeException()).when(jsonHandler).toJson(ArgumentMatchers.any(),
                ArgumentMatchers.any(Class.class));

        assertThrows(IOException.class, () -> jsonFileBasedPersister.save(repoDirectory, new SimpleDesignerArtifact()));
    }

    @Test
    void should_validate_beans_before_saving_them() throws Exception {
        Mockito.doThrow(ConstraintValidationException.class).when(validator)
                .validate(ArgumentMatchers.any(Object.class));
        var content = new SimpleDesignerArtifact("object1", "object1", 1);
        try {
            jsonFileBasedPersister.save(repoDirectory, content);
            failBecauseExceptionWasNotThrown(ConstraintValidationException.class);
        } catch (ConstraintValidationException e) {
            // should not have saved object1
            assertThat(repoDirectory.resolve("object1.json").toFile()).doesNotExist();
        }
    }

    @Test
    void should_persist_metadata_in_a_seperate_file() throws Exception {
        SimpleDesignerArtifact artifact = SimpleObjectBuilder.aSimpleObjectBuilder()
                .id("baz")
                .metadata("foobar")
                .build();

        jsonFileBasedPersister.save(repoDirectory, artifact);

        assertThat(new String(readAllBytes(repoDirectory.getParent().resolve(".metadata/baz.json"))))
                .isEqualTo("{\"favorite\":false,\"metadata\":\"foobar\"}");
    }

    @Test
    void should_support_parrelel_index_saves() throws Exception {
        Page page1 = PageBuilder.aPage().withUUID("baz-uuid").withName("baz").withId("baz-id").build();
        Page page2 = PageBuilder.aPage().withUUID("foo-uuid").withName("foo").withId("foo-id").build();
        JsonFileBasedPersister<Page> pageRepository = new JsonFileBasedPersister<Page>(jsonHandler, validator,
                DESIGNER_VERSION, MODEL_VERSION);
        Path metadataFolder = repoDirectory.resolve(".metadata");
        new Thread(() -> {
            try {
                pageRepository.saveInIndex(metadataFolder, page2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                pageRepository.saveInIndex(metadataFolder, page1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    String index = new String(readAllBytes(metadataFolder.resolve(".index.json")));
                    assertThat(index).contains("\"baz-uuid\":\"baz-id\"").contains("\"foo-uuid\":\"foo-id\"");
                });
    }

    @Test
    void should_persist_all_artifact_id_in_index_when_refresh_indexing_is_called() throws Exception {
        List<Page> pages = new ArrayList<>();
        Page page = PageBuilder.aPage().withUUID("baz-uuid").withId("page1").build();
        Page page2 = PageBuilder.aPage().withUUID("foo-uuid").withId("page2").withName("page2").build();
        pages.add(page);
        pages.add(page2);
        JsonFileBasedPersister<Page> pageRepository = new JsonFileBasedPersister<>(jsonHandler, validator,
                DESIGNER_VERSION, MODEL_VERSION);
        Path metadataFolder = repoDirectory.resolve(".metadata");
        new Thread(() -> {
            try {
                pageRepository.refreshIndexing(metadataFolder, pages);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(new ThrowingRunnable() {

                    @Override
                    public void run() throws Throwable {
                        String index = new String(readAllBytes(metadataFolder.resolve(".index.json")));
                        assertThat(index).contains("\"baz-uuid\":\"page1\"").contains("\"foo-uuid\":\"page2\"");
                    }
                });
    }

}
