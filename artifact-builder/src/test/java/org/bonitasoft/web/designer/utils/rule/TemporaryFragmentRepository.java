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
package org.bonitasoft.web.designer.utils.rule;

import static java.lang.String.format;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import org.bonitasoft.web.designer.Version;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.common.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.BeanValidator;

public class TemporaryFragmentRepository {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private FragmentRepository repository;

    private final WorkspaceProperties workspaceProperties;
    private Path tempPath;

    public void init(Path tempDir) {
        tempPath = tempDir;
        var uiDesignerProperties = new UiDesignerProperties("1.13.0", Version.MODEL_VERSION);
        workspaceProperties.getFragments().setDir(tempPath);
        uiDesignerProperties.setWorkspace(workspaceProperties);

        BeanValidator validator = mock(BeanValidator.class);
        Watcher watcher = mock(Watcher.class);

        repository = new FragmentRepository(
                uiDesignerProperties.getWorkspace().getFragments().getDir(),
                uiDesignerProperties.getWorkspaceUid().getExtractPath().resolve("angularjs").resolve("templates"),
                new JsonFileBasedPersister<>(jsonHandler, validator, uiDesignerProperties.getVersion(),
                        uiDesignerProperties.getModelVersion()),
                new JsonFileBasedLoader<>(jsonHandler, Fragment.class),
                validator,
                watcher);

    }

    public TemporaryFragmentRepository(WorkspaceProperties workspaceProperties) {
        this.workspaceProperties = workspaceProperties;
    }

    public FragmentRepository toRepository() {
        return repository;
    }

    public Path resolveFragmentJson(String id) {
        return this.tempPath.resolve(format("%s/%s.json", id, id));
    }

    public Path resolveFragmentMetadata(String id) {
        return this.tempPath.resolve(format("%s/%s.metadata.json", id, id));
    }

    public Fragment addFragment(FragmentBuilder fragmentBuilder) {
        return repository.save(fragmentBuilder.build());
    }
}
