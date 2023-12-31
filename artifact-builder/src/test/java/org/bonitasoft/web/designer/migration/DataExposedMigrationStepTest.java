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
package org.bonitasoft.web.designer.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataExposedMigrationStepTest {

    DataExposedMigrationStep dataExposedMigrationStep;

    @BeforeEach
    void setUp() {
        dataExposedMigrationStep = new DataExposedMigrationStep();
    }

    @Test
    void should_migrate_fragment_when_data_is_exposed() throws Exception {
        Fragment fragment = FragmentBuilder.aFilledFragment("myFragment");
        Variable variable = new Variable(DataType.JSON, "{}");
        variable.setExposed(true);
        fragment.addVariable("myExposedVariable", variable);

        dataExposedMigrationStep.migrate(fragment);

        assertThat(fragment.getVariables().get("myExposedVariable").isExposed()).isTrue();
        assertThat(fragment.getVariables().get("myExposedVariable").getType()).isEqualTo(DataType.CONSTANT);
        assertThat(fragment.getVariables().get("myExposedVariable").getValue()).isEqualTo(List.of(""));
    }

    @Test
    void should_not_migrate_fragment_when_data_is_not_exposed() throws Exception {
        Fragment fragment = FragmentBuilder.aFilledFragment("myFragment");
        Variable variable = new Variable(DataType.JSON, "{}");
        variable.setExposed(false);
        fragment.addVariable("myNotExposedVariable", variable);

        dataExposedMigrationStep.migrate(fragment);

        assertThat(fragment.getVariables().get("myNotExposedVariable").isExposed()).isFalse();
        assertThat(fragment.getVariables().get("myNotExposedVariable").getType()).isEqualTo(DataType.JSON);
        assertThat(fragment.getVariables().get("myNotExposedVariable").getValue()).isEqualTo(List.of("{}"));
    }
}
