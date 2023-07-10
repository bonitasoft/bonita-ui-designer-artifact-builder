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
package org.bonitasoft.web.designer.workspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.angularjs.GeneratorProperties;
import org.bonitasoft.web.angularjs.workspace.FragmentDirectiveBuilder;
import org.bonitasoft.web.angularjs.workspace.WidgetDirectiveBuilder;
import org.bonitasoft.web.designer.common.GeneratorStrategy;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.migration.LiveRepositoryUpdate;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkspaceInitializerTest {

    private Path extractPath;

    @TempDir
    public Path temporaryFolder;

    @Mock
    private LiveRepositoryUpdate<Page> pageRepositoryLiveUpdate;

    @Mock
    private LiveRepositoryUpdate<Widget> widgetRepositoryLiveUpdate;
    @Mock
    WidgetRepository widgetRepository;

    private Workspace workspace;

    @Mock
    private GeneratorStrategy generatorStrategy;

    @Mock
    private GeneratorProperties generatorProperties;

    @BeforeEach
    public void setUp() throws IOException {

        extractPath = Files.createDirectory(temporaryFolder.resolve("extract"));

        UiDesignerProperties uiDesignerProperties = newUiDesignerProperties();

        when(widgetRepository.resolvePath(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return uiDesignerProperties.getWorkspace().getWidgets().getDir().resolve(id);
        });

        when(generatorStrategy.getGeneratorProperties()).thenReturn(generatorProperties);
        when(generatorStrategy.widgetFileBuilder()).thenReturn(mock(WidgetDirectiveBuilder.class));
        when(generatorStrategy.fragmentDirectiveBuilder()).thenReturn(mock(FragmentDirectiveBuilder.class));
        when(generatorProperties.getExtractPath()).thenReturn(extractPath);
        PageRepository pageRepository = mock(PageRepository.class);
        AssetDependencyImporter<Widget> widgetAssetDependencyImporter = mock(AssetDependencyImporter.class);

        ResourcesCopier resourcesCopier = new ResourcesCopier();
        List<LiveRepositoryUpdate> migrations = List.of(widgetRepositoryLiveUpdate, pageRepositoryLiveUpdate);
        JsonHandler jsonHandler = new JsonHandlerFactory().create();

        workspace = spy(new Workspace(
                uiDesignerProperties,
                widgetRepository,
                pageRepository,
                generatorStrategy,
                widgetAssetDependencyImporter,
                resourcesCopier,
                migrations,
                jsonHandler));

    }

    private UiDesignerProperties newUiDesignerProperties() throws IOException {
        UiDesignerProperties uiDesignerProperties = new UiDesignerProperties();
        uiDesignerProperties.setModelVersion("2.0");

        WorkspaceProperties workspaceProperties = uiDesignerProperties.getWorkspace();
        final Path fakeProjectFolder = temporaryFolder;
        workspaceProperties.setPath(fakeProjectFolder);
        workspaceProperties.getPages().setDir(Files.createDirectory(temporaryFolder.resolve("pages")));
        workspaceProperties.getWidgets().setDir(Files.createDirectory(temporaryFolder.resolve("widgets")));
        workspaceProperties.getFragments().setDir(Files.createDirectory(temporaryFolder.resolve("fragments")));

        WorkspaceUidProperties workspaceUidProperties = uiDesignerProperties.getWorkspaceUid();
        workspaceUidProperties.setExtractPath(extractPath);

        return uiDesignerProperties;
    }

    @Test
    public void should_initialize_workspace() throws Exception {
        // When
        workspace.initialize();
        // Then
        verify(workspace).doInitialize();
        verify(workspace).cleanPageWorkspace();
        assertThat(workspace.initialized).isTrue();
    }

    @Test
    public void should_start_page_live_migration() throws Exception {
        // When
        workspace.initialize();
        // Then
        verify(pageRepositoryLiveUpdate).start();
    }

    @Test
    public void should_start_widget_live_migration() throws Exception {
        // When
        workspace.initialize();
        // Then
        verify(widgetRepositoryLiveUpdate).start();
    }

    @Test
    public void should_throw_runtimeException_if_error_occurs_while_initializing_workspace() throws Exception {
        doThrow(new IOException()).when(workspace).doInitialize();
        // When
        assertThrows(RuntimeException.class, () -> workspace.initialize());
        // Then
    }
}
