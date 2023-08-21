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
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WatcherTest {

    private final static long POLLING_DELAY = 10;

    private static FileAlterationMonitor monitor = new FileAlterationMonitor(POLLING_DELAY);

    private Watcher watcher;
    private Path subDirectory;

    @TempDir
    Path folder;

    @BeforeAll
    public static void startMonitor() throws Exception {
        monitor.start();
    }

    @AfterAll
    public static void stopMonitor() throws Exception {
        monitor.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        subDirectory = Files.createDirectory(folder.resolve("un répertoire"));
        watcher = new Watcher(new ObserverFactory(), monitor);
    }

    @Test
    void should_trigger_a_created_event_when_a_file_is_created() throws Exception {
        PathListenerStub listener = new PathListenerStub();
        watcher.watch(folder, listener);

        Path file = Files.createFile(subDirectory.resolve("file"));

        Awaitility.await().until(changedFilesContainsExactly(listener, file));
    }

    @Test
    void should_trigger_a_modified_event_when_a_file_is_modified() throws Exception {
        Path existingFile = Files.createFile(subDirectory.resolve("file"));
        PathListenerStub listener = new PathListenerStub();
        watcher.watch(folder, listener);

        Files.write(existingFile, "hello".getBytes(), StandardOpenOption.APPEND);

        Awaitility.await().until(changedFilesContainsExactly(listener, existingFile));
    }

    private Callable<Boolean> changedFilesContainsExactly(PathListenerStub listener, Path expectedFile) {
        return () -> {
            Set<Path> changed = listener.getChanged();
            return changed.size() == 1
                    && changed.contains(expectedFile);
        };
    }

}
