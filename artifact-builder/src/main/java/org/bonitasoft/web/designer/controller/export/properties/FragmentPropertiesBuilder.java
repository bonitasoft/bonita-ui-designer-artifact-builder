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
package org.bonitasoft.web.designer.controller.export.properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.bonitasoft.web.dao.generator.rendering.GenerationException;
import org.bonitasoft.web.dao.model.fragment.Fragment;
import org.bonitasoft.web.designer.config.UiDesignerProperties;

public class FragmentPropertiesBuilder {

    private final UiDesignerProperties uiDesignerProperties;

    public FragmentPropertiesBuilder(UiDesignerProperties uiDesignerProperties) {
        this.uiDesignerProperties = uiDesignerProperties;
    }

    public byte[] build(Fragment fragment) throws GenerationException, IOException {
        var properties = new Properties();
        properties.put("name", fragment.getName());
        properties.put("contentType", String.valueOf(fragment.getType()).toLowerCase(Locale.ENGLISH));
        properties.put("designerVersion", uiDesignerProperties.getVersion());
        var byteArrayOutputStream = new ByteArrayOutputStream();
        properties.store(byteArrayOutputStream, "Generated by Bonita UI Designer");

        return byteArrayOutputStream.toByteArray();
    }
}
