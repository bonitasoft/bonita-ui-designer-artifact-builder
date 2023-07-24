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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bonitasoft.web.angularjs.AngularJsGenerator;
import org.bonitasoft.web.angularjs.export.WidgetsExportStep;
import org.bonitasoft.web.angularjs.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.angularjs.workspace.FragmentDirectiveBuilder;
import org.bonitasoft.web.angularjs.workspace.WidgetDirectiveBuilder;
import org.bonitasoft.web.dao.JsonHandler;
import org.bonitasoft.web.dao.export.ExportStep;
import org.bonitasoft.web.dao.model.fragment.Fragment;
import org.bonitasoft.web.dao.model.page.Page;
import org.bonitasoft.web.dao.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.dao.visitor.FragmentIdVisitor;
import org.bonitasoft.web.dao.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.export.properties.FragmentPropertiesBuilder;
import org.bonitasoft.web.designer.controller.export.properties.PagePropertiesBuilder;
import org.bonitasoft.web.designer.controller.export.properties.WidgetPropertiesBuilder;
import org.bonitasoft.web.designer.controller.export.steps.*;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.PageImporter;
import org.bonitasoft.web.designer.controller.importer.WidgetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.FragmentDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetDependencyImporter;
import org.bonitasoft.web.designer.i18n.I18nInitializer;
import org.bonitasoft.web.designer.i18n.LanguagePackBuilder;
import org.bonitasoft.web.designer.i18n.LanguagePackFactory;
import org.bonitasoft.web.designer.migration.LiveRepositoryUpdate;
import org.bonitasoft.web.designer.rendering.IHtmlGenerator;
import org.bonitasoft.web.designer.workspace.ResourcesCopier;
import org.bonitasoft.web.designer.workspace.Workspace;

import lombok.RequiredArgsConstructor;

/**
 * @author Julien Mege
 */
@RequiredArgsConstructor
public class ArtifactBuilderFactory {

    private final UiDesignerProperties uiDesignerProperties;
    private final JsonHandler jsonHandler;
    private final UiDesignerCore core;

    public ArtifactBuilderFactory(UiDesignerProperties uiDesignerProperties) {
        this.uiDesignerProperties = uiDesignerProperties;
        this.jsonHandler = new JsonHandlerFactory().create();
        this.core = new UiDesignerCoreFactory(this.uiDesignerProperties, this.jsonHandler).create();
    }

