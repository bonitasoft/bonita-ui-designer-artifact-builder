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
package org.bonitasoft.web.designer.model.migrationReport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class MigrationResultTest {

    MigrationResult<?> migrationResult;

    @Test
    void should_return_only_Warning_status_report_when_the_most_severe_error_is_warning() {
        List<MigrationStepReport> migrationList = Arrays.asList(
                new MigrationStepReport(MigrationStatus.SUCCESS, "myPage", "First step of migration", "MigrationOne"),
                new MigrationStepReport(MigrationStatus.SUCCESS, "myPage", "Second step of migration", "MigrationTwo"),
                new MigrationStepReport(MigrationStatus.WARNING, "myPage", "Third step of migration", "MigrationTree"));

        this.migrationResult = new MigrationResult<>(new Object(), migrationList);

        assertEquals(MigrationStatus.WARNING, this.migrationResult.getFinalStatus());
        assertEquals(3, this.migrationResult.getMigrationStepReportList().size());
        assertEquals(1, this.migrationResult.getMigrationStepReportListFilterByFinalStatus().size());
    }

    @Test
    void should_return_an_empty_list_when_all_step_is_finish_on_success() {
        List<MigrationStepReport> migrationList = Arrays.asList(
                new MigrationStepReport(MigrationStatus.SUCCESS, "myPage", "First step of migration", "MigrationOne"),
                new MigrationStepReport(MigrationStatus.SUCCESS, "myPage", "Second step of migration", "MigrationTwo"),
                new MigrationStepReport(MigrationStatus.SUCCESS, "myPage", "Third step of migration", "MigrationTree"));

        this.migrationResult = new MigrationResult<>(new Object(), migrationList);

        assertEquals(MigrationStatus.SUCCESS, this.migrationResult.getFinalStatus());
        assertEquals(3, this.migrationResult.getMigrationStepReportList().size());
        assertEquals(0, this.migrationResult.getMigrationStepReportListFilterByFinalStatus().size());
    }

    @Test
    void should_return_error_final_status_when_one_step_is_on_error() {
        List<MigrationStepReport> migrationList = Arrays.asList(
                new MigrationStepReport(MigrationStatus.SUCCESS, "myPage", "First step of migration", "MigrationOne"),
                new MigrationStepReport(MigrationStatus.ERROR, "myPage", "Second step of migration", "MigrationTwo"),
                new MigrationStepReport(MigrationStatus.ERROR, "myPage", "Third step of migration", "MigrationTree"));

        this.migrationResult = new MigrationResult<>(new Object(), migrationList);

        assertEquals(MigrationStatus.ERROR, this.migrationResult.getFinalStatus());
        assertEquals(3, this.migrationResult.getMigrationStepReportList().size());
        assertEquals(2, this.migrationResult.getMigrationStepReportListFilterByFinalStatus().size());
    }
}
