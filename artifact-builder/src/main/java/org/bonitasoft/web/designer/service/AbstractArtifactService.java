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

import org.bonitasoft.web.designer.common.migration.Version;
import org.bonitasoft.web.designer.common.repository.Repository;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.Identifiable;

public abstract class AbstractArtifactService<R extends Repository<T>, T extends Identifiable>
        implements ArtifactService<T> {

    protected UiDesignerProperties uiDesignerProperties;

    protected R repository;

    protected AbstractArtifactService(UiDesignerProperties uiDesignerProperties, R repository) {
        this.repository = repository;
        this.uiDesignerProperties = uiDesignerProperties;
    }

    @Override
    public void markAsFavorite(String id, boolean favorite) {
        if (favorite) {
            repository.markAsFavorite(id);
        } else {
            repository.unmarkAsFavorite(id);
        }
    }

    public ArtifactStatusReport getStatus(T artifact) {
        return getArtifactStatus(artifact);
    }

    private ArtifactStatusReport getArtifactStatus(T artifact) {
        // Check status of this artifact
        var artifactVersion = artifact.getArtifactVersion();
        if (artifactVersion == null) {
            return new ArtifactStatusReport(true, true);
        }

        var modelVersion = new Version(uiDesignerProperties.getModelVersion());

        var migration = modelVersion.isGreaterThan(artifactVersion);
        var compatible = modelVersion.isGreaterOrEqualThan(artifactVersion);

        return new ArtifactStatusReport(compatible, migration);
    }

    public ArtifactStatusReport mergeStatusReport(ArtifactStatusReport artifactReport,
            ArtifactStatusReport dependenciesReport) {

        var isCompatible = artifactReport.isCompatible() && dependenciesReport.isCompatible();
        var needMigration = isCompatible && (artifactReport.isMigration() || dependenciesReport.isMigration());

        return new ArtifactStatusReport(isCompatible, needMigration);
    }

    /**
     * Return status of artifact without checking dependencies
     *
     * @return ArtifactStatusReport
     */
    public ArtifactStatusReport getStatusWithoutDependencies(T artifact) {
        return getArtifactStatus(artifact);
    }

}
