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
package org.bonitasoft.web.designer.builder;

import java.nio.file.Path;

import org.bonitasoft.web.designer.config.DesignerInitializerException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;

public class UiDesignerPropertiesTestBuilder {

    private Path workspacePath;

    public static UiDesignerPropertiesTestBuilder aUiDesignerPropertiesBuilder() {
        return new UiDesignerPropertiesTestBuilder();
    }

    public static UiDesignerProperties aUiDesignerProperties(Path workspacePath) {
        return aUiDesignerPropertiesBuilder().withWorkspacePath(workspacePath).build();
    }

    private UiDesignerPropertiesTestBuilder withWorkspacePath(Path workspacePath) {
        this.workspacePath = workspacePath;
        return this;
    }

    public UiDesignerProperties build() {

        if (workspacePath == null) {
            throw new DesignerInitializerException("Workspace Path can not be null.");
        }

        UiDesignerProperties uiDesignerProperties = new UiDesignerProperties();

        WorkspaceProperties workspaceProperties = uiDesignerProperties.getWorkspace();
        workspaceProperties.getPages().setDir(workspacePath.resolve("pages"));
        workspaceProperties.getWidgets().setDir(workspacePath.resolve("widgets"));
        workspaceProperties.getFragments().setDir(workspacePath.resolve("fragments"));

        return uiDesignerProperties;
    }

}
