package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.Position;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class EmailServiceIT {

    @Autowired
    private EmailService emailService;

    @Test
    void shouldSendAlertEmailWithLinks() {
        AISShip ship = new AISShip(
                "123456789",
                "MV TEST VESSEL",
                61.1792,   // latitude  — near Engebø
                5.9314,    // longitude
                12.5,      // speed knots
                270.0,     // course
                null,
                "ENGEBO",
                null
        );
        Position engebot = new Position(61.1792, 5.9314);

        emailService.sendShipAlert(ship, "Engebø", engebot);

        System.out.println("✅ Email sent — check your inbox for the HTML alert with MarineTraffic and OSM links");
    }
}
