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
package org.bonitasoft.web.designer.config;

import java.nio.file.Path;

import org.bonitasoft.web.designer.Version;

public class UiDesignerPropertiesBuilder {

    private boolean experimental = false;
    private boolean liveBuildEnabled = true;

    private final UiDesignerProperties.BonitaProperties bonita = new UiDesignerProperties.BonitaProperties();
    private final WorkspaceProperties workspace = new WorkspaceProperties();
    private final WorkspaceUidProperties workspaceUid = new WorkspaceUidProperties();

    private String widgetsFolderName = "widgets";
    private String fragmentsFolderName = "fragments";
    private String pagesFolderName = "pages";

    public UiDesignerPropertiesBuilder experimental(boolean experimental) {
        this.experimental = experimental;
        return this;
    }

    public UiDesignerPropertiesBuilder disableLiveBuild() {
        this.liveBuildEnabled = false;
        return this;
    }

    public UiDesignerPropertiesBuilder portal(String url, String user, String password) {
        this.bonita.getPortal().setUrl(url);
        this.bonita.getPortal().setUser(user);
        this.bonita.getPortal().setPassword(password);
        return this;
    }

    public UiDesignerPropertiesBuilder bdm(String url) {
        this.bonita.getBdm().setUrl(url);
        return this;
    }

    public UiDesignerPropertiesBuilder studioUrl(String url) {
        this.workspace.setApiUrl(url);
        return this;
    }

    public UiDesignerPropertiesBuilder workspaceUidPath(Path path) {
        this.workspaceUid.setPath(path);
        return this;
    }

    public UiDesignerPropertiesBuilder workspacePath(Path path) {
        this.workspace.setPath(path);
        return this;
    }

    public UiDesignerPropertiesBuilder fragmentsFolderName(String fragmentsFolderName) {
        this.fragmentsFolderName = fragmentsFolderName;
        return this;
    }

    public UiDesignerPropertiesBuilder pagesFolderName(String pagesFolderName) {
        this.pagesFolderName = pagesFolderName;
        return this;
    }

    public UiDesignerPropertiesBuilder widgetsFolderName(String widgetsFolderName) {
        this.widgetsFolderName = widgetsFolderName;
        return this;
    }

    public UiDesignerProperties build() {
        var properties = new UiDesignerProperties();

        properties.setVersion(Version.VERSION);
        properties.setEdition(Version.EDITION);
        properties.setModelVersion(Version.MODEL_VERSION);

        properties.setExperimental(experimental);
        properties.setBonita(bonita);
        workspaceUid.setLiveBuildEnabled(liveBuildEnabled);
        properties.setWorkspaceUid(workspaceUid);
        properties.setWorkspace(workspace);

        properties.getWorkspace().getWidgets().setDir(properties.getWorkspace().getPath().resolve(widgetsFolderName));
        properties.getWorkspace().getFragments()
                .setDir(properties.getWorkspace().getPath().resolve(fragmentsFolderName));
        properties.getWorkspace().getPages().setDir(properties.getWorkspace().getPath().resolve(pagesFolderName));

        return properties;
    }

}
