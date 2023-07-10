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
package org.bonitasoft.web.designer.migration;

import static java.lang.String.format;
import static java.nio.file.Files.write;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.common.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.Repository;
import org.bonitasoft.web.designer.common.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widgets.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LiveRepositoryUpdateTest {

    JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Mock
    JsonFileBasedPersister<Page> persister;

    JsonFileBasedLoader<Page> loader = new JsonFileBasedLoader<>(jsonHandler, Page.class);

    @Mock
    BeanValidator beanValidator;

    PageRepository repository;

    @TempDir
    Path folder;

    @BeforeEach
    public void setUp() throws Exception {
        repository = new PageRepository(folder, folder, persister, loader,
                beanValidator, mock(Watcher.class));
    }

    @Test
    public void should_migrate_a_page() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Page> migration = new Migration<Page>("2.1", mockMigrationStep);
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository,
                singletonList(migration));
        Page page = createPage("2.0");
        Optional<MigrationStepReport> stepReport = Optional
                .of(new MigrationStepReport(MigrationStatus.SUCCESS, "pageJson"));
        when(mockMigrationStep.migrate(page)).thenReturn(stepReport);

        liveRepositoryUpdate.migrate();

        page.setModelVersion("2.1");
        verify(persister).save(folder.resolve("pageJson"), page);
    }

    @Test
    public void should_not_migrate_file_which_are_not_json() throws Exception {
        Migration<Page> migration = mock(Migration.class);
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository,
                singletonList(migration));
        Files.createFile(folder.resolve("whatever"));

        liveRepositoryUpdate.migrate();

        verify(migration, never()).migrate(any(Page.class));
    }

    @Test
    public void should_not_save_an_artifact_already_migrated() throws Exception {
        Migration<Page> migration = new Migration<>("1.0.2", mock(MigrationStep.class));
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository,
                singletonList(migration));
        createPage("1.0.2");

        liveRepositoryUpdate.migrate();

        verify(persister, never()).save(any(Path.class), any(Page.class));
    }

    @Test
    public void should_exclude_assets() throws Exception {
        Migration<Page> migration = mock(Migration.class);
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository,
                singletonList(migration));
        createPage("1.0.0");
        var pageFolder = Files.createDirectory(folder.resolve("pageJson").resolve("assets"));
        Files.createFile(pageFolder.resolve("whatever.json"));

        liveRepositoryUpdate.migrate();

        verify(migration, only()).migrate(any(Page.class));
    }

    @Test
    public void should_be_refresh_repository_index_json_on_start() throws Exception {
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, EMPTY_LIST);
        createPage("1.7.25");

        liveRepositoryUpdate.start();

        verify(persister).saveInIndex(nullable(Path.class), any(Page.class));
        verify(persister).updateMetadata(any(Path.class), any(Page.class));
    }

    @Test
    public void should_order_LiveRepositoryUpdate() {
        LiveRepositoryUpdate<Page> pageLiveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, EMPTY_LIST);

        Repository<Widget> wRepo = new WidgetRepository(folder, folder,
                mock(JsonFileBasedPersister.class), mock(WidgetFileBasedLoader.class), beanValidator,
                mock(Watcher.class));

        LiveRepositoryUpdate<Widget> widgetLiveRepositoryUpdate = new LiveRepositoryUpdate<>(wRepo, EMPTY_LIST);

        List<LiveRepositoryUpdate> liveRepoList = new ArrayList<>();
        liveRepoList.add(pageLiveRepositoryUpdate);
        liveRepoList.add(widgetLiveRepositoryUpdate);

        Assertions.assertThat(liveRepoList).containsExactly(pageLiveRepositoryUpdate, widgetLiveRepositoryUpdate);
        Assertions.assertThat(liveRepoList.stream().sorted().collect(Collectors.toList()))
                .containsExactly(widgetLiveRepositoryUpdate, pageLiveRepositoryUpdate);
    }

    private Page createPage(String version) throws IOException {
        var pageJson = Files.createDirectory(folder.resolve("pageJson"));
        Path descriptor = Files.createFile(pageJson.resolve("pageJson.json"));
        write(descriptor, format("{ \"id\": \"pageJson\", \"modelVersion\": \"%s\" }", version).getBytes());

        return loader.load(descriptor);
    }
}
