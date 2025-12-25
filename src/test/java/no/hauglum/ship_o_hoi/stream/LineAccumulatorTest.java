package no.hauglum.ship_o_hoi.stream;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LineAccumulatorTest {

    @Test
    void shouldReassembleSplitJsonLines() {
        LineAccumulator acc = new LineAccumulator();

        List<String> first = acc.append("{\"a\":1,\"b\":");
        assertThat(first).isEmpty();

        List<String> second = acc.append("2}\n{\"c\":3}\n");
        assertThat(second).containsExactly(
                "{\"a\":1,\"b\":2}",
                "{\"c\":3}"
        );
    }

}