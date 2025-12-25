package no.hauglum.ship_o_hoi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AISWebClientConfig {

    @Bean
    @Qualifier("aisWebClient")
    public WebClient aisWebClient() {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(cfg ->
                        cfg.defaultCodecs().maxInMemorySize(1024 * 1024) // 10 MB
                )
                .build();

        return WebClient.builder()
                .baseUrl("https://live.ais.barentswatch.no")
                .exchangeStrategies(strategies)
                .build();
    }
}