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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LocalizationAddErrorDialogTranslationsMigrationStep extends AbstractMigrationStep<Page> {

    private static final Logger logger = LoggerFactory
            .getLogger(LocalizationAddErrorDialogTranslationsMigrationStep.class);

    private AssetService<Page> assetService;

    private JsonHandler jsonHandler;

    public LocalizationAddErrorDialogTranslationsMigrationStep(AssetService<Page> assetService) {
        this.assetService = assetService;
        this.jsonHandler = new JacksonJsonHandler(new ObjectMapper());
    }

    @Override
    public Optional<MigrationStepReport> migrate(Page artifact) throws IOException {
        for (var asset : artifact.getAssets()) {
            if (asset.getName().equals("localization.json")) {
                var pageLocalizationContent = assetService.getAssetBinaryContent(artifact, asset);
                assetService.save(artifact, asset, getMigratedAssetContent(pageLocalizationContent));
                logger.info(
                        "[MIGRATION] Adding error dialog translations in asset [{}] to {} [{}] (introduced in 1.17.5)",
                        asset.getName(), artifact.getType(), artifact.getName());
            }
        }
        return Optional.empty();
    }

    @Override
    public String getErrorMessage() {
        return "Error during adding error dialog translations in asset, Missing templates/page/assets/json/localization.json from classpath";
    }

    private byte[] getMigratedAssetContent(byte[] pageLocalizationContent) throws IOException {
        LinkedHashMap<String, Object> existingTranslations = jsonHandler.fromJsonToComplexMap(pageLocalizationContent);
        LinkedHashMap<String, Object> newLocalizationContent = new LinkedHashMap<>();

        try (var is = getClass()
                .getResourceAsStream("/templates/migration/assets/json/localizationAddErrorDialogTranslations.json")) {
            LinkedHashMap<String, Object> translationsToAdd = jsonHandler.fromJsonToComplexMap(IOUtils.toByteArray(is));
            for (String language : existingTranslations.keySet()) {
                LinkedHashMap<String, String> existingLanguageTranslations = (LinkedHashMap<String, String>) existingTranslations
                        .get(language);
                LinkedHashMap<String, String> languageTranslationsToAdd = (LinkedHashMap<String, String>) translationsToAdd
                        .get(language);
                LinkedHashMap<String, String> mergedLanguageTranslations = new LinkedHashMap<>();
                mergedLanguageTranslations.putAll(existingLanguageTranslations);
                if (languageTranslationsToAdd != null) {
                    mergedLanguageTranslations.putAll(languageTranslationsToAdd);
                }
                newLocalizationContent.put(language, mergedLanguageTranslations);
            }
            String test = IOUtils.toString(jsonHandler.toPrettyJsonFromComplexMap(newLocalizationContent), "UTF-8");
            return jsonHandler.toPrettyJsonFromComplexMap(newLocalizationContent);
        }
    }
}
