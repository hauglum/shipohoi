package no.hauglum.ship_o_hoi.stream;

import java.util.ArrayList;
import java.util.List;

public class LineAccumulator {

    private final StringBuilder buffer = new StringBuilder();

    /**
     * @return complete lines (no newline)
     */
    public List<String> append(String chunk) {
        buffer.append(chunk);

        List<String> lines = new ArrayList<>();
        int index;

        while ((index = buffer.indexOf("\n")) >= 0) {
            lines.add(buffer.substring(0, index).trim());
            buffer.delete(0, index + 1);
        }

        return lines;
    }
}
