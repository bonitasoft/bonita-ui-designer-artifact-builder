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

import org.bonitasoft.web.angularjs.export.HtmlExportStep;
import org.bonitasoft.web.angularjs.export.WidgetsExportStep;
import org.bonitasoft.web.angularjs.localization.LocalizationFactory;
import org.bonitasoft.web.angularjs.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.angularjs.rendering.DirectivesCollector;
import org.bonitasoft.web.angularjs.rendering.HtmlGenerator;
import org.bonitasoft.web.angularjs.visitor.*;
import org.bonitasoft.web.dao.CommonGenerator;
import org.bonitasoft.web.dao.JsonHandler;
import org.bonitasoft.web.dao.export.ExportStep;
import org.bonitasoft.web.dao.model.page.Page;
import org.bonitasoft.web.dao.model.widgets.Widget;
import org.bonitasoft.web.dao.repository.AssetRepository;
import org.bonitasoft.web.dao.repository.FragmentRepository;
import org.bonitasoft.web.dao.repository.PageRepository;
import org.bonitasoft.web.dao.repository.WidgetRepository;
import org.bonitasoft.web.dao.visitor.AssetVisitor;
import org.bonitasoft.web.dao.visitor.FragmentIdVisitor;
import org.bonitasoft.web.dao.visitor.PageFactory;
import org.bonitasoft.web.dao.visitor.WidgetIdVisitor;

import java.nio.file.Path;
import java.util.List;


public class AngularJsGenerator extends CommonGenerator {

    private final GeneratorProperties generatorProperties;
    private final DirectiveFileGenerator directiveFileGenerator;
    private final HtmlGenerator htmlGenerator;
    private final HtmlBuilderVisitor htmlBuilderVisitor;
    private final WidgetIdVisitor widgetIdVisitor;
    private final Path widgetUserRepoPath;

    public AngularJsGenerator(JsonHandler jsonHandler,
            FragmentIdVisitor fragmentIdVisitor,
            DirectiveFileGenerator directiveFileGenerator,
            WidgetIdVisitor widgetIdVisitor,
            PageRepository pageRepository,
            WidgetRepository widgetRepository,
            AssetRepository<Widget> widgetAssetRepository,
            AssetRepository<Page> pageAssetRepository,
            FragmentRepository fragmentRepository,
                              Path widgetUserRepoPath) {
        this.widgetIdVisitor = widgetIdVisitor;
        this.directiveFileGenerator = directiveFileGenerator;
        this.widgetUserRepoPath = widgetUserRepoPath;

        List<PageFactory> pageFactories = List.of(
                new LocalizationFactory(pageRepository),
                new ModelPropertiesVisitor(fragmentRepository),
                new PropertyValuesVisitor(fragmentRepository),
                new VariableModelVisitor(fragmentRepository));

        this.generatorProperties = new GeneratorProperties("workspace-uid", "i18n");;
        this.htmlBuilderVisitor = new HtmlBuilderVisitor(fragmentRepository);
        var directivesCollector = new DirectivesCollector(jsonHandler,
                generatorProperties.getTmpPagesRepositoryPath(),
                generatorProperties.getTmpFragmentsRepositoryPath(),
                directiveFileGenerator,
                fragmentIdVisitor,
                fragmentRepository);
        var requiredModulesVisitor= new RequiredModulesVisitor(widgetRepository, fragmentRepository);
        var assetVisitor = new AssetVisitor(widgetRepository, fragmentRepository);

        this.htmlGenerator = new HtmlGenerator(
                this.htmlBuilderVisitor,
                directivesCollector,requiredModulesVisitor,
                assetVisitor,
                widgetAssetRepository,
                pageAssetRepository,
                pageFactories);
    }


    public ExportStep[] getPageExportStep() {
//        var commonSteps = super.getPageExportStep();
//        var pageExportSteps = Stream
//                .concat(Arrays.stream(commonSteps), Arrays.stream(angularJsGenerator.getPageExportStep()))
//                .toArray(ExportStep[]::new);
        return new ExportStep[] {
                new HtmlExportStep(htmlGenerator, this.generatorProperties.getExportBackendResourcesPath()),
                new WidgetsExportStep<Page>(widgetUserRepoPath, widgetIdVisitor,
                        this.directiveFileGenerator)
        };
    }


    public GeneratorProperties getGeneratorProperties(){
        return this.generatorProperties;
    }

    public HtmlBuilderVisitor getHtmlBuilderVisitor() {
        return this.htmlBuilderVisitor;
    }

    public HtmlGenerator getHtmlGenerator() {
        return this.htmlGenerator;
    }
}
