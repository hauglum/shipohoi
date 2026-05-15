package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class AisStreamFuzhouIT {

    private static final List<String> FUZHOU_ALIASES = List.of("fuzhou", "cnfuz", "cn fuz", "mawei", "foochow");

    @Autowired
    private AisStreamService aisStreamService;

    @Test
    void shouldDetectFuzhouBoundShipsFromGlobalStream() {
        List<AISShip> ships = aisStreamService.streamShips()
                .filter(ship -> ship.destination() != null)
                .take(Duration.ofSeconds(30))
                .collectList()
                .block(Duration.ofSeconds(35));

        assertThat(ships).isNotNull().isNotEmpty();

        List<AISShip> fuzhouShips = ships.stream()
                .filter(s -> {
                    String dest = s.destination().toLowerCase();
                    return FUZHOU_ALIASES.stream().anyMatch(dest::contains);
                })
                .toList();

        if (fuzhouShips.isEmpty()) {
            System.out.println("No Fuzhou-bound ships broadcasting right now — " + ships.size() + " ships scanned.");
        } else {
            fuzhouShips.forEach(s -> {
                System.out.println("🇨🇳 Fuzhou-bound ship: " + s);
                assertThat(s.mmsi()).isNotBlank();
                assertThat(s.latitude()).isBetween(-90.0, 90.0);
                assertThat(s.longitude()).isBetween(-180.0, 180.0);
            });
        }
    }
}
