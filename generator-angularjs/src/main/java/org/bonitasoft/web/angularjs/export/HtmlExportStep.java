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
package org.bonitasoft.web.angularjs.export;

import static java.nio.file.Paths.get;
import static org.bonitasoft.web.designer.common.export.Zipper.ALL_DIRECTORIES;
import static org.bonitasoft.web.designer.common.export.Zipper.ALL_FILES;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.bonitasoft.web.angularjs.rendering.DefaultHtmlGenerator;
import org.bonitasoft.web.designer.common.export.ExportStep;
import org.bonitasoft.web.designer.common.export.Zipper;
import org.bonitasoft.web.designer.model.page.Page;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtmlExportStep implements ExportStep<Page> {

    private final DefaultHtmlGenerator generator;
    private final Path exportBackendResources;

    @Override
    public void execute(Zipper zipper, Page page) throws IOException {

        zipper.addDirectoryToZip(
                get(exportBackendResources.toUri()),
                ALL_DIRECTORIES,
                ALL_FILES,
                RESOURCES);

        byte[] html = generator.generateHtml(page).getBytes(StandardCharsets.UTF_8);
        zipper.addToZip(html, RESOURCES + "/index.html");
    }
}
