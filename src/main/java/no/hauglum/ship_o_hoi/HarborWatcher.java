package no.hauglum.ship_o_hoi;

import no.hauglum.ship_o_hoi.config.DestinationProperties;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.DestinationProfile;
import no.hauglum.ship_o_hoi.service.AisStreamService;
import no.hauglum.ship_o_hoi.service.BarentsWatchAISService;
import no.hauglum.ship_o_hoi.service.ShipAlertService;
import no.hauglum.ship_o_hoi.service.TrackRecorder;


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
import java.util.concurrent.ConcurrentHashMap;


@Component
public class HarborWatcher {
    private final Map<String, Instant> lastAlert = new ConcurrentHashMap<>();

    private final BarentsWatchAISService aisService;
    private final AisStreamService aisStreamService;
    private final ShipAlertService shipAlertService;
    private final DestinationProperties destinationProperties;
    private final TrackRecorder trackRecorder;
    private final Logger log = LoggerFactory.getLogger(HarborWatcher.class);

    public HarborWatcher(BarentsWatchAISService aisService, AisStreamService aisStreamService,
                         ShipAlertService shipAlertService, DestinationProperties destinationProperties,
                         TrackRecorder trackRecorder) {
        this.aisService = aisService;
        this.aisStreamService = aisStreamService;
        this.shipAlertService = shipAlertService;
        this.destinationProperties = destinationProperties;
        this.trackRecorder = trackRecorder;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        DestinationProfile destinationProfile = destinationProperties.resolveActive();
        log.info("🔎 Starting HarborWatcher for destination: {}", destinationProfile.name());

        Flux<AISShip> ships = aisService.streamShips().share();
        ships
                .window(Duration.ofMinutes(1))
                .flatMap(Flux::count)
                .subscribe(count ->
                        log.info("📊 AIS meldinger siste minutt (Barents Watch): {}", count)
                );

        ships
                .doOnSubscribe(s -> log.info("🚢 Barents Watch stream started"))
                .doOnError(e -> log.error("❌ Barents Watch stream failed", e))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(15))
                        .doBeforeRetry(r ->
                                log.warn("🔁 Restarting Barents Watch stream after error: {}",
                                        r.failure().getMessage())
                        ))
                .subscribe(ship -> handleShip(ship, destinationProfile));

        Flux<AISShip> globalShips = aisStreamService.streamShips()
                .doOnSubscribe(s -> log.info("🌍 AISStream global stream started"))
                .doOnError(e -> log.error("❌ AISStream stream failed", e))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(15))
                        .doBeforeRetry(r ->
                                log.warn("🔁 Restarting AISStream after error: {}",
                                        r.failure().getMessage())
                        ))
                .share();

        globalShips
                .window(Duration.ofMinutes(1))
                .flatMap(Flux::count)
                .subscribe(count -> log.info("📊 AISStream meldinger siste minutt: {}", count));

        globalShips.subscribe(ship -> handleShip(ship, destinationProfile));
    }

    private void handleShip(AISShip ship, DestinationProfile destination) {


        if (ship.name() == null || ship.destination() == null) {
            return;
        }

        boolean destinationMatch = matchesDestination(ship, destination);

        if (destinationMatch || trackRecorder.isWatchlisted(ship.mmsi())) {
            trackRecorder.record(ship);
        }

        if (destinationMatch && shouldAlert(ship)) {
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
        for (String alias : profile.aliases()) {
            String pattern = ".*\\b" + normalize(alias) + "\\b.*";
            if (dest.matches(pattern)) {
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
