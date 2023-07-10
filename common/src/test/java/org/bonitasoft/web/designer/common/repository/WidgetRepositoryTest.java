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

import static java.nio.file.Files.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Validation;

import org.bonitasoft.web.designer.builder.PropertyBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.common.livebuild.PathListener;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@ExtendWith(MockitoExtension.class)
public class WidgetRepositoryTest {

    private static final String DESIGNER_VERSION = "1.0.0";

    private static final String MODEL_VERSION = "2.0";

    @TempDir
    public Path temporaryFolder;

    private WidgetRepository widgetRepository;

    private JsonHandler jsonHandler;

    private WidgetFileBasedPersister widgetPersister;

    private WidgetFileBasedLoader widgetLoader;

    private Path widgetDir;

    @Mock
    private Watcher watcher;

    @BeforeEach
    public void setUp() throws IOException {
        widgetDir = temporaryFolder;

        // spying objectMapper to be able to simulate a json conversion error
        jsonHandler = spy(new JsonHandlerFactory().create());
        BeanValidator validator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());

        widgetPersister = Mockito
                .spy(new WidgetFileBasedPersister(jsonHandler, validator, DESIGNER_VERSION, MODEL_VERSION));
        widgetLoader = Mockito.spy(new WidgetFileBasedLoader(jsonHandler));

