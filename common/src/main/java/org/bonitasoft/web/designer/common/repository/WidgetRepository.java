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

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.exception.InUseException;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;

public class WidgetRepository extends AbstractRepository<Widget> {

    public static final String ANGULARJS_CUSTOM_PREFIX = "custom";
    public static final String ANGULARJS_STANDARD_PREFIX = "pb";
    public static final DirectoryStream.Filter<Path> CUSTOM_WIDGET_FILTER = path -> !path.getFileName().toString()
            .startsWith(ANGULARJS_STANDARD_PREFIX);

    public WidgetRepository(
            Path widgetsPath,
            Path templateResourcesPath,
            JsonFileBasedPersister<Widget> fileBasedRepository,
            JsonFileBasedLoader<Widget> widgetLoader,
            BeanValidator validator,
            Watcher watcher) {
        super(widgetsPath, fileBasedRepository, widgetLoader, validator, watcher,
                templateResourcesPath);
    }

    @Override
    public String getComponentName() {
        return "widget";
    }

    @Override
    public void delete(String widgetId) throws RepositoryException, NotAllowedException, InUseException {
        var widget = get(widgetId);
        if (!widget.isCustom()) {
            throw new NotAllowedException("We can only delete a custom widget");
        }
        super.delete(widgetId);
    }

    /**
     * Create a new widget, computing id with widget name
     */
    public Widget create(Widget widget) throws IllegalArgumentException {
        var id = ANGULARJS_CUSTOM_PREFIX + trimToEmpty(capitalize(widget.getName()));
        try {
            var existingWidget = get(id);
            throw new NotAllowedException("Widget with name " + existingWidget.getName() + " already exists");
        } catch (NotFoundException e) {
            widget.setCustom(true);
            widget.setId(id);
            createComponentDirectory(widget);
            updateLastUpdateAndSave(widget);
            return widget;
        }

    }

    public List<Widget> getByIds(Set<String> widgetIds) {
        List<Widget> result = new ArrayList<>();
        for (var widgetId : widgetIds) {
            result.add(get(widgetId));
        }
        return result;
    }

    public List<Property> addProperty(String widgetId, Property property) {
        var widget = get(widgetId);
        var existingProperty = widget.getProperty(property.getName());
        if (existingProperty != null) {
            throw new NotAllowedException(
                    format("Widget [ %s ] has already a property named %s", widgetId, property.getName()));
        }
        widget.addProperty(property);
        updateLastUpdateAndSave(widget);
        return widget.getProperties();
    }

    public List<Property> updateProperty(String widgetId, String propertyName, Property property) {
        var widget = get(widgetId);
        var existingProperty = widget.getProperty(propertyName);
        if (existingProperty == null) {
            throw new NotFoundException(format("Widget [ %s ] has no property named %s", widgetId, propertyName));
        }
        widget.replaceProperty(existingProperty, property);
        updateLastUpdateAndSave(widget);
        return widget.getProperties();
    }

    public List<Property> deleteProperty(String widgetId, String propertyName) {
        var widget = get(widgetId);
        var param = widget.getProperty(propertyName);
        if (param == null) {
            throw new NotFoundException(format("Widget [ %s ] has no property named %s", widgetId, propertyName));
        }
        widget.deleteProperty(param);
        updateLastUpdateAndSave(widget);
        return widget.getProperties();
    }

}
