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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.web.angularjs.visitor.HtmlBuilderVisitor;
import org.bonitasoft.web.angularjs.visitor.RequiredModulesVisitor;
import org.bonitasoft.web.designer.common.repository.AssetRepository;
import org.bonitasoft.web.designer.common.visitor.AssetVisitor;
import org.bonitasoft.web.designer.common.visitor.PageFactory;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widgets.Widget;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// FIXME: Fix this test and HTMLBuildervisitor
@ExtendWith(MockitoExtension.class)
public class HtmlGeneratorTest {

    @Mock
    private HtmlBuilderVisitor htmlBuilderVisitor;

    @InjectMocks
    private HtmlGenerator generator;
    @Mock
    private DirectivesCollector directivesCollector;
    @Mock
    private RequiredModulesVisitor requiredModulesVisitor;
    @Mock
    private AssetVisitor assetVisitor;

    @Mock
    private AssetRepository<Widget> widgetAssetRepository;
    @Mock
    private AssetRepository<Page> pageAssetRepository;

    @Mock
    private List<PageFactory> pageFactories;

    private static final byte[] assetsContent = new byte[0];

    @BeforeEach
    public void setUp() throws Exception {
        when(htmlBuilderVisitor.build(any())).thenReturn("foobar");

        when(requiredModulesVisitor.visit(any(Page.class))).thenReturn(Collections.emptySet());
        when(pageAssetRepository.readAllBytes(anyString(), any(Asset.class))).thenReturn(assetsContent);
        when(widgetAssetRepository.readAllBytes(any(Asset.class))).thenReturn(assetsContent);

        when(assetVisitor.visit(any(Page.class))).thenReturn(Collections.emptySet());
        when(directivesCollector.buildUniqueDirectivesFiles(any(), anyString())).thenReturn(Collections.emptyList());
    }
    //    @Test
    //    public void should_generate_an_html_with_the_list_of_widgets() throws Exception {
    //        Page page = aPage().withId("page-id").build();
    //        when(pageFactory.generate(page)).thenReturn("var foo = \"bar\";");
    //        when(directivesCollector.buildUniqueDirectivesFiles(page, page.getId()))
    //                .thenReturn(Arrays.asList("assets/widgets.js"));
    //
    //        // when we generate the html
    //        String generatedHtml = visitor.build(page, "mycontext/");
    //
    //        // then we should have the directive scripts included
    //        assertThat(generatedHtml).contains("<script src=\"assets/widgets.js\"></script>")
    //                .contains("pb-model='page-id'"); // and an empty object as constant
    //    }

    //    @Test
    //    public void should_generate_formatted_html_with_given_widgets() throws Exception {
    //        Page page = aPage().build();
    //        //        when(generator.build(page, "mycontext/")).thenReturn("foobar");
    //
    //        String generateHtml = this.generator.build(page, "mycontext/");
    //
    //        assertThat(generateHtml).isEqualTo(format("foobar"));
    //    }
    //
    //    @Test
    //    public void should_generate_formatted_html_with_no_context() throws Exception {
    //        Page page = aPage().build();
    //        //        when(generator.build(page, "")).thenReturn("foobar");
    //
    //        String generateHtml = generator.generateHtml(page);
    //
    //        assertThat(generateHtml).isEqualTo(format("foobar"));
    //    }
    //
    //    @Test
    //    public void should_generate_formatted_html_for_fragment_with_given_widgets() throws Exception {
    //        Fragment fragment = aFragment().build();
    //        //        when(generator.build(fragment, "mycontext/")).thenReturn("foobar");
    //
    //        String generateHtml = generator.generateHtml(fragment, "mycontext/");
    //
    //        assertThat(generateHtml).isEqualTo(format("foobar"));
    //    }

    private String format(String html) {
        return Jsoup.parse(html).toString();
    }
}
