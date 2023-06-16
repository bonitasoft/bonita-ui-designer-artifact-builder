/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;

import java.util.List;

public interface WidgetService extends AssetableArtifactService<Widget> {
    Widget create(Widget widget);

    Widget createFrom(String sourceWidgetId, Widget widget);

    Widget save(String widgetId, Widget widget);

    List<Property> addProperty(String widgetId, Property property);

    List<Property> updateProperty(String widgetId, String propertyName, Property property);

    List<Property> deleteProperty(String widgetId, String propertyName);

    Widget get(String id);

    Widget getWithAsset(String id);

    List<Widget> getAll();

    List<Widget> getAllWithUsedBy();

    void delete(String id);

    List<MigrationStepReport> migrateAllCustomWidgetUsedInPreviewable(Previewable previewable);

    MigrationStatusReport getMigrationStatusOfCustomWidgetUsed(Previewable previewable);
}
