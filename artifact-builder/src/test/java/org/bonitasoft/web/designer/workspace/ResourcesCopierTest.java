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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResourcesCopierTest {

    private static String FOLDER_TO_COPY = "tmpCopyResources";
    private static String TARGET_FOLDER = "tmpCopiedResources";

    private Path targetFolder;

    @TempDir
    public Path folderManager;

    private ResourcesCopier resourcesCopier = new ResourcesCopier();

    @BeforeEach
    void setUp() throws IOException {
        targetFolder = Files.createDirectory(folderManager.resolve(TARGET_FOLDER));
    }

    @Test
    void should_copy_not_empty_resources_only() throws IOException {
        //test
        resourcesCopier.copy(targetFolder, FOLDER_TO_COPY);
        Path emptyFile = targetFolder.resolve(FOLDER_TO_COPY).resolve("empty.po");
        Path file = targetFolder.resolve(FOLDER_TO_COPY).resolve("simple.po");
        Path folder = targetFolder.resolve(FOLDER_TO_COPY).resolve("pbAutocomplete");
        Path fileIntoFolder = targetFolder.resolve(FOLDER_TO_COPY).resolve("pbAutocomplete/pbAutocomplete.json");

        assertThat(file).exists();
        assertThat(emptyFile).doesNotExist();
        assertThat(folder).exists();
        assertThat(fileIntoFolder).exists();
    }
}
