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
package org.bonitasoft.web.designer.i18n;

import static java.nio.file.Files.write;

import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.angularjs.GeneratorProperties;
import org.bonitasoft.web.designer.common.livebuild.AbstractLiveFileBuilder;
import org.bonitasoft.web.designer.common.livebuild.Watcher;

public class LanguagePackBuilder extends AbstractLiveFileBuilder {

    private final LanguagePackFactory languagePackFactory;
    private final GeneratorProperties generatorProperties;

    public LanguagePackBuilder(Watcher watcher, LanguagePackFactory languagePackFactory,
            GeneratorProperties generatorProperties) {
        super(watcher, generatorProperties.isLiveBuildEnabled());
        this.languagePackFactory = languagePackFactory;
        this.generatorProperties = generatorProperties;
    }

    @Override
    public void build(Path poFile) throws IOException {
        var jsonFileName = poFile.getFileName().toString().replace(".po", ".json");
        write(generatorProperties.getTmpI18nPath().resolve(jsonFileName),
                languagePackFactory.create(poFile.toFile()).toJson());
    }

    @Override
    public boolean isBuildable(String path) {
        return path.endsWith(".po");
    }

}
