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

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.migration.AbstractMigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageUUIDMigrationStep extends AbstractMigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(PageUUIDMigrationStep.class);

    @Override
    public Optional<MigrationStepReport> migrate(Page page) throws Exception {
        if (StringUtils.isEmpty(page.getUUID())) {
            String uuid;
            try {
                UUID.fromString(page.getId());
                logger.info("[MIGRATION] Adding UUID to page [{}] (using the same value as the page ID)",
                        page.getName());
                uuid = page.getId();
            } catch (IllegalArgumentException e) {
                //The page ID is not a UUID - generating one
                logger.info("[MIGRATION] Adding generated UUID to page [{}]", page.getName());
                uuid = UUID.randomUUID().toString();
            }
            page.setUUID(uuid);
        }
        return Optional.empty();
    }
}
