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

import static org.bonitasoft.web.designer.common.migration.Version.INITIAL_MODEL_VERSION;

import java.util.List;

import javax.validation.Validation;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.bonitasoft.web.angularjs.GeneratorProperties;
import org.bonitasoft.web.designer.common.livebuild.ObserverFactory;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.*;
import org.bonitasoft.web.designer.common.visitor.AssetVisitor;
import org.bonitasoft.web.designer.common.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.common.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.migration.*;
import org.bonitasoft.web.designer.migration.page.*;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.service.*;
import org.bonitasoft.web.designer.visitor.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class UiDesignerCoreFactory {

    private final UiDesignerProperties uiDesignerProperties;
    private final JsonHandler jsonHandler;
    private final BeanValidator beanValidator;
    private GeneratorProperties generatorProperties;

    public UiDesignerCoreFactory(UiDesignerProperties uiDesignerProperties, GeneratorProperties generatorProperties,
            JsonHandler jsonHandler) {
        this.uiDesignerProperties = uiDesignerProperties;
        this.generatorProperties = generatorProperties;
        this.jsonHandler = jsonHandler;
        this.beanValidator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    /**
     * Use this method to create a UID core object holding references to core services
     *
     * @return
     */
    public UiDesignerCore create() {

        var watcher = createWatcher(createFileMonitor(false));

        // == Widget
        var widgetRepository = createWidgetRepository(watcher);
        var widgetAssetRepository = createWidgetAssetRepository(widgetRepository);

        // == Fragment
        var fragmentRepository = createFragmentRepository(watcher);

        // == Page
        var pageRepository = createPageRepository(watcher);
        var pageAssetRepository = createPageAssetRepository(pageRepository);

        return create(watcher, widgetRepository, widgetAssetRepository, fragmentRepository, pageRepository,
                pageAssetRepository);

    }

    /**
     * Use this method to create a UID core object holding references to core services using externally managed beans (like in a spring context)
     *
     * @param watcher
     * @param widgetRepository
     * @param widgetAssetRepository
     * @param fragmentRepository
     * @param pageRepository
     * @param pageAssetRepository
     * @return
     */
    public UiDesignerCore create(
            Watcher watcher,
            WidgetRepository widgetRepository,
            AssetRepository<Widget> widgetAssetRepository,
            FragmentRepository fragmentRepository,
            PageRepository pageRepository,
            AssetRepository<Page> pageAssetRepository) {

        var pageAssetDependencyImporter = new AssetDependencyImporter<>(pageAssetRepository);
        var pageAssetService = new AssetService<>(pageRepository, pageAssetRepository, pageAssetDependencyImporter);

        final var componentVisitor = new ComponentVisitor(fragmentRepository);
        List<Migration<Fragment>> fragmentMigrationStepsList = List.<Migration<Fragment>> of(
                new Migration<>("1.0.3",
                        new BondMigrationStep<>(componentVisitor, widgetRepository, new VisitorFactory())),
                new Migration<>("1.7.25", new TextWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.9.24", new TextWidgetLabelMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.12", new DataToVariableMigrationStep<>()),
                new Migration<>("1.10.16", new TableWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.18", new TableWidgetStylesMigrationStep<>(componentVisitor)),
                new Migration<>("1.11.46", new DataExposedMigrationStep<>()),
                new Migration<Fragment>(INITIAL_MODEL_VERSION,
                        new AddModelVersionMigrationStep<>("INITIAL_MODEL_VERSION")),
                new Migration<>("2.1", new AddModelVersionMigrationStep<>("2.1"),
                        new AutocompleteWidgetReturnedKeyMigrationStep<>(componentVisitor)),
                new Migration<>("2.2", new AddModelVersionMigrationStep<>("2.2"),
                        new BusinessVariableMigrationStep<>()),
                new Migration<>("2.3", new SetInterpretHtmlTrueMigrationStep<>(componentVisitor)),
                new Migration<>("2.4", new AccessibilityCheckListAndRadioButtonsMigrationStep(componentVisitor)),
                new Migration<>("2.5", new AddModelVersionMigrationStep<>("2.5")));

        List<Migration<Page>> pageMigrationStepsList = List.<Migration<Page>> of(
                new Migration<>("1.0.2", new AssetIdMigrationStep<>()),
                new Migration<>("1.0.3",
                        new BondMigrationStep<>(componentVisitor, widgetRepository, new VisitorFactory())),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<>()),
                new Migration<>("1.5.7",
                        new StyleAssetMigrationStep(
                                generatorProperties.getExtractPath(),
                                pageAssetService)),
                new Migration<>("1.5.10",
                        new UIBootstrapAssetMigrationStep(pageAssetService, componentVisitor, widgetRepository)),
                new Migration<>("1.7.4", new TextWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.7.25", new PageUUIDMigrationStep()),
                new Migration<>("1.8.29", new StyleAddModalContainerPropertiesMigrationStep(pageAssetService)),
                new Migration<>("1.9.24", new TextWidgetLabelMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.5", new DynamicTabsContainerMigrationStep<>()),
                new Migration<>("1.10.12", new DataToVariableMigrationStep<>()),
                new Migration<>("1.10.16", new TableWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.18", new TableWidgetStylesMigrationStep<>(componentVisitor)),
                new Migration<>("1.11.40", new BusinessVariableMigrationStep<>()),
                new Migration<>("1.11.46", new StyleUpdateInputRequiredLabelMigrationStep(pageAssetService)),
                new Migration<>(INITIAL_MODEL_VERSION, new AddModelVersionMigrationStep<>(INITIAL_MODEL_VERSION),
                        new AutocompleteWidgetReturnedKeyMigrationStep<Page>(componentVisitor)),
                new Migration<>("2.1", new AddModelVersionMigrationStep<>("2.1")),
                new Migration<>("2.2", new AddModelVersionMigrationStep<>("2.2")),
                new Migration<>("2.3", new SetInterpretHtmlTrueMigrationStep<>(componentVisitor)),
                new Migration<>("2.4", new AccessibilityCheckListAndRadioButtonsMigrationStep(componentVisitor)),
                new Migration<>("2.5", new StyleUpdateInputTypeMigrationStep(pageAssetService)));

        List<Migration<Widget>> widgetMigrationStepsList = List.<Migration<Widget>> of(
                new Migration<>("1.0.2", new AssetIdMigrationStep<>()),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<>()),
                new Migration<>("1.10.12", new SplitWidgetResourcesMigrationStep()),
                new Migration<>(INITIAL_MODEL_VERSION, new AddModelVersionMigrationStep<>(INITIAL_MODEL_VERSION)),
                new Migration<>("2.1", new AddModelVersionMigrationStep<>("2.1")),
                new Migration<>("2.2", new AddModelVersionMigrationStep<>("2.2")),
                new Migration<>("2.3", new AddModelVersionMigrationStep<>("2.3")),
                new Migration<>("2.4", new AddModelVersionMigrationStep<>("2.4"), new AddWebResourcesForWidget()),
                new Migration<>("2.5", new AddModelVersionMigrationStep<>("2.5")));

        var widgetMigrationApplyer = new WidgetMigrationApplyer(widgetMigrationStepsList);
        var widgetIdVisitor = new WidgetIdVisitor(fragmentRepository);
        var assetVisitor = new AssetVisitor(widgetRepository, fragmentRepository);

        var widgetAssetDependencyImporter = new AssetDependencyImporter<>(widgetAssetRepository);
        var widgetAssetService = new AssetService<>(widgetRepository, widgetAssetRepository,
                widgetAssetDependencyImporter);
        var widgetService = new DefaultWidgetService(
                widgetRepository,
                pageRepository,
                fragmentRepository,
                List.of(
                        new BondsTypesFixer<>(pageRepository),
                        new BondsTypesFixer<>(fragmentRepository)),
                widgetMigrationApplyer,
                widgetIdVisitor,
                assetVisitor,
                uiDesignerProperties,
                widgetAssetService);

        var fragmentMigrationApplyer = new FragmentMigrationApplyer(fragmentMigrationStepsList, widgetService);
        var fragmentService = new DefaultFragmentService(
                fragmentRepository,
                pageRepository,
                fragmentMigrationApplyer,
                new FragmentIdVisitor(fragmentRepository),
                new FragmentChangeVisitor(),
                new PageHasValidationErrorVisitor(),
                assetVisitor,
                uiDesignerProperties,
                new WebResourcesVisitor(fragmentRepository, widgetRepository));

        var pageMigrationApplyer = new PageMigrationApplyer(pageMigrationStepsList, widgetService, fragmentService);
        var pageService = new DefaultPageService(
                pageRepository,
                pageMigrationApplyer,
                new ComponentVisitor(fragmentRepository),
                assetVisitor,
                uiDesignerProperties,
                pageAssetService,
                new WebResourcesVisitor(fragmentRepository, widgetRepository));

        // Return core services
        return new UiDesignerCore(
                watcher,

                pageRepository,
                pageAssetRepository,
                pageService,
                pageAssetService,

                fragmentRepository,
                fragmentService,

                widgetRepository,
                widgetAssetRepository,
                widgetService,
                widgetAssetService,

                // TODO: should not be visible outside of services, but forced to because of workspace init
                pageMigrationStepsList,
                fragmentMigrationStepsList,
                widgetMigrationStepsList,
                widgetAssetDependencyImporter);
    }

    /**
     * Factory method for a new instance of apache common file monitor
     *
     * @param managed, <p>Set to true if start/stop method are called externaly (ex: managed by a spring context).
     *        If set to false, the {@link FileAlterationMonitor#start()} method will be called now and {@link FileAlterationMonitor#stop()} method call
     *        will happen on jvm exit via a shutdown hook
     *        </p>
     * @return a new instance of apache common file monitor
     */
    public FileAlterationMonitor createFileMonitor(boolean managed) {
        var monitor = new FileAlterationMonitor(1000);

        if (!managed) {
            try {
                monitor.start();
            } catch (Exception e) {
                throw new ArtifactBuilderException("Failed to start FileAlterationMonitor", e);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    monitor.stop();
                } catch (Exception e) {
                    log.warn("Failed to cleanly stop FileAlterationMonitor on shutdown", e);
                }
            }));
        }
        return monitor;
    }

    /**
     * Factory method for a Watcher
     *
     * @param monitor
     * @return
     */
    public Watcher createWatcher(FileAlterationMonitor monitor) {
        return new Watcher(new ObserverFactory(), monitor);
    }

    /**
     * Factory method for a page Asset Repository
     *
     * @param pageRepository
     * @return
     */
    public AssetRepository<Page> createPageAssetRepository(PageRepository pageRepository) {
        return new AssetRepository<>(pageRepository, beanValidator);
    }

    /**
     * Factory method for a Page Repository
     *
     * @param watcher
     * @return
     */
    public PageRepository createPageRepository(Watcher watcher) {
        return new PageRepository(
                uiDesignerProperties.getWorkspace().getPages().getDir(),
                generatorProperties.getTemplateResourcesPath(),
                new JsonFileBasedPersister<>(jsonHandler, beanValidator, this.uiDesignerProperties.getVersion(),
                        this.uiDesignerProperties.getModelVersion()),
                new JsonFileBasedLoader<>(jsonHandler, Page.class),
                beanValidator, watcher);
    }

    /**
     * Factory method for a Fragment Repository
     *
     * @param watcher
     * @return
     */
    public FragmentRepository createFragmentRepository(Watcher watcher) {
        return new FragmentRepository(
                uiDesignerProperties.getWorkspace().getFragments().getDir(),
                generatorProperties.getTemplateResourcesPath(),
                new JsonFileBasedPersister<>(jsonHandler, beanValidator, this.uiDesignerProperties.getVersion(),
                        this.uiDesignerProperties.getModelVersion()),
                new JsonFileBasedLoader<>(jsonHandler, Fragment.class),
                beanValidator, watcher);
    }

    /**
     * Factory method for a widget Asset Repository
     *
     * @param widgetRepository
     * @return
     */
    public AssetRepository<Widget> createWidgetAssetRepository(WidgetRepository widgetRepository) {
        return new AssetRepository<>(widgetRepository, beanValidator);
    }

    /**
     * Factory method for a Widget Repository
     *
     * @param watcher
     * @return
     */
    public WidgetRepository createWidgetRepository(Watcher watcher) {
        return new WidgetRepository(
                uiDesignerProperties.getWorkspace().getWidgets().getDir(),
                generatorProperties.getTemplateResourcesPath(),
                new WidgetFileBasedPersister(jsonHandler, beanValidator, this.uiDesignerProperties.getVersion(),
                        this.uiDesignerProperties.getModelVersion()),
                new WidgetFileBasedLoader(jsonHandler),
                beanValidator, watcher);
    }

}
