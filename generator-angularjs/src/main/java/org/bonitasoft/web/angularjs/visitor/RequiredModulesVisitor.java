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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequiredModulesVisitor implements ElementVisitor<Set<String>> {

    private final WidgetRepository widgetRepository;
    private final FragmentRepository fragmentRepository;

    @Override
    public Set<String> visit(FragmentElement fragmentElement) {
        return visitRows(fragmentRepository.get(fragmentElement.getId()).getRows());
    }

    @Override
    public Set<String> visit(Container container) {
        Set<String> modules = new HashSet<>();
        var widget = widgetRepository.get(container.getId());
        modules.addAll(widget.getRequiredModules());
        modules.addAll(visitRows(container.getRows()));
        return modules;
    }

    @Override
    public Set<String> visit(FormContainer formContainer) {
        Set<String> modules = new HashSet<>();
        var widget = widgetRepository.get(formContainer.getId());
        modules.addAll(widget.getRequiredModules());
        modules.addAll(formContainer.getContainer().accept(this));
        return modules;
    }

    @Override
    public Set<String> visit(TabsContainer tabsContainer) {
        Set<String> modules = new HashSet<>();
        var widget = widgetRepository.get(tabsContainer.getId());
        modules.addAll(widget.getRequiredModules());
        for (var tabContainer : tabsContainer.getTabList()) {
            modules.addAll(tabContainer.accept(this));
        }
        return modules;
    }

    @Override
    public Set<String> visit(ModalContainer modalContainer) {
        Set<String> modules = new HashSet<>();
        var widget = widgetRepository.get(modalContainer.getId());
        modules.addAll(widget.getRequiredModules());
        modules.addAll(modalContainer.getContainer().accept(this));
        return modules;
    }

    @Override
    public Set<String> visit(TabContainer tabContainer) {
        Set<String> modules = new HashSet<>();
        modules.addAll(tabContainer.getContainer().accept(this));
        return modules;
    }

    @Override
    public Set<String> visit(Component component) {
        var widget = widgetRepository.get(component.getId());
        return widget.getRequiredModules();
    }

    @Override
    public <P extends Previewable & Identifiable> Set<String> visit(P previewable) {
        return visitRows(previewable.getRows());
    }

    protected <P extends Previewable & Identifiable> Set<String> visitRows(List<List<Element>> rows) {
        Set<String> modules = new HashSet<>();
        for (var elements : rows) {
            for (var element : elements) {
                modules.addAll(element.accept(this));
            }
        }
        return modules;
    }

}
