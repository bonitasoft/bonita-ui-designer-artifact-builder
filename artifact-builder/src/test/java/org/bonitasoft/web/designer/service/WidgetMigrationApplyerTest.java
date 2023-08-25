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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WidgetMigrationApplyerTest {

    @Test
    void should_migrate_a_widget() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Optional<MigrationStepReport> stepReport = Optional
                .of(new MigrationStepReport(MigrationStatus.SUCCESS, "customWidget"));
        Migration<Widget> migrations = new Migration("2.0", mockMigrationStep);
        WidgetMigrationApplyer widgetMigrationApplyer = new WidgetMigrationApplyer(
                Collections.singletonList(migrations));
        Widget widget = WidgetBuilder.aWidget().withId("customWidget").designerVersion("1.0.1")
                .previousDesignerVersion("1.0.0").build();
        when(mockMigrationStep.migrate(widget)).thenReturn(stepReport);

        widgetMigrationApplyer.migrate(widget);

        assertEquals("1.0.1", widget.getPreviousArtifactVersion());
        assertEquals("2.0", widget.getArtifactVersion());
    }

    @Test
    void should_migrate_a_widget_with_new_model_version() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Optional<MigrationStepReport> stepReport = Optional
                .of(new MigrationStepReport(MigrationStatus.SUCCESS, "customWidget"));
        Migration<Widget> migrations = new Migration("2.1", mockMigrationStep);
        WidgetMigrationApplyer widgetMigrationApplyer = new WidgetMigrationApplyer(
                Collections.singletonList(migrations));
        Widget widget = WidgetBuilder.aWidget().withId("customWidget").modelVersion("2.0")
                .previousDesignerVersion("1.7.11").build();
        when(mockMigrationStep.migrate(widget)).thenReturn(stepReport);

        widgetMigrationApplyer.migrate(widget);

        assertEquals("2.0", widget.getPreviousArtifactVersion());
        assertEquals("2.1", widget.getArtifactVersion());
    }

    @Test
    void should_migrate_a_widget_with_no_previous_version() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Optional<MigrationStepReport> stepReport = Optional
                .of(new MigrationStepReport(MigrationStatus.SUCCESS, "customWidget"));
        Migration<Widget> migrations = new Migration("2.0", mockMigrationStep);
        WidgetMigrationApplyer widgetMigrationApplyer = new WidgetMigrationApplyer(
                Collections.singletonList(migrations));
        Widget widget = WidgetBuilder.aWidget().withId("customWidget").designerVersion("1.0.1").build();
        when(mockMigrationStep.migrate(widget)).thenReturn(stepReport);

        widgetMigrationApplyer.migrate(widget);

        assertEquals("1.0.1", widget.getPreviousArtifactVersion());
        assertEquals("2.0", widget.getArtifactVersion());
    }

    @Test
    void should_not_modify_previous_model_version_when_no_migration_done_on_widget() {
        Migration<Widget> migrations = new Migration("2.0", mock(MigrationStep.class));
        WidgetMigrationApplyer widgetMigrationApplyer = new WidgetMigrationApplyer(
                Collections.singletonList(migrations));
        Widget widget = WidgetBuilder.aWidget().withId("customWidget").modelVersion("2.0")
                .previousArtifactVersion("2.0").build();

        widgetMigrationApplyer.migrate(widget);

        assertEquals("2.0", widget.getPreviousArtifactVersion());
        assertEquals("2.0", widget.getArtifactVersion());
    }

    @Test
    void should_return_an_report_with_error_when_error_occurs_during_widget_migration() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Widget> migrations = new Migration("2.1", mockMigrationStep);
        WidgetMigrationApplyer widgetMigrationApplyer = new WidgetMigrationApplyer(
                Collections.singletonList(migrations));
        Widget widget = WidgetBuilder.aWidget().withId("customWidget").modelVersion("2.0")
                .previousArtifactVersion("1.0.1").build();
        when(mockMigrationStep.migrate(widget)).thenThrow(new Exception());

        MigrationResult result = widgetMigrationApplyer.migrate(widget);

        Widget migratedWidget = (Widget) result.getArtifact();
        assertEquals("2.0", migratedWidget.getPreviousArtifactVersion());
        assertEquals("2.1", migratedWidget.getArtifactVersion());
        MigrationStepReport report = (MigrationStepReport) result.getMigrationStepReportList().get(0);
        assertEquals(MigrationStatus.ERROR, report.getMigrationStatus());
        assertEquals("customWidget", report.getArtifactId());
    }

}
