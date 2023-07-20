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

import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.dao.model.Assetable;
import org.bonitasoft.web.dao.model.Identifiable;
import org.bonitasoft.web.dao.model.asset.Asset;
import org.bonitasoft.web.dao.model.asset.AssetType;
import org.bonitasoft.web.designer.controller.asset.AssetService.OrderType;

public interface AssetableArtifactService<T extends Identifiable & Assetable> extends ArtifactService<T> {

    Asset saveAsset(String id, Asset asset);

    Asset saveOrUpdateAsset(String id, AssetType assetType, String fileName, byte[] fileContent);

    Asset changeAssetOrder(String id, String assetId, OrderType orderType);

    void changeAssetStateInPreviewable(String id, String assetId, boolean active);

    void deleteAsset(String id, String assetId);

    Path findAssetPath(String id, String filename, String type) throws IOException;

}
