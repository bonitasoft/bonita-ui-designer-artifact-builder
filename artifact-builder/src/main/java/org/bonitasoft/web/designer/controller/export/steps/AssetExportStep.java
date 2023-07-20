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
package org.bonitasoft.web.designer.controller.export.steps;

import static org.bonitasoft.web.dao.export.Zipper.ALL_DIRECTORIES;
import static org.bonitasoft.web.dao.export.Zipper.ALL_FILES;

import java.io.IOException;

import org.bonitasoft.web.dao.export.ExportStep;
import org.bonitasoft.web.dao.model.asset.Asset;
import org.bonitasoft.web.dao.model.page.Page;
import org.bonitasoft.web.dao.export.Zipper;
import org.bonitasoft.web.dao.repository.AssetRepository;

public class AssetExportStep implements ExportStep<Page> {

    private final AssetRepository<Page> pageAssetRepository;

    public AssetExportStep(AssetRepository<Page> pageAssetRepository) {
        this.pageAssetRepository = pageAssetRepository;
    }

    @Override
    public void execute(Zipper zipper, Page page) throws IOException {
        for (Asset pageAsset : page.getAssets()) {
            if (!pageAsset.isExternal()) {
                zipper.addDirectoryToZip(
                        pageAssetRepository.findAssetPath(page.getId(), pageAsset.getName(), pageAsset.getType()),
                        ALL_DIRECTORIES, ALL_FILES,
                        String.format("%s/assets/%s/%s", RESOURCES, pageAsset.getType().getPrefix(),
                                pageAsset.getName()));
            }
        }

    }
}
