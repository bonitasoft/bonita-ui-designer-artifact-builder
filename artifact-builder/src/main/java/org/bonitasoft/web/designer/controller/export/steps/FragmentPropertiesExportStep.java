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
package org.bonitasoft.web.designer.controller.export.steps;

import java.io.IOException;

import org.bonitasoft.web.dao.export.ExportStep;
import org.bonitasoft.web.dao.model.fragment.Fragment;
import org.bonitasoft.web.dao.export.Zipper;
import org.bonitasoft.web.designer.controller.export.properties.FragmentPropertiesBuilder;

public class FragmentPropertiesExportStep implements ExportStep<Fragment> {

    private final FragmentPropertiesBuilder fragmentPropertiesBuilder;

    public FragmentPropertiesExportStep(FragmentPropertiesBuilder fragmentPropertiesBuilder) {
        this.fragmentPropertiesBuilder = fragmentPropertiesBuilder;
    }

    @Override
    public void execute(Zipper zipper, Fragment artifact) throws IOException {
        byte[] pageProperties = fragmentPropertiesBuilder.build(artifact);
        zipper.addToZip(pageProperties, "fragment.properties");
    }
}
