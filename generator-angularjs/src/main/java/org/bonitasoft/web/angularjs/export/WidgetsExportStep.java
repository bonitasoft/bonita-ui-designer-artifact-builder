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

import static org.bonitasoft.web.designer.common.export.Zipper.ALL_FILES;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.angularjs.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.designer.common.export.ExportStep;
import org.bonitasoft.web.designer.common.export.Zipper;
import org.bonitasoft.web.designer.common.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.model.page.AbstractPage;

public class WidgetsExportStep<T extends AbstractPage> implements ExportStep<T> {

    private final Path widgetsPath;
    private final WidgetIdVisitor widgetIdVisitor;
    private final DirectiveFileGenerator directiveFileGenerator;

    public WidgetsExportStep(Path widgetsPath, WidgetIdVisitor widgetIdVisitor,
            DirectiveFileGenerator directiveFileGenerator) {
        this.widgetsPath = widgetsPath;
        this.widgetIdVisitor = widgetIdVisitor;
        this.directiveFileGenerator = directiveFileGenerator;
    }

    @Override
    public void execute(Zipper zipper, T page) throws IOException {
        zipper.addDirectoryToZip(
                widgetsPath,
                new IncludeChildDirectoryPredicate(widgetsPath, widgetIdVisitor.visit(page)),
                ALL_FILES,
                RESOURCES + "/widgets");

        // Export widgets.js
        List<Path> files = directiveFileGenerator.getWidgetsFilesUsedInPage(page);
        byte[] content = directiveFileGenerator.concatenate(files);
        content = Minifier.minify(content);
        zipper.addToZip(content, RESOURCES + "/js/widgets-" + DigestUtils.sha1Hex(content) + ".min.js");
    }
}
