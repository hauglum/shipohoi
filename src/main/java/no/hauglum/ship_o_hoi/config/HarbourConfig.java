package no.hauglum.ship_o_hoi.config;

import no.hauglum.ship_o_hoi.service.HarbourPresence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HarbourConfig {

    @Bean
    HarbourPresence harbourPresence(DestinationProperties destinationProperties) {
        return new HarbourPresence(destinationProperties.resolveActiveHarbour());
    }
}
