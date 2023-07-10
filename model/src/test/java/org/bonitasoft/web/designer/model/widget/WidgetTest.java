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
package org.bonitasoft.web.designer.model.widget;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skyscreamer.jsonassert.JSONAssert;

public class WidgetTest {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Test
    public void jsonview_light_should_only_manage_id_name_hasValidationError_and_light_page() throws Exception {
        String json = jsonHandler.toJsonString(createAFilledWidget(), JsonViewLight.class);

        JSONAssert.assertEquals(json, "{"
                + "\"id\":\"ID2\","
                + "\"name\":\"aName\","
                + "\"custom\":false,"
                + "\"favorite\": true,"
                + "\"status\": {\"compatible\":true, \"migration\":true},"
                + "\"type\": \"widget\","
                + "\"usedBy\":{"
                + "\"page\":[{"
                + "\"id\":\"ID\","
                + "\"uuid\":\"UUID\","
                + "\"name\":\"myPage\","
                + "\"type\":\"page\","
                + "\"favorite\": false,"
                + "\"hasValidationError\": false,"
                + "\"status\": {\"compatible\":true, \"migration\":true}"
                + "}],"
                + "\"widget\":[{"
                + "\"id\":\"ID\","
                + "\"name\":\"aName\","
                + "\"custom\":false,"
                + "\"type\": \"widget\","
                + "\"favorite\": false,"
                + "\"status\": {\"compatible\":true, \"migration\":true}"
                + "}]"
                + "}}", true);
    }

    @Test
    public void jsonview_light_with_fragment_should_only_manage_id_name_hasValidationError_and_light_page()
            throws Exception {
        String json = jsonHandler.toJsonString(createAFilledWidgetWithFragment(), JsonViewLight.class);

        JSONAssert.assertEquals(json,
                "{\"id\":\"ID2\",\"name\":\"aName\",\"custom\":false,\"favorite\":false,\"type\":\"widget\",\"status\": {\"compatible\":true, \"migration\":true},"
                        + "\"usedBy\":{"
                        + "\"page\":[{\"id\":\"ID\",\"uuid\":\"UUID\",\"name\":\"myPage\",\"type\":\"page\", \"favorite\":false, \"hasValidationError\": false,\"status\": {\"compatible\":true, \"migration\":true}}],"
                        + "\"fragment\":[{\"id\":\"ID\",\"name\":\"father\",\"type\":\"fragment\", \"favorite\":false, \"hasValidationError\": false,\"status\": {\"compatible\":true, \"migration\":true}}],"
                        + "\"widget\":[{\"id\":\"ID\",\"name\":\"aName\",\"custom\":false,\"favorite\":false, \"type\":\"widget\", \"status\": {\"compatible\":true, \"migration\":true}}]}}",
                true);
    }

    @Test
    public void jsonview_persistence_should_manage_all_fields_except_rows_and_containers() throws Exception {
        Widget widgetInitial = createAFilledWidget();
        //We serialize and deserialize our object
        byte[] json = jsonHandler.toJson(widgetInitial, JsonViewPersistence.class);
        Widget widgetAfterJsonProcessing = jsonHandler.fromJson(json, Widget.class);

        assertThat(widgetAfterJsonProcessing.getName()).isNotNull();
        assertThat(widgetAfterJsonProcessing.getDescription()).isEqualTo("#widget fils d'son père!");
        assertThat(widgetAfterJsonProcessing.getId()).isNotNull();
        assertThat(widgetAfterJsonProcessing.getUsedBy()).isNull();
        assertThat(widgetAfterJsonProcessing.isFavorite()).isFalse();
        assertThat(widgetAfterJsonProcessing.hasHelp()).isFalse();
    }

    @Test
    public void should_convert_widget_id_in_spinal_case() throws Exception {
        String spinalCase = Widget.spinalCase("CUstomDisplayUTCDate");

        Assertions.assertThat(spinalCase).isEqualTo("c-ustom-display-u-t-c-date");
    }

    @Test
    public void should_not_add_useBy_components_when_list_is_empty() throws Exception {
        Widget widget = new Widget();
        widget.addUsedBy("component", new ArrayList<Identifiable>());

        assertThat(widget.getUsedBy()).isNull();
    }

    @Test
    public void should_not_add_useBy_components_when_list_is_null() throws Exception {
        Widget widget = new Widget();
        widget.addUsedBy("component", null);

        assertThat(widget.getUsedBy()).isNull();
    }

