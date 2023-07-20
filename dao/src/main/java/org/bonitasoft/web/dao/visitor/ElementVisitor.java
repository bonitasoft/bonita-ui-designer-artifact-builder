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
package org.bonitasoft.web.dao.visitor;

import org.bonitasoft.web.dao.model.Identifiable;
import org.bonitasoft.web.dao.model.page.*;

/**
 * A visitor used to visit all the element of a component
 *
 * @author JB Nizet
 */
public interface ElementVisitor<T> {

    T visit(Container container);

    T visit(FormContainer formContainer);

    T visit(TabsContainer tabsContainer);

    T visit(TabContainer tabContainer);

    T visit(ModalContainer modalContainer);

    T visit(Component component);

    T visit(FragmentElement fragmentElement);

    <P extends Previewable & Identifiable> T visit(P previewable);
}
