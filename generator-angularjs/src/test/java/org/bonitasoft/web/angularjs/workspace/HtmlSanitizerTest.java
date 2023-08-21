/** 
 * Copyright (C) 2023 BonitaSoft S.A.
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
package org.bonitasoft.web.angularjs.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HtmlSanitizerTest {

    @ParameterizedTest
    @MethodSource("stringsToEscape")
    void escapeSingleQuotesAndNewLines(String input, String expected) throws Exception {
        var sanitizer = new HtmlSanitizer();

        var escapedString = sanitizer.escapeSingleQuotesAndNewLines(input);

        assertThat(escapedString).isEqualTo(expected);
    }

    private static Stream<Arguments> stringsToEscape() {
        return Stream.of(Arguments.of("Hello 'romain' and 'benjamin'", "Hello \\'romain\\' and \\'benjamin\\'"),
                Arguments.of("Hello \n romain", "Hello \\n romain"),
                Arguments.of("Hello \r\n romain", "Hello \\n romain"));
    }

}
