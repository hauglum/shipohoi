package no.hauglum.ship_o_hoi;

import no.hauglum.ship_o_hoi.model.DestinationProfile;
import no.hauglum.ship_o_hoi.service.BarentsWatchAISService;
import no.hauglum.ship_o_hoi.model.AISShip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Set;


@Component
public class HarborWatcher {

    private final BarentsWatchAISService aisService;
    private final Logger log = LoggerFactory.getLogger(HarborWatcher.class);

    public HarborWatcher(BarentsWatchAISService aisService) {
        this.aisService = aisService;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        DestinationProfile destinationProfile = ENGEBØ;
        log.info("🔎 Starting HarborWatcher for destination: {}", destinationProfile.name());
        aisService.streamShips()
                .doOnSubscribe(s -> log.info("🚢 AIS stream started"))
                .doOnError(e -> log.error("❌ AIS stream failed", e))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(15))
                        .doBeforeRetry(r ->
                                log.warn("🔁 Restarting AIS stream after error: {}",
                                        r.failure().getMessage())
                        ))
                .subscribe(ship -> handleShip(ship, destinationProfile));
    }

    private static final DestinationProfile ENGEBØ =
            new DestinationProfile(
                    "Engebø",
                    Set.of(
                            "engebo",
                            "engebo hamn",
                            "engebo kai",
                            "engebohavn",
                            "engebo havn",
                            "engeb"
                    )
            );


    private static final DestinationProfile HAUGESUND =
            new DestinationProfile(
                    "Haugesund",
                    Set.of(
                            "Haugesund",
                            "Haugesund hamn",
                            "Haugesund kai",
                            "Haugesundhavn",
                            "Haugesund havn",
                            "Hauges"
                    )
            );


    private static final DestinationProfile BARENTSBURG =
            new DestinationProfile(
                    "Barentsburg",
                    Set.of(
                            "Barentsburg",
                            "Barentsburg hamn",
                            "Barentsburg kai",
                            "Barentsburghavn",
                            "Barentsburg havn",
                            "Barentsb"
                    )
            );

    private void handleShip(AISShip ship, DestinationProfile destination) {

        if (ship.name() == null || ship.destination() == null) {
            return;
        }

        if (matchesDestination(ship, destination)) {
            log.info(
                    "🚨 Skip mot {}: name={}, mmsi={}, sog={}, cog={}, pos=({}, {})",
                    destination.name(),
                    ship.name(),
                    ship.mmsi(),
                    ship.speed(),
                    ship.heading(),
                    ship.latitude(),
                    ship.longitude()
            );
        }
    }

    private boolean matchesDestination(AISShip ship, DestinationProfile profile) {
        if (ship.destination() == null) {
            return false;
        }

        String dest = normalize(ship.destination());
//        log.info("Normalized destination: '{}'", dest);
        for (String alias : profile.aliases()) {
            if (dest.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String s) {
        return s
                .toLowerCase()
                .replace("ø", "o")
                .replace("æ", "ae")
                .replace("å", "a")
                .replaceAll("[^a-z0-9 ]", "")
                .trim();
    }
}
