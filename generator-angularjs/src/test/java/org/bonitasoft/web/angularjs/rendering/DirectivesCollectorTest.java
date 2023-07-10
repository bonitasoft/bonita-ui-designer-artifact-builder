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
package org.bonitasoft.web.angularjs.rendering;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DirectivesCollectorTest {

    @TempDir
    Path temporaryFolder;

    private DirectivesCollector collector;

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private FragmentIdVisitor fragmentIdVisitor;

    @Mock
    private DirectiveFileGenerator directiveFileGenerator;

    private Path tmpPagesPath;
    private Path tmpFragmentsPath;

    @BeforeEach
    public void beforeEach() throws IOException {
        this.tmpPagesPath = temporaryFolder.resolve("pages");
        this.tmpFragmentsPath = temporaryFolder.resolve("fragments");
        collector = new DirectivesCollector(jsonHandler, tmpPagesPath,
                tmpFragmentsPath, directiveFileGenerator, fragmentIdVisitor, fragmentRepository);

    }

    @Test
    public void should_build_directives_from_the_preview() throws IOException {
        Page page = aPage().build();
        Path assets = tmpPagesPath.resolve(page.getId()).resolve("js");
        when(directiveFileGenerator.generateAllDirectivesFilesInOne(page, assets)).thenReturn("widgets-123456.min.js");

        List<String> imports = collector.buildUniqueDirectivesFiles(page, page.getId());

        Assertions.assertThat(imports).containsOnly("js/widgets-123456.min.js");
    }

    @Test
    public void should_collect_widgets_directives_from_the_fragment_preview() throws Exception {
        Fragment fragment = FragmentBuilder.aFragment().build();
        when(directiveFileGenerator.generateAllDirectivesFilesInOne(fragment,
                tmpFragmentsPath
                        .resolve(fragment.getId())))
                .thenReturn("widgets-654321");

        List<String> imports = collector.buildUniqueDirectivesFiles(fragment, fragment.getId());

        assertThat(imports).containsOnly("widgets-654321");
    }

    @Test
    public void should_collect_widget_file_directive_and_fragment_file_when_fragment_will_be_use_in_page()
            throws IOException {

        Page page = PageBuilder.aPage().build();
        Fragment fragment = FragmentBuilder.aFragment().build();
        byte[] content = this.jsonHandler.toJson(fragment);
        String fragmentSHA1 = DigestUtils.sha1Hex(content);
        initFileAndMockForPageWhoHasFragment(page, fragment);
        List<String> expected = asList("js/widgets-123456.js",
                "fragments/" + fragment.getId() + "/" + fragment.getId() + ".js?hash=" + fragmentSHA1);

        List<String> imports = collector.buildUniqueDirectivesFiles(page, page.getId());

        assertThat(imports).isEqualTo(expected);
    }

    private void initFileAndMockForPageWhoHasFragment(Page page, Fragment fragment) throws IOException {
        Path pagePathAsset = tmpPagesPath.resolve(page.getId()).resolve("js");
        Path fragmentsPath = tmpFragmentsPath.resolve(fragment.getId());

        lenient().when(directiveFileGenerator.generateAllDirectivesFilesInOne(page, pagePathAsset))
                .thenReturn("widgets-123456.js");
        lenient().when(directiveFileGenerator.generateAllDirectivesFilesInOne(fragment, fragmentsPath))
                .thenReturn("widgets-654321.js");

        HashSet<String> fragmentIds = new HashSet<>(asList(fragment.getId()));
        when(fragmentIdVisitor.visit(page)).thenReturn(fragmentIds);
        when(fragmentRepository.getByIds(fragmentIds)).thenReturn(asList(fragment));
    }
}
