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
package org.bonitasoft.web.designer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.UUID;

import org.bonitasoft.web.designer.common.generator.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.PageImporter;
import org.bonitasoft.web.designer.controller.importer.WidgetImporter;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.MigrationStatusReport;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.workspace.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultArtifactBuilderTest {

    private DefaultArtifactBuilder artifactBuilder;

    @Mock
    private PageService pageService;
    @Mock
    private FragmentService fragmentService;
    @Mock
    private WidgetService widgetService;

    @BeforeEach
    void setUp() throws Exception {

        artifactBuilder = new DefaultArtifactBuilder(
                // Workspace management
                mock(Workspace.class),
                widgetService,
                fragmentService,
                pageService,
                // Export
                mock(PageExporter.class),
                mock(FragmentExporter.class),
                mock(WidgetExporter.class),
                mock(HtmlGenerator.class),
                // Import
                new ImportStore(),
                mock(PageImporter.class),
                mock(FragmentImporter.class),
                mock(WidgetImporter.class));
    }

    @Test
    void build_page_should_call_page_service() throws ModelException, IOException {
        // Given
        var id = UUID.randomUUID().toString();
        when(pageService.get(id)).thenReturn(aPage().withId(id).withName(id).build());

        // When
        artifactBuilder.buildPage(id);

        // Then
        verify(pageService).get(id);
    }

    @Test
    void build_fragment_should_call_page_service() throws ModelException, IOException {
        // Given
        var id = UUID.randomUUID().toString();
        when(fragmentService.get(id)).thenReturn(aFragment().withId(id).withName(id).build());

        // When
        artifactBuilder.buildFragment(id);

        // Then
        verify(fragmentService).get(id);
    }

    @Test
    void build_widget_should_call_page_service() throws ModelException, IOException {
        // Given
        var id = UUID.randomUUID().toString();
        when(widgetService.get(id)).thenReturn(aWidget().withId(id).withName(id).build());

        // When
        artifactBuilder.buildWidget(id);

        // Then
        verify(widgetService).get(id);
    }

    @Test
    void get_page_status_should_call_page_service() {
        // Given
        var status = new MigrationStatusReport();
        var page = aPage().build();
        when(pageService.getStatus(page)).thenReturn(status);

        // When
        var result = artifactBuilder.getPageStatus(page);

        // Then
        verify(pageService).getStatus(page);
        assertThat(result).isEqualTo(status);
    }

    @Test
    void get_page_status_by_id_should_call_page_service() {
        // Given
        var id = UUID.randomUUID().toString();
        var status = new MigrationStatusReport();
        var page = aPage().withId(id).withMigrationStatusReport(status).build();
        when(pageService.get(id)).thenReturn(page);

        // When
        var result = artifactBuilder.getPageStatus(id);

        // Then
        verify(pageService).get(id);
        assertThat(result).isEqualTo(status);
    }

    @Test
    void get_fragment_status_should_call_fragment_service() {
        // Given
        var status = new MigrationStatusReport();
        var fragment = aFragment().build();
        when(fragmentService.getStatus(fragment)).thenReturn(status);

        // When
        var result = artifactBuilder.getFragmentStatus(fragment);

        // Then
        verify(fragmentService).getStatus(fragment);
        assertThat(result).isEqualTo(status);
    }

    @Test
    void get_fragment_status_by_id_should_call_fragment_service() {
        // Given
        var id = UUID.randomUUID().toString();
        var status = new MigrationStatusReport();
        var fragment = aFragment().withId(id).withMigrationStatusReport(status).build();
        when(fragmentService.get(id)).thenReturn(fragment);

        // When
        var result = artifactBuilder.getFragmentStatus(id);

        // Then
        verify(fragmentService).get(id);
        assertThat(result).isEqualTo(status);
    }

    @Test
    void get_widget_status_should_call_widget_service() {
        // Given
        var status = new MigrationStatusReport();
        var widget = aWidget().build();
        when(widgetService.getStatus(widget)).thenReturn(status);

        // When
        var result = artifactBuilder.getWidgetStatus(widget);

        // Then
        verify(widgetService).getStatus(widget);
        assertThat(result).isEqualTo(status);
    }

    @Test
    void get_widget_status_by_id_should_call_widget_service() {
        // Given
        var id = UUID.randomUUID().toString();
        var status = new ArtifactStatusReport();
        var widget = aWidget().withId(id).withArtifactStatusReport(status).build();
        when(widgetService.get(id)).thenReturn(widget);

        // When
        var result = artifactBuilder.getWidgetStatus(id);

        // Then
        verify(widgetService).get(id);
        assertThat(result).isEqualTo(status);
    }

}
