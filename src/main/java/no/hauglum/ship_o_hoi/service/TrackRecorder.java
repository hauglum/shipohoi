package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.config.TrackingProperties;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.ShipPosition;
import no.hauglum.ship_o_hoi.repository.ShipPositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TrackRecorder {

    private final ShipPositionRepository repository;
    private final TrackingProperties trackingProperties;
    private final Logger log = LoggerFactory.getLogger(TrackRecorder.class);

    public TrackRecorder(ShipPositionRepository repository, TrackingProperties trackingProperties) {
        this.repository = repository;
        this.trackingProperties = trackingProperties;
    }

    public boolean isWatchlisted(String mmsi) {
        return trackingProperties.getMmsiWatchlist().contains(mmsi);
    }

    public void record(AISShip ship) {
        if (ship.latitude() == null || ship.longitude() == null) {
            return;
        }
        ShipPosition position = new ShipPosition(
                ship.mmsi(),
                ship.name(),
                ship.latitude(),
                ship.longitude(),
                ship.speed(),
                ship.course(),
                Instant.now()
        );
        repository.save(position);
        log.debug("Recorded position for {} ({})", ship.name(), ship.mmsi());
    }
}
