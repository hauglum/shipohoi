package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleMapsServiceTest {

    private final GoogleMapsService service = new GoogleMapsService();

    @Test
    void generateMapUrl_createsCorrectUrl() {
        Position destination = new Position(59.4138, 5.2677);  // Haugesund
        Position ship = new Position(59.374913, 5.206227);     // Ship from log

        String url = service.generateMapUrl(destination, ship);

        System.out.println("Generated URL: " + url);
        
        assertTrue(url.contains("z=10"));
        assertTrue(url.contains("center=59.394357,5.236963"));
        assertTrue(url.contains("markers=59.4138,5.2677|59.374913,5.206227"));
    }
}
