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
package org.bonitasoft.web.designer.model.asset;

import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widgets.Widget;

public abstract class AssetScope {

    public static final String WIDGET = "widget";
    public static final String PAGE = "page";

    private AssetScope() {
        // Use factory method instead
    }

    public static String forComponent(Assetable assetable) {
        if (assetable instanceof Page) {
            return PAGE;
        }
        if (assetable instanceof Widget) {
            return WIDGET;
        }
        throw new IllegalArgumentException("Unknown assetable type");
    }
}
