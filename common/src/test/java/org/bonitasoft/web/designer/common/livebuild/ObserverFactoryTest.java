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

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.monitor.FileAlterationObserver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ObserverFactoryTest {

    private Path monitoredFolder;
    private ObserverFactory factory;
    private PathListenerStub listener;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        monitoredFolder = Files.createDirectory(tempDir.resolve("répertoire avec un nom b1z@re"));
        factory = new ObserverFactory();
        listener = new PathListenerStub();
    }

    @Test
    public void should_create_an_observer_that_notify_when_a_file_is_created() throws Exception {

        FileAlterationObserver observer = factory.create(monitoredFolder, listener);
        Path aFile = Files.write(monitoredFolder.resolve("aFile"), "coucou".getBytes());
        observer.checkAndNotify();

        Assertions.assertThat(listener.getChanged()).containsExactly(aFile);
    }

    @Test
    public void should_create_an_observer_that_notify_when_a_file_is_modified() throws Exception {
        Files.write(monitoredFolder.resolve("aFile"), "coucou".getBytes());

        FileAlterationObserver observer = factory.create(monitoredFolder, listener);
        Files.write(monitoredFolder.resolve("aFile"), "modified content".getBytes());
        observer.checkAndNotify();

        Assertions.assertThat(listener.getChanged()).containsExactly(monitoredFolder.resolve("aFile"));
    }

}
