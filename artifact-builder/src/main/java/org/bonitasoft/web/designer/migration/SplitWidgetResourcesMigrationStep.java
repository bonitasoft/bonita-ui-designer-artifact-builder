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

import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplitWidgetResourcesMigrationStep extends AbstractMigrationStep<Widget> {

    private static final Logger logger = LoggerFactory.getLogger(SplitWidgetResourcesMigrationStep.class);

    @Override
    public Optional<MigrationStepReport> migrate(Widget widget) {
        // This migration step is only needed to update the UID version so that the save is performed
        // (template and controller are extracted in the widget repository save)
        logger.info("[MIGRATION] Splitting controller and template into separate files for widget [{}]",
                widget.getName());
        return Optional.of(MigrationStepReport.warningMigrationReport(widget.getId(),
                "Splitting controller and template into separate files for widget", this.getClass().getName()));
    }
}
