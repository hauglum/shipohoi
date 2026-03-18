package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockEmailService implements ShipAlertService {
    private final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void sendShipAlert(AISShip ship, String destination) {
        log.info("📧 [MOCK] Would send email for ship {} heading to {}", ship.name(), destination);
    }
}
