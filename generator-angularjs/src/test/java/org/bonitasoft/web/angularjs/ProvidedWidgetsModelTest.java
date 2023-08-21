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
package org.bonitasoft.web.angularjs;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.jupiter.api.Test;

/**
 * For each json provided widgets, we test that it is deserializable in Widget model
 *
 * @author Colin Puy
 */
class ProvidedWidgetsModelTest {

    @Test
    void provided_widgets_should_be_deserializable() throws Exception {
        URI widgets = getClass().getResource("/widgets").toURI();
        var path = Paths.get(widgets);
        var visitor = new IsWidgetDeserializableVisitor();

        assertDoesNotThrow(() -> Files.walkFileTree(path, visitor));
    }

    private final class IsWidgetDeserializableVisitor extends SimpleFileVisitor<Path> {

        private final JsonHandler jsonHandler;

        private IsWidgetDeserializableVisitor() {
            this.jsonHandler = new JsonHandlerFactory().create();
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.endsWith(".json")) {
                try {
                    jsonHandler.fromJson(readAllBytes(file), Widget.class);
                } catch (Exception e) {
                    Assertions.fail(file.getFileName() + " cannot be deserialized to Widget model", e);
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