        widgetRepository = new WidgetRepository(
                temporaryFolder,
                temporaryFolder,
                widgetPersister,
                widgetLoader,
                validator,
                watcher);
    }

    @Test
    public void should_get_a_widget_by_its_id() throws Exception {
        Widget expectedWidget = aWidget().withId("input").build();
        Widget notExpectedWidget = aWidget().withId("label").build();
        addToRepository(expectedWidget, notExpectedWidget);

        Widget widget = widgetRepository.get("input");

        assertThat(widget).isEqualTo(expectedWidget);
    }

    @Test
    public void should_throw_NotFoundException_when_trying_to_get_an_unexisting_widget() {
        assertThrows(NotFoundException.class, () -> widgetRepository.get("notExistingWidget"));
    }

    @Test
    public void should_throw_RepositoryException_when_error_occurs_when_getting_a_widget() throws Exception {
        addToRepository(aWidget().withId("input").build());

        Mockito.doThrow(new IOException()).when(jsonHandler).fromJson(ArgumentMatchers.any(byte[].class),
                ArgumentMatchers.eq(Widget.class));

        assertThrows(RepositoryException.class, () -> widgetRepository.get("input"));
    }

    @Test
    public void should_retrieve_all_widgets() throws Exception {
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").build();
        addToRepository(input, label);

        List<Widget> widgets = widgetRepository.getAll();
        assertThat(widgets).containsOnly(input, label);
    }

    @Test
    public void should_throw_RepositoryException_if_error_occurs_while_getting_all_widgets() throws Exception {
        addToRepository(aWidget().withId("input").build());

        Mockito.doThrow(new IOException()).when(jsonHandler).fromJson(ArgumentMatchers.any(byte[].class),
                ArgumentMatchers.eq(Widget.class));

        assertThrows(RepositoryException.class, () -> widgetRepository.getAll());
    }

    @Test
    public void should_save_a_custom_widget() throws Exception {
        Widget customLabel = aWidget().custom().withId("customLabel").build();

        createDirectories(widgetDir.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);

        assertThat(jsonFile(customLabel)).exists();
        // last update field should be the current time
        assertThat(customLabel.getLastUpdate()).isAfter(Instant.now().minus(5000, ChronoUnit.MILLIS));
    }

    @Test
    public void should_save_a_page_without_updating_last_update_date() throws Exception {
        Widget widget = widgetRepository
                .updateLastUpdateAndSave(
                        aWidget().withId("customLabel").withName("theWidgetName").build());
        Instant lastUpdate = widget.getLastUpdate();

        widget.setName("newName");
        widgetRepository.save(widget);

        Widget fetchedWidget = widgetRepository.get(widget.getId());
        assertThat(fetchedWidget.getLastUpdate()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.MILLIS));
        assertThat(fetchedWidget.getName()).isEqualTo("newName");
    }

    @Test
    public void should_save_a_list_of_custom_widgets() throws Exception {
        Widget customLabel = aWidget().custom().withId("customLabel").build();
        Widget customInput = aWidget().custom().withId("customInput").build();

        //For the first widget a directory is present... for the second it will be created during the saving
        createDirectories(widgetDir.resolve("customLabel"));
        widgetRepository.saveAll(asList(customInput, customLabel));

        assertThat(jsonFile(customLabel)).exists();
        assertThat(jsonFile(customInput)).exists();
    }

    @Test
    public void should_set_model_version_while_saving_if_not_already_set() throws Exception {
        Widget customLabel = aWidget().custom().withId("customLabel").designerVersion("1.12.0").build();

        createDirectories(widgetDir.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);

        assertThat(customLabel.getModelVersion()).isEqualTo(MODEL_VERSION);
    }

    @Test
    public void should_not_set_model_version_while_saving_if_already_set() throws Exception {
        Widget customLabel = aWidget().custom().withId("customLabel").build();
        customLabel.setModelVersion("alreadySetModelVersion");
        createDirectories(widgetDir.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);

        assertThat(customLabel.getModelVersion()).isEqualTo("alreadySetModelVersion");
    }

    @Test
    public void should_throw_IllegalArgumentException_while_saving_a_custom_widget_with_no_id_set() {
        Widget aWidget = aWidget().withId(null).custom().build();

        assertThrows(IllegalArgumentException.class, () -> widgetRepository.updateLastUpdateAndSave(aWidget));
    }

    @Test
    public void should_not_allow_to_save_a_widget_without_name() {
        Widget widget = aWidget().withName(" ").build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.updateLastUpdateAndSave(widget));
    }

    @Test
    public void should_not_allow_to_save_a_widget_with_name_containing_non_alphanumeric_chars() {
        Widget widget = aWidget().withName("héllo").build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.create(widget));
    }

    @Test
    public void should_not_allow_to_save_as_widget_with_an_invalid_property() throws Exception {
        Widget widget = aWidget().property(PropertyBuilder.aProperty().name("ze invalid name")).custom()
                .build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.create(widget));
    }

    @Test
    public void should_allow_to_save_a_custom_widget_with_name_containing_space() throws Exception {
        Widget widget = aWidget().withName("hello world").custom().build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.create(widget));
    }

    @Test
    public void should_allow_to_save_a_widget_with_name_containing_space() throws Exception {
        Widget widget = aWidget().withName("hello world").build();
        createDirectories(widgetDir.resolve("anId"));
        widgetRepository.updateLastUpdateAndSave(widget);
    }

    @Test
    public void should_delete_a_custom_widget() throws Exception {
        Widget customLabel = aWidget().withId("customLabel").build();
        customLabel.setController("$scope.hello = 'Hello'");
        customLabel.setTemplate("<div>{{ hello + 'there'}}</div>");
        customLabel.setCustom(true);
        createDirectories(widgetDir.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);
        // emulate js generation
        write(widgetDir.resolve("customLabel").resolve("customLabel.js"), jsonHandler.toJson(""));

        widgetRepository.delete("customLabel");

        assertThat(jsonFile(customLabel)).doesNotExist();
        assertThat(jsFile(customLabel)).doesNotExist();
    }

    @Test
    public void should_not_allow_to_delete_a_pb_widget() throws Exception {
        Widget pbLabel = aWidget().withId("pbLabel").build();
        pbLabel.setCustom(false);
        addToRepository(pbLabel);

        assertThrows(NotAllowedException.class, () -> widgetRepository.delete("pbLabel"));
    }

    @Test
    public void should_not_allow_to_create_a_widget_without_name() throws Exception {
        Widget widget = aWidget().withName(" ").build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.create(widget));
    }

    @Test
    public void should_not_allow_to_create_a_widget_with_name_containing_non_alphanumeric_chars() throws Exception {
        Widget widget = aWidget().withName("héllo").build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.create(widget));
    }

    @Test
    public void should_not_allow_to_create_a_custom_widget_with_name_containing_non_space() {
        Widget widget = aWidget().withName("hello world").custom().build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.create(widget));
    }

    @Test
    public void should_allow_create_a_widget_with_name_containing_space_for_normal_widget() {
        Widget widget = aWidget().withName("hello world").build();

        assertThrows(ConstraintValidationException.class, () -> widgetRepository.create(widget));
    }

    @Test
    public void should_create_a_widget_and_set_his_id() throws Exception {
        Widget expectedWidget = aWidget().withName("aName").build();
        Widget createdWidget = widgetRepository.create(expectedWidget);

        expectedWidget.setId("customAName");
        expectedWidget.setCustom(true);
        assertThat(expectedWidget).isEqualTo(createdWidget);
        assertThat(jsonFile(createdWidget)).exists();
    }

    @Test
    public void should_not_allow_to_create_a_widget_with_an_already_existing_name() throws Exception {
        Widget widget = aWidget().withName("existingName").withId("customExistingName").build();
        addToRepository(widget);

        assertThrows(NotAllowedException.class,
                () -> widgetRepository.create(aWidget().withName("existingName").build()));
    }

    @Test
    public void should_verify_that_a_widget_exists_in_the_repository() throws Exception {
        Files.write(Files.createDirectory(temporaryFolder.resolve("pbInput")).resolve("pbInput.json"),
                "contents".getBytes());

        assertThat(widgetRepository.exists("pbInput")).isTrue();
        assertThat(widgetRepository.exists("pbLabel")).isFalse();
    }

    @Test
    public void should_save_a_new_property() throws Exception {
        Widget aWidget = addToRepository(aWidget().custom().build());
        Property expectedProperty = PropertyBuilder.aProperty().build();

        widgetRepository.addProperty(aWidget.getId(), expectedProperty);

        Widget widget = getFromRepository(aWidget.getId());
        assertThat(widget.getProperties()).contains(expectedProperty);
    }

    @Test
    public void should_not_allow_to_save_a_new_property_when_property_with_same_name_already_exists() throws Exception {
        Property alreadyAddedProperty = PropertyBuilder.aProperty().build();
        Widget aWidget = addToRepository(aWidget().custom().property(alreadyAddedProperty).build());

        assertThrows(NotAllowedException.class,
                () -> widgetRepository.addProperty(aWidget.getId(), alreadyAddedProperty));
    }

    @Test
    public void should_update_an_existing_property() throws Exception {
        Property initialParam = PropertyBuilder.aProperty().name("propertyName").label("propertyLabel").build();
        Property updatedParam = PropertyBuilder.aProperty().name("newName").label("newLablel").build();
        Widget aWidget = addToRepository(aWidget().custom().property(initialParam).build());

        widgetRepository.updateProperty(aWidget.getId(), initialParam.getName(), updatedParam);

        Widget widget = getFromRepository(aWidget.getId());
        assertThat(widget.getProperties()).contains(updatedParam);
        assertThat(widget.getProperties()).doesNotContain(initialParam);
    }

    @Test
    public void should_fail_when_trying_to_update_a_not_existing_property() throws Exception {
        Widget expectedWidget = addToRepository(aWidget().custom().build());

        assertThrows(NotFoundException.class,
                () -> widgetRepository.updateProperty(expectedWidget.getId(), "notExistingProperty", new Property()));
    }

    @Test
    public void should_delete_a_widget_property() throws Exception {
        Property aProperty = PropertyBuilder.aProperty().name("aParam").build();
        Widget aWidget = addToRepository(
                aWidget().property(aProperty).property(PropertyBuilder.aProperty().name("anotherParam"))
                        .build());

        List<Property> properties = widgetRepository.deleteProperty(aWidget.getId(), "aParam");

        Widget widget = getFromRepository(aWidget.getId());
        assertThat(widget.getProperties()).doesNotContain(aProperty);
        assertThat(properties).containsOnlyElementsOf(widget.getProperties());
    }

    @Test
    public void should_fail_when_trying_to_delete_a_property_on_an_unknown_widget() {
        assertThrows(NotFoundException.class, () -> widgetRepository.deleteProperty("unknownWidget", "aParam"));
    }

    @Test
    public void should_fail_when_trying_to_delete_an_unknown_property() throws Exception {
        Widget aWidget = addToRepository(aWidget().build());
        assertThrows(NotFoundException.class,
                () -> widgetRepository.deleteProperty(aWidget.getId(), "unknownPrameter"));

    }

    @Test
    public void should_find_widget_which_use_another_widget() throws Exception {
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").template("use <input>").build();
        addToRepository(input, label);

        //input is used by label
        assertThat(widgetRepository.findByObjectId("input")).extracting("id").containsExactly("label");
    }

    @Test
    public void should_find_widget_which_not_use_another_widget() throws Exception {
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").template("use <input>").build();
        addToRepository(input, label);

        //label is used by noone
        assertThat(widgetRepository.findByObjectId("label")).isEmpty();
    }

    @Test
    public void should_find_widget_by_id() throws Exception {
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").build();
        addToRepository(input, label);

        //input is used by label
        assertThat(widgetRepository.getByIds(Set.of("input", "label"))).hasSize(2)
                .extracting("id").containsOnly("input", "label");
    }

    @Test
    public void should_walk_widget_repository() throws Exception {
        Path file = createFile(temporaryFolder.resolve("file"));
        final List<Path> visitedPaths = new ArrayList<>();

        widgetRepository.walk(new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                visitedPaths.add(file);
                return super.visitFile(file, attrs);
            }
        });

        assertThat(visitedPaths).containsExactly(file);
    }

    @Test
    public void should_watch_widget_repository() throws Exception {
        PathListener pathListener = path -> System.out.println(path);

        widgetRepository.watch(pathListener);

        Mockito.verify(watcher).watch(widgetDir, pathListener);
    }

    @Test
    public void should_mark_a_widget_as_favorite() throws Exception {
        Widget widget = addToRepository(aWidget().notFavorite());

        widgetRepository.markAsFavorite(widget.getId());

        Widget fetchedWidget = getFromRepository(widget.getId());
        assertThat(fetchedWidget.isFavorite()).isTrue();

    }

    @Test
    public void should_unmark_a_widget_as_favorite() throws Exception {
        Widget widget = addToRepository(aWidget().favorite());

        widgetRepository.unmarkAsFavorite(widget.getId());

        Widget fetchedWidget = getFromRepository(widget.getId());
        assertThat(fetchedWidget.isFavorite()).isFalse();
    }

    private void addToRepository(Widget... widgets) throws Exception {
        for (Widget widget : widgets) {
            addToRepository(widget);
        }
    }

    private Widget addToRepository(WidgetBuilder widget) throws Exception {
        return addToRepository(widget.build());
    }

    private Widget addToRepository(Widget widget) throws Exception {
        return addToRepository(widgetDir, widgetRepository, widget);
    }

    private Widget addToRepository(Path widgetDirectory, WidgetRepository widgetRepository, Widget widget)
            throws Exception {
        Path widgetDir = createDirectory(widgetDirectory.resolve(widget.getId()));
        writeWidgetMetadataInFile(widget, widgetDir.resolve(widget.getId() + ".json"));
        return getFromRepository(widgetRepository, widget.getId());
    }

    private Widget getFromRepository(String widgetId) {
        return getFromRepository(widgetRepository, widgetId);
    }

    private Widget getFromRepository(WidgetRepository widgetRepository, String widgetId) {
        return widgetRepository.get(widgetId);
    }

    private void writeWidgetMetadataInFile(Widget widget, Path path) throws IOException {
        ObjectWriter writer = new ObjectMapper().writer();
        writer.writeValue(path.toFile(), widget);
    }

    private File jsonFile(Widget widget) {
        return jsonFile(widget.getId());
    }

    private File jsonFile(String widgetId) {
        return widgetDir.resolve(widgetId).resolve(widgetId + ".json").toFile();
    }

    private File jsFile(Widget widget) {
        return widgetDir.resolve(widget.getId()).resolve(widget.getId() + ".js").toFile();
    }
}
