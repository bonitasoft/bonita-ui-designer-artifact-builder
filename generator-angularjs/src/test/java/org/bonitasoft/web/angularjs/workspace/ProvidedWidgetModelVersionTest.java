/** 
 * Copyright (C) 2023 BonitaSoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.bonitasoft.web.designer.common.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.junit.jupiter.api.Test;

class ProvidedWidgetModelVersionTest {

    @Test
    void wigdetModelVersionIsUptoDate() throws Exception {
        var widgetFolder = Paths.get(ProvidedWidgetModelVersionTest.class.getResource("/widgets").getFile());
        var loader = new WidgetFileBasedLoader(new JsonHandlerFactory().create());
        var widgets = loader.loadAll(widgetFolder);

        var modelVersion = readModelVersion();
        assertThat(widgets)
                .hasSize(27)
                .allSatisfy(widget -> assertThat(widget.getModelVersion())
                        .isEqualTo(modelVersion));
    }

    private static String readModelVersion() throws IOException {
        try (var is = ProvidedWidgetModelVersionTest.class.getResourceAsStream("/version.properties")) {
            var properties = new Properties();
            properties.load(is);
            return properties.getProperty("uidModelVersion");
        }
    }

}
