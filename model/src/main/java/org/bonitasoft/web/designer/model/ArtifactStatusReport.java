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
package org.bonitasoft.web.designer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(value = { "compatible", "migration" }, allowGetters = true)
@JsonView({ JsonViewLight.class })
@Getter
@Setter
public class ArtifactStatusReport {

    private boolean compatible;
    private boolean migration;

    public ArtifactStatusReport(boolean compatible, boolean migration) {
        this.compatible = compatible;
        this.migration = migration;
    }

    public ArtifactStatusReport() {
        this.compatible = true;
        this.migration = true;
    }

    @Override
    public String toString() {
        return "{" +
                "\"compatible\":" + compatible +
                ",\"migration\":" + migration +
                '}';
    }
}
