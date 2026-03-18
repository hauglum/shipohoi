package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class EmailService implements ShipAlertService {
    private final JavaMailSender mailSender;
    private final String to;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender,
                        @Value("${email.to}") String to) {
        this.mailSender = mailSender;
        this.to = to;
    }

    @Override
    public void sendShipAlert(AISShip ship, String destination) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("🚢 Ship Alert: " + ship.name() + " heading to " + destination);
            message.setText(String.format(
                    "Ship: %s%nMMSI: %s%nDestination: %s%nSpeed: %s knots%nHeading: %s%nPosition: (%.4f, %.4f)",
                    ship.name(),
                    ship.mmsi(),
                    ship.destination(),
                    ship.speed(),
                    ship.heading(),
                    ship.latitude(),
                    ship.longitude()
            ));

            mailSender.send(message);
            log.info("📧 Email alert sent for ship {} heading to {}", ship.name(), destination);
        } catch (Exception e) {
            log.error("Failed to send email alert for ship {}", ship.name(), e);
        }
    }
}
