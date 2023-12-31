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
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;

import org.junit.jupiter.api.Test;

class ConstantPropertyValuePredicateTest {

    @Test
    void should_return_true_when_the_property_value_matches() throws Exception {
        ConstantPropertyValuePredicate predicate = new ConstantPropertyValuePredicate("bar");

        assertThat(predicate.test(aComponent()
                .withPropertyValue("foo", "constant", "bar")
                .build())).isTrue();
    }

    @Test
    void should_return_false_when_property_value_do_not_match() throws Exception {
        ConstantPropertyValuePredicate predicate = new ConstantPropertyValuePredicate("qux");

        assertThat(predicate.test(aComponent()
                .withPropertyValue("foo", "constant", "bar")
                .build())).isFalse();
    }

    @Test
    void should_return_false_when_property_value_is_not_a_constant() throws Exception {
        ConstantPropertyValuePredicate predicate = new ConstantPropertyValuePredicate("bar");

        assertThat(predicate.test(aComponent()
                .withPropertyValue("foo", "whatever", "bar")
                .build())).isFalse();
    }
}
