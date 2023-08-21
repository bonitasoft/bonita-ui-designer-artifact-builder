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
package org.bonitasoft.web.designer.workspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.UiDesignerPropertiesTestBuilder.aUiDesignerProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import org.bonitasoft.web.designer.*;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.migration.page.UIBootstrapAssetMigrationStep;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorkspaceMigrationTest {

    private static final String HIGHER_MIGRATION_VERSION = Version.MODEL_VERSION;

    private static PageService pageService;

    private static WidgetService widgetService;

    private static Workspace workspace;

    @BeforeAll
    public static void setUp(@TempDir Path tempdir) throws Exception {
        Path workspacePath = Paths.get(WorkspaceMigrationTest.class.getClassLoader().getResource("workspace").toURI());

        UiDesignerProperties uiDesignerProperties = aUiDesignerProperties(workspacePath);
        uiDesignerProperties.setModelVersion(HIGHER_MIGRATION_VERSION);
        uiDesignerProperties.setVersion("1.13.0-SNAPSHOT");
        uiDesignerProperties.setEdition("Community");
        uiDesignerProperties.getWorkspaceUid().setPath(tempdir);
        Path extract = Files.createDirectories(tempdir.resolve("extract"));
        uiDesignerProperties.getWorkspaceUid().setExtractPath(extract);

        Path assetCss = Files.createDirectories(
                uiDesignerProperties.getWorkspaceUid().getExtractPath().resolve("angularjs/templates/page/assets/css"));
        Files.createFile(assetCss.resolve("style.css"));

        JsonHandler jsonHandler = new JsonHandlerFactory().create();
        final UiDesignerCore core = new UiDesignerCoreFactory(uiDesignerProperties, jsonHandler).create();
        ArtifactBuilder artifactBuilder = new ArtifactBuilderFactory(uiDesignerProperties, jsonHandler, core).create();

        workspace = artifactBuilder.getWorkspace();
        workspace.initialize();

        widgetService = core.getWidgetService();
        pageService = core.getPageService();
    }

    @Test
    void should_migrate_a_page() {
        // When
        workspace.migrateWorkspace();

        // Then
        Page page = pageService.get("page_1_0_0");

        assertThat(page.getArtifactVersion()).isEqualTo(HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousArtifactVersion()).isEqualTo("1.0.0");

        assertThat(page.getAssets().stream().map(Asset::getId)).doesNotContainNull();
    }

    @Test
    void should_migrate_a_page_property_values() {
        // When
        workspace.migrateWorkspace();

        // Then
        Page page = pageService.get("page_1_0_1");

        Map<String, PropertyValue> propertyValues = page.getRows().stream().flatMap(Collection::stream).iterator()
                .next().getPropertyValues();

        assertThat(propertyValues.values().stream().map(PropertyValue::getType)).doesNotContain("data");
    }

    @Test
    void should_migrate_a_page_adding_text_widget_interpret_HTML_property_value() {
        // When
        workspace.migrateWorkspace();

        // Then
        Page page = pageService.get("page_1_5_51");

        assertThat(page.getArtifactVersion()).isEqualTo(HIGHER_MIGRATION_VERSION);
        Map<String, PropertyValue> propertyValues = page.getRows().stream().flatMap(Collection::stream).iterator()
                .next().getPropertyValues();
        PropertyValue allowHTMLProperty = propertyValues.get("allowHTML");

        assertThat(allowHTMLProperty).isNotNull();
        assertThat(allowHTMLProperty.getType()).isEqualTo(BondType.CONSTANT.toJson());
        assertThat(allowHTMLProperty.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void should_migrate_a_custom_widget() throws IOException {
        // When
        workspace.migrateWorkspace();

        // Then
        Widget widget = widgetService.get("widget_1_0_0");

        assertThat(widget.getArtifactVersion()).isEqualTo(HIGHER_MIGRATION_VERSION);
        assertThat(widget.getPreviousArtifactVersion()).isEqualTo("1.0.0");

        assertThat(widget.getAssets().stream().map(Asset::getId)).doesNotContainNull();
    }

    @Test
    void should_migrate_a_page_adding_uiBootstrap() {
        // When
        workspace.migrateWorkspace();

        // Then
        Page page = pageService.get("page_1_0_1");

        assertThat(page.getAssets().stream().map(Asset::getName))
                .contains(UIBootstrapAssetMigrationStep.ASSET_FILE_NAME);
    }

    @Test
    void should_migrate_a_page_not_adding_uiBootstrap_when_already_a_page_asset() {
        // When
        workspace.migrateWorkspace();

        // Then
        Page page = pageService.get("pageWithUIBootstrap");

        assertThat(page.getArtifactVersion()).isEqualTo(HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousArtifactVersion()).isEqualTo("1.0.1");

        assertThat(page.getAssets().stream().map(Asset::getName))
                .doesNotContain(UIBootstrapAssetMigrationStep.ASSET_FILE_NAME).hasSize(2);
    }

    @Test
    void should_migrate_a_page_not_adding_uiBootstrap_when_already_a_widget_asset() {
        // When
        workspace.migrateWorkspace();

        // Then
        Page page = pageService.get("pageWithUIBootstrapWidget");

        assertThat(page.getArtifactVersion()).isEqualTo(HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousArtifactVersion()).isEqualTo("1.0.1");

        assertThat(page.getAssets().stream().map(Asset::getName))
                .doesNotContain(UIBootstrapAssetMigrationStep.ASSET_FILE_NAME).hasSize(1);
    }
}
