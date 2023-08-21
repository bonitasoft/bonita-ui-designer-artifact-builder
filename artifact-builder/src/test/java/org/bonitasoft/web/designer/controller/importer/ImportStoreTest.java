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
package org.bonitasoft.web.designer.controller.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportStoreTest {

    @Mock
    private AbstractArtifactImporter<?> artifactImporter;

    private ImportStore importStore;

    @BeforeEach
    void setUp() throws Exception {
        importStore = new ImportStore();
    }

    @Test
    void should_store_import() throws Exception {
        Path importPath = Paths.get("import/path");

        Import storedImport = importStore.store(artifactImporter, importPath);

        assertThat(storedImport.getUUID()).isNotNull();
        assertThat(storedImport.getImporter()).isEqualTo(artifactImporter);
        assertThat(storedImport.getPath()).isEqualTo(importPath);
    }

    @Test
    void should_get_a_stored_import() throws Exception {
        Import expectedImport = importStore.store(artifactImporter, Paths.get("import/path"));

        Import fetchedImport = importStore.get(expectedImport.getUUID());

        assertThat(expectedImport).isEqualTo(fetchedImport);
    }

    @Test
    void should_throw_not_found_exception_while_getting_an_unknown_import() throws Exception {
        assertThrows(NotFoundException.class, () -> importStore.get("unknown-import"));
    }

    @Test
    void should_remove_a_stored_import() throws Exception {
        Import addedReport = importStore.store(artifactImporter, Paths.get("import/path"));

        importStore.remove(addedReport.getUUID());
        var uuid = addedReport.getUUID();

        assertThrows(NotFoundException.class, () -> importStore.get(uuid));
    }

    @Test
    void should_delete_folder_while_removing_a_stored_import(@TempDir Path temporaryFolder) throws Exception {
        Path importFolder = Files.createDirectory(temporaryFolder.resolve("importFolder"));
        Import addedReport = importStore.store(artifactImporter, importFolder);

        importStore.remove(addedReport.getUUID());

        assertThat(importFolder).doesNotExist();
    }

    @Test
    void should_fail_silently_while_removing_an_unexisting_import() throws Exception {
        assertDoesNotThrow(() -> importStore.remove("unexinting id"));
    }
}
