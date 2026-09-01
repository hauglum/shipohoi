package no.hauglum.ship_o_hoi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HarbourAreaTest {

    private static final Position ENGEBO = new Position(61.487982, 5.442754);
    private static final double ONE_HUNDRED_METERS_NORTH = 0.0009;
    private static final double TWO_KILOMETERS_NORTH = 0.018;

    private final HarbourArea harbour = new HarbourArea(ENGEBO, 1000, 0.5);

    @Test
    void shipStoppedAtTheQuayIsBerthed() {
        assertThat(harbour.isBerthed(ENGEBO, 0.0)).isTrue();
    }

    @Test
    void mooringDriftStillCountsAsStopped() {
        assertThat(harbour.isBerthed(nearQuay(), 0.5)).isTrue();
    }

    @Test
    void shipStillMakingWayIsNotBerthed() {
        assertThat(harbour.isBerthed(nearQuay(), 0.6)).isFalse();
    }

    @Test
    void shipStoppedFarFromTheHarbourIsNotBerthed() {
        assertThat(harbour.isBerthed(twoKilometersOut(), 0.0)).isFalse();
    }

    @Test
    void shipUnderwayFarFromTheHarbourIsNotBerthed() {
        assertThat(harbour.isBerthed(twoKilometersOut(), 12.0)).isFalse();
    }

    @Test
    void harbourWithoutACentreIsRejected() {
        assertThatThrownBy(() -> new HarbourArea(null, 1000, 0.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Position nearQuay() {
        return new Position(ENGEBO.latitude() + ONE_HUNDRED_METERS_NORTH, ENGEBO.longitude());
    }

    private Position twoKilometersOut() {
        return new Position(ENGEBO.latitude() + TWO_KILOMETERS_NORTH, ENGEBO.longitude());
    }
}
