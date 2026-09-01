package no.hauglum.ship_o_hoi.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.hauglum.ship_o_hoi.model.AISShip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AisStreamParser {

    private static final Logger log = LoggerFactory.getLogger(AisStreamParser.class);
    private static final int LOG_SNIPPET_LENGTH = 300;

    private final ObjectMapper mapper = new ObjectMapper();

    public AISShip parseLine(String json) {
        try {
            JsonNode root = mapper.readTree(json);

            if (!"ShipStaticData".equals(root.path("MessageType").asText())) {
                log.warn("Ignoring AISStream message without ShipStaticData: {}", abbreviate(json));
                return null;
            }

            JsonNode meta = root.path("MetaData");
            JsonNode msg = root.path("Message").path("ShipStaticData");

            String mmsi = meta.path("MMSI_String").asText(null);
            if (mmsi == null) {
                mmsi = String.valueOf(meta.path("MMSI").asLong());
            }

            String name = trimOrNull(meta.path("ShipName").asText(null));
            double latitude = meta.path("latitude").asDouble();
            double longitude = meta.path("longitude").asDouble();
            String destination = trimOrNull(msg.path("Destination").asText(null));

            return new AISShip(mmsi, name, latitude, longitude, null, null, null, destination, null);

        } catch (Exception e) {
            log.warn("Failed to parse AISStream message: {} ({})", abbreviate(json), e.getMessage());
            return null;
        }
    }

    private String abbreviate(String s) {
        if (s == null) return null;
        return s.length() <= LOG_SNIPPET_LENGTH ? s : s.substring(0, LOG_SNIPPET_LENGTH) + "…";
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
