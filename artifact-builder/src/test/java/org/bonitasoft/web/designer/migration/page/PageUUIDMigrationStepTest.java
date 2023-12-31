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
package org.bonitasoft.web.designer.migration.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import java.util.UUID;

import org.bonitasoft.web.designer.model.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PageUUIDMigrationStepTest {

    PageUUIDMigrationStep pageUUIDMigrationStep;

    @BeforeEach
    void setUp() throws Exception {
        pageUUIDMigrationStep = new PageUUIDMigrationStep();
    }

    @Test
    void should_migrate_page_with_UUID_like_id() throws Exception {
        Page pageWithoutUUID = aPage().withId(UUID.randomUUID().toString()).withUUID(null).build();

        pageUUIDMigrationStep.migrate(pageWithoutUUID);

        assertThat(pageWithoutUUID.getUUID()).isEqualTo(pageWithoutUUID.getId());
    }

    @Test
    void should_migrate_page_generating_a_UUID() throws Exception {
        Page pageWithoutUUID = aPage().withId("maPage").withUUID(null).build();

        pageUUIDMigrationStep.migrate(pageWithoutUUID);

        assertThat(pageWithoutUUID.getUUID()).isNotNull();
    }

    @Test
    void should_not_migrate_a_page_with_already_a_uuid() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Page pageWithUUID = aPage().withId("maPage").withUUID(uuid).build();

        pageUUIDMigrationStep.migrate(pageWithUUID);

        assertThat(pageWithUUID.getUUID()).isEqualTo(uuid);
    }

}
