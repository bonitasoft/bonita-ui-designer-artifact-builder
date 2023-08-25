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
package org.bonitasoft.web.angularjs.visitor;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.angularjs.utils.assertions.CustomAssertions.assertThatHtmlBody;
import static org.bonitasoft.web.angularjs.utils.assertions.CustomAssertions.toBody;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.*;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.ResponsiveDimension.*;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.bonitasoft.web.angularjs.rendering.DefaultHtmlGenerator;
import org.bonitasoft.web.angularjs.utils.rule.TestResource;
import org.bonitasoft.web.designer.builder.ModalContainerBuilder;
import org.bonitasoft.web.designer.common.generator.rendering.GenerationException;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HtmlBuilderVisitorTest {

    private HtmlBuilderVisitor visitor;
    @Mock
    private FragmentRepository fragmentRepository;

    public TestResource testResource = new TestResource(DefaultHtmlGenerator.class);

    @BeforeEach
    void setUp() throws Exception {
        visitor = new HtmlBuilderVisitor(fragmentRepository);
    }

    @Test
    void should_build_a_component_html_when_visiting_a_component() throws Exception {
        assertThatHtmlBody(visitor.visit(aComponent("pbWidget")
                .withReference("component-reference")
                .withPropertyValue("property", "value")
                .build())).isEqualToBody(testResource.load("component.html"));
    }

    @Test
    void should_add_dimension_to_component() throws Exception {

        Element element = toBody(visitor.visit(aComponent("pbWidget")
                .withPropertyValue("property", "value")
                .withDimensions(sm(3))
                .build()));

        assertThatHtmlBody(element.childNode(1).outerHtml()).hasClass("col-xs-12", "col-sm-3");
    }

    @Test
    void should_build_a_container() throws GenerationException {

        assertThatHtmlBody(visitor.visit(aContainer()
                .withReference("container-reference")
                .withPropertyValue("property", "value")
                .build())).isEqualToBody(testResource.load("simplecontainer.html"));
    }

    @org.junit.Test
    void should_add_rows_to_the_container() {

        assertThatHtmlBody(visitor.visit(aContainer().with(aRow()).withReference("container-reference").build()))
                .isEqualToBody(testResource.load("containerWithRow.html"));
    }

    @Test
    void should_build_a_repeatable_container() throws GenerationException {

        final String html = visitor.visit(
                aContainer()
                        .withReference("container-reference")
                        .withPropertyValue("repeatedCollection", "json", "[\"foo\",\"bar\"]")
                        .build());
        assertThatHtmlBody(html)
                .isEqualToBody(testResource.load("repeatedContainer.html"));
    }

    @Test
    void should_not_build_a_repeatable_container_if_repeated_collection_is_an_empty_string()
            throws GenerationException {

        assertThatHtmlBody(visitor.visit(aContainer().withReference("container-reference")
                .withPropertyValue("repeatedCollection", "json", "")
                .build())).isEqualToBody(testResource.load("notRepeatedContainer.html"));
    }

    @Test
    void should_add_dimension_to_the_container() throws GenerationException {
        Container container = aContainer().withDimensions(xs(5), sm(7), md(9), lg(10)).build();

        String html = visitor.visit(container);

        assertThatHtmlBody(html).isEqualToBody(testResource.load("containerWithDimension.html"));
    }

    @Test
    void should_add_elements_to_the_container_rows() throws Exception {

        // we should have two div.col-xs-12 with two div.row containing added components
        Elements rows = toBody(visitor.visit(aContainer().with(
                aRow().with(
                        aComponent().withWidgetId("pbLabel").build()),
                aRow().with(
                        aComponent().withWidgetId("customLabel").build()))
                .build())).select(".row");

        assertThat(rows).hasSize(2);
        assertThat(rows.first().select("pb-label").outerHtml()).isEqualTo("<pb-label></pb-label>");
        assertThat(rows.last().select("custom-label").outerHtml()).isEqualTo("<custom-label></custom-label>");
    }

    @Test
    void should_build_a_tabsContainer_html_when_visiting_a_tabsContainer() throws Exception {
        TabContainer tab = aTabContainer().withId("1").with(aContainer().withReference("first-container"))
                .withReference("tab-container-1").build();
        TabContainer tab1 = aTabContainer().withId("2").with(aContainer().withReference("last-container"))
                .withReference("tab-container-2").build();

        assertThatHtmlBody(visitor.visit(aTabsContainer().with(tab, tab1)
                .withReference("tabs-container-reference")
                .build())).isEqualToBody(testResource.load("tabsContainerWithTwoTabs.html"));
    }

    @Test
    void should_build_a_tab_container_bootstrap_like() throws Exception {

        assertThatHtmlBody(visitor.visit(aTabsContainer()
                .withReference("tabs-container-reference")
                .withDimension(4)
                .withPropertyValue("property", "value")
                .build())).isEqualToBody(testResource.load("simpleTabContainer.html"));
    }

    @Test
    void should_add_elements_to_the_tab_container_tabs() throws Exception {
        TabContainer tab = aTabContainer()
                .withId("1")
                .with(aContainer()
                        .with(aRow().with(aParagraph().withReference("paragraph-reference")))
                        .withReference("container-reference"))
                .withReference("tab-reference").build();
        assertThatHtmlBody(visitor.visit(aTabsContainer()
                .with(tab)
                .withReference("tabs-container-reference")
                .build())).isEqualToBody(testResource.load("tabsContainerWithContent.html"));
    }

    @org.junit.Test
    void should_generate_html_for_a_formcontainer() throws GenerationException {
        assertThatHtmlBody(
                visitor.visit(aFormContainer()
                        .with(aContainer().withReference("container-reference").build())
                        .withReference("formcontainer-reference")
                        .build()))
                .isEqualToBody(testResource.load("formContainerSimple.html"));
    }

    @Test
    void should_add_dimension_to_the_formcontainer() throws GenerationException {
        FormContainer formContainer = aFormContainer()
                .with(aContainer().withReference("container-reference").build())
                .withDimensions(xs(5), sm(7), md(9), lg(10)).build();

        String html = visitor.visit(formContainer);

        assertThatHtmlBody(html).isEqualToBody(testResource.load("formContainerWithDimension.html"));
    }

    @Test
    void should_add_container_to_the_formcontainer() throws Exception {
        FormContainer formContainer = aFormContainer()
                .with(aContainer().with(aRow().with(
                        aComponent().withWidgetId("pbLabel").withReference("component-reference").build()))
                        .withReference("container-reference").build())
                .withReference("formcontainer-reference")
                .build();

        assertThatHtmlBody(visitor.visit(formContainer))
                .isEqualToBody(testResource.load("formContainerWithContainer.html"));
    }

    /**
     * Test for Modal Container
     */
    @Test
    void should_build_a_modal_container() throws GenerationException {
        assertThatHtmlBody(
                visitor.visit(aModalContainer().with(aContainer().withReference("container-reference").build())
                        .withReference("modal-container-reference")
                        .withPropertyValue("property", "value")
                        .withPropertyValue("modalId", "modal1")
                        .build()))
                .isEqualToBody(testResource.load("modalContainer.html"));
    }

    @Test
    void should_add_row_to_the_modal_container() throws Exception {
        ModalContainerBuilder modal = aModalContainer();
        modal.withPropertyValue("modalId", "modal1");
        modal.with(aContainer().with(aRow().build()).withReference("first-container").build());
        assertThatHtmlBody(visitor.visit(modal.build()))
                .isEqualToBody(testResource.load("modalContainerWithRow.html"));
    }

    @Test
    void should_build_rows() throws Exception {

        String html = visitor.build(asList(
                aRow().with(aParagraph().withReference("1")).build(),
                aRow().with(anInput().withReference("2"), aParagraph().withReference("3")).build()));

        assertThatHtmlBody(html).isEqualToBody(testResource.load("rowsWithComponents.html"));
    }

    @Test
    void should_get_html_from_main_container_of_associated_fragment() throws Exception {
        when(fragmentRepository.get("fragment-id")).thenReturn(aFragment()
                .withId("fragment-id")
                .withName("person")
                .withVariable("aKey", aConstantVariable().value("aValue").exposed(true))
                .build());

        assertThatHtmlBody(visitor.visit(aFragmentElement()
                .withFragmentId("fragment-id")
                .withBinding("fragmentVariable1", "pageVariable1")
                .withReference("fragment-reference")
                .build())).isEqualToBody(testResource.load("fragment.html"));
    }

    @Test
    void should_throw_generation_exception_when_associated_fragment_is_not_found() throws Exception {
        when(fragmentRepository.get("unknown-fragment")).thenThrow(new NotFoundException("not found"));
        var fragment = aFragmentElement()
                .withFragmentId("unknown-fragment")
                .build();

        assertThrows(GenerationException.class, () -> visitor.visit(fragment));
    }

    @Test
    void should_throw_generation_exception_when_error_occurs_while_getting_associated_fragment_from_repository() {
        when(fragmentRepository.get("bad-fragment")).thenThrow(new RepositoryException("error", new Exception()));
        var fragment = aFragmentElement()
                .withFragmentId("bad-fragment")
                .build();

        assertThrows(GenerationException.class, () -> visitor.visit(fragment));
    }

}
