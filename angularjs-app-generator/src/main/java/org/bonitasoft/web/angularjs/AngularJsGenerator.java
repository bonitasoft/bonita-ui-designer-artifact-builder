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
package org.bonitasoft.web.angularjs;

import java.util.List;

import org.bonitasoft.web.angularjs.localization.LocalizationFactory;
import org.bonitasoft.web.angularjs.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.angularjs.rendering.DirectivesCollector;
import org.bonitasoft.web.angularjs.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.Generator;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;

public class AngularJsGenerator implements Generator {

    private final FragmentIdVisitor fragmentIdVisitor;
    private final UiDesignerProperties uiDesignerProperties;
    private final DirectiveFileGenerator directiveFileGenerator;
    private final HtmlGenerator htmlGenerator;
    private final HtmlBuilderVisitor htmlBuilderVisitor;
    private final WidgetIdVisitor widgetIdVisitor;
    private final AssetRepository<Page> pageAssetRepository;

    public AngularJsGenerator(JsonHandler jsonHandler,
            FragmentIdVisitor fragmentIdVisitor,
            DirectiveFileGenerator directiveFileGenerator,
            UiDesignerProperties uiDesignerProperties,
            WidgetIdVisitor widgetIdVisitor,
            PageRepository pageRepository,
            WidgetRepository widgetRepository,
            AssetRepository<Widget> widgetAssetRepository,
            AssetRepository<Page> pageAssetRepository,
            FragmentRepository fragmentRepository) {
        this.fragmentIdVisitor = fragmentIdVisitor;
        this.uiDesignerProperties = uiDesignerProperties;
        this.widgetIdVisitor = widgetIdVisitor;
        this.directiveFileGenerator = directiveFileGenerator;
        this.pageAssetRepository = pageAssetRepository;

        List<PageFactory> pageFactories = List.of(
                new LocalizationFactory(pageRepository),
                new ModelPropertiesVisitor(fragmentRepository),
                new PropertyValuesVisitor(fragmentRepository),
                new VariableModelVisitor(fragmentRepository));

        this.htmlBuilderVisitor = new HtmlBuilderVisitor(
                new AssetVisitor(widgetRepository, fragmentRepository),
                pageFactories,
                new RequiredModulesVisitor(widgetRepository, fragmentRepository),
                new DirectivesCollector(jsonHandler, uiDesignerProperties.getWorkspaceUid(),
                        directiveFileGenerator,
                        fragmentIdVisitor,
                        fragmentRepository),
                pageAssetRepository,
                widgetAssetRepository,
                fragmentRepository);

        this.htmlGenerator = new HtmlGenerator(this.htmlBuilderVisitor);
    }

    @Override
    public ExportStep[] getPageExportStep() {
        return new ExportStep[] {
                new HtmlExportStep(htmlGenerator, uiDesignerProperties.getWorkspaceUid()),
                new WidgetsExportStep<Page>(uiDesignerProperties.getWorkspace().getWidgets().getDir(), widgetIdVisitor,
                        this.directiveFileGenerator),
                new AssetExportStep(pageAssetRepository),
                new FragmentsExportStep<Page>(fragmentIdVisitor,
                        uiDesignerProperties.getWorkspace().getFragments().getDir())
        };
    }

    @Override
    public HtmlBuilderVisitor getHtmlBuilderVisitor() {
        return this.htmlBuilderVisitor;
    }

    @Override
    public HtmlGenerator getHtmlGenerator() {
        return this.htmlGenerator;
    }
}
