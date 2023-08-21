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

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bonitasoft.web.angularjs.rendering.TemplateEngine;
import org.bonitasoft.web.angularjs.visitor.HtmlBuilderVisitor;
import org.bonitasoft.web.angularjs.workspace.FragmentDirectiveBuilder;
import org.bonitasoft.web.designer.common.livebuild.PathListener;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FragmentDirectiveBuilderTest {

    @TempDir
    public Path repositoryDirectory;

    @Mock
    private Watcher watcher;

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Mock
    private HtmlBuilderVisitor htmlBuilderVisitor;

    private FragmentDirectiveBuilder fragmentDirectiveBuilder;

    private Path fragmentFile;

    private Fragment fragment;

    private final TemplateEngine htmlBuilder = new TemplateEngine("fragmentDirectiveTemplate.hbs.js");

    @BeforeEach
    void setUp() throws Exception {
        fragmentDirectiveBuilder = new FragmentDirectiveBuilder(watcher, jsonHandler, htmlBuilderVisitor, true);
        fragmentFile = Files.createFile(repositoryDirectory.resolve("fragment.json"));
        fragment = aFragment()
                .withName("aUnicornFragment")
                .with(anInput().build())
                .build();

        write(fragmentFile, jsonHandler.toJson(fragment));

    }

    @Test
    void should_build_a_fragment_directive() throws Exception {
        when(htmlBuilderVisitor.build(anyList())).thenReturn("<p>content</p>");
        fragmentDirectiveBuilder.build(fragmentFile);

        String directive = new String(readAllBytes(repositoryDirectory.resolve("fragment.js")));

        assertThat(directive).isEqualTo(generateDirective(fragment, "<p>content</p>"));
    }

    @Test
    void should_watch_given_directory_to_build_directives_on_change() throws Exception {
        when(htmlBuilderVisitor.build(anyList())).thenReturn("");
        fragmentDirectiveBuilder.start(repositoryDirectory);

        verify(watcher).watch(eq(repositoryDirectory), any(PathListener.class));
    }

    @Test
    void should_not_watch_given_directory_when_live_build_is_disabled() throws Exception {
        when(htmlBuilderVisitor.build(anyList())).thenReturn("");
        fragmentDirectiveBuilder = new FragmentDirectiveBuilder(watcher, jsonHandler, htmlBuilderVisitor, false);
        fragmentDirectiveBuilder.start(repositoryDirectory);

        verify(watcher, never()).watch(eq(repositoryDirectory), any(PathListener.class));
    }

    @Test
    void should_exclude_metadata_from_the_build() throws Exception {

        boolean isBuildable = fragmentDirectiveBuilder.isBuildable(".metadata/123.json");

        assertThat(isBuildable).isFalse();
    }

    /**
     * Generate directive
     *
     * @param fragment from which the directive is generated
     * @return the directive as a string
     * @throws IOException
     */
    private String generateDirective(Fragment fragment, String content) throws IOException {
        return htmlBuilder.with("rowsHtml", content).build(fragment);
    }
}
