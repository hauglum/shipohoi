package no.hauglum.ship_o_hoi.parser;

import no.hauglum.ship_o_hoi.model.AISShip;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoJsonAISParserTest {

    private final GeoJsonAISParser parser = new GeoJsonAISParser();

    @Test
    void shouldParseGeoJsonLineToAISShip() {
        String json = """
            {"type":"Feature","geometry":{"type":"Point","coordinates":[16.909168,68.4016]},"properties":{"mmsi":257047350,"name":"OTTAR","msgtime":"2022-11-02T13:18:26+00:00","speedOverGround":null,"courseOverGround":null,"trueHeading":6,"destination":"HEKKELSTRAND"}}
            """;

        AISShip ship = parser.parseLine(json);

        assertThat(ship.mmsi()).isEqualTo("257047350");
        assertThat(ship.name()).isEqualTo("OTTAR");
        assertThat(ship.destination()).isEqualTo("HEKKELSTRAND");
        assertThat(ship.latitude()).isEqualTo(68.4016);
        assertThat(ship.longitude()).isEqualTo(16.909168);
        assertThat(ship.speed()).isNull();
        assertThat(ship.course()).isNull();
        assertThat(ship.heading()).isEqualTo(6);
    }
}
