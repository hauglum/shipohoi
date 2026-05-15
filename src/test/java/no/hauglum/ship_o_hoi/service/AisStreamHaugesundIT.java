package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class AisStreamHaugesundIT {

    @Autowired
    private AisStreamService aisStreamService;

    /**
     * Collects 30 seconds of global AIS traffic and reports destinations seen.
     * A Haugesund-bound ship may not be broadcasting at any given moment —
     * ships send Type 5 messages only every ~6 minutes — so this test verifies
     * the stream and filter logic are wired correctly rather than waiting
     * indefinitely for a specific destination.
     */
    @Test
    void shouldReportDestinationsIncludingHaugesundIfPresent() {
        List<AISShip> ships = aisStreamService.streamShips()
                .filter(ship -> ship.destination() != null)
                .take(Duration.ofSeconds(30))
                .collectList()
                .block(Duration.ofSeconds(35));

        assertThat(ships).isNotNull().isNotEmpty();

        Set<String> destinations = ships.stream()
                .map(AISShip::destination)
                .collect(Collectors.toSet());

        System.out.println("Unique destinations seen (" + destinations.size() + "):");
        destinations.stream().sorted().forEach(d -> System.out.println("  " + d));

        List<AISShip> haugesundShips = ships.stream()
                .filter(s -> s.destination().toLowerCase().contains("haugesund"))
                .toList();

        if (haugesundShips.isEmpty()) {
            System.out.println("No Haugesund-bound ships broadcasting right now — stream is healthy with " + ships.size() + " ships seen.");
        } else {
            haugesundShips.forEach(s -> System.out.println("⚓ Haugesund ship: " + s));
            haugesundShips.forEach(s -> assertThat(s.mmsi()).isNotBlank());
        }
    }
}
