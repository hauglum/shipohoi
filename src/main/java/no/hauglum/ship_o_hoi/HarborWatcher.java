package no.hauglum.ship_o_hoi;

import no.hauglum.ship_o_hoi.config.DestinationProperties;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.DestinationProfile;
import no.hauglum.ship_o_hoi.service.AisStreamService;
import no.hauglum.ship_o_hoi.service.AlertCooldown;
import no.hauglum.ship_o_hoi.service.BarentsWatchAISService;
import no.hauglum.ship_o_hoi.service.HarbourPresence;
import no.hauglum.ship_o_hoi.service.ShipAlertService;
import no.hauglum.ship_o_hoi.service.TrackRecorder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Clock;
import java.time.Duration;


@Component
public class HarborWatcher {
    private static final Duration ALERT_COOLDOWN = Duration.ofHours(1);
    private static final Duration STREAM_RESTART_DELAY = Duration.ofSeconds(15);

    private final AlertCooldown alertCooldown = new AlertCooldown(Clock.systemUTC(), ALERT_COOLDOWN);
    // A ship hovering on the edge of the harbour area must not mail an arrival on every crossing.
    private final AlertCooldown arrivalCooldown = new AlertCooldown(Clock.systemUTC(), ALERT_COOLDOWN);

    private final BarentsWatchAISService aisService;
    private final AisStreamService aisStreamService;
    private final ShipAlertService shipAlertService;
    private final DestinationProperties destinationProperties;
    private final TrackRecorder trackRecorder;
    private final HarbourPresence harbourPresence;
    private final Logger log = LoggerFactory.getLogger(HarborWatcher.class);

    public HarborWatcher(BarentsWatchAISService aisService, AisStreamService aisStreamService,
                         ShipAlertService shipAlertService, DestinationProperties destinationProperties,
                         TrackRecorder trackRecorder, HarbourPresence harbourPresence) {
        this.aisService = aisService;
        this.aisStreamService = aisStreamService;
        this.shipAlertService = shipAlertService;
        this.destinationProperties = destinationProperties;
        this.trackRecorder = trackRecorder;
        this.harbourPresence = harbourPresence;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        DestinationProfile destinationProfile = destinationProperties.resolveActive();
        log.info("🔎 Starting HarborWatcher for destination: {}", destinationProfile.name());

        Flux<AISShip> ships = aisService.streamShips()
                .doOnSubscribe(s -> log.info("🚢 Barents Watch stream started"))
                .doOnError(e -> log.error("❌ Barents Watch stream failed", e))
                // Restart on clean completion, but pace it: an immediately-completing
                // upstream (e.g. a non-2xx body) would otherwise resubscribe in a tight loop.
                .repeatWhen(completed -> completed.delayElements(STREAM_RESTART_DELAY))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, STREAM_RESTART_DELAY)
                        .doBeforeRetry(r ->
                                log.warn("🔁 Restarting Barents Watch stream after error: {}",
                                        r.failure().getMessage())
                        ))
                .share();

        ships
                .window(Duration.ofMinutes(1))
                .flatMap(Flux::count)
                .subscribe(count ->
                        log.info("📊 AIS meldinger siste minutt (Barents Watch): {}", count)
                );

        ships.subscribe(ship -> handleShip(ship, destinationProfile));

        Flux<AISShip> globalShips = aisStreamService.streamShips()
                .doOnSubscribe(s -> log.info("🌍 AISStream global stream started"))
                .doOnError(e -> log.error("❌ AISStream stream failed", e))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, STREAM_RESTART_DELAY)
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
        if (ship.name() == null) {
            return;
        }

        boolean isBoundForDestination = matchesDestination(ship, destination);
        if (isBoundForDestination) {
            trackRecorder.addToWatchlistIfNew(ship);
        }
        // A berthed ship often blanks or rewrites its destination, so watchlisted ships are
        // followed on position alone — otherwise they go dark exactly when they arrive.
        if (!isBoundForDestination && !trackRecorder.isWatchlisted(ship.mmsi())) {
            return;
        }
        trackRecorder.record(ship);

        if (harbourPresence.hasJustArrived(ship) && arrivalCooldown.shouldAlert(ship.mmsi())) {
            announceArrival(ship, destination);
            return;
        }

        if (isBoundForDestination
                && !harbourPresence.isInHarbour(ship.mmsi())
                && alertCooldown.shouldAlert(ship.mmsi())) {
            announceApproach(ship, destination);
        }
    }

    private void announceApproach(AISShip ship, DestinationProfile destination) {
        log.info(
                "🚨 Skip mot {}: name={}, mmsi={}, sog={}, heading={}, pos=({}, {})",
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

    private void announceArrival(AISShip ship, DestinationProfile destination) {
        log.info(
                "⚓ Skip framme i {}: name={}, mmsi={}, sog={}, pos=({}, {})",
                destination.name(),
                ship.name(),
                ship.mmsi(),
                ship.speed(),
                ship.latitude(),
                ship.longitude()
        );
        shipAlertService.sendArrivalAlert(ship, destination.name());
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
