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

import static org.bonitasoft.web.dao.model.data.DataType.BUSINESSDATA;

import java.util.function.Predicate;

import org.bonitasoft.web.dao.model.data.Variable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BonitaBusinessDataResourcePredicate implements Predicate<Variable> {

    @Override
    public boolean test(Variable variable) {
        return variable != null
                && BUSINESSDATA.equals(variable.getType())
                && variable.getValue() != null
                && !variable.getValue().isEmpty();
    }
}
