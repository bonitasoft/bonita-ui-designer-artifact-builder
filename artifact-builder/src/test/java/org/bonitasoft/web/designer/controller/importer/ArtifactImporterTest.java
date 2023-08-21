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
package org.bonitasoft.web.designer.controller.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.UNEXPECTED_ZIP_STRUCTURE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.ArtifactBuilderFactory;
import org.bonitasoft.web.designer.UiDesignerCore;
import org.bonitasoft.web.designer.UiDesignerCoreFactory;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.AssetRepository;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.importer.mocks.PageImportMock;
import org.bonitasoft.web.designer.controller.importer.mocks.WidgetImportMock;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.MigrationStatusReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.service.DefaultPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArtifactImporterTest {

    private static final String WIDGETS_FOLDER = "widgets";

    @Mock
    private PageRepository pageRepository;

    @Mock
    private DefaultPageService pageService;

    @Mock
    private WidgetRepository widgetRepository;

    @Spy
    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private UiDesignerProperties uiDesignerProperties;

    private Path pageImportPath;

    private Path widgetImportPath;

    private Path pageUnzippedPath;

    private WidgetImportMock wMocks;

    private PageImportMock pMocks;

    private ArtifactBuilder artifactBuilder;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        pageImportPath = Files.createTempDirectory(tempDir, "pageImport");
        widgetImportPath = Files.createTempDirectory(tempDir, "widgetImport");

        uiDesignerProperties = new UiDesignerProperties();
        uiDesignerProperties.getWorkspace().getPages().setDir(Files.createTempDirectory(tempDir, "pages"));
        uiDesignerProperties.getWorkspace().getWidgets().setDir(Files.createTempDirectory(tempDir, "widgets"));
        uiDesignerProperties.getWorkspace().getFragments()
                .setDir(Files.createTempDirectory(tempDir, "fragments"));

        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(widgetRepository.resolvePath(any())).thenAnswer(invocation -> {
            String widgetId = invocation.getArgument(0);
            return tempDir.resolve(WIDGETS_FOLDER).resolve(widgetId);
        });

        UiDesignerCore core = new UiDesignerCoreFactory(uiDesignerProperties, jsonHandler).create(
                mock(Watcher.class),
                widgetRepository,
                mock(AssetRepository.class),
                mock(FragmentRepository.class),
                pageRepository,
                mock(AssetRepository.class));

        artifactBuilder = new ArtifactBuilderFactory(uiDesignerProperties, jsonHandler, core).create();

        pageUnzippedPath = pageImportPath.resolve("resources");
        Files.createDirectory(pageUnzippedPath);
        when(pageRepository.getComponentName()).thenReturn("page");

        Path widgetUnzippedPath = widgetImportPath.resolve("resources");
        Files.createDirectory(widgetUnzippedPath);

        wMocks = new WidgetImportMock(pageUnzippedPath, widgetRepository);
        pMocks = new PageImportMock(pageRepository, jsonHandler);
    }

    @Test
    void should_import_artifact_located_on_disk() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.updateLastUpdateAndSave(page)).thenReturn(page);

        artifactBuilder.importPage(pageImportPath, true);

        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository, times(3)).updateLastUpdateAndSave(page);
    }

    @Test
    void should_prepare_widget_to_deserialize_on_import_widget() throws Exception {
        Widget widget = spy(aWidget().withId("aWidget").custom().build());
        doReturn(widget).when(jsonHandler).fromJson(any(Path.class), eq(Widget.class), eq(JsonViewPersistence.class));
        when(widgetRepository.updateLastUpdateAndSave(widget)).thenReturn(widget);

        artifactBuilder.importWidget(widgetImportPath, true);

        verify(widget).prepareWidgetToDeserialize(any(Path.class));
    }

    @Test
    void should_return_an_import_report_containing_imported_element_and_imported_dependencies()
            throws Exception {
        var addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        var overridenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.updateLastUpdateAndSave(page)).thenReturn(page);

        //        ImportReport report = pageImporter.doImport(anImport(pageImportPath));
        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getDependencies().getAdded()).containsEntry("widget", new ArrayList<>(addedWidgets));
        assertThat(report.getDependencies().getOverwritten()).containsEntry("widget",
                new ArrayList<>(overridenWidgets));
        assertThat(report.getElement()).isEqualTo(page);
    }

    @Test
    void should_return_an_import_report_saying_that_page_is_going_to_be_overwritten_when_element_already_exists_in_repository()
            throws Exception {
        Page page = pMocks.mockPageToBeImported();
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withName("alreadyHere").build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
    }

    @Test
    void should_return_an_import_report_saying_that_widget_is_going_to_be_overwritten_when_element_already_exists_in_repository()
            throws Exception {
        Widget widget = aWidget().withId("aWidget").custom().build();
        Widget existingWidgetInRepo = aWidget().withId("aWidget").favorite().custom().build();

        when(widgetRepository.exists(widget.getId())).thenReturn(true);
        when(widgetRepository.get(widget.getId())).thenReturn(existingWidgetInRepo);
        doReturn(existingWidgetInRepo).when(jsonHandler).fromJson(any(Path.class), eq(Widget.class),
                eq(JsonViewPersistence.class));
        when(widgetRepository.updateLastUpdateAndSave(existingWidgetInRepo))
                .thenAnswer((Answer<Widget>) invocationOnMock -> {
                    Widget widgetArg = invocationOnMock.getArgument(0);
                    widgetArg.setLastUpdate(Instant.now());
                    return widgetArg;
                });

        final ImportReport report = artifactBuilder.importWidget(widgetImportPath, true);

        assertThat(report.getElement()).isEqualTo(existingWidgetInRepo);
        assertThat(report.isOverwritten()).isTrue();
    }

    @Test
    void should_return_an_import_report_saying_that_element_has_not_been_overwritten_when_element_does_not_exists_in_repository()
            throws Exception {
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(null);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.isOverwritten()).isFalse();
        assertThat(report.getOverwrittenElement()).isNull();
    }

    @Test
    void should_return_an_import_report_saying_that_element_has_been_imported_when_there_are_no_conflict()
            throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository, times(3)).updateLastUpdateAndSave(page);
    }

    @Test
    void should_return_an_import_report_saying_that_element_has_not_been_imported_when_there_are_conflict()
            throws Exception {
        List<Widget> overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();

        final ImportReport report = artifactBuilder.importPage(pageImportPath, false);

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.CONFLICT);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository, never()).updateLastUpdateAndSave(page);
    }

    @Test
    void should_throw_import_exception_when_there_is_no_resource_folder_in_import_path(@TempDir Path tempDir)
            throws Exception {
        Path newFolder = Files.createDirectory(tempDir.resolve("emptyFolder"));

        ImportException exception = assertThrows(ImportException.class,
                () -> artifactBuilder.importPage(newFolder, true));
        assertThat(exception.getType()).isEqualTo(UNEXPECTED_ZIP_STRUCTURE);
    }

    @Test
    void should_throw_server_import_exception_when_error_occurs_while_saving_files_in_repository()
            throws Exception {
        Page page = pMocks.mockPageToBeImported(aPage().withId("aPage"));
        when(pageRepository.updateLastUpdateAndSave(page)).thenThrow(RepositoryException.class);

        assertThrows(ServerImportException.class, () -> artifactBuilder.importPage(pageImportPath, true));
    }

    @Test
    void should_throw_import_exception_when_an_error_occurs_while_getting_widgets() throws Exception {
        Files.createDirectory(pageUnzippedPath.resolve(WIDGETS_FOLDER));
        wMocks.mockWidgetsAsAddedDependencies();
        pMocks.mockPageToBeImported(aPage().withId("aPage"));
        when(widgetRepository.loadAll(pageUnzippedPath.resolve(WIDGETS_FOLDER),
                WidgetRepository.CUSTOM_WIDGET_FILTER)).thenThrow(IOException.class);

        assertThrows(ServerImportException.class, () -> artifactBuilder.importPage(pageImportPath, true));
    }

    @Test
    void should_force_an_import() throws Exception {
        var addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        var overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(false);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getDependencies().getAdded()).containsEntry("widget", new ArrayList<>(addedWidgets));
        assertThat(report.getDependencies().getOverwritten()).containsEntry("widget",
                new ArrayList<>(overriddenWidgets));
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getUUID()).isNotBlank();
        assertThat(UUID.fromString(report.getUUID())).isNotNull();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository, times(3)).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    void should_force_an_import_overwriting_page() throws Exception {
        wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withId("alreadyHere").withName("alreadyHere")
                .build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);
        when(pageRepository.exists(page.getId())).thenReturn(false);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        assertThat(page.getId()).isEqualTo("id");
        verify(pageRepository).delete(existingPageInRepo.getId());
        verify(pageRepository, times(3)).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    void should_force_an_import_when_another_page_with_same_id_exist() throws Exception {
        wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();
        page.setName("myPage");
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withId("alreadyHere").withName("alreadyHere")
                .build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);
        when(pageRepository.exists(page.getId())).thenReturn(true);
        when(pageRepository.getNextAvailableId(page.getName())).thenReturn("myPage1");

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        assertThat(page.getId()).isEqualTo("myPage1");
        verify(pageRepository).delete(existingPageInRepo.getId());
        verify(pageRepository, times(3)).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    void should_return_incompatible_status_if_version_is_not_compatible_with_uid() throws Exception {
        uiDesignerProperties.setModelVersion("11.0.0");
        wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported(aPage().withName("myPage").withId("myPage").withModelVersion("12.0.0"));
        lenient().when(pageRepository.getNextAvailableId(page.getName())).thenReturn("myPage1");
        when(pageService.getStatusWithoutDependencies(page)).thenReturn(new MigrationStatusReport(false, false));

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.INCOMPATIBLE);
        assertThat(page.getId()).isEqualTo("myPage");
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
    }
}
