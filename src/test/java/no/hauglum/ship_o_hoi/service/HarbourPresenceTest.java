package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.HarbourArea;
import no.hauglum.ship_o_hoi.model.Position;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class HarbourPresenceTest {

    private static final String MMSI = "304011027";
    private static final Position ENGEBO = new Position(61.487982, 5.442754);
    private static final double TWO_KILOMETERS_NORTH = 0.018;

    private final HarbourPresence presence = new HarbourPresence(new HarbourArea(ENGEBO, 1000, 0.5));

    @Test
    void shipIsNotInHarbourUntilItHasBeenSeenThere() {
        assertThat(presence.isInHarbour(MMSI)).isFalse();
    }

    @Test
    void arrivalIsReportedOnceThenTheShipStaysInHarbour() {
        assertThat(presence.hasJustArrived(berthed())).isTrue();

        assertThat(presence.hasJustArrived(berthed())).isFalse();
        assertThat(presence.isInHarbour(MMSI)).isTrue();
    }

    @Test
    void shipUnderwayNearTheQuayHasNotArrived() {
        assertThat(presence.hasJustArrived(underwayAtQuay())).isFalse();
        assertThat(presence.isInHarbour(MMSI)).isFalse();
    }

    @Test
    void shipStoppedOutsideTheHarbourHasNotArrived() {
        assertThat(presence.hasJustArrived(stoppedOffshore())).isFalse();
        assertThat(presence.isInHarbour(MMSI)).isFalse();
    }

    @Test
    void messageWithoutSpeedKeepsThePreviousVerdict() {
        presence.hasJustArrived(berthed());

        // AISStream delivers static data with no speed — it must not read as a departure.
        assertThat(presence.hasJustArrived(withoutSpeed())).isFalse();
        assertThat(presence.isInHarbour(MMSI)).isTrue();
    }

    @Test
    void departureReArmsTheArrivalReport() {
        presence.hasJustArrived(berthed());

        presence.hasJustArrived(underwayAtQuay());
        assertThat(presence.isInHarbour(MMSI)).isFalse();

        assertThat(presence.hasJustArrived(berthed())).isTrue();
    }

    @Test
    void eachShipIsTrackedIndependently() {
        presence.hasJustArrived(berthed());

        assertThat(presence.isInHarbour("314551000")).isFalse();
    }

    @Test
    void shouldReportArrivalOnceWhenBothFeedsDeliverTheSameShipConcurrently() throws Exception {
        int feedCount = 16;
        ExecutorService feeds = Executors.newFixedThreadPool(feedCount);
        CountDownLatch startTogether = new CountDownLatch(1);
        AtomicInteger arrivalsReported = new AtomicInteger();

        for (int i = 0; i < feedCount; i++) {
            feeds.submit(() -> {
                startTogether.await();
                if (presence.hasJustArrived(berthed())) {
                    arrivalsReported.incrementAndGet();
                }
                return null;
            });
        }
        startTogether.countDown();
        feeds.shutdown();
        assertThat(feeds.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(arrivalsReported).hasValue(1);
    }

    private AISShip berthed() {
        return ship(ENGEBO.latitude(), ENGEBO.longitude(), 0.1);
    }

    private AISShip underwayAtQuay() {
        return ship(ENGEBO.latitude(), ENGEBO.longitude(), 6.0);
    }

    private AISShip stoppedOffshore() {
        return ship(ENGEBO.latitude() + TWO_KILOMETERS_NORTH, ENGEBO.longitude(), 0.0);
    }

    private AISShip withoutSpeed() {
        return ship(ENGEBO.latitude(), ENGEBO.longitude(), null);
    }

    private AISShip ship(Double latitude, Double longitude, Double speed) {
        return new AISShip(MMSI, "THESEUS", latitude, longitude, speed, null, null, "ENGEBO", null);
    }
}
