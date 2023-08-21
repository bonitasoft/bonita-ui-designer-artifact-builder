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
        return Stream.of(Arguments.of("Hello 'romain' and 'benjamin'", "Hello \\\\'romain\\\\' and \\\\'benjamin\\\\'"),
                Arguments.of("Hello \n romain", "Hello \\n romain"),
                Arguments.of("Hello \r\n romain", "Hello \\n romain"));
    }

}
