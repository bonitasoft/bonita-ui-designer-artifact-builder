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
package org.bonitasoft.web.designer.common.repository;

import static org.bonitasoft.web.designer.common.repository.BeanValidatorTest.TestBean.aValidBean;
import static org.bonitasoft.web.designer.common.repository.BeanValidatorTest.TestBean.anInvalidBean;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.validation.Validation;
import javax.validation.constraints.NotNull;

import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.Test;

public class BeanValidatorTest {

    @Test
    public void should_do_nothing_if_bean_is_valid() {
        BeanValidator validator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());

        validator.validate(aValidBean());
    }

    @Test
    public void should_throw_constraintViolationException_when_bean_is_not_valid() throws Exception {
        BeanValidator validator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());

        assertThrows(ConstraintValidationException.class, () -> validator.validate(anInvalidBean()));
    }

    public static class TestBean {

        @NotNull
        private String valid;

        public TestBean(String valid) {
            this.valid = valid;
        }

        public static TestBean aValidBean() {
            return new TestBean("valid");
        }

        public static TestBean anInvalidBean() {
            return new TestBean(null);
        }
    }
}
