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
package org.bonitasoft.web.designer.migration.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import java.util.Arrays;

import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.builder.VariableBuilder;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BusinessVariableMigrationStepTest {

    BusinessVariableMigrationStep<AbstractPage> businessVariableMigrationStep;

    @BeforeEach
    public void setUp() throws Exception {
        businessVariableMigrationStep = new BusinessVariableMigrationStep<>();
    }

    @Test
    public void should_migrate_page_with_business_data() throws Exception {

        var businessDataValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com_company_model_BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":0,\"c\":10}}";
        var expectedValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com.company.model.BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":\"0\",\"c\":\"10\"}}";
        Page pageWithData = aPage().withId("pageWithData")
                .withVariable("aBusinessData", VariableBuilder.aBusinessDataVariable().value(businessDataValue).build())
                .build();

        businessVariableMigrationStep.migrate(pageWithData);

        assertThat(pageWithData.getVariables().get("aBusinessData").getValue()).isEqualTo(Arrays.asList(expectedValue));

    }

    @Test
    public void should_migrate_fragment_with_business_data() throws Exception {

        var businessDataValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com_company_model_BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":0,\"c\":10}}";
        var expectedValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com.company.model.BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":\"0\",\"c\":\"10\"}}";
        Fragment fragmentWithData = FragmentBuilder.aFragment().withId("fragmentWithData")
                .withVariable("aBusinessData", VariableBuilder.aBusinessDataVariable().value(businessDataValue).build())
                .build();

        businessVariableMigrationStep.migrate(fragmentWithData);

        assertThat(fragmentWithData.getVariables().get("aBusinessData").getValue())
                .isEqualTo(Arrays.asList(expectedValue));
    }

}
