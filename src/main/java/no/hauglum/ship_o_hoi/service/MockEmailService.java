package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@Profile("unused")
public class MockEmailService implements ShipAlertService {
    private final MapService mapService;
    private final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    private final String mapboxAccessToken;

    public MockEmailService(MapService mapService,
                            @Value("${mapbox.access-token:}") String mapboxAccessToken) {
        this.mapService = mapService;
        this.mapboxAccessToken = mapboxAccessToken;
    }

    @Override
    public void sendShipAlert(AISShip ship, String destination, Position destinationPosition) {
        String mapUrl = null;
        if (destinationPosition != null && ship.latitude() != null && ship.longitude() != null) {
            mapUrl = mapService.generateMapUrl(
                    destinationPosition,
                    new Position(ship.latitude(), ship.longitude())
            );
        }

        log.info("📧 [MOCK] Would send email for ship {} heading to {}", ship.name(), destination);
        log.info("🗺️ Map URL: {}", mapUrl != null ? mapUrl : "N/A");

        if (destinationPosition != null && ship.latitude() != null && ship.longitude() != null) {
            try {
                int heading = ship.heading() != null ? ship.heading() : 0;
                double speed = ship.speed() != null ? ship.speed() : 0.0;
                byte[] mapImage = mapService.generateStaticMapImage(
                        destinationPosition,
                        new Position(ship.latitude(), ship.longitude()),
                        heading,
                        speed,
                        mapboxAccessToken
                );

                if (mapImage != null) {
                    Path savedPath = mapService.saveMapImage(mapImage, "ship_map.png");
                    log.info("🗺️ [MOCK] Saved map image to: {}", savedPath);
                }
            } catch (IOException e) {
                log.warn("🗺️ [MOCK] Would generate map image but failed: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("🗺️ [MOCK] Map image generation skipped: {}", e.getMessage());
            }
        }
    }
}
