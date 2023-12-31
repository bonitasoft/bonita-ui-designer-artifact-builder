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

import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.model.asset.AssetType.CSS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StyleAddModalContainerPropertiesMigrationStepTest {

    @Mock
    private AssetService<Page> pageAssetService;

    @InjectMocks
    private StyleAddModalContainerPropertiesMigrationStep step;

    @BeforeEach
    void setUp() throws Exception {
        step = new StyleAddModalContainerPropertiesMigrationStep(pageAssetService);
    }

    private Asset expectedAsset(String name) {
        return anAsset().withType(CSS).withName(name).build();
    }

    @Test
    void should_migrate_style_asset_to_add_new_modal_container_css_classes() throws Exception {
        Asset style = anAsset().withType(CSS).withName("style.css").build();
        String initContent = ".my-content {background: red}";
        Page page = aPage()
                .withDesignerVersion("1.7.9").withAsset(style).build();

        when(pageAssetService.getAssetContent(page, style)).thenReturn(initContent);
        InputStream is = getClass()
                .getResourceAsStream("/templates/migration/assets/css/styleAddModalContainerProperties.css");

        step.migrate(page);

        String expectedContent = initContent.concat("\n\n").concat(IOUtils.toString(is));
        verify(pageAssetService).save(page, expectedAsset("style.css"), expectedContent.getBytes());
    }

}
