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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.bonitasoft.web.designer.Version;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.junit.jupiter.api.Test;

class UiDesignerPropertiesBuilderTest {

    @Test
    void default_props_should_be_set() {
        var properties = new UiDesignerPropertiesBuilder()
                .workspacePath(Path.of("some/place/"))
                .build();

        assertThat(properties.getVersion()).isEqualTo(Version.VERSION);
        assertThat(properties.getEdition()).isEqualTo(Version.EDITION);
        assertThat(properties.getModelVersion()).isEqualTo(Version.MODEL_VERSION);
    }

    @Test
    void default_paths_should_be_set() {
        var workspacePath = Path.of("some", "place");

        var properties = new UiDesignerPropertiesBuilder()
                .workspacePath(workspacePath)
                .build();

        assertThat(properties.getWorkspace().getPath()).isEqualTo(workspacePath);
        assertThat(properties.getWorkspaceUid()).isNotNull();
    }

    @Test
    void should_override_default_folder_name() {
        var workspacePath = Path.of("some", "place");

        var pageFolder = "web-pages";
        var fragmentFolder = "web-fragments";
        var widgetFolder = "web-widgets";

        var properties = new UiDesignerPropertiesBuilder()
                .workspacePath(workspacePath)
                .pagesFolderName(pageFolder)
                .fragmentsFolderName(fragmentFolder)
                .widgetsFolderName(widgetFolder)
                .build();

        assertThat(properties.getWorkspace().getPath()).isEqualTo(workspacePath);
        assertThat(properties.getWorkspace().getPages().getDir())
                .isEqualTo(properties.getWorkspace().getPath().resolve(pageFolder));
    }
}
