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
package org.bonitasoft.web.designer.common.livebuild;

import java.nio.file.Path;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class Watcher {

    private final ObserverFactory observerFactory;
    private final FileAlterationMonitor monitor;

    public Watcher(ObserverFactory observerFactory, FileAlterationMonitor monitor) {
        this.observerFactory = observerFactory;
        this.monitor = monitor;
    }

    public void watch(Path path, final PathListener listener) {
        FileAlterationObserver observer = observerFactory.create(path, listener);
        monitor.addObserver(observer);
    }

}
