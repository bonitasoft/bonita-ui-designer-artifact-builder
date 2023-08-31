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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.web.angularjs.GeneratorProperties;
import org.bonitasoft.web.designer.common.livebuild.PathListener;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class LanguagePackBuilderTest {

    @Mock
    private Watcher watcher;

    private LanguagePackBuilder builder;
    private GeneratorProperties generatorProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        newLanguagePackBuilder(true);
    }

    private void newLanguagePackBuilder(boolean liveBuildEnabled) {
        generatorProperties = new GeneratorProperties(this.tempDir);
        generatorProperties.setLiveBuildEnabled(liveBuildEnabled);
        builder = new LanguagePackBuilder(watcher, new LanguagePackFactory(
                new JacksonJsonHandler(new ObjectMapper())), generatorProperties);
    }

    @Test
    void should_build_all_language_pack_under_provided_directory() throws Exception {
        Path frFile = Files.createFile(tempDir.resolve("fr.po"));
        Path appClassPath = Files.createDirectory(tempDir.resolve("appClassPath"));
        Path enFile = Files.createFile(appClassPath.resolve("en.po"));
        write(frFile, aSimplePoFile());
        write(enFile, aSimplePoFile());

        builder.start(tempDir);
        assertThat(generatorProperties.getTmpI18nPath().resolve("fr.json")).exists();
        assertThat(resolveJson(frFile)).exists();
        assertThat(resolveJson(enFile)).exists();
    }

    @Test
    void should_watch_directives_files() throws Exception {
        Path path = generatorProperties.getTmpI18nPath();

        builder.start(path);

        verify(watcher).watch(eq(path), any(PathListener.class));
    }

    @Test
    void should_not_watch_directives_files() throws Exception {
        newLanguagePackBuilder(false);
        Path path = generatorProperties.getTmpI18nPath();

        builder.start(path);

        verify(watcher, never()).watch(eq(path), any(PathListener.class));
    }

    @Test
    void should_ignore_files_which_are_not_po_files() throws Exception {
        Path poFile = Files.createFile(tempDir.resolve("fr.po"));
        Files.createFile(tempDir.resolve("script.js"));
        write(poFile, aSimplePoFile());

        builder.start(tempDir);

        List<String> jsonFiles = Files.walk(generatorProperties.getTmpI18nPath()).filter(Files::isRegularFile)
                .map(entry -> entry.getFileName().toString()).collect(Collectors.toList());
        assertThat(jsonFiles).containsOnly("fr.json");
    }

    @Test
    void should_replace_a_previous_build_with_new_one() throws Exception {
        Path poFile = Files.createFile(tempDir.resolve("file.po"));
        write(poFile, aSimplePoFile());

        builder.build(poFile);

        assertThat(read(generatorProperties.getTmpI18nPath().resolve("file.json").toFile()))
                .isEqualTo("{\"francais\":{\"A page\":\"Une page\"}}");
    }

    private String read(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    public File resolveJson(Path poFile) throws IOException {
        return new File(
                generatorProperties.getTmpI18nPath().resolve(poFile.getFileName().toString().replace(".po", ".json"))
                        .toString());
    }

    private byte[] aSimplePoFile() throws Exception {
        return readAllBytes(Paths.get(getClass().getResource("/i18n/simple.po").toURI()));
    }
}
