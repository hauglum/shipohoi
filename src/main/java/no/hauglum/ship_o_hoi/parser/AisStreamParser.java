package no.hauglum.ship_o_hoi.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.hauglum.ship_o_hoi.model.AISShip;

public class AisStreamParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public AISShip parseLine(String json) {
        try {
            JsonNode root = mapper.readTree(json);

            if (!"ShipStaticData".equals(root.path("MessageType").asText())) {
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
            return null;
        }
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
