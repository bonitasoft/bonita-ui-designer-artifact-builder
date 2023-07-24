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
package org.bonitasoft.web.designer;

import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.dao.generator.rendering.GenerationException;
import org.bonitasoft.web.dao.model.fragment.Fragment;
import org.bonitasoft.web.dao.model.page.Page;
import org.bonitasoft.web.dao.model.widgets.Widget;
import org.bonitasoft.web.dao.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.controller.export.ExportException;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.workspace.Workspace;

public interface ArtifactBuilder {

    Workspace getWorkspace();

    byte[] buildPage(String id) throws ModelException, ExportException, IOException;

    byte[] build(Page page) throws ModelException, ExportException, IOException;

    byte[] buildFragment(String id) throws ModelException, ExportException, IOException;

    byte[] build(Fragment fragment) throws ModelException, ExportException, IOException;

    byte[] buildWidget(String id) throws ModelException, ExportException, IOException;

    byte[] build(Widget widget) throws ModelException, ExportException, IOException;

    String buildHtml(Page page, String context) throws GenerationException, NotFoundException;

    String buildHtml(Fragment fragment, String context) throws GenerationException, NotFoundException;

    /**
     * Import an artifact
     *
     * @param path the path to import artifact from
     * @param ignoreConflicts if false, one can choose to {@link #replayImportIgnoringConflicts(String)} in case of conflicting resources
     * @return the report import, it's uuid can be used to rerun and ignoreConflicts import in case of conflicts
     */
    ImportReport importArtifact(Path path, boolean ignoreConflicts);

    ImportReport importPage(Path path, boolean ignoreConflicts);

    ImportReport importFragment(Path path, boolean ignoreConflicts);

    ImportReport importWidget(Path path, boolean ignoreConflicts);

    /**
     * Allow to replay and force an import that overrides existing resources
     *
     * @param uuid the uuid of the previous import with conflicts
     * @return the report import
     */
    ImportReport replayImportIgnoringConflicts(String uuid);

    void cancelImport(String uuid);
}
