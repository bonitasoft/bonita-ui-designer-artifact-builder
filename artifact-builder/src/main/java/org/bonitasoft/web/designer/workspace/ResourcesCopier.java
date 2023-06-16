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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourcesCopier {

    public void copy(Path destinationPath, String patternLocation) throws IOException {

        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver
                .getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + patternLocation + "/**");
        for (var resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                var url = resource.getURL();
                var urlString = url.toExternalForm();
                var targetName = urlString.substring(urlString.indexOf(patternLocation));
                var destination = new File(destinationPath.toAbsolutePath().toFile(), targetName);
                FileUtils.copyURLToFile(url, destination);
                log.debug("Copied " + url + " to " + destination.getAbsolutePath());
            } else {
                log.debug("Did not copy, seems to be directory: " + resource.getDescription());
            }
        }
    }
}
