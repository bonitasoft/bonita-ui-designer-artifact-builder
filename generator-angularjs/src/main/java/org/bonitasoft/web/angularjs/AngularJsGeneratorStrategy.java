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
package org.bonitasoft.web.angularjs;

import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.angularjs.export.HtmlExportStep;
import org.bonitasoft.web.angularjs.export.WidgetsExportStep;
import org.bonitasoft.web.angularjs.localization.LocalizationFactory;
import org.bonitasoft.web.angularjs.rendering.DefaultHtmlGenerator;
import org.bonitasoft.web.angularjs.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.angularjs.rendering.DirectivesCollector;
import org.bonitasoft.web.angularjs.visitor.HtmlBuilderVisitor;
import org.bonitasoft.web.angularjs.visitor.ModelPropertiesVisitor;
import org.bonitasoft.web.angularjs.visitor.PropertyValuesVisitor;
import org.bonitasoft.web.angularjs.visitor.RequiredModulesVisitor;
import org.bonitasoft.web.angularjs.visitor.VariableModelVisitor;
import org.bonitasoft.web.angularjs.workspace.FragmentDirectiveBuilder;
import org.bonitasoft.web.angularjs.workspace.WidgetDirectiveBuilder;
import org.bonitasoft.web.designer.common.CommonGeneratorStrategy;
import org.bonitasoft.web.designer.common.export.ExportStep;
import org.bonitasoft.web.designer.common.generator.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.common.livebuild.AbstractLiveFileBuilder;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.*;
import org.bonitasoft.web.designer.common.visitor.AssetVisitor;
import org.bonitasoft.web.designer.common.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.common.visitor.PageFactory;
import org.bonitasoft.web.designer.common.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;

public class AngularJsGeneratorStrategy extends CommonGeneratorStrategy {

    private final GeneratorProperties generatorProperties;
    private final DirectiveFileGenerator directiveFileGenerator;
    private final DefaultHtmlGenerator htmlGenerator;
    private final HtmlBuilderVisitor htmlBuilderVisitor;
    private final WidgetIdVisitor widgetIdVisitor;
    private final Path widgetUserRepoPath;
    private final FragmentDirectiveBuilder fragmentDirectiveBuilder;
    private final WidgetDirectiveBuilder widgetFileBuilder;

    public AngularJsGeneratorStrategy(JsonHandler jsonHandler,
            Watcher watcher,
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

        this.generatorProperties = new GeneratorProperties("workspace-uid", "i18n");
        this.htmlBuilderVisitor = new HtmlBuilderVisitor(fragmentRepository);
        var directivesCollector = new DirectivesCollector(jsonHandler,
                generatorProperties.getTmpPagesRepositoryPath(),
                generatorProperties.getTmpFragmentsRepositoryPath(),
                directiveFileGenerator,
                fragmentIdVisitor,
                fragmentRepository);
        var requiredModulesVisitor = new RequiredModulesVisitor(widgetRepository, fragmentRepository);
        var assetVisitor = new AssetVisitor(widgetRepository, fragmentRepository);

        this.htmlGenerator = new DefaultHtmlGenerator(
                this.htmlBuilderVisitor,
                directivesCollector, requiredModulesVisitor,
                assetVisitor,
                widgetAssetRepository,
                pageAssetRepository,
                pageFactories);
        this.fragmentDirectiveBuilder = new FragmentDirectiveBuilder(watcher, jsonHandler,
                this.getHtmlBuilderVisitor(),
                this.generatorProperties.isLiveBuildEnabled());

        this.widgetFileBuilder = new WidgetDirectiveBuilder(watcher,
                new WidgetFileBasedLoader(jsonHandler), this.generatorProperties.isLiveBuildEnabled());
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

    @Override
    public GeneratorProperties getGeneratorProperties() {
        return this.generatorProperties;
    }

    @Override
    public AbstractLiveFileBuilder widgetFileBuilder() {
        return widgetFileBuilder;
    }

    @Override
    public AbstractLiveFileBuilder fragmentDirectiveBuilder() {
        return fragmentDirectiveBuilder;
    }

    public HtmlBuilderVisitor getHtmlBuilderVisitor() {
        return this.htmlBuilderVisitor;
    }

    public HtmlGenerator getHtmlGenerator() {
        return this.htmlGenerator;
    }
}
