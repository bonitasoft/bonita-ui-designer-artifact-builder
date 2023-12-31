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
package org.bonitasoft.web.designer.controller.export.steps;

import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.*;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.angularjs.export.Minifier;
import org.bonitasoft.web.angularjs.export.WidgetsExportStep;
import org.bonitasoft.web.angularjs.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.designer.common.export.Zipper;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.utils.rule.TemporaryWidgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WidgetsExportStepTest {

    @Mock
    private FragmentRepository fragmentRepository;

    private final WorkspaceProperties workspaceProperties = new WorkspaceProperties();

    public TemporaryWidgetRepository repository = new TemporaryWidgetRepository(workspaceProperties);

    private WidgetsExportStep step;

    @Mock
    private Zipper zipper;

    @Mock
    private DirectiveFileGenerator directiveFileGenerator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void beforeEach() {
        step = new WidgetsExportStep(
                tempDir,
                new WidgetIdVisitor(fragmentRepository), directiveFileGenerator);
        zipper = spy(new Zipper(mock(OutputStream.class)));
        try {
            repository.init(tempDir);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void should_add_page_widgets_to_zip() throws Exception {
        repository.addWidget(aWidget().withId("widget1"));
        repository.addWidget(aWidget().withId("widget2"));
        Page page = aPage().with(
                aComponent("widget1"),
                aComponent("widget2"))
                .build();
        String content = "Mon   content   to                 minify";
        byte[] expected = Minifier.minify(content.getBytes());
        when(directiveFileGenerator.getWidgetsFilesUsedInPage(page))
                .thenReturn(Arrays.asList(Paths.get("widget1"), Paths
                        .get("widget2")));
        when(directiveFileGenerator.concatenate(Arrays.asList(Paths.get("widget1"), Paths
                .get("widget2")))).thenReturn(content.getBytes());

        step.execute(zipper, page);

        verify(zipper).addToZip(repository.resolveWidgetJson("widget1"), "resources/widgets/widget1/widget1.json");
        verify(zipper).addToZip(repository.resolveWidgetJson("widget2"), "resources/widgets/widget2/widget2.json");
        verify(zipper).addToZip(expected, "resources/js/widgets-" + DigestUtils.sha1Hex(expected) + ".min.js");
    }

    @Test
    void should_not_add_widget_metadata_to_zip() throws Exception {
        repository.addWidget(aWidget().withId("widget"));
        Page page = aPage().with(aComponent("widget")).build();
        String content = "content";
        when(directiveFileGenerator.getWidgetsFilesUsedInPage(page)).thenReturn(Arrays.asList(Paths.get("widget")));
        when(directiveFileGenerator.concatenate(Arrays.asList(Paths.get("widget")))).thenReturn(content.getBytes());

        step.execute(zipper, page);

        verify(zipper, never()).addToZip(repository.resolveWidgetMetadata("widget"), "resources/widgets/widget/widget" +
                ".metadata.json");
    }
}
