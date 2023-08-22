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

class VariableAdvancedOptionsTest {

    private JsonHandler jsonHandler;

    @BeforeEach
    void createJsonHandler() {
        jsonHandler = new JsonHandlerFactory().create();
    }

    @Test
    void deserializeVariableAdvancedOptions() throws Exception {
        var variable = jsonHandler.fromJson(json("Content-Type: application/json", "200", "error"),
                VariableAdvancedOptions.class, JsonViewPersistence.class);

        assertThat(variable).isNotNull();
        assertThat(variable.getHeaders()).isEqualTo("Content-Type: application/json");
        assertThat(variable.getStatusCode()).isEqualTo("200");
        assertThat(variable.getFailedResponseValue()).isEqualTo("error");
        assertThat(variable.toString())
                .endsWith("headers=Content-Type: application/json,statusCode=200,failedResponseValue=error]");
    }

    @Test
    void serializeVariableAdvancedOptions() throws Exception {
        var variable = new VariableAdvancedOptions(null, null, null);
        variable.setType("Content-Type: application/json");
        variable.setFailedResponseValue("error");
        variable.setStatusCode("200");

        var json = jsonHandler.toJson(variable, JsonViewPersistence.class);

        assertThat(json).isEqualTo(json("Content-Type: application/json", "200", "error"));
    }

    private byte[] json(String headers, String statusCode, String failedResponseValue) {
        return String.format("{\"headers\":\"%s\",\"statusCode\":\"%s\",\"failedResponseValue\":\"%s\"}", headers,
                statusCode, failedResponseValue).getBytes();
    }

}
