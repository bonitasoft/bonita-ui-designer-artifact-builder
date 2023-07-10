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
package org.bonitasoft.web.designer.model.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.validation.Validation;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AssetTest {

    private Asset asset;

    private BeanValidator beanValidator;
    private JsonHandler jsonHandler;

    @BeforeEach
    public void init() {
        jsonHandler = new JsonHandlerFactory().create();
        beanValidator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());
        asset = AssetBuilder.anAsset().withScope(AssetScope.PAGE).build();

    }

    /**
     * AssetNames injected in the test {@link #should_be_valid_when_name_is_valid(String)}. A name can be a filename or an URL
     */
    static Stream<Arguments> validNames() {
        return java.util.stream.Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("test.js"),
                org.junit.jupiter.params.provider.Arguments.of("jquery-1.11.3.min.js"),
                org.junit.jupiter.params.provider.Arguments.of("jquery-1.11.3.min-SNAPSHOT.js"),
                org.junit.jupiter.params.provider.Arguments.of("copy jquery.js"),
                org.junit.jupiter.params.provider.Arguments.of("jquery(1).js"),
                org.junit.jupiter.params.provider.Arguments.of("myimage_test.jpg"),
                org.junit.jupiter.params.provider.Arguments.of("https://code.jquery.com/jquery-2.1.4.min.js"),
                org.junit.jupiter.params.provider.Arguments.of("http://code.jquery.com/jquery-2.1.4.min.js"),
                org.junit.jupiter.params.provider.Arguments.of("https://code.jquery.com/jquery-2.1.4.min.JS"),
                org.junit.jupiter.params.provider.Arguments.of("http://code.jquery.com/jquery%20%402.1.4.min.js"),
                org.junit.jupiter.params.provider.Arguments.of("myé&name.js.js"),
                org.junit.jupiter.params.provider.Arguments.of("http_test.js"),
                org.junit.jupiter.params.provider.Arguments.of("http_test.js"));
    }

    @ParameterizedTest
    @MethodSource("validNames")
    public void should_be_valid_when_name_is_valid(String name) {
        asset.setName(name);
        beanValidator.validate(asset);
    }

    /**
     * AssetNames injected in the test {@link #should_be_invalid_when_name_is_invalid(String, String)}
     * <ul>
     * <li>The value is the asset name (example jquery-1.11.3.min.js)</li>
     * <li>Second value is the expected error message</li>
     * </ul>
     */
    static Stream<Arguments> invalidNames() {
        return java.util.stream.Stream.of(
                //Not null
                org.junit.jupiter.params.provider.Arguments.of(null, "Asset name should not be blank"));
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    public void should_be_invalid_when_name_is_invalid(String name, String expectedErrorMessage) {
        asset.setName(name);
        final ConstraintValidationException exception = assertThrows(ConstraintValidationException.class,
                () -> beanValidator.validate(asset));
        assertThat(exception.getMessage()).isEqualTo(expectedErrorMessage);
    }

    @Test
    public void should_be_invalid_when_type_null() {
        asset.setType(null);

        final ConstraintValidationException exception = assertThrows(ConstraintValidationException.class,
                () -> beanValidator.validate(asset));
        assertThat(exception.getMessage()).isEqualTo("Asset type may not be null");
    }

    @Test
    public void json_view_asset_should_persist_all_the_properties() throws Exception {

        Asset fileJsAsset = new Asset()
                .setId("UIID")
                .setName("file.js")
                .setType(AssetType.JAVASCRIPT)
                .setComponentId("page-id")
                .setOrder(1)
                .setActive(true)
                .setScope(AssetScope.PAGE);

        byte[] data = jsonHandler.toJson(fileJsAsset, Asset.JsonViewAsset.class);
        String json = new String(data, StandardCharsets.UTF_8);

        Assertions.assertThat(json).isEqualTo(
                "{\"id\":\"UIID\",\"name\":\"file.js\",\"type\":\"js\",\"componentId\":\"page-id\",\"scope\":\"page\",\"order\":1,\"active\":true,\"external\":false}");
    }

    @Test
    public void json_view_persistence_should_persist_a_subset_of_properties() throws Exception {

        Asset fileJsAsset = new Asset()
                .setId("UIID")
                .setName("file.js")
                .setType(AssetType.JAVASCRIPT)
                .setComponentId("page-id")
                .setOrder(1)
                .setActive(true)
                .setScope(AssetScope.PAGE);

        byte[] data = jsonHandler.toJson(fileJsAsset, JsonViewPersistence.class);
        String json = new String(data, StandardCharsets.UTF_8);

        //should only persist id, name, type and order
        Assertions.assertThat(json)
                .isEqualTo("{\"id\":\"UIID\",\"name\":\"file.js\",\"type\":\"js\",\"order\":1,\"external\":false}");
    }
}
