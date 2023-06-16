/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.controller.export.ExportException;
import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.importer.AbstractArtifactImporter;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.PageImporter;
import org.bonitasoft.web.designer.controller.importer.WidgetImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.workspace.Workspace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.MODEL_NOT_FOUND;
import static org.bonitasoft.web.designer.controller.importer.ImportPathResolver.resolveImportPath;
import static org.bonitasoft.web.designer.controller.importer.report.ImportReport.Status.IMPORTED;

@RequiredArgsConstructor
public class AngularJsArtifactBuilder implements ArtifactBuilder {

    public static final List<String> supportedArtifactTypes = List.of("page", "fragment", "widget");

    @Getter
    private final Workspace workspace;
    private final WidgetService widgetService;
    private final FragmentService fragmentService;
    private final PageService pageService;
    private final PageExporter pageExporter;
    private final FragmentExporter fragmentExporter;
    private final WidgetExporter widgetExporter;
    private final HtmlGenerator htmlGenerator;
    private final ImportStore importStore;
    private final PageImporter pageImporter;
    private final FragmentImporter fragmentImporter;
    private final WidgetImporter widgetImporter;

    @Override
    public byte[] buildPage(String id) throws ModelException, ExportException, IOException {
        return build(pageService.get(id));
    }

    @Override
    public byte[] build(Page page) throws ModelException, ExportException, IOException {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            pageExporter.handleFileExport(page.getId(), outputStream);
        }
        return outputStream.toByteArray();
    }

    @Override
    public byte[] buildFragment(String id) throws ModelException, ExportException, IOException {
        return build(fragmentService.get(id));
    }

    @Override
    public byte[] build(Fragment fragment) throws ModelException, ExportException, IOException {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            fragmentExporter.handleFileExport(fragment.getId(), outputStream);
        }
        return outputStream.toByteArray();
    }

    @Override
    public byte[] buildWidget(String id) throws ModelException, ExportException, IOException {
        return build(widgetService.get(id));
    }

    @Override
    public byte[] build(Widget widget) throws ModelException, ExportException, IOException {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            widgetExporter.handleFileExport(widget.getId(), outputStream);
        }
        return outputStream.toByteArray();
    }

    @Override
    public String buildHtml(Page page, String context) throws GenerationException, NotFoundException {
        return htmlGenerator.generateHtml(page, context);
    }

    @Override
    public String buildHtml(Fragment fragment, String context) throws GenerationException, NotFoundException {
        return htmlGenerator.generateHtml(fragment, context);
    }

    @Override
    public ImportReport importArtifact(Path path, boolean ignoreConflicts) {

        var zipFiles = resolveImportPath(path);

        var artifactType = resolveArtifactType(zipFiles);
        ImportReport report = null;
        switch (artifactType) {
            case "page":
                report = importPage(path, ignoreConflicts);
                break;
            case "fragment":
                report = importFragment(path, ignoreConflicts);
                break;
            case "widget":
                report = importWidget(path, ignoreConflicts);
                break;
            default:
                // Should never happen since resolveArtifactType() should have already thrown an exception.
                throw new ImportException(MODEL_NOT_FOUND, "Unknown artifact type: " + artifactType);
        }
        return report;
    }

    protected String resolveArtifactType(Path zipFiles) {
        return supportedArtifactTypes.stream()
                .filter(
                        type -> Files.exists(zipFiles.resolve(type + ".json"))
                ).findFirst()
                .orElseThrow(() -> {
                    var importException = new ImportException(MODEL_NOT_FOUND, "Could not load component, artifact model file not found");
                    importException.addInfo("modelfiles", supportedArtifactTypes.stream().map(type -> type + ".json").collect(toList()));
                    return importException;
                });
    }

    @Override
    public ImportReport importPage(Path path, boolean ignoreConflicts) {
        return importFromPath(path, ignoreConflicts, pageImporter);
    }

    @Override
    public ImportReport importFragment(Path path, boolean ignoreConflicts) {
        return importFromPath(path, ignoreConflicts, fragmentImporter);
    }

    @Override
    public ImportReport importWidget(Path path, boolean ignoreConflicts) {
        return importFromPath(path, ignoreConflicts, widgetImporter);
    }

    @Override
    public ImportReport replayImportIgnoringConflicts(String uuid) {
        var anImport = importStore.get(uuid);
        return anImport.getImporter().tryToImportAndGenerateReport(anImport, true);
    }

    @Override
    public void cancelImport(String uuid) {
        importStore.remove(uuid);
    }

    protected ImportReport importFromPath(Path path, boolean ignoreConflicts, AbstractArtifactImporter<?> importer) {
        var anImport = importStore.store(importer, path);
        ImportReport report = null;
        try {
            report = importer.tryToImportAndGenerateReport(anImport, ignoreConflicts);
        } finally {
            if (report == null || IMPORTED.equals(report.getStatus())) {
                importStore.remove(anImport.getUUID());
            }
        }
        return report;
    }
}
