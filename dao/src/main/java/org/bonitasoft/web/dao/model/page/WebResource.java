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
package org.bonitasoft.web.dao.model.page;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.dao.model.JsonViewPersistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class WebResource {

    private String method;
    private String value;
    private Set<String> scopes = new HashSet<>();
    private boolean isAutomaticDetection = false;

    @JsonCreator
    public WebResource(@JsonProperty("method") String method, @JsonProperty("value") String value) {
        this.method = method;
        this.value = value;
    }

    public WebResource(String method, String value, String scope) {
        this.method = method.toLowerCase();
        this.value = value;
        this.scopes.add(scope);
        this.isAutomaticDetection = true;
    }

    @JsonView({ JsonViewPersistence.class })
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @JsonView({ JsonViewPersistence.class })
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }

    public void addToScopes(String value) {
        this.scopes.add(value);
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String toDefinition() {
        return this.method.toUpperCase().concat("|").concat(this.value);
    }

    @JsonView
    public boolean isAutomatic() {
        return this.isAutomaticDetection;
    }

    @JsonIgnore
    public void setAutomatic(boolean isAutomaticDetection) {
        this.isAutomaticDetection = isAutomaticDetection;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("method", method).append("value", value)
                .append("isAutomaticDetection", isAutomaticDetection).append("scope", scopes.toString()).toString();
    }
}
