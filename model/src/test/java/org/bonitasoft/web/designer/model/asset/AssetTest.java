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

import java.nio.charset.StandardCharsets;

import javax.validation.Validation;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class AssetTest {

    private Asset asset;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private BeanValidator beanValidator;
    private JsonHandler jsonHandler;

    @Before
    public void init() {
        jsonHandler = new JsonHandlerFactory().create();
        beanValidator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());
        asset = AssetBuilder.anAsset().withScope(AssetScope.PAGE).build();

    }

    /**
     * AssetNames injected in the test {@link #should_be_valid_when_name_is_valid(String)}. A name can be a filename or an URL
     */
    protected Object[] validNames() {
        return JUnitParamsRunner.$(
                JUnitParamsRunner.$("test.js"),
                //Minified files
                JUnitParamsRunner.$("jquery-1.11.3.min.js"),
                //Snapshot verison
                JUnitParamsRunner.$("jquery-1.11.3.min-SNAPSHOT.js"),
                //Space
                JUnitParamsRunner.$("copy jquery.js"),
                //Parenthesis
                JUnitParamsRunner.$("jquery(1).js"),
                //underscore
                JUnitParamsRunner.$("myimage_test.jpg"),
                //Extension in uppercase
                JUnitParamsRunner.$("myimage_test.PNG"),
                //URL
                JUnitParamsRunner.$("https://code.jquery.com/jquery-2.1.4.min.js"),
                //URL in https
                JUnitParamsRunner.$("http://code.jquery.com/jquery-2.1.4.min.js"),
                //URL with extension in uppercase
                JUnitParamsRunner.$("https://code.jquery.com/jquery-2.1.4.min.JS"),
                //URL with space
                JUnitParamsRunner.$("https://code.jquery.com/jquery version 2.1.4.min.js"),
                //encoded URL
                JUnitParamsRunner.$("http://code.jquery.com/jquery%20%402.1.4.min.js"),
                //Special character
                JUnitParamsRunner.$("myé&name.js.js"),
                //Local asset with name starting by http
                JUnitParamsRunner.$("http_test.js"));
    }

    /**
     * AssetNames injected in the test {@link #should_be_invalid_when_name_is_invalid(String, String)}
     * <ul>
     * <li>The value is the asset name (example jquery-1.11.3.min.js)</li>
     * <li>Second value is the expected error message</li>
     * </ul>
     */
    protected Object[] invalidNames() {
        String errorMessage = "Asset name should be a filename containing only alphanumeric characters and no space or an external URL";
        return JUnitParamsRunner.$(
                //Not null
                JUnitParamsRunner.$(null, "Asset name should not be blank"));
    }

    @Test
    @Parameters(method = "validNames")
    public void should_be_valid_when_name_is_valid(String name) {
        asset.setName(name);
        beanValidator.validate(asset);
    }

    @Test
    @Parameters(method = "invalidNames")
    public void should_be_invalid_when_name_is_invalid(String name, String expectedErrorMessage) {
        exception.expect(ConstraintValidationException.class);
        exception.expectMessage(expectedErrorMessage);
        asset.setName(name);

        beanValidator.validate(asset);
    }

    @Test
    public void should_be_invalid_when_type_null() {
        exception.expect(ConstraintValidationException.class);
        exception.expectMessage("Asset type may not be null");
        asset.setType(null);

        beanValidator.validate(asset);
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
