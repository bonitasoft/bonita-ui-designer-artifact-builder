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

import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FragmentMigrationApplyerTest {

    @Mock
    private WidgetService widgetService;

    @Test
    void should_migrate_a_fragment() throws Exception {
        var mockMigrationStep = mock(MigrationStep.class);
        Migration<Fragment> migration = new Migration("2.0", mockMigrationStep);
        FragmentMigrationApplyer migrationApplyer = new FragmentMigrationApplyer(Collections.singletonList(migration),
                widgetService);
        Fragment fragment = aFragment().withId("myFragment").withDesignerVersion("1.0.0")
                .withPreviousDesignerVersion("1.0.0").build();
        when(mockMigrationStep.migrate(fragment)).thenReturn(Optional.empty());

        migrationApplyer.migrate(fragment, false);

        assertEquals("1.0.0", fragment.getPreviousArtifactVersion());
        assertEquals("2.0", fragment.getArtifactVersion());
        verify(widgetService, never()).migrateAllCustomWidgetUsedInPreviewable(fragment);
    }

    @Test
    void should_migrate_a_fragment_with_new_model_version() throws Exception {
        var mockMigrationStep = mock(MigrationStep.class);
        Migration<Fragment> migration = new Migration("2.1", mockMigrationStep);
        FragmentMigrationApplyer migrationApplyer = new FragmentMigrationApplyer(Collections.singletonList(migration),
                widgetService);
        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.0")
                .withPreviousDesignerVersion("1.7.11").build();
        when(mockMigrationStep.migrate(fragment)).thenReturn(Optional.empty());

        migrationApplyer.migrate(fragment, false);

        assertEquals("2.0", fragment.getPreviousArtifactVersion());
        assertEquals("2.1", fragment.getArtifactVersion());
        verify(widgetService, never()).migrateAllCustomWidgetUsedInPreviewable(fragment);
    }

    @Test
    void should_not_migrate_a_fragment_when_its_already_in_good_version() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Fragment> migration = new Migration("2.0", mock(MigrationStep.class));
        FragmentMigrationApplyer migrationApplyer = new FragmentMigrationApplyer(Collections.singletonList(migration),
                widgetService);

        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.0").withPreviousArtifactVersion("2.0")
                .build();
        lenient().when(mockMigrationStep.migrate(fragment)).thenReturn(Optional.empty());

        migrationApplyer.migrate(fragment, false);

        assertEquals("2.0", fragment.getPreviousArtifactVersion());
        assertEquals("2.0", fragment.getArtifactVersion());
        verify(widgetService, never()).migrateAllCustomWidgetUsedInPreviewable(fragment);
    }

    @Test
    void should_migrate_all_widgets_use_in_fragment_when_fragment_is_migrated() throws Exception {
        var mockMigrationStep = mock(MigrationStep.class);
        Migration<Fragment> migration = new Migration<>("2.0", mockMigrationStep);
        FragmentMigrationApplyer migrationApplyer = new FragmentMigrationApplyer(Collections.singletonList(migration),
                widgetService);

        Fragment fragment = aFragment().withId("myFragment").withDesignerVersion("1.0.0")
                .withPreviousDesignerVersion("1.0.0").build();
        when(mockMigrationStep.migrate(fragment)).thenReturn(Optional.empty());

        migrationApplyer.migrate(fragment, true);

        assertEquals("1.0.0", fragment.getPreviousArtifactVersion());
        assertEquals("2.0", fragment.getArtifactVersion());
        verify(widgetService).migrateAllCustomWidgetUsedInPreviewable(fragment);

    }

}
