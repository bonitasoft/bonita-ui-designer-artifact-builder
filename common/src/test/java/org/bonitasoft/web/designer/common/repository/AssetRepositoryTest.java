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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetRepositoryTest {

    @Mock
    private BeanValidator validator;

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private AssetRepository<Page> assetRepository;

    @TempDir
    Path pagesPath;

    @Test
    void should_resolveAssetPath() {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);

        Path path = assetRepository.resolveAssetPath(asset);

        assertThat(path.toUri()).isEqualTo(pagesPath.resolve("assets").resolve("js").resolve(asset.getName()).toUri());
    }

    @Test
    void should_not_resolveAssetPath_when_asset_invalid() {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);

        doThrow(ConstraintValidationException.class).when(validator)
                .validate(asset);

        assertThrows(ConstraintValidationException.class, () -> assetRepository.resolveAssetPath(asset));
    }

    @Test
    void should_save_asset() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        Path fileExpected = pagesPath.resolve("assets").resolve("js").resolve(asset.getName());
        assertThat(fileExpected.toFile()).doesNotExist();

        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assetRepository.save(asset, "My example with special characters réè@# \ntest".getBytes(UTF_8));

        //A json file has to be created in the repository
        assertThat(fileExpected.toFile()).exists();
        assertThat(Files.readAllLines(fileExpected, UTF_8).get(0))
                .isEqualTo("My example with special characters réè@# ");
    }

    @Test
    void should_throw_NullPointerException_when_deleting_asset_componentId_null() throws Exception {
        Asset asset = new Asset();
        
        assertThrows(NullPointerException.class, () -> assetRepository.delete(asset));
    }

    @Test
    void should_delete_asset() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        Path fileExpected = pagesPath.resolve("assets").resolve("js").resolve(asset.getName());
        Path jsPath = createDirectories(pagesPath.resolve("assets").resolve("js"));
        createFile(jsPath.resolve(asset.getName()));
        assertThat(fileExpected.toFile()).exists();
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assetRepository.delete(asset);

        assertThat(fileExpected.toFile()).doesNotExist();
    }

    @Test
    void should_throw_NotFoundException_when_deleting_inexisting_page() {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        Mockito.when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        Mockito.lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assertThrows(NotFoundException.class, () -> assetRepository.delete(asset));
    }

    @Test
    void readAllBytes_for_null_id_should_throw_ex() {
        assertThatThrownBy(() -> assetRepository.readAllBytes(null, Mockito.mock(Asset.class)))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(AssetRepository.COMPONENT_ID_REQUIRED);
    }

    @Test
    void should_readAllBytes_asset() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        Path fileExpected = pagesPath.resolve("assets").resolve("js").resolve(asset.getName());
        Path jsPath = createDirectories(pagesPath.resolve("assets").resolve("js"));
        createFile(jsPath.resolve(asset.getName()));
        assertThat(fileExpected.toFile()).exists();
        Mockito.when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        Mockito.lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assertThat(assetRepository.readAllBytes(asset)).isNotNull().isEmpty();
    }

    @Test
    void should_throw_NullPointerException_when_reading_asset_with_component_id_null() {
        assertThrows(NullPointerException.class, () -> assetRepository.readAllBytes(new Asset()));
    }

    @Test
    void should_throw_NotFoundException_when_reading_inexisting_page() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        Mockito.when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        Mockito.lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assertThrows(NotFoundException.class, () -> assetRepository.readAllBytes(asset));
    }

    @Test
    void should_find_asset_path_used_by_a_component() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        System.out.println(asset.getName());
        pagesPath.resolve("assets").resolve("js").resolve(asset.getName());

        Path jsPath = createDirectories(pagesPath.resolve("assets").resolve("js"));
        createFile(jsPath.resolve(asset.getName()));
        Mockito.when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        Mockito.when(pageRepository.get("page-id")).thenReturn(
                PageBuilder.aPage().withAsset(asset).build());

        assertThat(assetRepository.findAssetPath("page-id", "myasset.js", AssetType.JAVASCRIPT).toFile()).exists();
    }

    @Test
    void should_throw_NotAllowedException_when_find_external_asset() {
        Page page = PageBuilder.aPage().withId("page-id").build();

        Asset asset = AssetBuilder.aFilledAsset(page);
        asset.setName("http://mycdnserver.myasset.js");
        asset.setExternal(true);

        Mockito.when(pageRepository.get("page-id")).thenReturn(
                PageBuilder.aPage().withAsset(asset).build());

        final NotAllowedException exception = assertThrows(NotAllowedException.class, () -> {
            assetRepository.findAssetPath(
                    "page-id", "http://mycdnserver.myasset.js", AssetType.JAVASCRIPT);
        });
        assertThat(exception.getMessage())
                .isEqualTo("We can't load an external asset. Use the link http://mycdnserver.myasset.js");
    }

    @Test
    void should_throw_NullPointerException_when_find_asset_with_filename_null() {
        final NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            assetRepository.findAssetPath("page-id", null, AssetType.JAVASCRIPT);
        });
        assertThat(exception.getMessage()).isEqualTo("Filename is required");

    }

    @Test
    void should_throw_NullPointerException_when_find_asset_path_with_type_null() throws Exception {
        final NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            assetRepository.findAssetPath("page-id", "myfile.js", null);
        });
        assertThat(exception.getMessage()).contains("Asset type is required (filename: myfile.js)");
    }

    @Test
    void should_throw_NoSuchElementException_when_finding_inexistant_asset() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();
        Asset asset = AssetBuilder.aFilledAsset(page);
        createFile(pagesPath.resolve(asset.getName()));
        Mockito.lenient().when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        Mockito.when(pageRepository.get("page-id")).thenReturn(PageBuilder.aPage().withAsset(asset).build());

        assertThrows(NoSuchElementException.class,
                () -> assetRepository.findAssetPath("page-id", "inexistant.js", AssetType.JAVASCRIPT));
    }

    @Test
    void should_findAssetInPath_asset() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();
        write(pagesPath.resolve("file1.css"), "<style>.maclass1{}</style>".getBytes());
        write(pagesPath.resolve("file2.css"), "<style>.maclass2{}</style>".getBytes());

        List<Asset> assets = assetRepository.findAssetInPath(page, AssetType.CSS, pagesPath);

        assertThat(assets).hasSize(2);
        assertThat(assets).extracting("name").contains("file1.css", "file2.css");
    }

    @Test
    void should_findAssetInPath_asset_when_noone_is_present() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id").build();

        List<Asset> assets = assetRepository.findAssetInPath(page, AssetType.CSS, pagesPath);

        assertThat(assets).isEmpty();
    }

    @Test
    void should_refresh_component_assets_from_disk() throws Exception {
        Page page = PageBuilder.aPage().withId("page-id")
                .withAsset(AssetBuilder.anAsset().withName("existing-asset.js")).build();
        createDirectories(pagesPath.resolve("page-id/assets/css"));
        write(pagesPath.resolve("page-id/assets/css/file1.css"), "<style>.maclass1{}</style>".getBytes());
        write(pagesPath.resolve("page-id/assets/css/file2.css"), "<style>.maclass2{}</style>".getBytes());
        Mockito.when(pageRepository.resolvePath("page-id")).thenReturn(pagesPath.resolve("page-id"));

        assetRepository.refreshAssets(page);

        assertThat(page.getAssets()).hasSize(3);
        assertThat(page.getAssets()).extracting("name").contains("file1.css", "file2.css", "existing-asset.js");
        Mockito.verify(pageRepository).updateLastUpdateAndSave(page);
    }
}
