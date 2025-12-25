package no.hauglum.ship_o_hoi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final int MAX_BUFFER_SIZE = 10* 1024 * 1024; // 1 MB

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://live.ais.barentswatch.no")
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE);
                    configurer.customCodecs().register(
                            new Jackson2JsonDecoder(
                                    new ObjectMapper(),
                                    MediaType.APPLICATION_NDJSON
                            )
                    );
                })
                .build();
    }
}