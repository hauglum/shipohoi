package no.hauglum.ship_o_hoi.service;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class EmailService implements ShipAlertService {
    private final JavaMailSender mailSender;
    private final MapService mapService;
    private final String to;
    private final String mapboxAccessToken;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender,
                        MapService mapService,
                        @Value("${email.to}") String to,
                        @Value("${mapbox.access-token}") String mapboxAccessToken) {
        this.mailSender = mailSender;
        this.mapService = mapService;
        this.to = to;
        this.mapboxAccessToken = mapboxAccessToken;
    }

    @Override
    public void sendShipAlert(AISShip ship, String destination, Position destinationPosition) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to.split(","));
            helper.setSubject("🚢 Ship Alert: " + ship.name() + " heading to " + destination);

            String mapUrl = null;
            if (destinationPosition != null && ship.latitude() != null && ship.longitude() != null) {
                mapUrl = mapService.generateMapUrl(
                        destinationPosition,
                        new Position(ship.latitude(), ship.longitude())
                );
            }

            String body = String.format(
                    """
                    Ship: %s
                    MMSI: %s
                    Destination: %s
                    Speed: %s knots
                    Heading: %s degrees
                    Position: (%.4f, %.4f)
                    
                    Map URL: %s
                    """,
                    ship.name(),
                    ship.mmsi(),
                    ship.destination(),
                    ship.speed() != null ? ship.speed() : "N/A",
                    ship.heading() != null ? ship.heading() : "N/A",
                    ship.latitude() != null ? ship.latitude() : 0.0,
                    ship.longitude() != null ? ship.longitude() : 0.0,
                    mapUrl != null ? mapUrl : "N/A"
            );
            helper.setText(body);

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
                        DataSource dataSource = new ByteArrayDataSource(mapImage, "image/png");
                        helper.addAttachment("ship_map.png", dataSource);
                        log.info("🗺️ Attached map image to email");
                    }
                } catch (Exception e) {
                    log.warn("Failed to generate map image, sending email without attachment: {}", e.getMessage());
                }
            }

            mailSender.send(message);
            log.info("📧 Email alert sent for ship {} heading to {}", ship.name(), destination);
        } catch (Exception e) {
            log.error("Failed to send email alert for ship {}", ship.name(), e);
        }
    }
}
