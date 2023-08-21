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
import static java.nio.file.Files.exists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Validation;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PageRepositoryTest {

    @TempDir
    public Path temporaryFolder;

    private JsonFileBasedPersister<Page> persister;

    private JsonFileBasedLoader<Page> loader;

    private PageRepository repository;

    private Path pageDir;

    @BeforeEach
    void setUp() throws Exception {
        JsonHandler jsonHandler = new JsonHandlerFactory().create();
        BeanValidator validator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());

        pageDir = temporaryFolder;

        persister = spy(new JsonFileBasedPersister<>(jsonHandler, validator, null, null));
        loader = spy(new JsonFileBasedLoader<>(jsonHandler, Page.class));

        repository = new PageRepository(
                pageDir,
                Path.of("./target/test-classes/templates"),
                persister,
                loader,
                validator,
                Mockito.mock(Watcher.class));
    }

    private Page addToRepository(PageBuilder page) throws Exception {
        return addToRepository(page.build());
    }

    private Page addToRepository(Page page) throws Exception {
        Path repo = createDirectory(temporaryFolder.resolve(page.getId()));
        persister.save(repo, page);
        return page;
    }

    @Test
    void should_get_a_page_from_a_json_file_repository() throws Exception {
        Page expectedPage = PageBuilder.aFilledPage("page-id");
        addToRepository(expectedPage);

        Page fetchedPage = repository.get(expectedPage.getId());

        assertThat(fetchedPage).isEqualTo(expectedPage);
    }

    @Test
    void should_throw_NotFoundException_when_getting_an_inexisting_page() throws Exception {
        assertThrows(NotFoundException.class, () -> repository.get("page-id-unknown"));
    }

    @Test
    void should_get_all_page_from_repository_empty() throws Exception {
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    void should_get_all_page_from_repository() throws Exception {
        Page expectedPage = PageBuilder.aFilledPage("page-id");
        addToRepository(expectedPage);

        List<Page> fetchedPages = repository.getAll();

        assertThat(fetchedPages).containsExactly(expectedPage);
    }

    @Test
    void should_save_a_page_in_a_json_file_repository() throws Exception {
        Page page = PageBuilder.aFilledPage("page-id");
        assertThat(pageDir.resolve(page.getId()).resolve(page.getId() + ".json").toFile()).doesNotExist();

        repository.updateLastUpdateAndSave(page);

        //A json file has to be created in the repository
        assertThat(pageDir.resolve(page.getId()).resolve(page.getId() + ".json").toFile()).exists();
        assertThat(page.getLastUpdate()).isAfter(Instant.now().minus(5000, ChronoUnit.MILLIS));
        assertThat(exists(repository.resolvePath(page.getId()).resolve("assets/css/style.css"))).isTrue();
    }

    @Test
    void should_give_new_id_if_there_is_already_a_page_with_same_id() throws Exception {
        Page page = PageBuilder.aFilledPage("pageName");
        repository.updateLastUpdateAndSave(page);

        String newPageId = repository.getNextAvailableId("pageName");

        assertThat(newPageId).isEqualTo("pageName1");
    }

    @Test
    void should_keep_page_name_id_if_there_is_no_page_with_same_id() throws Exception {
        String newPageId = repository.getNextAvailableId("pageName");

        assertThat(newPageId).isEqualTo("pageName");
    }

    @Test
    void should_throw_RepositoryException_when_error_occurs_while_saving_a_page() throws Exception {
        Page expectedPage = PageBuilder.aFilledPage("page-id");
        Path pagePath = pageDir.resolve(expectedPage.getId());
        Mockito.doThrow(new IOException()).when(persister).save(pagePath, expectedPage);

        assertThrows(RepositoryException.class, () -> repository.updateLastUpdateAndSave(expectedPage));
    }

    @Test
    void should_save_a_page_without_updating_last_update_date() throws Exception {
        Page page = repository
                .updateLastUpdateAndSave(PageBuilder.aPage().withId("page-id").withName("thePageName").build());
        Instant lastUpdate = page.getLastUpdate();

        page.setName("newName");
        repository.save(page);

        Page fetchedPage = repository.get(page.getId());
        assertThat(fetchedPage.getLastUpdate()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.MILLIS));
        assertThat(fetchedPage.getName()).isEqualTo("newName");
    }

    @Test
    void should_throw_IllegalArgumentException_while_saving_a_page_with_no_id_set() throws Exception {
        Page expectedPage = PageBuilder.aPage().withId(null).build();
        assertThrows(IllegalArgumentException.class, () -> repository.updateLastUpdateAndSave(expectedPage));
    }

    @Test
    void should_throw_ConstraintValidationException_while_saving_a_page_with_bad_name() throws Exception {
        Page expectedPage = PageBuilder.aPage().withId("page-id").withName("éé&é&z").build();

        assertThrows(ConstraintValidationException.class, () -> repository.updateLastUpdateAndSave(expectedPage));
    }

    @Test
    void should_save_all_page_in_a_json_file_repository() throws Exception {
        Page expectedPage = PageBuilder.aFilledPage("page-id");

        assertThat(pageDir.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile())
                .doesNotExist();
        repository.saveAll(Collections.singletonList(expectedPage));

        //A json file has to be created in the repository
        assertThat(pageDir.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile()).exists();
        assertThat(expectedPage.getLastUpdate()).isAfter(Instant.now().minus(5000, ChronoUnit.MILLIS));
    }

    @Test
    void should_delete_a_page_with_his_json_file_repository() throws Exception {
        Page expectedPage = PageBuilder.aFilledPage("page-id");
        addToRepository(expectedPage);

        assertThat(pageDir.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile()).exists();
        repository.delete(expectedPage.getId());
        assertThat(pageDir.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile())
                .doesNotExist();
    }

    @Test
    void should_delete_page_metadata_when_deleting_a_page() throws Exception {
        Page expectedPage = addToRepository(PageBuilder.aFilledPage("page-id"));
        assertThat(pageDir.resolve(".metadata").resolve(expectedPage.getId() + ".json").toFile()).exists();

        repository.delete(expectedPage.getId());

        assertThat(pageDir.resolve(".metadata").resolve(expectedPage.getId() + ".json").toFile()).doesNotExist();
    }

    @Test
    void should_throw_NotFoundException_when_deleting_inexisting_page() throws Exception {
        assertThrows(NotFoundException.class, () -> repository.delete("foo"));
    }

    @Test
    void should_throw_RepositoryException_when_error_occurs_on_object_included_search_list() throws Exception {
        Page expectedPage = PageBuilder.aFilledPage("page-id");
        Mockito.doThrow(new IOException()).when(loader).findByObjectId(pageDir, expectedPage.getId());
        assertThrows(RepositoryException.class, () -> repository.findByObjectId(expectedPage.getId()));

    }

    @Test
    void should_mark_a_page_as_favorite() throws Exception {
        Page page = addToRepository(PageBuilder.aPage().notFavorite());

        repository.markAsFavorite(page.getId());

        Page fetchedPage = repository.get(page.getId());
        assertThat(fetchedPage.isFavorite()).isTrue();
    }

    @Test
    void should_unmark_a_page_as_favorite() throws Exception {
        Page page = addToRepository(PageBuilder.aPage().favorite());

        repository.unmarkAsFavorite(page.getId());

        Page fetchedPage = repository.get(page.getId());
        assertThat(fetchedPage.isFavorite()).isFalse();
    }

    @Test
    void should_refresh_repository() throws Exception {
        Page page = addToRepository(PageBuilder.aPage());
        pageDir.resolve(".metadata").resolve(page.getId() + ".json").toFile().delete();
        pageDir.resolve(".metadata").resolve(".index.json").toFile().delete();

        repository.refresh(page.getId());

        Page fetchedPage = repository.get(page.getId());
        assertThat(fetchedPage.isFavorite()).isFalse();
        assertThat(pageDir.resolve(".metadata").resolve(".index.json").toFile()).exists();
    }

    @Test
    void should_refreshIndexing_repository() throws Exception {
        List<Page> pages = new ArrayList<>();
        Page page = PageBuilder.aPage().withUUID("baz-uuid").withId("page1").build();
        Page page2 = PageBuilder.aPage().withUUID("foo-uuid").withId("page2").withName("page2").build();
        pages.add(page);
        pages.add(page2);

        repository.refreshIndexing(pages);

        Mockito.verify(persister, Mockito.times(1)).refreshIndexing(pageDir.resolve(".metadata"), pages);
    }

    @Test
    void should_return_artefacts_name_which_contains_widget_id() throws Exception {
        Page page = PageBuilder.aPage()
                .withUUID("baz-uuid")
                .withId("page1")
                .with(aComponent().withWidgetId("aInput"))
                .build();
        Page page2 = PageBuilder.aPage()
                .withUUID("foo-uuid")
                .withId("page2")
                .withName("page2")
                .with(aComponent().withWidgetId("aInput"))
                .build();

        when(loader.findByObjectId(pageDir, "aInput")).thenReturn(Arrays.asList(page, page2));
        var result = repository.getArtifactsUsingWidget("aInput");

        assertThat(result).containsExactly(page, page2);
    }

    @Test
    void should_return_artefact_name_which_using_widget_id() throws Exception {
        Page page = PageBuilder.aPage()
                .withUUID("baz-uuid")
                .withId("page1")
                .with(aComponent().withWidgetId("aInput"))
                .build();
        Page page2 = PageBuilder.aPage()
                .withUUID("foo-uuid")
                .withId("page2")
                .withName("page2")
                .with(aComponent().withWidgetId("aInput"))
                .with(aComponent().withWidgetId("aCheckbox"))
                .build();

        when(loader.findByObjectIds(pageDir, Arrays.asList("aInput", "aCheckbox")))
                .thenReturn(
                        Map.of("aInput", Arrays.asList(page, page2), "aCheckbox", Collections.singletonList(page2)));
        var result = repository.getArtifactsUsingWidgets(List.of("aInput", "aCheckbox"));

        assertThat(result.get("aInput")).containsExactly(page, page2);
        assertThat(result.get("aCheckbox")).containsExactly(page2);
    }

    @Test
    void should_throw_RepositoryException_when_error_occurs_while_refresh_a_page() throws Exception {
        Page expectedPage = PageBuilder.aFilledPage("page-id");
        Path pagePath = pageDir.resolve(expectedPage.getId());
        doThrow(new IOException()).when(persister).save(pagePath,expectedPage);

        assertThrows(RepositoryException.class, () -> repository.updateLastUpdateAndSave(expectedPage));
    }

}
