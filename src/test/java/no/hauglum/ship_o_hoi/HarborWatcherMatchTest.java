package no.hauglum.ship_o_hoi;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.DestinationProfile;
import no.hauglum.ship_o_hoi.model.Position;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HarborWatcherMatchTest {

    private static final DestinationProfile ENGEBØ = new DestinationProfile(
            "Engebø",
            Set.of("engebo", "engebo hamn", "engebo kai", "engebohavn", "engebo havn"),
            new Position(61.487982, 5.442754)
    );

    private boolean matches(String destination) throws Exception {
        AISShip ship = new AISShip("123", "TEST", 0.0, 0.0, null, null, null, destination, null);
        Method method = HarborWatcher.class.getDeclaredMethod("matchesDestination", AISShip.class, DestinationProfile.class);
        method.setAccessible(true);
        HarborWatcher watcher = new HarborWatcher(null, null, null, null);
        return (boolean) method.invoke(watcher, ship, ENGEBØ);
    }

    @Test
    void shouldMatchEngebøDestinations() throws Exception {
        assertThat(matches("ENGEBO")).isTrue();
        assertThat(matches("ENGEBO HAVN")).isTrue();
        assertThat(matches("ENGEBO KAI")).isTrue();
        assertThat(matches("Engebø")).isTrue();
    }

    @Test
    void shouldNotFalsePositiveOnHavengebied() throws Exception {
        // Regression: "havengebied" embeds "engeb" as a substring — must not trigger
        assertThat(matches("HAVENGEBIED R'DAM")).isFalse();
        assertThat(matches("HAVENGEBIED")).isFalse();
    }

    @Test
    void shouldNotMatchUnrelatedDestinations() throws Exception {
        assertThat(matches("HAUGESUND")).isFalse();
        assertThat(matches("ROTTERDAM")).isFalse();
        assertThat(matches("BERGEN")).isFalse();
    }
}