    /**
     * Factory method for an instance of {@link ArtifactBuilder}
     *
     * @return
     */
    public ArtifactBuilder create() {

        var fragmentIdVisitor = new FragmentIdVisitor(core.getFragmentRepository());
        var widgetIdVisitor = new WidgetIdVisitor(core.getFragmentRepository());

        var directiveFileGenerator = new DirectiveFileGenerator(
                uiDesignerProperties.getWorkspace().getWidgets().getDir(),
                core.getWidgetRepository(), widgetIdVisitor);

        //Page
        var angularJsGenerator = new AngularJsGenerator(
                this.jsonHandler,
                fragmentIdVisitor,
                directiveFileGenerator,
                widgetIdVisitor,
                core.getPageRepository(),
                core.getWidgetRepository(),
                core.getWidgetAssetRepository(),
                core.getPageAssetRepository(),
                core.getFragmentRepository(),
                uiDesignerProperties.getWorkspace().getWidgets().getDir());

        var commonExportStep = new ExportStep[] {
                new PagePropertiesExportStep(new PagePropertiesBuilder(uiDesignerProperties, core.getPageService())),
                new AssetExportStep(core.getPageAssetRepository()),
                new FragmentsExportStep<Page>(fragmentIdVisitor,
                        uiDesignerProperties.getWorkspace().getFragments().getDir())
        };
        var pageExportSteps = Stream
                .concat(Arrays.stream(commonExportStep), Arrays.stream(angularJsGenerator.getPageExportStep()))
                .toArray(ExportStep[]::new);

        //Fragment
        var fragmentExportSteps = new ExportStep[] {
                new WidgetsExportStep<Fragment>(uiDesignerProperties.getWorkspace().getWidgets().getDir(),
                        widgetIdVisitor, directiveFileGenerator),
                new FragmentsExportStep<Fragment>(fragmentIdVisitor,
                        uiDesignerProperties.getWorkspace().getFragments().getDir()),
                new FragmentPropertiesExportStep(new FragmentPropertiesBuilder(uiDesignerProperties))
        };

        //Widget
        var widgetExportSteps = new ExportStep[] {
                new WidgetByIdExportStep(core.getWidgetRepository(), new WidgetPropertiesBuilder(uiDesignerProperties))
        };

        // == Builder
        var widgetExporter = new WidgetExporter(jsonHandler, core.getWidgetService(), widgetExportSteps);
        var fragmentExporter = new FragmentExporter(jsonHandler, core.getFragmentService(), fragmentExportSteps);
        var pageExporter = new PageExporter(jsonHandler, core.getPageService(), pageExportSteps);

        // Dependency importers
        var widgetAssetDependencyImporter = new AssetDependencyImporter<>(core.getWidgetAssetRepository());
        var fragmentDependencyImporter = new FragmentDependencyImporter(core.getFragmentRepository());
        var widgetDependencyImporter = new WidgetDependencyImporter(core.getWidgetRepository(),
                widgetAssetDependencyImporter);

        // Widget
        var widgetDependencyImporters = new DependencyImporter<?>[] {
                widgetAssetDependencyImporter
        };
        var widgetImporter = new WidgetImporter(
                jsonHandler,
                core.getWidgetService(),
                core.getWidgetRepository(),
                widgetDependencyImporters);

        // Fragment
        var fragmentDependencyImporters = new DependencyImporter<?>[] {
                fragmentDependencyImporter,
                widgetDependencyImporter
        };
        var fragmentImporter = new FragmentImporter(
                jsonHandler,
                core.getFragmentService(),
                core.getFragmentRepository(),
                fragmentDependencyImporters);

        // Page
        var pageDependencyImporters = new DependencyImporter<?>[] {
                fragmentDependencyImporter,
                widgetDependencyImporter,
                new AssetDependencyImporter<>(core.getPageAssetRepository())
        };
        var pageImporter = new PageImporter(
                jsonHandler,
                core.getPageService(),
                core.getPageRepository(),
                pageDependencyImporters);

        // Init workspace now

        var widgetDirectiveBuilder = new WidgetDirectiveBuilder(core.getWatcher(),
                new WidgetFileBasedLoader(jsonHandler), uiDesignerProperties.getWorkspaceUid().isLiveBuildEnabled());

        var fragmentDirectiveBuilder = new FragmentDirectiveBuilder(core.getWatcher(), jsonHandler,
                angularJsGenerator.getHtmlBuilderVisitor(),
                uiDesignerProperties.getWorkspaceUid().isLiveBuildEnabled());

        var resourcesCopier = new ResourcesCopier();
        var workspace = new Workspace(
                uiDesignerProperties,
                core.getWidgetRepository(),
                core.getPageRepository(),
                //TODO: Here we want a mechanism to call angularJsGenerator.construct() at workspace.initilization
                // It's the generator responsability to start with right behavior
                widgetDirectiveBuilder, fragmentDirectiveBuilder,
                core.getWidgetAssetDependencyImporter(),
                resourcesCopier,
                List.of(
                        new LiveRepositoryUpdate<>(core.getPageRepository(), core.getPageMigrationStepsList()),
                        new LiveRepositoryUpdate<>(core.getFragmentRepository(), core.getFragmentMigrationStepsList()),
                        new LiveRepositoryUpdate<>(core.getWidgetRepository(), core.getWidgetMigrationStepsList())),
                jsonHandler);
        workspace.initialize();

        var i18nInitializer = new I18nInitializer(
                new LanguagePackBuilder(
                        core.getWatcher(),
                        new LanguagePackFactory(jsonHandler),
                        uiDesignerProperties.getWorkspaceUid()),
                resourcesCopier,
                uiDesignerProperties.getWorkspaceUid());
        i18nInitializer.initialize();

        return new AngularJsArtifactBuilder(
                // Workspace management
                workspace,
                core.getWidgetService(),
                core.getFragmentService(),
                core.getPageService(),
                // Export
                pageExporter,
                fragmentExporter,
                widgetExporter,
                (IHtmlGenerator) angularJsGenerator.getHtmlGenerator(),
                // Import
                new ImportStore(),
                pageImporter,
                fragmentImporter,
                widgetImporter);
    }

}
