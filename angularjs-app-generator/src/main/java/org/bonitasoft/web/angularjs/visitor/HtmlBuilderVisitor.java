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
package org.bonitasoft.web.angularjs.visitor;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.dao.model.widgets.Widget.spinalCase;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.angularjs.rendering.TemplateEngine;
import org.bonitasoft.web.dao.generator.rendering.GenerationException;
import org.bonitasoft.web.dao.model.page.*;
import org.bonitasoft.web.dao.repository.FragmentRepository;
import org.bonitasoft.web.dao.repository.exception.NotFoundException;
import org.bonitasoft.web.dao.repository.exception.RepositoryException;
import org.bonitasoft.web.dao.visitor.ElementVisitor;

import lombok.RequiredArgsConstructor;

/**
 * An element visitor which traverses the tree of elements recursively to collect html parts of a page
 */
@RequiredArgsConstructor
public class HtmlBuilderVisitor implements ElementVisitor<String> {

    private final FragmentRepository fragmentRepository;

    @Override
    public String visit(FragmentElement fragmentElement) {

        try {
            var fragment = fragmentRepository.get(fragmentElement.getId());
            return new TemplateEngine("fragment.hbs.html")
                    .with("reference", fragmentElement.getReference())
                    .with("dimensionAsCssClasses", fragmentElement.getDimensionAsCssClasses())
                    .with("tagName", spinalCase(fragment.getDirectiveName()))
                    .build(fragment);

        } catch (RepositoryException | NotFoundException e) {
            throw new GenerationException("Error while generating html for fragment " + fragmentElement.getId(), e);
        }
    }

    @Override
    public String visit(Container container) {
        return new TemplateEngine("container.hbs.html")
                .with("rowsHtml", build(container.getRows()))
                .build(container);
    }

    public String build(List<List<Element>> rows) {
        return new TemplateEngine("rows.hbs.html")
                .with("rows",
                        rows.stream()
                                .map(elements -> elements.stream()
                                        .map(element -> element.accept(HtmlBuilderVisitor.this))
                                        .collect(joining("")))
                                .collect(toList()))
                .build(new Object());
    }

    @Override
    public String visit(FormContainer formContainer) {
        return new TemplateEngine("formContainer.hbs.html")
                .with("content", formContainer.getContainer().accept(this))
                .build(formContainer);
    }

    @Override
    public String visit(TabsContainer tabsContainer) {

        var tabTemplates = new ArrayList<TabContainerTemplate>();
        for (var tab : tabsContainer.getTabList()) {
            tabTemplates.add(new TabContainerTemplate(tab.accept(this)));
        }

        return new TemplateEngine("tabsContainer.hbs.html")
                .with("tabTemplates", tabTemplates)
                .build(tabsContainer);
    }

    @Override
    public String visit(TabContainer tabContainer) {
        return new TemplateEngine("tabContainer.hbs.html")
                .with("content", tabContainer.getContainer().accept(this))
                .build(tabContainer);
    }

    @Override
    public String visit(ModalContainer modalContainer) {
        return new TemplateEngine("modalContainer.hbs.html")
                .with("content", modalContainer.getContainer().accept(this))
                .with("modalidHtml", modalContainer.getPropertyValues().get("modalId").getValue())
                .build(modalContainer);
    }

    @Override
    public String visit(Component component) {
        return new TemplateEngine("component.hbs.html")
                .with("template", "<" + spinalCase(component.getId()) + "></" + spinalCase(component
                        .getId()) + ">")
                .build(component);
    }

    @Override
    public String visit(Previewable previewable) {
        //TODO: Add new exeption to replace ArtifactBuilderException
        throw new RuntimeException("Can't build previewable html by visiting it. Need to call " +
                "HtmlBuilderVisitor#build.");
    }

    static class TabContainerTemplate {

        private final String content;

        public TabContainerTemplate(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }
}
