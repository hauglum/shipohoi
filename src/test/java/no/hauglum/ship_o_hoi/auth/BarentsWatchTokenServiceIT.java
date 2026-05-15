package no.hauglum.ship_o_hoi.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BarentsWatchTokenServiceIT {

    @Autowired
    private BarentsWatchTokenService tokenService;

    @Test
    void shouldFetchAndCacheAccessToken() {
        // First call – should hit BarentsWatch
        String token1 = tokenService.getAccessToken().block();

        // Second call – should use cache
        String token2 = tokenService.getAccessToken().block();

        assertThat(token1).isNotNull();
        assertThat(token1).isNotBlank();
        assertThat(token2).isEqualTo(token1);

        System.out.println("Access token fetched and cached successfully");
    }
}
