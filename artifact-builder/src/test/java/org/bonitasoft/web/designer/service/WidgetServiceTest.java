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
package org.bonitasoft.web.designer.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.parse;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.model.widget.BondType.CONSTANT;
import static org.bonitasoft.web.designer.model.widget.BondType.INTERPOLATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.PropertyBuilder;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.common.repository.exception.InUseException;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.common.visitor.AssetVisitor;
import org.bonitasoft.web.designer.common.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.exception.NotFoundException;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WidgetServiceTest {

    private static final String CURRENT_MODEL_VERSION = "2.0";

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private BondsTypesFixer<?> bondsTypesFixer;

    @Mock
    private WidgetMigrationApplyer widgetMigrationApplyer;

    @Mock
    private WidgetIdVisitor widgetIdVisitor;

    @Mock
    private AssetService<Widget> widgetAssetService;

    @InjectMocks
    private DefaultWidgetService widgetService;
    private ArtifactStatusReport artifactStatusReport;

    UiDesignerProperties uiDesignerProperties;

    @BeforeEach
    void setUp() {
        AssetVisitor assetVisitor = new AssetVisitor(widgetRepository, fragmentRepository);

        uiDesignerProperties = new UiDesignerProperties("1.13.0", CURRENT_MODEL_VERSION);
        uiDesignerProperties.getWorkspace().getWidgets().setDir(Paths.get("Widget"));

        widgetService = spy(new DefaultWidgetService(
                widgetRepository,
                pageRepository,
                fragmentRepository,
                singletonList(bondsTypesFixer),
                widgetMigrationApplyer,
                widgetIdVisitor,
                assetVisitor,
                uiDesignerProperties,
                widgetAssetService));
        artifactStatusReport = new ArtifactStatusReport(true, false);
    }

    @Test
    void should_serve_all_widgets_in_repository() {
        //Given
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").build();
        List<Widget> expectedWidgetList = asList(input, label);
        when(widgetRepository.getAll()).thenReturn(expectedWidgetList);

        //When
        List<Widget> widgets = widgetService.getAll();

        //Then
        assertThat(widgets).hasSameSizeAs(expectedWidgetList)
                .contains(input)
                .contains(label);
    }

    @Test
    void should_serve_empty_list_if_widget_repository_is_empty() {
        when(widgetRepository.getAll()).thenReturn(Collections.emptyList());

        List<Widget> widgets = widgetService.getAll();

        assertThat(widgets).isEmpty();

    }

    @Test
    void should_throw_repo_exception_if_an_error_occurs_while_getting_widgets() {
        when(widgetRepository.getAll()).thenThrow(new RepositoryException("error occurs", new Exception()));

        assertThatThrownBy(() -> widgetService.getAll()).isInstanceOf(RepositoryException.class);
    }

    @Test
    void should_get_a_widget_by_its_id() {
        String widgetId = "input";
        Widget input = aWidget().withId(widgetId).build();

        doReturn(artifactStatusReport).when(widgetService).getStatus(any());
        when(widgetRepository.get(widgetId)).thenReturn(input);
        when(widgetService.migrate(input)).thenReturn(input);

        Widget widget = widgetService.get(widgetId);

        assertThat(widget).isEqualTo(input);
        assertThat(widget.getAssets()).isEmpty();
    }

    @Test
    void should_get_a_widget_with_asset_by_its_id() {
        String widgetId = "input";
        Widget input = aWidget().withId(widgetId)
                .assets(anAsset().withName("myScopeWidgetAsset").withType(AssetType.CSS)).build();

        doReturn(artifactStatusReport).when(widgetService).getStatus(any());
        when(widgetRepository.get(widgetId)).thenReturn(input);
        when(widgetService.migrate(input)).thenReturn(input);

        Widget widget = widgetService.getWithAsset(widgetId);

        assertThat(widget).isEqualTo(input);
        assertThat(widget.getAssets()).hasSize(1);
        assertThat(widget.getAssets().stream().findFirst().get().getScope()).isEqualTo("widget");
    }

    @Test
    void should_throw_NotFoundException_when_getting_an_unexisting_widget() {
        when(widgetRepository.get("notExistingWidget")).thenThrow(new NotFoundException("not found"));

        assertThatThrownBy(() -> widgetService.getWithAsset("notExistingWidget")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void should_save_a_widget() {
        Widget customLabel = aWidget().withId("customLabel").custom().build();

        widgetService.save("customLabel", customLabel);

        verify(widgetRepository).updateLastUpdateAndSave(customLabel);
    }

    @Test
    void should_not_allow_to_save_a_pb_widget() {
        Widget pbWidget = aWidget().custom().build();

        assertThatThrownBy(() -> widgetService.save("pbLabel", pbWidget)).isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_not_allow_to_save_a_not_custom_widget() {
        Widget pbWidget = aWidget().withId("input").build();

        assertThatThrownBy(() -> widgetService.save("customLabel", pbWidget)).isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_throw_RepositoryException_if_an_error_occurs_while_saving_a_widget() {
        Widget customLabel = aWidget().withId("customLabel").custom().build();
        doThrow(new RepositoryException("error occurs", new Exception())).when(widgetRepository)
                .updateLastUpdateAndSave(customLabel);

        assertThatThrownBy(() -> widgetService.save("customLabel", customLabel))
                .isInstanceOf(RepositoryException.class);
    }

    @Test
    void should_create_a_new_widget() {
        Widget customLabel = aWidget().withName("label").custom().build();
        when(widgetRepository.create(customLabel)).thenReturn(customLabel);

        widgetService.create(customLabel);

        verify(widgetRepository).create(notNull());
    }

    @Test
    void should_duplicate_a_widget_from_a_widget() {
        Widget customLabel = aWidget().withName("label").assets(anAsset().withName("myfile.js")).custom().build();
        String sourceWidgetId = "my-widget-source";
        when(widgetRepository.create(customLabel)).thenReturn(customLabel);
        Path sourceWidgetPath = Paths.get("my-widget-source");
        when(widgetRepository.resolvePath(sourceWidgetId)).thenReturn(sourceWidgetPath);

        Widget savedWidget = widgetService.createFrom(sourceWidgetId, customLabel);

        verify(widgetRepository).create(customLabel);
        verify(widgetAssetService).duplicateAsset(uiDesignerProperties.getWorkspace().getWidgets().getDir(),
                sourceWidgetPath, sourceWidgetId, customLabel.getId());
        assertThat(savedWidget).isEqualTo(customLabel);
    }

    @Test
    void should_not_allow_to_create_a_widget_with_an_empty_name() {
        Widget customLabel = aWidget().withName("").custom().build();
        when(widgetRepository.create(customLabel)).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> widgetService.create(customLabel)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_not_allow_to_create_a_widget_with_an_existing_name() {
        Widget customLabel = aWidget().withName("alreadyExistingName").build();
        when(widgetRepository.create(customLabel)).thenThrow(new NotAllowedException("already existing name"));

        assertThatThrownBy(() -> widgetService.create(customLabel)).isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_delete_a_widget() {
        Widget customLabel = aWidget().custom().withId("customLabel").build();
        when(widgetRepository.get("customLabel")).thenReturn(customLabel);
        when(fragmentRepository.getArtifactsUsingWidget(customLabel.getId())).thenReturn(emptyList());
        when(pageRepository.getArtifactsUsingWidget(customLabel.getId())).thenReturn(emptyList());
        widgetService.delete("customLabel");
        verify(widgetRepository).delete("customLabel");
    }

    @Test
    void should_not_allow_to_delete_a_pb_widget() {
        when(widgetRepository.get("pbWidget")).thenReturn(aWidget().withId("pbWidget").build());
        assertThatThrownBy(() -> widgetService.delete("pbWidget")).isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_throw_NotFoundException_if_trying_to_delete_an_unknown_widget() {
        doThrow(new NotFoundException("not found")).when(widgetRepository).get("customLabel");
        assertThatThrownBy(() -> widgetService.delete("customLabel")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void should_not_allow_to_delete_a_custom_widget_used_in_a_page() {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().withId("customLabel").build());
        when(pageRepository.getComponentName()).thenReturn("page");
        when(pageRepository.getArtifactsUsingWidget("customLabel")).thenReturn(singletonList(aPage().withName("person").build()));
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(fragmentRepository.getArtifactsUsingWidget("customLabel"))
                .thenReturn(asList(aFragment().withName("personFragment1").build(),
                        aFragment().withName("personFragment2").build()));

        assertThatThrownBy(() -> widgetService.delete("customLabel"))
                .isInstanceOf(InUseException.class)
                .hasMessage("The widget cannot be deleted because it is used in 2 fragments, <personFragment1>, <personFragment2> 1 page, <person>");
    }

    @Test
    void should_throw_not_found_if_custom_widget_is_not_existing_when_renaming() {
        Property requestProperty = new PropertyBuilder().name("hello").build();
        when(widgetRepository.updateProperty("my-widget", "name", requestProperty))
                .thenThrow(new NotFoundException("page not found"));

        assertThatThrownBy(() -> widgetService.updateProperty("my-widget", "name", requestProperty))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void should_add_a_property_to_a_widget_and_return_the_list_of_properties() {
        Property property = aProperty().build();
        List<Property> expectedProperties = singletonList(property);
        when(widgetRepository.addProperty("customLabel", property)).thenReturn(expectedProperties);

        widgetService.addProperty("customLabel", property);

        verify(widgetRepository).addProperty("customLabel", property);
    }

    @Test
    void should_not_allow_to_add_a_property_to_a_pb_widget() {
        Property property = aProperty().build();

        assertThatThrownBy(() -> widgetService.addProperty("pbLabel", property))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_throw_NotFoundException_when_adding_a_property_to_an_unexisting_widget() {
        when(widgetRepository.addProperty(eq("unknownWidget"), any(Property.class))).thenThrow(new NotFoundException("not found"));
        var property = aProperty().build();
        
        assertThatThrownBy(() ->
                widgetService.addProperty("unknownWidget", property))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void should_throw_RepositoryException_when_error_appear_while_saving_property() {
        when(widgetRepository.addProperty(eq("label"), any(Property.class))).thenThrow(RepositoryException.class);
        var property = aProperty().build();
        
        assertThatThrownBy(() ->
                widgetService.addProperty("label", property))
                .isInstanceOf(RepositoryException.class);
    }

    @Test
    void should_update_a_property_of_a_widget_and_return_the_list_of_properties() {
        Property property = aProperty().build();
        List<Property> expectedProperties = singletonList(property);
        when(widgetRepository.updateProperty("customLabel", "toBeUpdated", property)).thenReturn(expectedProperties);

        widgetService.updateProperty("customLabel", "toBeUpdated", property);

        verify(widgetRepository).updateProperty("customLabel", "toBeUpdated", property);
    }

    @Test
    void should_not_allow_to_update_a_property_of_a_pb_widget() {
        var property = aProperty().build();

        assertThatThrownBy(() -> widgetService.updateProperty("pbLabel", "toBeUpdated", property))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_throw_NotFoundException_when_widget_or_property_not_found_while_updating_property() {
        var property = aProperty().build();

        assertThatThrownBy(() -> widgetService.updateProperty("pbLabel", "toBeUpdated", property))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_throw_RepositoryException_when_error_appear_while_updating_property() {
        when(widgetRepository.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(RepositoryException.class);
        var property = aProperty().build();
        
        assertThatThrownBy(() ->
                widgetService.updateProperty("label", "toBeUpdated", property))
                .isInstanceOf(RepositoryException.class);
    }

    @Test
    void should_delete_a_property_of_a_widget_and_return_the_list_of_properties() {
        Property property = aProperty().build();
        List<Property> expectedProperties = singletonList(property);
        when(widgetRepository.deleteProperty("customLabel", "toBeDeleted")).thenReturn(expectedProperties);

        widgetService.deleteProperty("customLabel", "toBeDeleted");

        verify(widgetRepository).deleteProperty("customLabel", "toBeDeleted");
    }

    @Test
    void should_not_allow_to_delete_a_property_of_a_pb_widget() {
        assertThatThrownBy(() -> widgetService.deleteProperty("pbLabel", "toBeDeleted"))
                .isInstanceOf(NotAllowedException.class)
                .hasMessage("Not allowed to modify a non custom widgets");
    }

    @Test
    void should_throw_NotFoundException_when_widget_or_property_not_found_while_deleting_property()  {
        when(widgetRepository.deleteProperty("label", "toBeDeleted"))
                .thenThrow(new NotFoundException("Widget [ toBeDeleted ] not found"));

        assertThatThrownBy(() ->
                widgetService.deleteProperty("label", "toBeDeleted"))
                .isInstanceOf(NotFoundException.class);
     }

    @Test
    void should_respond_500_when_error_appear_while_deleting_property()  {
        when(widgetRepository.deleteProperty("label", "toBeDeleted")).thenThrow(RepositoryException.class);
        assertThatThrownBy(() ->
                widgetService.deleteProperty("label", "toBeDeleted"))
                .isInstanceOf(RepositoryException.class);
    }

    @Test
    void should_save_a_local_asset() {
        byte[] fileContent = "var hello = 'hello';".getBytes(UTF_8);
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        String assetGeneratedId = "assetId";
        when(widgetAssetService.save(eq(widget), any(), eq(fileContent))).thenAnswer(invocationOnMock -> {
            Asset assetToSave = invocationOnMock.getArgument(1);

            assertThat(assetToSave.getId()).isNull();

            assetToSave.setId(assetGeneratedId);
            return assetToSave;
        });
        doReturn(artifactStatusReport).when(widgetService).getStatus(any());

        Asset savedAsset = widgetService.saveOrUpdateAsset("my-widget", AssetType.JAVASCRIPT, "myfile.js", fileContent);
        assertThat(savedAsset.getId()).isEqualTo(assetGeneratedId);
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(widgetAssetService).save(eq(widget), assetCaptor.capture(), eq(fileContent));
        Asset assetToSave = assetCaptor.getValue();
        assertThat(assetToSave.getName()).isEqualTo("myfile.js");
        assertThat(assetToSave.getType()).isEqualTo(AssetType.JAVASCRIPT);
        assertThat(assetToSave.getOrder()).isEqualTo(1);
    }

    @Test
    void should_not_upload_an_asset_for_internal_widget() {
        byte[] bytes = "foo".getBytes();

        assertThatThrownBy(
                () -> widgetService.saveOrUpdateAsset("pbwidget", AssetType.JAVASCRIPT, "myfile.js", bytes))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_save_an_external_asset() {
        Widget widget = aWidget().withId("my-widget").custom().build();
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2)
                .withType(AssetType.JAVASCRIPT).build();
        doReturn(artifactStatusReport).when(widgetService).getStatus(any());
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetService.migrate(widget)).thenReturn(widget);

        widgetService.saveAsset("my-widget", expectedAsset);

        verify(widgetAssetService).save(widget, expectedAsset);
    }

    @Test
    void should_not_save_an_external_asset_for_internal_widget() {
        Asset asset = anAsset().build();

        assertThatThrownBy(() -> widgetService.saveAsset("pb-widget", asset))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    void should_not_save_an_external_asset_when_upload_send_an_error() {
        Widget widget = aWidget().withId("my-widget").custom().build();
        Asset asset = anAsset().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetAssetService.save(widget, asset)).thenThrow(IllegalArgumentException.class);
        doReturn(artifactStatusReport).when(widgetService).getStatus(any());

        assertThatThrownBy(() -> widgetService.saveAsset("my-widget", asset))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_delete_an_asset() {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        doReturn(artifactStatusReport).when(widgetService).getStatus(any());

        widgetService.deleteAsset("my-widget", "UIID");

        verify(widgetAssetService).delete(widget, "UIID");
    }

    @Test
    void should_increment_an_asset() {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        doReturn(artifactStatusReport).when(widgetService).getStatus(any());

        widgetService.changeAssetOrder("my-widget", "UIID", INCREMENT);

        verify(widgetAssetService).changeAssetOrderInComponent(widget, "UIID", INCREMENT);
    }

    @Test
    void should_decrement_an_asset() {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        doReturn(artifactStatusReport).when(widgetService).getStatus(any());

        widgetService.changeAssetOrder("my-widget", "UIID", DECREMENT);

        verify(widgetAssetService).changeAssetOrderInComponent(widget, "UIID", DECREMENT);
    }

    @Test
    void should_mark_a_widget_as_favorite() {
        widgetService.markAsFavorite("my-widget", true);

        verify(widgetRepository).markAsFavorite("my-widget");
    }

    @Test
    void should_unmark_a_widget_as_favorite() {
        widgetService.markAsFavorite("my-widget", false);

        verify(widgetRepository).unmarkAsFavorite("my-widget");
    }

    @Test
    void should_load_widget_asset_on_disk() {
        widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix());

        verify(widgetAssetService).findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix());
    }

    @Test
    void should_throw_IOException_when_widget_asset_included_in_page_produce_IOException()  {
        when(widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix()))
                .thenThrow(new RuntimeException("can't read file"));
        var prefix = AssetType.JAVASCRIPT.getPrefix();
        
        assertThatThrownBy(() ->
            widgetService.findAssetPath("widget-id", "asset.js", prefix))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void should_serve_all_light_widgets_in_repository() {
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").lastUpdate(parse("2015-02-02T00:00:00.000Z")).build();
        when(widgetRepository.getAll()).thenReturn(asList(input, label));
        String[] ids = { "input", "label" };
        Map<String, List<Page>> map = new HashMap<>();
        map.put("input", singletonList(aPage().withName("hello").build()));
        when(pageRepository.getArtifactsUsingWidgets(asList(ids))).thenReturn(map);
        when(pageRepository.getComponentName()).thenReturn("page");
        Map<String, List<Fragment>> map2 = new HashMap<>();
        map2.put("label", singletonList(aFragment().withName("helloFragment").build()));
        when(fragmentRepository.getArtifactsUsingWidgets(asList(ids))).thenReturn(map2);
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        List<Widget> expectedWidgets = asList(input, label);
        when(widgetRepository.getAll()).thenReturn(expectedWidgets);

        List<Widget> returnedWidgets = widgetService.getAllWithUsedBy();

        assertThat(returnedWidgets).hasSameSizeAs(expectedWidgets)
                .contains(input)
                .contains(label);
        assertThat(input.getUsedBy()).hasSize(1);
        assertThat(input.getUsedBy().get("page").get(0).getName()).isEqualTo("hello");
        assertThat(label.getUsedBy()).hasSize(1);
        assertThat(label.getUsedBy().get("fragment").get(0).getName()).isEqualTo("helloFragment");
    }

    @Test
    void should_not_allow_to_delete_a_custom_widget_used_in_a_fragment()  {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().withId("customLabel").build());
        when(pageRepository.getArtifactsUsingWidget("customLabel")).thenReturn(emptyList());
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(fragmentRepository.getArtifactsUsingWidget("customLabel"))
                .thenReturn(singletonList(aFragment().withName("person").build()));

        assertThatThrownBy(() -> widgetService.delete("customLabel"))
                .isInstanceOf(InUseException.class)
                .hasMessage("The widget cannot be deleted because it is used in 1 fragment, <person>");

    }

    @Test
    void should_fix_bonds_types_on_save() {
        Property constantTextProperty = aProperty().name("text").bond(CONSTANT).build();
        Property interpolationTextProperty = aProperty().name("text").bond(INTERPOLATION).build();
        Widget persistedWidget = aWidget().withId("labelWidget").modelVersion("2.0").property(constantTextProperty)
                .build();
        lenient().when(widgetRepository.get("labelWidget")).thenReturn(persistedWidget);

        widgetService.updateProperty("labelWidget", "text", interpolationTextProperty);

        verify(bondsTypesFixer).fixBondsTypes("labelWidget", singletonList(interpolationTextProperty));
    }

    @Test
    void should_migrate_found_widget_when_get_is_called() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").designerVersion("1.0.0").build();
        Widget widgetMigrated = aWidget().withId("widget").modelVersion("2.0").previousArtifactVersion("1.0.0").build();
        when(widgetRepository.get("widget")).thenReturn(widget);
        MigrationResult<Widget> mr = new MigrationResult<>(widgetMigrated,
                List.of(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);

        widgetService.get("widget");

        verify(widgetMigrationApplyer).migrate(widget);
        verify(widgetRepository).updateLastUpdateAndSave(mr.getArtifact());
    }

    @Test
    void should_not_update_and_save_widget_if_no_migration_done() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").modelVersion("2.0").build();
        Widget widgetMigrated = aWidget().withId("widget").modelVersion("2.0").previousArtifactVersion("2.0").build();
        MigrationResult<Widget> mr = new MigrationResult<>(widget, singletonList(any(MigrationStepReport.class)));
        lenient().when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);
        when(widgetRepository.get("widget")).thenReturn(widget);

        widgetService.get("widget");

        verify(widgetMigrationApplyer, never()).migrate(widget);
        verify(widgetRepository, never()).updateLastUpdateAndSave(widgetMigrated);
    }

    @Test
    void should_migrate_all_custom_widget() throws Exception {
        reset(widgetService);
        Widget widget1 = aWidget().withId("widget1").designerVersion("1.0.0").build();
        Widget widget2 = aWidget().withId("widget2").designerVersion("1.0.0").build();
        Widget widget1Migrated = aWidget().withId("widget1").designerVersion("2.0").build();
        Widget widget2Migrated = aWidget().withId("widget2").designerVersion("2.0").build();
        lenient().when(widgetRepository.get("widget1")).thenReturn(widget1);
        lenient().when(widgetRepository.get("widget2")).thenReturn(widget2);
        when(widgetMigrationApplyer.migrate(widget1)).thenReturn(new MigrationResult<>(widget1Migrated,
                List.of(new MigrationStepReport(MigrationStatus.SUCCESS, "widget1"))));
        when(widgetMigrationApplyer.migrate(widget2)).thenReturn(new MigrationResult<>(widget2Migrated,
                List.of(new MigrationStepReport(MigrationStatus.SUCCESS, "widget2"))));

        Set<String> h = new HashSet<>(Arrays.asList("widget1", "widget1"));
        when(widgetRepository.getByIds(h)).thenReturn(Arrays.asList(widget1, widget2));
        Page page = aPage().with(
                aComponent("widget1"),
                aComponent("widget2"))
                .build();

        when(widgetIdVisitor.visit(page)).thenReturn(h);

        widgetService.migrateAllCustomWidgetUsedInPreviewable(page);

        verify(widgetMigrationApplyer).migrate(widget1);
        verify(widgetMigrationApplyer).migrate(widget2);
    }

    @Test
    void should_not_update_and_save_widget_if_migration_finish_on_error() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").modelVersion("1.0").build();
        Widget widgetMigrated = aWidget().withId("widget").modelVersion("2.0").previousArtifactVersion("2.0").build();
        MigrationResult<Widget> mr = new MigrationResult<>(widget,
                List.of(new MigrationStepReport(MigrationStatus.ERROR)));
        when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);
        when(widgetRepository.get("widget")).thenReturn(widget);

        widgetService.get("widget");

        verify(widgetMigrationApplyer).migrate(widget);
        verify(widgetRepository, never()).updateLastUpdateAndSave(widgetMigrated);
    }

    @Test
    void should_get_correct_migration_status_when_dependency_is_to_migrate() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").designerVersion("1.10.0").build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(List.of("widget"));
        when(widgetRepository.getByIds(ids)).thenReturn(singletonList(widget));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        ArtifactStatusReport status = widgetService.getArtifactStatusOfCustomWidgetUsed(page);
        assertEquals(getArtifactStatusReport(true, true), status.toString());
    }

    @Test
    void should_get_correct_migration_status_when_dependency_is_not_compatible() {
        reset(widgetService);
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Widget widget1 = aWidget().withId("widget1").designerVersion("1.10.0").build();
        Widget widget2 = aWidget().withId("widget2").modelVersion("2.1").isCompatible(false).isMigration(false).build(); //incompatible
        Set<String> ids = new HashSet<>(Arrays.asList("widget1", "widget2"));
        when(widgetRepository.getByIds(ids)).thenReturn(Arrays.asList(widget1, widget2));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        ArtifactStatusReport status = widgetService.getArtifactStatusOfCustomWidgetUsed(page);
        assertEquals(getArtifactStatusReport(false, false), status.toString());
    }

    @Test
    void should_get_correct_migration_status_when_dependency_is_not_to_migrate() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").designerVersion("2.0").isCompatible(true).isMigration(false).build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(List.of("widget"));
        when(widgetRepository.getByIds(ids)).thenReturn(singletonList(widget));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        ArtifactStatusReport status = widgetService.getArtifactStatusOfCustomWidgetUsed(page);
        assertEquals(getArtifactStatusReport(true, false), status.toString());
    }

    private String getArtifactStatusReport(boolean compatible, boolean migration) {
        return new ArtifactStatusReport(compatible, migration).toString();
    }

}
