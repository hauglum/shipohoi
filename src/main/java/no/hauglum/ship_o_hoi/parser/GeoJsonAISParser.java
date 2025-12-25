package no.hauglum.ship_o_hoi.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.hauglum.ship_o_hoi.model.AISShip;

import java.time.OffsetDateTime;

public class GeoJsonAISParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public AISShip parseLine(String jsonLine) {
        try {
            JsonNode root = mapper.readTree(jsonLine);

            JsonNode geometry = root.path("geometry");
            JsonNode properties = root.path("properties");

            double longitude = geometry.path("coordinates").get(0).asDouble();
            double latitude = geometry.path("coordinates").get(1).asDouble();

            return new AISShip(
                    properties.path("mmsi").asText(),
                    properties.path("name").asText(null),
                    latitude,
                    longitude,
                    properties.path("speedOverGround").isNull() ? null : properties.path("speedOverGround").asDouble(),
                    properties.path("courseOverGround").isNull() ? null : properties.path("courseOverGround").asDouble(),
                    properties.path("trueHeading").isNull() ? null : properties.path("trueHeading").asInt(),
                    properties.path("destination").asText(null),
                    properties.path("eta").asText(null)
            );

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid GeoJSON line", e);
        }
    }
}

