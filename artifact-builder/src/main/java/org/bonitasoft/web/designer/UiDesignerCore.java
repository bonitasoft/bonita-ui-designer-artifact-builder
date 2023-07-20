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
package org.bonitasoft.web.designer;

import java.util.List;

import org.bonitasoft.web.dao.model.fragment.Fragment;
import org.bonitasoft.web.dao.model.page.Page;
import org.bonitasoft.web.dao.model.widgets.Widget;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.dao.repository.AssetRepository;
import org.bonitasoft.web.dao.repository.FragmentRepository;
import org.bonitasoft.web.dao.repository.PageRepository;
import org.bonitasoft.web.dao.repository.WidgetRepository;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UiDesignerCore {

    private final Watcher watcher;
    private final PageRepository pageRepository;
    private final AssetRepository<Page> pageAssetRepository;
    private final PageService pageService;
    private final AssetService<Page> pageAssetService;

    private final FragmentRepository fragmentRepository;
    private final FragmentService fragmentService;

    private final WidgetRepository widgetRepository;
    private final AssetRepository<Widget> widgetAssetRepository;
    private final WidgetService widgetService;
    private final AssetService<Widget> widgetAssetService;

    private final List<Migration<Page>> pageMigrationStepsList;
    private final List<Migration<Fragment>> fragmentMigrationStepsList;
    private final List<Migration<Widget>> widgetMigrationStepsList;
    private final AssetDependencyImporter<Widget> widgetAssetDependencyImporter;
}
