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
package org.bonitasoft.web.designer;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.el.ExpressionFactory;
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
 * <p>
 * The test lives in this module (not in model, where the constraints are declared) on purpose: hibernate-validator
 * and expressly are runtime-scoped HERE, so this test classpath is the exact combination consumers get, whereas the
 * model module only sees its own test-scoped provider. The model and common test classpaths are not separately
 * guarded: they resolve the same BOM-managed versions, so a divergence would surface here first. The test also
 * asserts the EL provider identity, so a transitive jakarta-generation EL cannot silently stand in for the pin.
 * <p>
 * NOTE: ${validatedValue} relies on Hibernate Validator's default EL feature level for constraint messages
 * (bean-properties), which HV has been tightening since 6.2. If this test fails after an HV upgrade, check whether
 * the default feature level changed before suspecting the expressly pin.
 */
class ElMessageInterpolationTest {

    private record Sample(@Size(max = 3, message = "value '${validatedValue}' is too long") String name) {
    }

    @Test
    void should_resolve_expressly_as_the_el_provider() {
        // guard the pin, not just "some EL provider": a transitive jakarta-generation EL (e.g. tomcat-embed-el,
        // managed by the imported Spring Boot BOM) would keep interpolation green while the expressly pin rots.
        // The provider is identified by its code source, NOT by class name: expressly 5.x kept the legacy
        // com.sun.el.* packages, so its factory FQCN is identical to the org.glassfish:jakarta.el one
        var codeSource = ExpressionFactory.newInstance().getClass().getProtectionDomain().getCodeSource();

        assertThat(codeSource).as("EL provider must be the pinned expressly jar").isNotNull();
        assertThat(codeSource.getLocation()).asString().contains("expressly");
    }

    @Test
    void should_interpolate_el_expressions_in_constraint_messages() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(new Sample("too-long"));

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .containsExactly("value 'too-long' is too long");
        }
    }
}
