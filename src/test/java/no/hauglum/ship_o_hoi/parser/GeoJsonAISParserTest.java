package no.hauglum.ship_o_hoi.parser;

import no.hauglum.ship_o_hoi.model.AISShip;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoJsonAISParserTest {

    private final GeoJsonAISParser parser = new GeoJsonAISParser();

    @Test
    void shouldParseGeoJsonLineToAISShip() {
        String json = """
            {"type":"Feature","geometry":{"type":"Point","coordinates":[59.4138,5.2677]},"properties":{"mmsi":257047350,"name":"OTTAR","msgtime":"2022-11-02T13:18:26+00:00","speedOverGround":null,"courseOverGround":null,"trueHeading":6,"destination":"HAUGESUND"}}
            """;

        AISShip ship = parser.parseLine(json);

        assertThat(ship.mmsi()).isEqualTo("257047350");
        assertThat(ship.name()).isEqualTo("OTTAR");
        assertThat(ship.destination()).isEqualTo("HAUGESUND");
        assertThat(ship.latitude()).isEqualTo(5.2677);
        assertThat(ship.longitude()).isEqualTo(59.4138);
        assertThat(ship.speed()).isNull();
        assertThat(ship.course()).isNull();
        assertThat(ship.heading()).isEqualTo(6);
    }
}
