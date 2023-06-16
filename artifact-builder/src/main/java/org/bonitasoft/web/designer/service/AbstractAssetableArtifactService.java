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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.AssetService.OrderType;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.repository.Repository;

public abstract class AbstractAssetableArtifactService<R extends Repository<T>, T extends Identifiable & Assetable>
        extends AbstractArtifactService<R, T> implements AssetableArtifactService<T> {

    protected AssetService<T> assetService;

    protected AbstractAssetableArtifactService(UiDesignerProperties uiDesignerProperties, AssetService<T> assetService,
            R repository) {
        super(uiDesignerProperties, repository);
        this.assetService = assetService;
    }

    @Override
    public Asset saveAsset(String id, Asset asset) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        return assetService.save(identifiable, asset);
    }

    @Override
    public Asset saveOrUpdateAsset(String id, AssetType assetType, String fileName, byte[] fileContent) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        var asset = new Asset()
                .setName(fileName)
                .setType(assetType)
                .setOrder(identifiable.getNextAssetOrder());

        identifiable.getAssets().stream()
                .filter(asset::equalsWithoutComponentId).findFirst()
                .ifPresent(existingAsset -> asset.setId(existingAsset.getId()));

        return assetService.save(identifiable, asset, fileContent);
    }

    @Override
    public Asset changeAssetOrder(String id, String assetId, OrderType orderType) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        return assetService.changeAssetOrderInComponent(identifiable, assetId, orderType);
    }

    @Override
    public void changeAssetStateInPreviewable(String id, String assetId, boolean active) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        assetService.changeAssetStateInPreviewable(identifiable, assetId, active);
    }

    @Override
    public void deleteAsset(String id, String assetId) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        assetService.delete(identifiable, assetId);
    }

    @Override
    public Path findAssetPath(String id, String filename, String type) throws IOException {
        return assetService.findAssetPath(id, filename, type);
    }

    protected void checkUpdatable(String id) {
        requireNonNull(id, "id is null");
    }
}
