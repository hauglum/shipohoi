package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class AisStreamServiceIT {

    @Autowired
    private AisStreamService aisStreamService;

    @Test
    void shouldReceiveAndParseShipFromGlobalStream() {
        StepVerifier.create(
                aisStreamService.streamShips().take(1)
        )
        .assertNext(ship -> {
            System.out.println("🌍 Received from AISStream: " + ship);
            assertThat(ship.mmsi()).isNotBlank();
            assertThat(ship.latitude()).isBetween(-90.0, 90.0);
            assertThat(ship.longitude()).isBetween(-180.0, 180.0);
        })
        .expectComplete()
        .verify(Duration.ofSeconds(30));
    }
}
