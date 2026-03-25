package no.hauglum.ship_o_hoi;

import no.hauglum.ship_o_hoi.model.DestinationProfile;
import no.hauglum.ship_o_hoi.model.Position;
import no.hauglum.ship_o_hoi.service.BarentsWatchAISService;
import no.hauglum.ship_o_hoi.service.ShipAlertService;
import no.hauglum.ship_o_hoi.model.AISShip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class HarborWatcher {
    private final Map<String, Instant> lastAlert = new ConcurrentHashMap<>();

    private final BarentsWatchAISService aisService;
    private final ShipAlertService shipAlertService;
    private final Logger log = LoggerFactory.getLogger(HarborWatcher.class);

    public HarborWatcher(BarentsWatchAISService aisService, ShipAlertService shipAlertService) {
        this.aisService = aisService;
        this.shipAlertService = shipAlertService;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        //TODO: Make it possible to configure which destination to watch for, maybe via application properties or environment variable or UI
        DestinationProfile destinationProfile = ENGEBØ;
//        DestinationProfile destinationProfile = HAUGESUND;
        log.info("🔎 Starting HarborWatcher for destination: {}", destinationProfile.name());

        Flux<AISShip> ships = aisService.streamShips().share();
        ships
                .window(Duration.ofMinutes(1))
                .flatMap(Flux::count)
                .subscribe(count ->
                        log.info("📊 AIS meldinger siste minutt: {}", count)
                );

        ships
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
                    ),
                    new Position(61.487982, 5.442754)
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
                    ),
                    new Position(59.4138, 5.2677)
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
                    ),
                    new Position(78.0667, 14.2333)
            );


    private static final DestinationProfile ASKEPOTT =
            new DestinationProfile(
                    "Askepott",
                    Set.of(
                            "Askepott"
                    ),
                    null
            );

    private void handleShip(AISShip ship, DestinationProfile destination) {


        if (ship.name() == null || ship.destination() == null) {
            return;
        }

        if (matchesDestination(ship, destination) && shouldAlert(ship)) {
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
            shipAlertService.sendShipAlert(ship, destination.name(), destination.position());
        }
    }

    private boolean shouldAlert(AISShip ship) {
        return lastAlert.compute(ship.mmsi(), (mmsi, last) -> {
            Instant now = Instant.now();
            if (last == null || last.isBefore(now.minus(Duration.ofHours(1)))) {
                return now;
            }
            return last;
        }).equals(Instant.now());
    }

    private boolean matchesDestination(AISShip ship, DestinationProfile profile) {
        if (ship.destination() == null) {
            return false;
        }

        String dest = normalize(ship.destination());
//        log.info("Normalized destination: '{}'", dest);
        for (String alias : profile.aliases()) {
            if (dest.contains(normalize(alias) )) {
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
