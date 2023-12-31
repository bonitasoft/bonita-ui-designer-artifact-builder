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
package org.bonitasoft.web.designer.model.page;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.validation.Validation;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.builder.ComponentBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.PageWithFragmentBuilder;
import org.bonitasoft.web.designer.model.*;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class PageTest {

    private BeanValidator beanValidator;
    private JsonHandler jsonHandler;

    @BeforeEach
    void init() {
        jsonHandler = new JsonHandlerFactory().create();
        beanValidator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void jsonview_light_should_only_manage_id_name_hasValidationError_and_favorite() throws Exception {
        String json = jsonHandler.toJsonString(createAFilledPage(), JsonViewLight.class);

        JSONAssert.assertEquals(json, "{"
                + "\"id\":\"ID\","
                + "\"uuid\":\"UUID\","
                + "\"name\":\"myPage\","
                + "\"type\":\"page\","
                + "\"favorite\": true,"
                + "\"hasValidationError\": false,"
                + "\"status\": {compatible:true, migration:true}"
                + "}", false);
    }

    @Test
    void jsonview_persistence_should_manage_all_properties() throws Exception {
        Page myPage = createAFilledPage();
        myPage.setFavorite(true);

        //We serialize and deserialize our object
        byte[] data = jsonHandler.toJson(myPage, JsonViewPersistence.class);
        Page pageAfterJsonProcessing = jsonHandler.fromJson(data, Page.class);

        Assertions.assertThat(pageAfterJsonProcessing.getName()).isEqualTo(myPage.getName());
        Assertions.assertThat(pageAfterJsonProcessing.getId()).isEqualTo(myPage.getId());
        Assertions.assertThat(pageAfterJsonProcessing.getVariables()).isNotEmpty();
        Assertions.assertThat(pageAfterJsonProcessing.getRows()).isNotEmpty();
        Assertions.assertThat(pageAfterJsonProcessing.isFavorite()).isFalse();

        //A rows contains a list of elements. We verify the first
        Element element = pageAfterJsonProcessing.getRows().get(0).get(0);
        Assertions.assertThat(element.getPropertyValues()).isNotNull();
        Assertions.assertThat(element.getDimension().get("xs")).isEqualTo(12);

    }

    @Test
    void jsonview_persistence_page_with_fragment_should_manage_all_properties() throws Exception {
        Page myPage = PageWithFragmentBuilder.aPageWithFragmentElement();

        //We serialize and deserialize our object
        byte[] data = jsonHandler.toJson(myPage, JsonViewPersistence.class);
        Page pageAfterJsonProcessing = jsonHandler.fromJson(data, Page.class);

        Assertions.assertThat(pageAfterJsonProcessing.getName()).isEqualTo(myPage.getName());
        Assertions.assertThat(pageAfterJsonProcessing.getId()).isEqualTo(myPage.getId());
        Assertions.assertThat(pageAfterJsonProcessing.getVariables()).isNotEmpty();
        Assertions.assertThat(pageAfterJsonProcessing.getRows()).isNotEmpty();

        //A rows contains a list of elements. We verify the first
        Element element = pageAfterJsonProcessing.getRows().get(0).get(0);
        Assertions.assertThat(element.getPropertyValues()).isNotNull();
        Assertions.assertThat(element.getDimension().get("xs")).isEqualTo(12);

    }

    @Test
    void jsonview_persitence_should_manage_all_fields() throws Exception {
        Page myPage = createAFilledPage();
        //We serialize and deserialize our object
        byte[] data = jsonHandler.toJson(myPage);
        Page pageAfterJsonProcessing = jsonHandler.fromJson(data, Page.class);

        Assertions.assertThat(pageAfterJsonProcessing.getName()).isNotNull();
        Assertions.assertThat(pageAfterJsonProcessing.getId()).isNotNull();
        Assertions.assertThat(pageAfterJsonProcessing.getVariables()).isNotEmpty();
        Assertions.assertThat(pageAfterJsonProcessing.getRows()).isNotNull();
        Assertions.assertThat(
                ((Component) ((Container) pageAfterJsonProcessing.getRows().get(0).get(1)).getRows().get(1).get(1))
                        .getDescription())
                .isNotNull().isNotEmpty();
    }

    @Test
    void jsonview_persitence_should_persist_component_name_and_description() throws Exception {
        Page myPage = PageBuilder.aPage()
                .with(ComponentBuilder.anInput().withDescription("A mandatory name input").build()).build();

        //We serialize and deserialize our object
        byte[] data = jsonHandler.toJson(myPage);
        Page pageAfterJsonProcessing = jsonHandler.fromJson(data, Page.class);

        Component component = (Component) pageAfterJsonProcessing.getRows().get(0).get(0);
        Assertions.assertThat(component.getDescription()).isNotNull().isNotEmpty();
    }

    @Test
    void should_not_have_a_name_containing_space() throws Exception {
        Page page = PageBuilder.aPage().withName("the name").build();

        assertThrows(ConstraintValidationException.class, () -> beanValidator.validate(page));
    }

    @Test
    void should_not_have_a_name_containing_special_characters() throws Exception {
        Page page = PageBuilder.aPage().withName("the-name").build();

        assertThrows(ConstraintValidationException.class, () -> beanValidator.validate(page));
    }

    @Test
    void should_check_for_assets_by_type_and_name() throws Exception {
        Page page = PageBuilder.aPage().withAsset(
                AssetBuilder.anAsset().withType(AssetType.CSS).withName("aName"))
                .build();

        Assertions.assertThat(page.hasAsset(AssetType.CSS, "aName")).isTrue();
        Assertions.assertThat(page.hasAsset(AssetType.CSS, "anotherName")).isFalse();
        Assertions.assertThat(page.hasAsset(AssetType.JAVASCRIPT, "aName")).isFalse();
    }

    /**
     * Create a filled page with a value for all fields
     */
    private Page createAFilledPage() throws Exception {
        Page page = PageBuilder.aFilledPage("ID");
        page.setUUID("UUID");
        page.setFavorite(true);
        page.setName("myPage");
        page.setStatus(new ArtifactStatusReport());
        return page;
    }
}
