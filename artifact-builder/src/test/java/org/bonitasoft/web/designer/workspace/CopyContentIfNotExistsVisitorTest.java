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

import static java.nio.file.Files.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CopyContentIfNotExistsVisitorTest {

    @TempDir
    Path directory;

    Path source;

    Path destination;

    CopyContentIfNotExistsVisitor visitor;

    @BeforeEach
    void setup() throws Exception {
        source = createDirectory(directory.resolve("source"));
        destination = createDirectory(directory.resolve("destination"));
        visitor = new CopyContentIfNotExistsVisitor(source, destination);
    }

    @Test
    void should_create_given_directory() throws Exception {

        visitor.preVisitDirectory(source.resolve("widgets"), null);

        assertThat(exists(destination.resolve("widgets"))).isTrue();
    }

    @Test
    void should_not_create_given_directory_if_it_already_exist() throws Exception {
        createDirectories(destination.resolve("widgets"));

        visitor.preVisitDirectory(source.resolve("widgets"), null);

        assertThat(exists(destination.resolve("widgets"))).isTrue();
    }

    @Test
    void should_copy_given_file_from_source_directory() throws Exception {
        write(source.resolve("pbButton.json"), "contents".getBytes());

        visitor.visitFile(source.resolve("pbButton.json"), null);

        assertThat(readAllBytes(destination.resolve("pbButton.json"))).isEqualTo("contents".getBytes());
    }

    @Test
    void should_not_copy_given_file_from_source_directory_if_it_already_exist() throws Exception {
        write(source.resolve("pbButton.json"), "contents from source".getBytes());
        write(destination.resolve("pbButton.json"), "contents from destination".getBytes());

        visitor.visitFile(source.resolve("pbButton.json"), null);

        assertThat(readAllBytes(destination.resolve("pbButton.json")))
                .isEqualTo("contents from destination".getBytes());
    }
}
