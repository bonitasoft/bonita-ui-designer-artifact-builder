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
package org.bonitasoft.web.designer.builder;

import java.util.Map;

import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.Page;

public class PageWithFragmentBuilder {

    /**
     * Create a filled page with a value for all fields
     */
    public static Page aPageWithFragmentElement() throws Exception {
        FragmentElement fragment = new FragmentElement();
        fragment.setId("a-fragment");
        fragment.setDimension(Map.of("md", 8));
        Container fragmentContainer = ContainerBuilder.aContainer().with(fragment).build();

        return PageBuilder.aPage()
                .withId("UUID")
                .withName("myPage")
                .with(fragmentContainer)
                .withVariable("aVariable", VariableBuilder.aConstantVariable().value("a value"))
                .build();
    }
}
