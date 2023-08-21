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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.designer.SimpleDesignerArtifact;
import org.bonitasoft.web.designer.builder.SimpleObjectBuilder;
import org.bonitasoft.web.designer.common.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonFileBasedLoaderTest {

    @TempDir
    Path temporaryFolder;

    private Path repoDirectory;
    private JsonHandler jsonHandler;
    private JsonFileBasedLoader<SimpleDesignerArtifact> loader;

    @BeforeEach
    void setUp() throws IOException {
        repoDirectory = createDirectory(temporaryFolder.resolve("jsonrepository"));
        jsonHandler = spy(new JsonHandlerFactory().create());
        loader = new JsonFileBasedLoader<>(jsonHandler, SimpleDesignerArtifact.class);
    }

    private void addToRepository(SimpleDesignerArtifact... pages) throws Exception {
        for (SimpleDesignerArtifact page : pages) {
            //A page is in its own folder
            Path repo = createDirectories(temporaryFolder.resolve("jsonrepository").resolve(page.getId()));
            write(repo.resolve(page.getId() + ".json"), jsonHandler.toJson(page));
        }
    }

    @Test
    void should_get_an_object_from_a_json_file_and_deserialize_it() throws Exception {
        SimpleDesignerArtifact expectedObject = SimpleObjectBuilder.aFilledSimpleObject("id");
        addToRepository(expectedObject);

        assertThat(loader.get(repoDirectory.resolve("id/id.json"))).isEqualTo(expectedObject);
    }

    @Test
    void test_loader() throws Exception {
        SimpleDesignerArtifact a = new SimpleDesignerArtifact("id", "id", 5);
        a.setDesignerVersion("1.12.0");
        addToRepository(a);
        SimpleDesignerArtifact pouet = loader.get(repoDirectory.resolve("id/id.json"));
        assertThat(pouet.getDesignerVersion()).isEqualTo("1.12.0");
        assertThat(pouet).isEqualTo(a);
    }

    @Test
    void should_throw_RepositoryException_when_error_occurs_while_getting_a_object() throws Exception {
        addToRepository(SimpleObjectBuilder.aFilledSimpleObject("foobar"));
        Mockito.doThrow(new IOException()).when(jsonHandler).fromJson(ArgumentMatchers.any(byte[].class),
                ArgumentMatchers.eq(SimpleDesignerArtifact.class));

        var path = repoDirectory.resolve("foobar/foobar.json");
        assertThrows(RepositoryException.class, () -> loader.get(path));
    }

    @Test
    void should_throw_NotFoundException_when_while_getting_an_unexisting_object() throws Exception {
        var path = repoDirectory.resolve("unexisting/unexisting.json");
        assertThrows(NotFoundException.class, () -> loader.get(path));
    }

    @Test
    void should_get_all_objects_from_json_files_and_deserialize_them() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aFilledSimpleObject("objet1");
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aFilledSimpleObject("objet2");
        addToRepository(object1, object2);

        List<SimpleDesignerArtifact> objects = loader.getAll(repoDirectory);
        assertThat(objects).containsOnly(object1, object2);
    }

    @Test
    void should_throw_IOException_when_error_occurs_while_getting_all_object() throws Exception {
        addToRepository(SimpleObjectBuilder.aFilledSimpleObject("objet1"));
        Mockito.doThrow(new IOException()).when(jsonHandler).fromJson(ArgumentMatchers.any(byte[].class),
                ArgumentMatchers.eq(SimpleDesignerArtifact.class));
        assertThrows(RepositoryException.class, () -> loader.getAll(repoDirectory));

    }

    @Test
    void should_fail_silently_when_an_object_is_not_found_while_getting_all_object() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aFilledSimpleObject("object1");
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aFilledSimpleObject("object2");
        addToRepository(object1, object2);
        Files.delete(repoDirectory.resolve("object1/object1.json"));

        List<SimpleDesignerArtifact> all = loader.getAll(repoDirectory);

        // Expect no exception and results contains only object2
        assertThat(all).containsOnly(object2);
    }

    @Test
    void should_fail_silently_when_a_model_file_is_corrupted_while_getting_all_object() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aFilledSimpleObject("object1");
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aFilledSimpleObject("object2");
        addToRepository(object1, object2);
        Files.write(repoDirectory.resolve("object1/object1.json"), "json corrupted".getBytes());

        List<SimpleDesignerArtifact> all = loader.getAll(repoDirectory);

        // Expect no exception and results contains only object2
        assertThat(all).containsOnly(object2);
    }

    @Test
    void should_find_a_byte_array_in_an_another() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), "exem".getBytes())).isEqualTo(4);
    }

    @Test
    void should_find_a_byte_array_in_an_another_on_start_position() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), "mon ex".getBytes())).isZero();
    }

    @Test
    void should_not_find_a_byte_array_in_an_another() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), "rex".getBytes())).isEqualTo(-1);
    }

    @Test
    void should_not_find_null_in_byte_array() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), null)).isEqualTo(-1);
    }

    @Test
    void should_not_find_occurence_in_byte_array_null() {
        assertThat(loader.indexOf(null, "search".getBytes())).isEqualTo(-1);
    }

    @Test
    void should_find_one_object_included_in_another_and_deserialize_it() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1)
                .build();

        addToRepository(object1, object2);

        List<SimpleDesignerArtifact> objects = loader.findByObjectId(repoDirectory, "objet1");

        assertThat(objects).containsOnly(object2);
    }

    @Test
    void should_returns_an_empty_list_when_directory_does_not_exist() throws Exception {
        assertThat(loader.findByObjectId(get("/does/not/exist"), "objectId")).isEmpty();
    }

    @Test
    void should_not_fail_when_searching_object_by_id_and_repo_contains_an_hidden_file() throws Exception {
        createDirectories(temporaryFolder.resolve("jsonrepository"));
        createDirectories(temporaryFolder.resolve(".DS_Store"));

        assertDoesNotThrow(() -> loader.findByObjectId(repoDirectory, "object"));
    }

    @Test
    void should_not_fail_when_searching_object_by_id_and_artifact_folder_has_no_model_file() throws Exception {
        Files.createDirectory(repoDirectory.resolve("artifactid"));

        List<SimpleDesignerArtifact> objects = loader.findByObjectId(repoDirectory, "object");

        // expect no exception and result list is empty
        assertThat(objects).isEmpty();
    }

    @Test
    void should_not_find_an_object_even_if_id_start_the_same_as_the_one_looking_for() throws Exception {
        addToRepository(SimpleObjectBuilder.aSimpleObjectBuilder().id("abcd").build());

        List<SimpleDesignerArtifact> objects = loader.findByObjectId(repoDirectory, "abc");

        assertThat(objects).isEmpty();
    }

    @Test
    void should_not_find_object_included_in_another() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1)
                .build();

        addToRepository(object1, object2);

        List<SimpleDesignerArtifact> objects = loader.findByObjectId(repoDirectory, "objet2");
        assertThat(objects).isEmpty();
    }

    @Test
    void should_throw_IOException_when_error_occurs_on_finding_object_by_id() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1)
                .build();

        addToRepository(object1, object2);

        Mockito.doThrow(new IOException()).when(jsonHandler).fromJson(ArgumentMatchers.any(byte[].class),
                ArgumentMatchers.eq(SimpleDesignerArtifact.class));
        assertThrows(IOException.class, () -> loader.findByObjectId(repoDirectory, "objet1"));
    }

    @Test
    void should_find_object_included_in_another() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1)
                .build();

        addToRepository(object1, object2);

        //In this test we see the object id in the file not in the name of the file. So we
        //consider that our id is object1
        assertThat(loader.contains(repoDirectory, "objet1")).isTrue();
    }

    @Test
    void should_find_any_object_included_in_another() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleDesignerArtifact object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1)
                .build();

        addToRepository(object1, object2);

        //In this test we see the object id in the file not in the name of the file. So we
        //consider that our id is object1
        assertThat(loader.contains(repoDirectory, "object2")).isFalse();
    }

    @Test
    void should_load_a_single_page_in_the_import_folder() throws Exception {
        SimpleDesignerArtifact object1 = SimpleObjectBuilder.aFilledSimpleObject("objet1");
        addToRepository(object1);

        SimpleDesignerArtifact loadedSimpleVersioned = loader.load(repoDirectory.resolve("objet1/objet1.json"));

        assertThat(loadedSimpleVersioned).isEqualTo(object1);
    }

    @Test
    void should_throw_notfound_exception_when_there_are_no_pages_in_folder() {
        assertThrows(NotFoundException.class, () -> loader.load(repoDirectory.resolve("test")));
    }

    @Test
    void should_throw_json_read_exception_when_loaded_file_is_not_valid_json() throws Exception {
        write(repoDirectory.resolve("wrongjson.json"), "notJson".getBytes());

        assertThrows(JsonReadException.class, () -> loader.load(repoDirectory.resolve("wrongjson.json")));
    }

    @Test
    void should_get_object_metadata() throws Exception {
        SimpleDesignerArtifact expectedObject = SimpleObjectBuilder.aSimpleObjectBuilder().id("id").metadata("foobar")
                .build();
        addToRepository(expectedObject);

        assertThat(loader.get(loader.resolve(repoDirectory, "id")).getMetadata()).isEqualTo("foobar");
    }

    @Test
    void should_not_load_object_metadata() throws Exception {
        SimpleDesignerArtifact expectedObject = SimpleObjectBuilder.aSimpleObjectBuilder().id("id").metadata("foobar")
                .build();
        addToRepository(expectedObject);

        assertThat(loader.load(loader.resolve(repoDirectory, "id")).getMetadata()).isNull();
    }

    @Test
    void should_get_same_object_id_as_page_name() throws Exception {
        assertThat(loader.getNextAvailableObjectId(repoDirectory, "pageNameToUseAsPrefix"))
                .isEqualTo("pageNameToUseAsPrefix");
    }

    @Test
    void should_get_object_id_if_page_name_taken() throws Exception {
        Files.createDirectory(repoDirectory.resolve("pageNameToUseAsPrefix"));
        Files.createDirectory(repoDirectory.resolve("pageNameToUseAsPrefix20"));
        Files.createDirectory(repoDirectory.resolve("pageNameToUseAsPrefix245"));
        assertThat(loader.getNextAvailableObjectId(repoDirectory, "pageNameToUseAsPrefix"))
                .isEqualTo("pageNameToUseAsPrefix246");
    }

    @Test
    void should_get_next_available_object_id() throws Exception {
        Files.createDirectory(repoDirectory.resolve("pageNameToUseAsPrefix"));
        assertThat(loader.getNextAvailableObjectId(repoDirectory, "pageNameToUseAsPrefix"))
                .isEqualTo("pageNameToUseAsPrefix1");
    }
}
