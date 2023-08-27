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

import static java.nio.file.Files.createDirectories;

import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.designer.common.IGeneratorProperties;

import lombok.Getter;
import lombok.Setter;

public class GeneratorProperties implements IGeneratorProperties {

    public static final String EXTRACT_BACKEND_RESOURCES = "META-INF/resources";
    public static final String I18N_RESOURCE = "i18n";
    private final Path path;

    @Getter
    @Setter
    private boolean isLiveBuildEnabled = true;

    public GeneratorProperties(Path uidWorkspace) {
        this.path = uidWorkspace;
    }

    public static final String FRAGMENTS = "fragments";
    public static final String TEMPLATES_RESOURCES = "templates";
    private static final String PAGES_DEFAULT_DIRECTORY = "pages";

    @Override
    public Path getTmpFragmentsRepositoryPath() {
        return getPath().resolve(FRAGMENTS);
    }

    @Override
    public Path getTmpPagesRepositoryPath() {
        return getPath().resolve(PAGES_DEFAULT_DIRECTORY);
    }

    public Path getTmpI18nPath() throws IOException {
        return createDirectories(getPath().resolve(I18N_RESOURCE));
    }

    @Override
    public Path getExportBackendResourcesPath() {
        return getExtractPath().resolve(EXTRACT_BACKEND_RESOURCES).resolve("runtime");
    }

    @Override
    public Path getTemplateResourcesPath() {
        return getExtractPath().resolve(TEMPLATES_RESOURCES);
    }

    private Path getPath() {
        return this.path;
    }

    @Override
    public Path getExtractPath() {
        return getPath().resolve("extract").resolve("angularjs");
    }

}
