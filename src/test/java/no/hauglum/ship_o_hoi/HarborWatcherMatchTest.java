package no.hauglum.ship_o_hoi;

import no.hauglum.ship_o_hoi.config.DestinationProperties;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.DestinationProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarborWatcherMatchTest {

    private static DestinationProfile activeProfile;

    @BeforeAll
    static void loadProfile() throws Exception {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources = loader.load("application.yml", new ClassPathResource("application.yml"));
        MutablePropertySources propertySources = new MutablePropertySources();
        sources.forEach(propertySources::addFirst);
        Binder binder = new Binder(ConfigurationPropertySources.from(propertySources));
        DestinationProperties props = binder.bind("destinations", DestinationProperties.class).get();
        activeProfile = props.resolveActive();
    }

    private boolean matches(String destination) throws Exception {
        AISShip ship = new AISShip("123", "TEST", 0.0, 0.0, null, null, null, destination, null);
        Method method = HarborWatcher.class.getDeclaredMethod("matchesDestination", AISShip.class, DestinationProfile.class);
        method.setAccessible(true);
        HarborWatcher watcher = new HarborWatcher(null, null, null, null);
        return (boolean) method.invoke(watcher, ship, activeProfile);
    }

    @Test
    void shouldMatchEngebøDestinations() throws Exception {
        assertThat(matches("ENGEBO")).isTrue();
        assertThat(matches("ENGEBO HAVN")).isTrue();
        assertThat(matches("ENGEBO KAI")).isTrue();
        assertThat(matches("Engebø")).isTrue();
        assertThat(matches("ENGEBØ RG FØRDE")).isTrue();
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
