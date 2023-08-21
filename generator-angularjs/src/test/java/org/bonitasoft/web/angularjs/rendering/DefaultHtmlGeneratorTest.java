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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.angularjs.utils.assertions.CustomAssertions.assertThatHtmlBody;
import static org.bonitasoft.web.angularjs.utils.assertions.CustomAssertions.assertThatHtmlHead;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aParagraph;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.angularjs.utils.rule.TestResource;
import org.bonitasoft.web.angularjs.visitor.HtmlBuilderVisitor;
import org.bonitasoft.web.angularjs.visitor.PropertyValuesVisitor;
import org.bonitasoft.web.angularjs.visitor.RequiredModulesVisitor;
import org.bonitasoft.web.angularjs.visitor.VariableModelVisitor;
import org.bonitasoft.web.designer.common.repository.AssetRepository;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.common.visitor.AssetVisitor;
import org.bonitasoft.web.designer.common.visitor.PageFactory;
import org.bonitasoft.web.designer.common.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultHtmlGeneratorTest {

    private static final byte[] assetsContent = new byte[0];

    @Rule
    public TestResource testResource = new TestResource(DefaultHtmlGenerator.class);

    @Mock
    private PageFactory pageFactory;

    @Mock
    private RequiredModulesVisitor requiredModulesVisitor;

    @Mock
    private AssetVisitor assetVisitor;

    @Mock
    private AssetRepository<Page> pageAssetRepository;

    @Mock
    private AssetRepository<Widget> widgetAssetRepository;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private PropertyValuesVisitor propertyValuesVisitor;

    @Mock
    private VariableModelVisitor variableModelVisitor;

    @Mock
    private WidgetIdVisitor widgetIdVisitor;

    private String assetSHA1;

    @Mock
    private DirectivesCollector directivesCollector;

    private HtmlBuilderVisitor htmlBuilderVisitor;
    private DefaultHtmlGenerator generator;

    @BeforeEach
    void setUp() throws Exception {
        htmlBuilderVisitor = new HtmlBuilderVisitor(fragmentRepository);
        generator = new DefaultHtmlGenerator(
                htmlBuilderVisitor,
                directivesCollector,
                requiredModulesVisitor,
                assetVisitor,
                widgetAssetRepository,
                pageAssetRepository,
                List.of(pageFactory));
        when(requiredModulesVisitor.visit(any(Page.class))).thenReturn(Collections.emptySet());

        //        when(widgetAssetRepository.readAllBytes(any(Asset.class))).thenReturn(assetsContent);
        assetSHA1 = DigestUtils.sha1Hex(assetsContent);
    }
    //    @Test
    //    void should_generate_an_html_with_the_list_of_widgets() throws Exception {
    //        Page page = aPage().withId("page-id").build();
    //        when(directivesCollector.buildUniqueDirectivesFiles(page, page.getId()))
    //                .thenReturn(Arrays.asList("assets/widgets.js"));
    //
    //        // when we generate the html
    //        String generatedHtml = generator.build(page, "mycontext/");
    //
    //        // then we should have the directive scripts included
    //        assertThat(generatedHtml).contains("<script src=\"assets/widgets.js\"></script>")
    //                .contains("pb-model='page-id'"); // and an empty object as constant
    //    }

    //    @Test
    //    void should_generate_formatted_html_with_given_widgets() throws Exception {
    //        Page page = aPage().build();
    //        when(generator.build(page, "mycontext/")).thenReturn("foobar");
    //
    //        String generateHtml = this.generator.build(page, "mycontext/");
    //
    //        assertThat(generateHtml).isEqualTo(format("foobar"));
    //    }

    //    @Test
    //    void should_generate_formatted_html_with_no_context() throws Exception {
    //        Page page = aPage().build();
    //        //        when(generator.build(page, "")).thenReturn("foobar");
    //
    //        String generateHtml = generator.generateHtml(page);
    //
    //        assertThat(generateHtml).isEqualTo(format("foobar"));
    //    }
    //
    //    @Test
    //    void should_generate_formatted_html_for_fragment_with_given_widgets() throws Exception {
    //        Fragment fragment = aFragment().build();
    //        when(generator.build(fragment, "mycontext/")).thenReturn("foobar");
    //
    //        String generateHtml = generator.generateHtml(fragment, "mycontext/");
    //
    //        assertThat(generateHtml).isEqualTo(format("foobar"));
    //    }

    @Test
    void should_build_a_container_fluid_for_a_previewable() throws Exception {
        Page page = aPage().build();
        when(pageFactory.generate(page)).thenReturn("var foo = \"bar\";");

        assertThatHtmlBody(generator.build(page, "mycontext/")).hasElement("div.container-fluid");
    }

    @Test
    void should_generate_html_for_a_page() throws Exception {
        Asset assetLocal = anAsset().withOrder(1)
                .withName("bonita.vendors.js").withExternal(false)
                .build();
        Asset assetJquery = anAsset().withOrder(2)
                .withName("//code.jquery.com/jquery-2.1.4.min.js").withExternal(true)
                .build();
        Asset assetRelative = anAsset().withOrder(3)
                .withName("bonita.min.js").withExternal(true)
                .build();
        Page page = aPage().withId("page-id")
                .withAsset(assetRelative, assetJquery)
                .with(aContainer().with(
                        aRow().with(
                                anInput().withReference("input-reference"),
                                aParagraph().withReference("paragraph-reference")))
                        .withReference("container-reference"))
                .build();
        when(pageAssetRepository.readAllBytes(anyString(), any(Asset.class))).thenReturn(assetsContent);
        when(pageFactory.generate(page)).thenReturn("var baz = \"qux\";");
        when(directivesCollector.buildUniqueDirectivesFiles(page, page.getId()))
                .thenReturn(Arrays.asList("assets/widgets-f8b2ef17808cccb95dbf0973e7745cd53c29c684.js"));
        when(assetVisitor.visit(page)).thenReturn(Set.of(assetRelative, assetJquery, assetLocal));

        String html = generator.build(page, "mycontext/");

        assertThatHtmlBody(html).isEqualToBody(testResource.load("page.html"));
        assertThatHtmlHead(html).isEqualToHead(testResource.load("page.html"));
    }

    @Test
    void should_generate_html_for_a_page_with_a_custom_display_name_put_in_title_tag() throws Exception {
        Page page = aPage().withId("page-id")
                .withDisplayName("This is a beautiful title for this page")
                .build();
        when(pageFactory.generate(page)).thenReturn("var baz = \"qux\";");

        String html = generator.build(page, "mycontext/");

        assertThatHtmlBody(html).isEqualToBody(testResource.load("pageCustomDisplayName.html"));
        assertThatHtmlHead(html).isEqualToHead(testResource.load("pageCustomDisplayName.html"));
    }

    @Test
    void should_add_extra_modules_when_widgets_needs_them() throws Exception {
        Page page = aPage().build();
        when(requiredModulesVisitor.visit(page)).thenReturn(Set.of("needed.module"));

        String html = generator.build(page, "");

        Element head = Jsoup.parse(html).head();
        assertThat(head.html()).contains("angular.module('bonitasoft.ui').requires.push('needed.module');");
    }

    @Test
    void should_not_add_extra_modules_when_no_widgets_needs_them() throws Exception {
        Page page = aPage().build();
        when(requiredModulesVisitor.visit(page)).thenReturn(Collections.<String> emptySet());

        String html = generator.build(page, "");

        Element head = Jsoup.parse(html).head();
        assertThat(head.html()).doesNotContain("angular.module('bonitasoft.ui').requires.push");
    }

    @Test
    void should_add_asset_import_in_header() throws Exception {
        Page page = aPage().build();
        when(pageAssetRepository.readAllBytes(anyString(), any(Asset.class))).thenReturn(assetsContent);
        when(widgetAssetRepository.readAllBytes(any(Asset.class))).thenReturn(assetsContent);
        when(assetVisitor.visit(page)).thenReturn(
                Set.of(
                        //A css file in the page
                        new Asset().setName("myfile.css").setType(AssetType.CSS),
                        new Asset().setName("http://moncdn/myfile.css").setExternal(true).setType(AssetType.CSS),
                        //An external css file in the page
                        //A js file in a widget
                        new Asset().setName("myfile.js").setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET)
                                .setComponentId("widget-id")));

        String html = generator.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();
        assertThat(head).contains("<link rel=\"stylesheet\" href=\"assets/css/myfile.css?hash=" + assetSHA1 + "\">")
                .contains("<link rel=\"stylesheet\" href=\"http://moncdn/myfile.css\">")
                .contains("<script src=\"widgets/widget-id/assets/js/myfile.js?hash=" + assetSHA1 + "\"></script>");
    }

    @Test
    void should_add_active_and_ordered_asset_import_in_header() throws Exception {
        Page page = aPage().build();
        when(pageAssetRepository.readAllBytes(anyString(), any(Asset.class))).thenReturn(assetsContent);
        when(widgetAssetRepository.readAllBytes(any(Asset.class))).thenReturn(assetsContent);
        when(assetVisitor.visit(page)).thenReturn(
                Set.of(
                        //Widgets assets
                        new Asset().setName("myfile3.js").setOrder(3).setType(AssetType.JAVASCRIPT)
                                .setScope(AssetScope.WIDGET).setComponentId("widget-id"),
                        new Asset().setName("myfile2.js").setOrder(2).setType(AssetType.JAVASCRIPT)
                                .setScope(AssetScope.WIDGET).setComponentId("widget-id"),
                        new Asset().setName("myfile99.js").setOrder(99).setActive(false).setType(AssetType.JAVASCRIPT)
                                .setScope(AssetScope.WIDGET)
                                .setComponentId("widget-id"),
                        //Another widget but with a name starting with z
                        new Asset().setName("myfile4.js").setOrder(1).setType(AssetType.JAVASCRIPT)
                                .setScope(AssetScope.WIDGET).setComponentId("zidget-id"),
                        //Page asset
                        new Asset().setName("myfile1.js").setOrder(0).setType(AssetType.JAVASCRIPT)));

        String html = generator.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();

        //The header not contain inactive asset
        assertThat(head).doesNotContain("myfile99.js")
                //Page asset should be the last one, after widget assets identified by [widget-id] and widget assets identified by [zidget-id]
                .contains("<script src=\"widgets/widget-id/assets/js/myfile2.js?hash=" + assetSHA1 + "\"></script>\n" +
                        "<script src=\"widgets/widget-id/assets/js/myfile3.js?hash=" + assetSHA1 + "\"></script>\n" +
                        "<script src=\"widgets/zidget-id/assets/js/myfile4.js?hash=" + assetSHA1 + "\"></script>\n" +
                        "<script src=\"assets/js/myfile1.js?hash=" + assetSHA1 + "\"></script>");
    }

    @Test
    void should_import_asset_only_once_for_each_widget() throws Exception {
        Page page = aPage().build();

        var assets = new HashSet<Asset>();
        assets.add(new Asset().setName("myfile5.js").setOrder(0).setType(AssetType.JAVASCRIPT));
        assets.add(new Asset().setName("myfile2.js").setOrder(1).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile3.js").setOrder(2).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(3).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile3.js").setOrder(4).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile2.js").setOrder(5).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        when(pageAssetRepository.readAllBytes(anyString(), any(Asset.class))).thenReturn(assetsContent);
        when(widgetAssetRepository.readAllBytes(any(Asset.class))).thenReturn(assetsContent);
        when(assetVisitor.visit(page)).thenReturn(assets);

        String html = generator.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();

        // The page should contain exactly these imports
        assertThat(head).containsOnlyOnce(
                "<script src=\"widgets/widget-id/assets/js/myfile2.js?hash=" + assetSHA1 + "\"></script>\n" +
                        "<script src=\"widgets/widget-id/assets/js/myfile3.js?hash=" + assetSHA1 + "\"></script>\n" +
                        "<script src=\"widgets/widget-id/assets/js/myfile1.js?hash=" + assetSHA1 + "\"></script>\n" +
                        "<script src=\"assets/js/myfile5.js?hash=" + assetSHA1 + "\"></script>");
    }

    @Test
    void should_import_asset_only_once_globally() throws Exception {
        Page page = aPage().build();

        var assets = new HashSet<Asset>();
        assets.add(new Asset().setName("myfile1.js").setOrder(0).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(1).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("zidget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(2).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("gidget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(3).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("vidget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(4).setType(AssetType.JAVASCRIPT)
                .setScope(AssetScope.WIDGET).setComponentId("nidget-id"));
        when(widgetAssetRepository.readAllBytes(any(Asset.class))).thenReturn(assetsContent);
        when(assetVisitor.visit(page)).thenReturn(assets);

        String html = generator.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();

        // The page should contain exactly these imports
        assertThat(head).containsOnlyOnce(
                "<script src=\"widgets/widget-id/assets/js/myfile1.js?hash=" + assetSHA1 + "\"></script>\n");
    }

    private String format(String html) {
        return Jsoup.parse(html).toString();
    }
}
