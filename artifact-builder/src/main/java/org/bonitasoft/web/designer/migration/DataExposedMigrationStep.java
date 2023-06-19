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

import java.util.Optional;

import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataExposedMigrationStep<T extends Fragment> extends AbstractMigrationStep<T> {

    private static final Logger logger = LoggerFactory.getLogger(DataExposedMigrationStep.class);

    @Override
    public Optional<MigrationStepReport> migrate(T artifact) {
        artifact.getVariables().values().stream()
                .filter(Variable::isExposed)
                .forEach(variable -> {
                    variable.setType(DataType.CONSTANT);
                    variable.setDisplayValue("");
                });

        logger.info("[MIGRATION] Set type to constant for each exposed data into fragments [{}]", artifact.getName());
        return Optional.empty();
    }
}
