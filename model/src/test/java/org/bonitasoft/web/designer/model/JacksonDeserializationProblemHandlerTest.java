/** 
 * Copyright (C) 2023 BonitaSoft S.A.
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
package org.bonitasoft.web.designer.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;

import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.junit.jupiter.api.Test;

class JacksonDeserializationProblemHandlerTest {

    @Test
    void handleInactiveAssetUnknownProperty() throws Exception {
        var deserializer = new JacksonDeserializationProblemHandler();
        var asset = AssetBuilder.anAsset().build();

        var handled = deserializer.handleUnknownProperty(null, null, null, asset, "inactive");

        assertThat(handled).isTrue();
    }

    @Test
    void handleBidirectionalUnknownProperty() throws Exception {
        var deserializer = new JacksonDeserializationProblemHandler();
        var property = aProperty().build();

        var handled = deserializer.handleUnknownProperty(null, null, null, property, "bidirectional");

        assertThat(handled).isTrue();
    }

}
