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
package org.bonitasoft.web.designer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Size;

import org.junit.jupiter.api.Test;

/**
 * Guards the pairing between hibernate-validator (version managed by the Spring Boot BOM) and the expressly EL
 * implementation (version pinned in the artifact-builder-dependencies BOM). If the two ever drift apart, EL
 * interpolation stops working: depending on the failure mode the default validator factory either throws at creation
 * or falls back to an interpolator that leaves EL expressions unresolved. Both make this test fail, whereas the rest
 * of the codebase only uses literal constraint messages and would stay green.
 */
class ElMessageInterpolationTest {

    private static class Sample {

        @Size(max = 3, message = "value '${validatedValue}' is too long")
        private final String name;

        private Sample(String name) {
            this.name = name;
        }
    }

    @Test
    void should_interpolate_el_expressions_in_constraint_messages() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();

        var violations = validator.validate(new Sample("too-long"));

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactly("value 'too-long' is too long");
    }
}