    @Test
    public void should_not_add_useBy_components() throws Exception {
        Page page = PageBuilder.aPage().build();
        Widget widget = new Widget();
        widget.addUsedBy("component", List.of(page));

        assertThat(widget.getUsedBy().get("component")).containsOnly(page);
    }

    @Test
    public void should_have_a_default_type_on_desieralization() throws Exception {
        byte[] content = toByteArray(this.getClass().getResourceAsStream("widget-with-no-type.json"));

        Widget widget = jsonHandler.fromJson(content, Widget.class);

        assertThat(widget.getType()).isEqualTo("widget");
    }

    /**
     * Create a filled widget with a value for all fields
     */
    private Widget createAFilledWidget() throws Exception {
        Widget widget = WidgetBuilder.aWidget().withId("ID").build();

        Widget widgetSon = WidgetBuilder.aWidget().withId("ID2").build();
        Page page = PageBuilder.aFilledPage("ID");
        page.setUUID("UUID");
        page.setName("myPage");
        widgetSon.addUsedBy("page", List.of(page));
        widgetSon.addUsedBy("widget", List.of(widget));
        widgetSon.setDescription("#widget fils d'son père!");
        widgetSon.setFavorite(true);
        return widgetSon;
    }

    /**
     * Create a filled widget with a value for all fields
     */
    private Widget createAFilledWidgetWithFragment() throws Exception {
        Widget widget = WidgetBuilder.aWidget().withId("ID").build();

        Widget widgetSon = WidgetBuilder.aWidget().withId("ID2").build();
        Fragment fragment = FragmentBuilder.aFragment().withId("ID").withName("father").withHasValidationError(false)
                .build();
        Page page = PageBuilder.aFilledPage("ID");
        page.setUUID("UUID");
        page.setName("myPage");
        page.setHasValidationError(false);
        widgetSon.addUsedBy("page", List.of(page));
        widgetSon.addUsedBy("fragment", List.of(fragment));
        widgetSon.addUsedBy("widget", List.of(widget));
        widgetSon.setDescription("#widget fils d'son père!");
        return widgetSon;
    }

    @Test
    public void should_prepare_widget_before_serialize() throws Exception {
        Widget widget = new Widget();
        widget.setId("widgetToSerialize");
        widget.setTemplate("<div>A widget template</div>");
        widget.setController("function(){return 'aa';}");

        widget.prepareWidgetToSerialize();

        assertThat(widget.getTemplate()).isEqualTo("@widgetToSerialize.tpl.html");
        assertThat(widget.getController()).isEqualTo("@widgetToSerialize.ctrl.js");
    }

    @Test
    public void should_prepare_widget_before_deserialize(@TempDir Path tempDir) throws Exception {
        Widget widget = new Widget();
        widget.setId("widgetToDeserialize");
        widget.setController("@widgetToDeserialize.ctrl.js");
        widget.setTemplate("@widgetToDeserialize.tpl.html");

        Files.createDirectory(tempDir.resolve("aWidget"));
        var widgetPath = tempDir.resolve("aWidget");

        byte[] ctrlContent = "function widgetToSerializeCtrl() { function comparator(initialValue, item) { return angular.equals(initialValue, ctrl.getValue(item));}}"
                .getBytes();
        Files.write(widgetPath.resolve("widgetToDeserialize.ctrl.js"), ctrlContent);

        byte[] htmlContent = "<div>A widget Template</div>".getBytes();
        Files.write(widgetPath.resolve("widgetToDeserialize.tpl.html"), htmlContent);

        widget.prepareWidgetToDeserialize(widgetPath);

        assertThat(widget.getController().getBytes()).isEqualTo(ctrlContent);
        assertThat(widget.getTemplate().getBytes()).isEqualTo(htmlContent);
    }

    @Test
    public void should_prepare_widget_before_deserialize_when_field_not_referred_to_files(@TempDir Path tempDir)
            throws Exception {
        Widget widget = new Widget();
        widget.setId("aWidget");
        widget.setController("function widgetCtrl(){}");
        widget.setTemplate("<div>my Template</div>");

        widget.prepareWidgetToDeserialize(tempDir.resolve("aWidget"));

        assertThat(widget.getController()).isEqualTo("function widgetCtrl(){}");
        assertThat(widget.getTemplate()).isEqualTo("<div>my Template</div>");
    }

}
