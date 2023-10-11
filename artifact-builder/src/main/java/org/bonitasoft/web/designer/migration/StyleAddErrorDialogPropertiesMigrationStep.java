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
package org.bonitasoft.web.designer.migration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StyleAddErrorDialogPropertiesMigrationStep extends AbstractMigrationStep<Page> {

    private final AssetService<Page> assetService;

    public StyleAddErrorDialogPropertiesMigrationStep(AssetService<Page> assetService) {
        this.assetService = assetService;
    }

    @Override
    public Optional<MigrationStepReport> migrate(Page artifact) throws IOException {
        for (var asset : artifact.getAssets()) {
            if (asset.getName().equals("style.css")) {
                var pageStyleCssContent = assetService.getAssetContent(artifact, asset);
                assetService.save(artifact, asset, getMigratedAssetContent(pageStyleCssContent));
                log.info("[MIGRATION] Adding error dialog classes in asset [{}] to {} [{}] (introduced in 1.17.5)",
                        asset.getName(), artifact.getType(), artifact.getName());
            }
        }
        return Optional.empty();
    }

    @Override
    public String getErrorMessage() {
        return "Error during adding error dialog classes in asset, Missing templates/page/assets/css/style.css from classpath";
    }

    private byte[] getMigratedAssetContent(String styleCssContent) throws IOException {
        try (var is = getClass()
                .getResourceAsStream("/templates/migration/assets/css/styleAddErrorDialogProperties.css");
                var sis = new SequenceInputStream(
                        new ByteArrayInputStream(styleCssContent.getBytes(StandardCharsets.UTF_8)),
                        new ByteArrayInputStream(IOUtils.toByteArray(is)))) {
            return IOUtils.toByteArray(sis);
        }
    }
}
