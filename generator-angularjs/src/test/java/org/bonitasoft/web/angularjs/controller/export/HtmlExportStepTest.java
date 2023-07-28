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
package org.bonitasoft.web.angularjs.controller.export;

import static java.nio.file.Paths.get;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bonitasoft.web.angularjs.export.HtmlExportStep;
import org.bonitasoft.web.angularjs.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.common.export.Zipper;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HtmlExportStepTest {

    @Mock
    private HtmlGenerator htmlGenerator;

    private HtmlExportStep step;

    @Mock
    private Zipper zipper;

    @Before
    public void setUp() throws Exception {
        Path exportBackendResources = Paths.get("src/test/resources/");
        step = new HtmlExportStep(htmlGenerator, exportBackendResources);
        Mockito.when(htmlGenerator.generateHtml(ArgumentMatchers.any(Page.class))).thenReturn("");
    }

    @Test
    public void should_export_webapp_generator_folder() throws Exception {
        step.execute(zipper, PageBuilder.aPage().build());

        Mockito.verify(zipper).addDirectoryToZip(get(new File("src/test/resources/").toURI()), Zipper.ALL_DIRECTORIES,
                Zipper.ALL_FILES,
                "resources");
    }

    @Test
    public void should_export_generated_html() throws Exception {
        Mockito.when(htmlGenerator.generateHtml(ArgumentMatchers.any(Page.class))).thenReturn("foobar");

        step.execute(zipper, PageBuilder.aPage().build());

        Mockito.verify(zipper).addToZip("foobar".getBytes(), "resources/index.html");
    }
}
