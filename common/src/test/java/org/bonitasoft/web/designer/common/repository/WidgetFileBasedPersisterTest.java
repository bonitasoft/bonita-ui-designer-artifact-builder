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
package org.bonitasoft.web.designer.common.repository;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readAllLines;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WidgetFileBasedPersisterTest {

    private static final String DESIGNER_VERSION = "2.0.0";

    @TempDir
    private Path tempDir;
    private JsonHandler jsonHandler;
    private WidgetFileBasedPersister widgetPersister;

    @Mock
    private BeanValidator validator;

    private Path repoDirectory;

    @BeforeEach
    public void setUp() throws IOException {
        repoDirectory = Files.createDirectory(tempDir.resolve("jsonrepository"));
        jsonHandler = spy(new JsonHandlerFactory().create());
        widgetPersister = new WidgetFileBasedPersister(jsonHandler, validator, DESIGNER_VERSION, DESIGNER_VERSION);
    }

    private Widget getFromRepository(String id) throws IOException {
        byte[] json = readAllBytes(repoDirectory.resolve(id + ".json"));
        return jsonHandler.fromJson(json, Widget.class);
    }

    @Test
    public void should_serialize_an_object_and_save_it_to_a_file() throws Exception {
        String htmlContent = "<div></div>";
        String jsContent = "function ($scope) {}";
        Widget widgetToSave = WidgetBuilder.aWidget().withId("input").template(htmlContent).controller(jsContent)
                .build();

        widgetPersister.save(repoDirectory, widgetToSave);

        Widget savedObject = getFromRepository("input");
        assertThat(savedObject.getTemplate()).isEqualTo("@" + widgetToSave.getId() + ".tpl.html");
        assertThat(savedObject.getController()).isEqualTo("@" + widgetToSave.getId() + ".ctrl.js");
        assertThat(
                readAllLines(repoDirectory.resolve(widgetToSave.getId() + ".ctrl.js"), StandardCharsets.UTF_8).get(0))
                .isEqualTo(jsContent);
        assertThat(
                readAllLines(repoDirectory.resolve(widgetToSave.getId() + ".tpl.html"), StandardCharsets.UTF_8).get(0))
                .isEqualTo(htmlContent);

    }

}
