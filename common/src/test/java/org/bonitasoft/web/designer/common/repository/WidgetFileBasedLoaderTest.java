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

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.common.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class WidgetFileBasedLoaderTest {

    @TempDir
    public Path temporaryFolder;

    private Path widgetDirectory;
    private WidgetFileBasedLoader widgetLoader;

    @BeforeEach
    public void setUp() {
        widgetDirectory = temporaryFolder;
        widgetLoader = new WidgetFileBasedLoader(new JsonHandlerFactory().create());
    }

    private void addToDirectory(Path directory, Widget... widgets) throws Exception {
        for (Widget widget : widgets) {
            Path widgetDir = createDirectory(directory.resolve(widget.getId()));
            writeWidgetInFile(widget, widgetDir.resolve(widget.getId() + ".json"));
        }
    }

    private void writeWidgetInFile(Widget widget, Path path) throws IOException {
        ObjectWriter writer = new ObjectMapper().writer();
        writer.writeValue(path.toFile(), widget);
    }

    @Test
    public void should_get_a_widget_by_its_id() throws Exception {
        Widget expectedWidget = WidgetBuilder.aWidget().withId("input").build();
        Widget notExpectedWidget = WidgetBuilder.aWidget().withId("label").build();
        addToDirectory(widgetDirectory, expectedWidget, notExpectedWidget);

        Widget widget = widgetLoader.get(widgetDirectory.resolve("input/input.json"));

        assertThat(widget).isEqualTo(expectedWidget);
    }

    @Test
    public void should_get_a_widget_with_template_and_controller_by_its_id() throws Exception {
        String templateFileName = "input.tpl.html";
        String controllerFileName = "input.ctrl.js";
        Widget expectedWidget = WidgetBuilder.aWidget().withId("input").template("@" + templateFileName)
                .controller("@" + controllerFileName).build();
        addToDirectory(widgetDirectory, expectedWidget);
        String htmlContent = "<div></div>";
        Files.write(widgetDirectory.resolve(expectedWidget.getId()).resolve(templateFileName),
                htmlContent.getBytes(StandardCharsets.UTF_8));
        String jsContent = "function ($scope) {}";
        Files.write(widgetDirectory.resolve(expectedWidget.getId()).resolve(controllerFileName),
                jsContent.getBytes(StandardCharsets.UTF_8));

        Widget widget = widgetLoader.get(widgetDirectory.resolve("input/input.json"));

        assertThat(widget.getTemplate()).isEqualTo(htmlContent);
        assertThat(widget.getController()).isEqualTo(jsContent);
    }

    @Test
    public void should_load_a_widget_with_template_and_controller_by_its_id() throws Exception {
        String templateFileName = "input.tpl.html";
        String controllerFileName = "input.ctrl.js";
        Widget expectedWidget = WidgetBuilder.aWidget().withId("input").template("@" + templateFileName)
                .controller("@" + controllerFileName).build();
        addToDirectory(widgetDirectory, expectedWidget);
        String htmlContent = "<div></div>";
        Files.write(widgetDirectory.resolve(expectedWidget.getId()).resolve(templateFileName),
                htmlContent.getBytes(StandardCharsets.UTF_8));
        String jsContent = "function ($scope) {}";
        Files.write(widgetDirectory.resolve(expectedWidget.getId()).resolve(controllerFileName),
                jsContent.getBytes(StandardCharsets.UTF_8));

        Widget widget = widgetLoader.load(widgetDirectory.resolve("input/input.json"));

        assertThat(widget.getTemplate()).isEqualTo(htmlContent);
        assertThat(widget.getController()).isEqualTo(jsContent);
    }

    @Test
    public void should_retrieve_all_widgets() throws Exception {
        Widget input = WidgetBuilder.aWidget().withId("input").build();
        Widget label = WidgetBuilder.aWidget().withId("label").build();
        addToDirectory(widgetDirectory, input, label);

        List<Widget> widgets = widgetLoader.getAll(widgetDirectory);

        assertThat(widgets).containsOnly(input, label);
    }

    @Test
    public void should_not_failed_when_directory_contains_an_hidden_file() throws Exception {
        createDirectory(widgetDirectory.resolve(".DS_Store"));

        widgetLoader.getAll(widgetDirectory);
    }

    @Test
    public void should_retrieve_all_custom_widgets() throws Exception {
        Widget input = WidgetBuilder.aWidget().withId("pbInput").build();
        Widget custom1 = WidgetBuilder.aWidget().withId("custom1").custom().build();
        Widget custom2 = WidgetBuilder.aWidget().withId("custom2").custom().build();
        addToDirectory(widgetDirectory, input, custom1, custom2);

        List<Widget> widgets = widgetLoader.loadAll(widgetDirectory, path -> !path.getFileName().toString()
                .startsWith(WidgetRepository.ANGULARJS_STANDARD_PREFIX));

        assertThat(widgets).containsOnly(custom1, custom2);
    }

    @Test
    public void should_only_load_persisted_properties() throws Exception {
        addToDirectory(widgetDirectory, WidgetBuilder.aWidget().withId("customWidget")
                .custom()
                .favorite()
                .build());

        List<Widget> widgets = widgetLoader.loadAll(widgetDirectory, WidgetRepository.CUSTOM_WIDGET_FILTER);

        assertThat(widgets.get(0).isFavorite()).isFalse();
    }

    @Test
    public void should_find_widget_which_use_another_widget() throws Exception {
        Widget input = WidgetBuilder.aWidget().withId("input").build();
        Widget label = WidgetBuilder.aWidget().withId("label").template("use <input>").build();
        addToDirectory(widgetDirectory, input, label);

        //input is used by label
        assertThat(widgetLoader.findByObjectId(widgetDirectory, "input")).extracting("id").contains("label");
    }

    @Test
    public void should_find_widget_which_not_use_another_widget() throws Exception {
        Widget input = WidgetBuilder.aWidget().withId("input").build();
        Widget label = WidgetBuilder.aWidget().withId("label").template("use <input>").build();
        addToDirectory(widgetDirectory, input, label);

        //label is used by noone
        assertThat(widgetLoader.findByObjectId(widgetDirectory, "label")).isEmpty();
    }

    @Test
    public void should_load_a_single_page_in_the_import_folder() throws Exception {
        Widget input = WidgetBuilder.aWidget().withId("input").build();
        addToDirectory(widgetDirectory, input);

        Widget widget = widgetLoader.load(widgetDirectory.resolve("input/input.json"));

        assertThat(widget).isEqualTo(input);
    }

    @Test
    public void should_throw_notfound_exception_when_there_are_no_pages_in_folder() throws Exception {
        assertThrows(NotFoundException.class, () -> widgetLoader.load(widgetDirectory.resolve("test")));
    }

    @Test
    public void should_throw_json_read_exception_when_loaded_file_is_not_valid_json() throws Exception {
        write(widgetDirectory.resolve("wrongjson.json"), "notJson".getBytes());

        assertThrows(JsonReadException.class, () -> widgetLoader.load(widgetDirectory.resolve("wrongjson.json")));
    }

    @Test
    public void should_throw_notfound_exception_when_widget_template_is_not_found() throws Exception {
        Widget input = WidgetBuilder.aWidget().withId("input").build();
        input.setTemplate("@missingTemplate.tpl.html");
        addToDirectory(widgetDirectory, input);

        assertThrows(org.bonitasoft.web.designer.model.exception.NotFoundException.class,
                () -> widgetLoader.load(widgetDirectory.resolve("input/input.json")));
    }

    @Test
    public void should_throw_notfound_exception_when_widget_controller_is_not_found() throws Exception {
        Widget input = WidgetBuilder.aWidget().withId("input").build();
        input.setController("@missingController.ctrl.js");
        addToDirectory(widgetDirectory, input);

        assertThrows(org.bonitasoft.web.designer.model.exception.NotFoundException.class,
                () -> widgetLoader.load(widgetDirectory.resolve("input/input.json")));
    }
}
