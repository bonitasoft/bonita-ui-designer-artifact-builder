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

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bonitasoft.web.designer.ArtifactBuilderException;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

class LanguagePackTest {

    private LanguagePackFactory languagePackFactory;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        languagePackFactory = new LanguagePackFactory(new JacksonJsonHandler(new ObjectMapper()));
    }

    @Test
    void should_convert_translation_into_json() throws Exception {
        Path poFile = Files.createFile(tempDir.resolve("simple.po"));
        write(poFile, readResource("/i18n/simple.po"));

        assertThat(new String(languagePackFactory.create(poFile.toFile()).toJson()))
                .isEqualTo("{\"francais\":{\"A page\":\"Une page\"}}");
    }

    @Test
    void should_convert_plural_translations_into_json() throws Exception {
        Path poFile = Files.createFile(tempDir.resolve("plural.po"));
        write(poFile, readResource("/i18n/plural.po"));

        assertThat(new String(languagePackFactory.create(poFile.toFile()).toJson()))
                .isEqualTo("{\"francais\":{\"A page\":[\"Une page\",\"Des pages\"]}}");
    }

    @Test
    void should_throw_a_runtime_exception_if_the_po_file_does_not_contains_the_language() throws Exception {
        Path folder = Files.createDirectory(tempDir.resolve("i18n"));
        Path poFile = Files.createFile(folder.resolve("plural.po"));
        write(poFile,
                new String(readResource("/i18n/simple.po")).replace("Language: francais", "").getBytes());
        assertThrows(ArtifactBuilderException.class,
                () -> new String(languagePackFactory.create(poFile.toFile()).toJson()));
    }

    private byte[] readResource(String path) throws Exception {
        return readAllBytes(Paths.get(getClass().getResource(path).toURI()));
    }
}
