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
package org.bonitasoft.web.designer.model.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataTest {

    private JsonHandler jsonHandler;

    @BeforeEach
    void createJsonHandler() {
        jsonHandler = new JsonHandlerFactory().create();
    }

    @Test
    void deserializeData() throws Exception {
        var data = jsonHandler.fromJson(json(DataType.CONSTANT.name(), "a constant value", false),
                Data.class, JsonViewPersistence.class);

        assertThat(data).isNotNull();
        assertThat(data.getType()).isEqualTo(DataType.CONSTANT);
        assertThat(data.getValue()).isEqualTo("a constant value");
        assertThat(data.toString())
                .endsWith("type=CONSTANT,value=a constant value,exposed=false]");
    }

    @Test
    void serializeData() throws Exception {
        var data = new Data(null, null);
        data.setType(DataType.CONSTANT);
        data.setValue("a constant value");
        data.setExposed(true);

        var json = jsonHandler.toJson(data, JsonViewPersistence.class);

        assertThat(json).isEqualTo(json(DataType.CONSTANT.name(), "a constant value", true));
        assertThat(jsonHandler.fromJson(json, Data.class, JsonViewPersistence.class)).isEqualTo(data);
    }

    private byte[] json(String type, String value, boolean exposed) {
        if (exposed) {
            return String.format("{\"type\":\"%s\",\"value\":\"%s\",\"exposed\":true}", type.toLowerCase(),
                    value).getBytes();
        }
        return String.format("{\"type\":\"%s\",\"value\":\"%s\"}", type.toLowerCase(),
                value).getBytes();
    }

}
