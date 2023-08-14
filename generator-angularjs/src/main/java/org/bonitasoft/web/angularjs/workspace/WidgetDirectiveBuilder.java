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
package org.bonitasoft.web.angularjs.workspace;

import static java.lang.String.valueOf;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.bonitasoft.web.angularjs.rendering.TemplateEngine;
import org.bonitasoft.web.designer.common.livebuild.AbstractLiveFileBuilder;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.WidgetFileBasedLoader;

public class WidgetDirectiveBuilder extends AbstractLiveFileBuilder {

    private final WidgetFileBasedLoader widgetLoader;
    private final HtmlSanitizer htmlSanitizer;
    private final TemplateEngine htmlBuilder = new TemplateEngine("widgetDirectiveTemplate.hbs.js");

    public WidgetDirectiveBuilder(Watcher watcher,
            WidgetFileBasedLoader widgetLoader, boolean isLiveBuildEnabled) {
        super(watcher, isLiveBuildEnabled);
        this.widgetLoader = widgetLoader;
        this.htmlSanitizer = new HtmlSanitizer();
    }

    /**
     * Build directive corresponding to the widget descriptive json file which has changed.
     * Resulting js file is created in the same directory than the json file overriding previous build.
     *
     * @param jsonPath is the path to the widget file to build.
     * @throws IOException
     */
    @Override
    public void build(Path jsonPath) throws IOException {
        var widget = widgetLoader.get(jsonPath);
        write(
                get(valueOf(jsonPath).replace(".json", ".js")),
                htmlBuilder
                        .with("escapedTemplate", htmlSanitizer.escapeSingleQuotesAndNewLines(widget.getTemplate()))
                        .build(widget).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isBuildable(String path) {
        return path.endsWith(".json") && !path.contains(".metadata");
    }

}
