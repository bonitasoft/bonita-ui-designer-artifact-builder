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
package org.bonitasoft.web.angularjs.rendering;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.common.generator.rendering.GenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Benjamin Parisel
 */
public class WidgetFileHelperTest {

    @TempDir
    Path temporaryFolder;

    @Test
    public void should_write_content_at_path() throws Exception {
        byte[] content = "Mon content".getBytes();

        Path path = WidgetFileHelper.writeFile(content, temporaryFolder, "f8a4574");

        Assertions.assertThat(path.toFile()).exists();
        Assertions.assertThat(path.getFileName().toString()).isEqualTo("widgets-f8a4574.js");
    }

    @Test
    public void should_delete_on_folder_all_old_widgets_directives_file() throws Exception {
        Path assetsFolder = Files.createDirectories(temporaryFolder.resolve("maPage/assets"));
        Path expectToBeDeletedFile = Files.createFile(assetsFolder.resolve("widgets-fdsf45741sf.min.js"));
        Path fragment = Files.createFile(assetsFolder.resolve("123456.js"));
        Path expectExistFile = Files.createFile(assetsFolder.resolve("12345654.json"));

        WidgetFileHelper.deleteOldConcatenateFiles(assetsFolder, "aa");

        Assertions.assertThat(expectToBeDeletedFile).doesNotExist();
        Assertions.assertThat(expectExistFile).exists();
        Assertions.assertThat(fragment).exists();
    }

    @Test
    public void should_delete_on_root_folder_all_old_widgets_directives_file() throws Exception {
        Path assetsFolder = Files.createDirectories(temporaryFolder.resolve("myFragmentId"));
        Path fragmentJS = Files.createFile(assetsFolder.resolve("myFragmentId.js"));
        Path oldConcatDirectiveFile = Files.createFile(assetsFolder.resolve("widgets-11111.min.js"));
        Path descriptorFile = Files.createFile(assetsFolder.resolve("myFragmentId.json"));

        WidgetFileHelper.deleteOldConcatenateFiles(assetsFolder, "123");

        Assertions.assertThat(fragmentJS).exists();
        Assertions.assertThat(descriptorFile).exists();
        Assertions.assertThat(oldConcatDirectiveFile).doesNotExist();
    }

    @Test
    public void should_throw_generation_exception_if_not_exist_folder_path_when_write_a_file() {
        Path unexistingFile = temporaryFolder.resolve("FileNotFound");

        assertThrows(GenerationException.class,
                () -> WidgetFileHelper.writeFile("Mon content".getBytes(), unexistingFile, "notUsedForThisTest"));
    }

    @Test
    public void should_throw_generation_exception_if_not_exist_folder_path_dont_exist_when_delete_a_file() {
        Path folderNotFound = temporaryFolder.resolve("folderNotFound");

        assertThrows(GenerationException.class,
                () -> WidgetFileHelper.deleteOldConcatenateFiles(folderNotFound, "notUsedForThisTest"));
    }

    @Test
    public void should_delete_files_if_old_files_exists() throws IOException {
        Path assetsFolder = Files.createDirectories(temporaryFolder.resolve("maPage/assets"));
        Path expectFileDeleted = Files.createFile(assetsFolder.resolve("widgets-4576.min.js"));
        Path expectFileAlreadyExist = Files.createFile(assetsFolder.resolve("widgets-1z2a3456.min.js"));

        WidgetFileHelper.deleteOldConcatenateFiles(assetsFolder, "1z2a3456");

        Assertions.assertThat(expectFileDeleted).doesNotExist();
        Assertions.assertThat(expectFileAlreadyExist).exists();
    }

    @Test
    public void should_delete_files_if_exists() throws Exception {
        Path assetsFolder = Files.createDirectories(temporaryFolder.resolve("maPage/js"));
        Path expectFileDeleted = Files.createFile(assetsFolder.resolve("widgets-4576.min.js"));

        WidgetFileHelper.deleteConcatenateFile(assetsFolder);

        Assertions.assertThat(expectFileDeleted).doesNotExist();
    }

}
