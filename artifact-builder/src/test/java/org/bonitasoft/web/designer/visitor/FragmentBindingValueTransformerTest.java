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
package org.bonitasoft.web.designer.visitor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.bonitasoft.web.dao.model.page.PropertyValue;
import org.junit.Test;

public class FragmentBindingValueTransformerTest {

    @Test
    public void should_create_a_property_value_of_type_data() throws Exception {
        FragmentBindingValueTransformer transformer = new FragmentBindingValueTransformer();

        PropertyValue propertyValue = transformer.apply(Map.entry("name", "value"));

        assertThat(propertyValue.getType()).isEqualTo("variable");
        assertThat(propertyValue.getValue()).isEqualTo("value");
    }
}
