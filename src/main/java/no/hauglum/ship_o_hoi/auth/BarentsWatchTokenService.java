package no.hauglum.ship_o_hoi.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@Service
public class BarentsWatchTokenService {

    private final Logger log = LoggerFactory.getLogger(BarentsWatchTokenService.class);
    private final WebClient webClient;

    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    private volatile String cachedToken;
    private volatile Instant expiresAt;

    public BarentsWatchTokenService(
            WebClient.Builder builder,
            @Value("${barentswatch.auth.token-url}") String tokenUrl,
            @Value("${barentswatch.auth.client-id}") String clientId,
            @Value("${barentswatch.auth.client-secret}") String clientSecret,
            @Value("${barentswatch.auth.scope}") String scope
    ) {
        this.webClient = builder.build();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    public Mono<String> getAccessToken() {
        if (cachedToken != null && !isExpired()) {
            return Mono.just(cachedToken);
        }

        return refreshToken()
                .subscribeOn(Schedulers.boundedElastic())
                .cache();
    }

    private boolean isExpired() {
        return expiresAt == null || Instant.now().isAfter(expiresAt.minusSeconds(60));
    }

    private Mono<String> refreshToken() {
        return webClient.post()
                .uri(tokenUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("scope", scope))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(r -> log.info("Token scope: {}", scope))
                .doOnNext(r -> {
                    this.cachedToken = r.getAccessToken();
                    this.expiresAt = Instant.now().plusSeconds(r.getExpiresIn());
                })
                .map(TokenResponse::getAccessToken);
    }
}
