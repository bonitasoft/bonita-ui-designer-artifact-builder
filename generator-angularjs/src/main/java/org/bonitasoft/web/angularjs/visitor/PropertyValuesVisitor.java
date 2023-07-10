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

import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.angularjs.rendering.TemplateEngine;
import org.bonitasoft.web.designer.common.generator.rendering.GenerationException;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.common.visitor.PageFactory;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import lombok.RequiredArgsConstructor;

/**
 * An element visitor which traverses the tree of elements recursively to collect property values in a page
 */
@RequiredArgsConstructor
public class PropertyValuesVisitor implements ElementVisitor<Map<String, Map<String, PropertyValue>>>, PageFactory {

    private final FragmentRepository fragmentRepository;

    @Override
    public Map<String, Map<String, PropertyValue>> visit(FragmentElement fragmentElement) {
        try {
            return fragmentRepository.get(fragmentElement.getId()).toContainer(fragmentElement).accept(this);
        } catch (RepositoryException | NotFoundException e) {
            throw new GenerationException(
                    "Error while generating property values for fragment " + fragmentElement.getId(), e);
        }
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Component component) {
        return singletonMap(component.getReference(), component.getPropertyValues());
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Container container) {
        var propertyValues = getPropertyValuesFor(container.getRows());
        propertyValues.put(container.getReference(), container.getPropertyValues());
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(FormContainer formContainer) {
        var propertyValues = formContainer.getContainer().accept(this);
        propertyValues.put(formContainer.getReference(), formContainer.getPropertyValues());
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(TabsContainer tabsContainer) {
        Map<String, Map<String, PropertyValue>> propertyValues = new HashMap<>();
        propertyValues.put(tabsContainer.getReference(), tabsContainer.getPropertyValues());
        for (var tabContainer : tabsContainer.getTabList()) {
            propertyValues.putAll(tabContainer.accept(this));
        }
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(TabContainer tabContainer) {
        var propertyValues = tabContainer.getContainer().accept(this);
        propertyValues.put(tabContainer.getReference(), tabContainer.getPropertyValues());
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(ModalContainer modalContainer) {
        var propertyValues = modalContainer.getContainer().accept(this);
        propertyValues.put(modalContainer.getReference(), modalContainer.getPropertyValues());
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Previewable previewable) {
        return getPropertyValuesFor(previewable.getRows());
    }

    public <P extends Previewable & Identifiable> String generate(P previewable) {
        return new TemplateEngine("factory.hbs.js")
                .with("name", "propertyValues")
                .with("resources", this.visit(previewable))
                .build(this);
    }

    private Map<String, Map<String, PropertyValue>> getPropertyValuesFor(List<List<Element>> rows) {
        Map<String, Map<String, PropertyValue>> propertyValues = new HashMap<>();
        for (var row : rows) {
            for (var element : row) {
                propertyValues.putAll(element.accept(this));
            }
        }
        return propertyValues;
    }
}
