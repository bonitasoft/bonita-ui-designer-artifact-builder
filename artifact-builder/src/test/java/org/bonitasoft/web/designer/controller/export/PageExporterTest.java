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
package org.bonitasoft.web.designer.controller.export;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.designer.common.export.ExportStep;
import org.bonitasoft.web.designer.common.export.Zipper;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.service.PageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PageExporterTest {

    @Mock
    private PageService pageService;

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private PageExporter exporter;

    private ByteArrayOutputStream artifactStream;

    @BeforeEach
    void setUp() throws Exception {
        artifactStream = new ByteArrayOutputStream();
        exporter = new PageExporter(jsonHandler, pageService, mock(ExportStep.class));
    }

    private Page create(Page page, Path tmpDir) throws IOException {
        if (page.getId() == null) {
            page.setId("default-id");
        }
        when(pageService.get(page.getId())).thenReturn(page);
        write(tmpDir.resolve(format("%s.json", page.getId())),
                jsonHandler.toJson(page, JsonViewPersistence.class));
        return page;
    }

    private Path unzip(ByteArrayOutputStream artifactZipStream) throws IOException {
        return new Unzipper().unzipInTempDir(new ByteArrayInputStream(artifactZipStream.toByteArray()), "exportertest");
    }

    @Test
    void should_throw_exception_when_id_is_null() {
        assertThrows(IllegalArgumentException.class, () -> exporter.handleFileExport(null, artifactStream));
    }

    @Test
    void should_throw_exception_when_id_is_blank() {
        assertThrows(IllegalArgumentException.class, () -> exporter.handleFileExport(" ", artifactStream));
    }

    @Test
    void should_throw_exception_when_artefact_to_export_is_not_found() {
        NotFoundException cause = new NotFoundException("Page not found");
        when(pageService.get("unknown-id")).thenThrow(cause);

        Throwable throwable = catchThrowable(() -> exporter.handleFileExport("unknown-id", artifactStream));

        assertThat(throwable)
                .isInstanceOf(ExportException.class)
                .hasCause(cause);

    }

    @Test
    void should_failed_when_page_is_not_compatible_with_product_model_version(@TempDir Path tmpDir)
            throws Exception {
        Page page = create(aPage().withModelVersion("5.0").isCompatible(false).build(), tmpDir);

        assertThrows(ModelException.class, () -> exporter.handleFileExport(page.getId(), artifactStream));
    }

    @Test
    void should_fill_output_stream(@TempDir Path tmpDir) throws Exception {
        Page page = create(aPage().withType("layout").withName("thelayout").build(), tmpDir);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        exporter.handleFileExport(page.getId(), stream);

        assertThat(stream.size()).isPositive();
    }

    @Test
    void should_export_json_model_of_the_exported_artefact(@TempDir Path tmpDir) throws Exception {
        Page page = create(aPage().withId("myPage").build(), tmpDir);

        exporter.handleFileExport(page.getId(), artifactStream);

        Path unzipped = unzip(artifactStream);
        byte[] actual = readAllBytes(unzipped.resolve("resources/page.json"));
        byte[] expected = readAllBytes(tmpDir.resolve(page.getId() + ".json"));
        assertThat(actual).isEqualTo(expected);
        deleteDirectory(unzipped.toFile());
    }

    @Test
    void should_execute_export_steps(@TempDir Path tmpDir) throws Exception {
        FakeStep fakeStep1 = new FakeStep("This is some content", "resources/file1.json");
        FakeStep fakeStep2 = new FakeStep("This is another content", "resources/deep/file2.json");
        Exporter<Page> exporter = new PageExporter(jsonHandler, pageService, fakeStep1, fakeStep2);
        Page page = create(aPage().build(), tmpDir);

        exporter.handleFileExport(page.getId(), artifactStream);

        Path unzipped = unzip(artifactStream);
        assertThat(readAllBytes(unzipped.resolve("resources/file1.json"))).isEqualTo("This is some content".getBytes());
        assertThat(readAllBytes(unzipped.resolve("resources/deep/file2.json")))
                .isEqualTo("This is another content".getBytes());
        deleteDirectory(unzipped.toFile());
    }

    /**
     * Fake step that add things to zip
     */
    private class FakeStep implements ExportStep<Page> {

        private String content;

        private String filename;

        public FakeStep(String content, String filename) {
            this.content = content;
            this.filename = filename;
        }

        @Override
        public void execute(Zipper zipper, Page page) throws IOException {
            zipper.addToZip(content.getBytes(), filename);
        }
    }
}
