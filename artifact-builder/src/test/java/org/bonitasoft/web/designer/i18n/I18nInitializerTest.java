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
package org.bonitasoft.web.designer.i18n;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bonitasoft.web.angularjs.GeneratorProperties;
import org.bonitasoft.web.designer.workspace.ResourcesCopier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class I18nInitializerTest {

    @Mock
    private GeneratorProperties generatorProperties;
    @Mock
    private ResourcesCopier resourcesCopier;
    @Mock
    private LanguagePackBuilder languagePackBuilder;

    @InjectMocks
    private I18nInitializer i18nInitializer;

    @BeforeEach
    void setUp() throws Exception {
        when(generatorProperties.getExtractPath()).thenReturn(Paths.get("target/test-classes"));
    }

    @Test
    void should_start_live_build_on_po_directory() throws Exception {

        i18nInitializer.initialize();

        verify(languagePackBuilder).start(eq(generatorProperties.getExtractPath().resolve("i18n")));
    }

    @Test
    void should_throw_a_runtime_exception_on_io_error() throws Exception {
        doThrow(new IOException()).when(languagePackBuilder).start(any(Path.class));

        assertThrows(RuntimeException.class, () -> i18nInitializer.initialize());
    }
}
