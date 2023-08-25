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

import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilesConcatenatorTest {

    @TempDir
    Path temporaryFolder;

    @Test
    void should_concatenate_files() throws IOException {
        write(Files.createFile(temporaryFolder.resolve("file1.js")), "file1".getBytes());
        write(Files.createFile(temporaryFolder.resolve("file2.js")), "file2".getBytes());
        List<Path> files = asList(temporaryFolder.resolve("file1.js"), temporaryFolder.resolve("file2.js"));

        byte[] content = FilesConcatenator.concat(files);

        assertThat(content).isEqualTo("file1file2".getBytes());
    }

}
