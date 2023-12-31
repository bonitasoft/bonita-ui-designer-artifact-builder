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
package org.bonitasoft.web.designer.model.exception;

import static org.apache.commons.lang3.StringUtils.chop;

import java.util.Set;

import javax.validation.ConstraintViolation;

public class ConstraintValidationException extends RuntimeException {

    public ConstraintValidationException(Set<ConstraintViolation<Object>> violations) {
        super(buildMessage(violations));
    }

    private static String buildMessage(Set<ConstraintViolation<Object>> violations) {
        var message = new StringBuilder();
        for (ConstraintViolation<Object> constraintViolation : violations) {
            message.append(constraintViolation.getMessage()).append(",");
        }
        return chop(message.toString());
    }
}
