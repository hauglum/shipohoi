package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.config.TrackingProperties;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.ShipPosition;
import no.hauglum.ship_o_hoi.repository.ShipPositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrackRecorder {

    private static final double MIN_MOVE_METERS = 50.0;

    private final ShipPositionRepository repository;
    private final TrackingProperties trackingProperties;
    private final Logger log = LoggerFactory.getLogger(TrackRecorder.class);
    private final Map<String, double[]> lastPosition = new ConcurrentHashMap<>();

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
        double[] last = lastPosition.get(ship.mmsi());
        if (last != null && haversineMeters(last[0], last[1], ship.latitude(), ship.longitude()) < MIN_MOVE_METERS) {
            return;
        }
        lastPosition.put(ship.mmsi(), new double[]{ship.latitude(), ship.longitude()});
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

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
