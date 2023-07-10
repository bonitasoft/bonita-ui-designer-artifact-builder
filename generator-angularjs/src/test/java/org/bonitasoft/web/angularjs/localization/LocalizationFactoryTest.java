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
package org.bonitasoft.web.angularjs.localization;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.lenient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LocalizationFactoryTest {

    @Mock
    PageRepository pageRepository;

    @InjectMocks
    LocalizationFactory localizationFactory;

    String localizationFileContent = "{\"fr-FR\":{\"Hello\":\"Bonjour\"}}";
    Page page = aPage().withId("page").build();
    File localizationFile;

    @TempDir
    Path temporaryFolder;

    @BeforeEach
    public void setUp() throws Exception {
        Path pageFolder = Files.createDirectory(temporaryFolder.resolve("page"));
        Files.createDirectory(temporaryFolder.resolve("assets"));
        Files.createDirectory(temporaryFolder.resolve("json"));
        Path jsonFolder = Files.createDirectories(temporaryFolder.resolve("page/assets/json/"));
        localizationFile = Files.createFile(jsonFolder.resolve("localization.json")).toFile();
        lenient().when(pageRepository.resolvePath("page")).thenReturn(pageFolder);
    }

    @Test
    public void should_create_a_factory_which_contains_localizations() throws Exception {
        writeByteArrayToFile(localizationFile, localizationFileContent.getBytes());

        assertThat(localizationFactory.generate(page))
                .isEqualTo(createFactory(localizationFileContent));
    }

    @Test
    public void should_create_an_empty_factory_whenever_localization_file_is_not_valid_json() throws Exception {
        writeByteArrayToFile(localizationFile, "invalid json".getBytes());

        assertThat(localizationFactory.generate(page))
                .isEqualTo(createFactory("{}"));
    }

    @Test
    public void should_create_an_empty_factory_whenever_localization_file_does_not_exist() throws Exception {
        deleteQuietly(localizationFile);

        assertThat(localizationFactory.generate(page))
                .isEqualTo(createFactory("{}"));
    }

    @Test
    public void should_create_an_empty_string_for_fragments() throws Exception {
        assertThat(localizationFactory.generate(aFragment().build()))
                .isEqualTo("angular.module('bonitasoft.ui.services').factory('localizationFactory', function() {"
                        + System.lineSeparator() +
                        "  return {" + System.lineSeparator() +
                        "    get: function() {" + System.lineSeparator() +
                        "      return {};" + System.lineSeparator() +
                        "    }" + System.lineSeparator() +
                        "  };" + System.lineSeparator() +
                        "});" + System.lineSeparator());
    }

    private String createFactory(String content) {
        return format("angular.module('bonitasoft.ui.services').factory('localizationFactory', function() {"
                + System.lineSeparator() +
                "  return {" + System.lineSeparator() +
                "    get: function() {" + System.lineSeparator() +
                "      return %s;" + System.lineSeparator() +
                "    }" + System.lineSeparator() +
                "  };" + System.lineSeparator() +
                "});" + System.lineSeparator(), content);
    }
}
