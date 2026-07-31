package no.hauglum.ship_o_hoi.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AlertCooldownTest {

    private static final String MMSI = "304011027";
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    private final MutableClock clock = new MutableClock(Instant.parse("2026-07-31T12:00:00Z"));
    private final AlertCooldown cooldown = new AlertCooldown(clock, ONE_HOUR);

    @Test
    void shouldAlertOnFirstSighting() {
        assertThat(cooldown.shouldAlert(MMSI)).isTrue();
    }

    @Test
    void shouldNotAlertTwiceForTheSameSighting() {
        assertThat(cooldown.shouldAlert(MMSI)).isTrue();
        assertThat(cooldown.shouldAlert(MMSI)).isFalse();
    }

    @Test
    void shouldNotAlertBeforeCooldownExpires() {
        cooldown.shouldAlert(MMSI);

        clock.advance(Duration.ofMinutes(59));

        assertThat(cooldown.shouldAlert(MMSI)).isFalse();
    }

    @Test
    void shouldAlertAgainAfterCooldownExpires() {
        cooldown.shouldAlert(MMSI);

        clock.advance(Duration.ofMinutes(61));

        assertThat(cooldown.shouldAlert(MMSI)).isTrue();
    }

    @Test
    void shouldTrackEachShipIndependently() {
        assertThat(cooldown.shouldAlert(MMSI)).isTrue();
        assertThat(cooldown.shouldAlert("314551000")).isTrue();
    }

    @Test
    void shouldAlertOnceWhenBothFeedsDeliverTheSameShipConcurrently() throws Exception {
        int feedCount = 16;
        ExecutorService feeds = Executors.newFixedThreadPool(feedCount);
        CountDownLatch startTogether = new CountDownLatch(1);
        AtomicInteger alertsGranted = new AtomicInteger();

        for (int i = 0; i < feedCount; i++) {
            feeds.submit(() -> {
                startTogether.await();
                if (cooldown.shouldAlert(MMSI)) {
                    alertsGranted.incrementAndGet();
                }
                return null;
            });
        }
        startTogether.countDown();
        feeds.shutdown();
        assertThat(feeds.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(alertsGranted).hasValue(1);
    }

    private static final class MutableClock extends Clock {

        private volatile Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration amount) {
            now = now.plus(amount);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("Test clock is UTC only");
        }
    }
}
