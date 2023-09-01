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
package org.bonitasoft.web.designer.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "designer.workspace-uid")
public class WorkspaceUidProperties {

    public static final String FRAGMENTS = "fragments";
    private static final String PAGES_DEFAULT_DIRECTORY = "pages";

    private boolean liveBuildEnabled = true;

    private Path path = Path.of(System.getProperty("java.io.tmpdir")).resolve("workspace-uid");

    public Path getTmpFragmentsRepositoryPath() {
        return getPath().resolve(FRAGMENTS);
    }

    public Path getTmpPagesRepositoryPath() {
        return getPath().resolve(PAGES_DEFAULT_DIRECTORY);
    }

    public Path getExtractPath() {
        return path.resolve("extract");
    }

}
