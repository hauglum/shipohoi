package no.hauglum.ship_o_hoi.service;

import jakarta.mail.internet.MimeMessage;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements ShipAlertService {

    private final JavaMailSender mailSender;
    private final String to;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender, @Value("${email.to}") String to) {
        this.mailSender = mailSender;
        this.to = to;
    }

    @Override
    public void sendShipAlert(AISShip ship, String destination, Position destinationPosition) {
        try {
            String marineTrafficUrl = "https://www.marinetraffic.com/en/ais/details/ships/mmsi:" + ship.mmsi();
            String osmUrl = String.format(
                    "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f&zoom=8",
                    ship.latitude(), ship.longitude()
            );

            String body = String.format("""
                    <html><body style="font-family: sans-serif;">
                    <h3>🚢 %s is heading to %s</h3>
                    <table cellpadding="4">
                      <tr><td><b>MMSI</b></td><td>%s</td></tr>
                      <tr><td><b>Destination</b></td><td>%s</td></tr>
                      <tr><td><b>Speed</b></td><td>%s knots</td></tr>
                      <tr><td><b>Heading</b></td><td>%s°</td></tr>
                      <tr><td><b>Position</b></td><td>%.4f, %.4f</td></tr>
                    </table>
                    <br>
                    <a href="%s">🔍 Track on MarineTraffic</a><br><br>
                    <a href="%s">🗺️ View on OpenStreetMap</a>
                    </body></html>
                    """,
                    ship.name(), destination,
                    ship.mmsi(),
                    ship.destination(),
                    ship.speed() != null ? ship.speed() : "N/A",
                    ship.heading() != null ? ship.heading() : "N/A",
                    ship.latitude(), ship.longitude(),
                    marineTrafficUrl,
                    osmUrl
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to.split(",\\s*"));
            helper.setSubject("🚢 Ship Alert: " + ship.name() + " → " + destination);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("📧 Email alert sent for ship {} heading to {}", ship.name(), destination);
        } catch (Exception e) {
            log.error("Failed to send email alert for ship {}", ship.name(), e);
        }
    }
}
