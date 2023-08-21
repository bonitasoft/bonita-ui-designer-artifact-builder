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

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WidgetPropertiesBuilderTest {

    private static final String DESIGNER_VERSION = "1.12.1";

    private WidgetPropertiesBuilder widgetPropertiesBuilder;
    private Widget widget;

    @BeforeEach
    public void setUp() throws Exception {
        UiDesignerProperties uiDesignerProperties = new UiDesignerProperties();
        uiDesignerProperties.setVersion(DESIGNER_VERSION);
        widgetPropertiesBuilder = new WidgetPropertiesBuilder(uiDesignerProperties);
        widget = new Widget();
        widget.setName("myWidget");
    }

    @Test
    public void should_build_a_well_formed_page_property_file() throws Exception {
        widget.setDesignerVersion("1.12.1");

        byte[] a = widgetPropertiesBuilder.build(widget);
        String properties = new String(a);

        assertThat(properties).contains("contentType=widget");
        assertThat(properties).contains("name=myWidget");
        assertThat(properties).contains("designerVersion=1.12.1");
    }

}
