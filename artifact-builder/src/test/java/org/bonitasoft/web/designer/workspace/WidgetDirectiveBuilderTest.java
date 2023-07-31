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
package org.bonitasoft.web.designer.workspace;

import static java.nio.file.Files.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bonitasoft.web.angularjs.rendering.TemplateEngine;
import org.bonitasoft.web.angularjs.workspace.WidgetDirectiveBuilder;
import org.bonitasoft.web.designer.common.livebuild.PathListener;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.common.repository.WidgetFileBasedPersister;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.widgets.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WidgetDirectiveBuilderTest {

    @TempDir
    Path widgetRepositoryDirectory;

    @Mock
    Watcher watcher;

    @Mock
    BeanValidator validator;

    WidgetDirectiveBuilder widgetDirectiveBuilder;

    Widget pbInput;

    Widget pbButton;

    TemplateEngine htmlBuilder = new TemplateEngine("widgetDirectiveTemplate.hbs.js");

    JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @BeforeEach
    public void setup() throws Exception {
        widgetDirectiveBuilder = new WidgetDirectiveBuilder(watcher,
                new WidgetFileBasedLoader(jsonHandler), true);
        WidgetFileBasedLoader widgetLoader = new WidgetFileBasedLoader(jsonHandler);
        WidgetRepository repository = new WidgetRepository(
                widgetRepositoryDirectory,
                widgetRepositoryDirectory,
                new WidgetFileBasedPersister(jsonHandler, validator, "1.13.0", "2.0"),
                widgetLoader,
                validator,
                mock(Watcher.class));

        pbInput = aWidget().withId("pbInput").build();
        pbInput.setCustom(true);
        createDirectories(repository.resolvePath(pbInput.getId()));
        repository.updateLastUpdateAndSave(pbInput);

        pbButton = aWidget().withId("pbButton").build();
        pbButton.setCustom(true);
        createDirectories(repository.resolvePath(pbButton.getId()));
        repository.updateLastUpdateAndSave(pbButton);
    }

    @Test
    public void should_build_directives_of_a_given_directory() throws Exception {
        widgetDirectiveBuilder.start(widgetRepositoryDirectory);

        assertThat(readDirective("pbInput")).isEqualTo(generateDirective(pbInput));
        assertThat(readDirective("pbButton")).isEqualTo(generateDirective(pbButton));
    }

    @Test
    public void should_only_build_directives_files() throws Exception {
        Files.createFile(widgetRepositoryDirectory.resolve("whatever.txt"));

        widgetDirectiveBuilder.start(widgetRepositoryDirectory);

        //assert that we do not create a whatever.js file ?
        assertThat(widgetRepositoryDirectory.toFile().list()).containsOnly(".metadata", "pbButton", "whatever.txt",
                "pbInput");
    }

    @Test
    public void should_watch_given_directory_to_build_directives_on_change() throws Exception {

        widgetDirectiveBuilder.start(widgetRepositoryDirectory);

        verify(watcher).watch(eq(widgetRepositoryDirectory), any(PathListener.class));
    }

    @Test
    public void should_note_watch_given_directory_when_live_build_is_disabled() throws Exception {
        widgetDirectiveBuilder = new WidgetDirectiveBuilder(watcher,
                new WidgetFileBasedLoader(jsonHandler), false);
        widgetDirectiveBuilder.start(widgetRepositoryDirectory);

        verify(watcher, never()).watch(eq(widgetRepositoryDirectory), any(PathListener.class));
    }

    @Test
    public void should_build_directive_even_if_it_already_exist() throws Exception {
        writeDirective("pbInput", "previous content".getBytes());

        widgetDirectiveBuilder.build(resolve("pbInput/pbInput.json"));

        assertThat(readDirective("pbInput")).isEqualTo(generateDirective(pbInput));
    }

    @Test
    public void should_exclude_metadata_from_the_build() throws Exception {

        boolean isBuildable = widgetDirectiveBuilder.isBuildable(".metadata/123.json");

        assertThat(isBuildable).isFalse();
    }

    /**
     * Read directive content found at widgetId/widgetId.js
     *
     * @param widgetId id of the widget
     * @return the directive as a string
     * @throws IOException
     */
    private String readDirective(String widgetId) throws IOException {
        return new String(readAllBytes(getDirectivePath(widgetId)));
    }

    private Path getDirectivePath(String widgetId) {
        return resolve(widgetId + "/" + widgetId + ".js");
    }

    /**
     * Generate directive
     *
     * @param widget from which the directive is generated
     * @return the directive as a string
     * @throws IOException
     */
    private String generateDirective(Widget widget) throws IOException {
        return htmlBuilder.with("escapedTemplate", widget.getTemplate()).build(widget);
    }

    /**
     * Write contents into widgetId/widgetId.js file.
     *
     * @param widgetId id of the widget
     * @param contents contents of the file
     * @throws IOException
     */
    private void writeDirective(String widgetId, byte[] contents) throws IOException {
        write(resolve(widgetId + "/" + widgetId + ".js"), contents);
    }

    /**
     * Resolve path from widgetRepositoryDirectory
     *
     * @param path to resolve
     * @return resolved path
     */
    private Path resolve(String path) {
        return widgetRepositoryDirectory.resolve(path);
    }

}
