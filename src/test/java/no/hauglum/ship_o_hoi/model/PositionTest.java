package no.hauglum.ship_o_hoi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PositionTest {

    private static final Position ENGEBO = new Position(61.487982, 5.442754);
    private static final Position HAUGESUND = new Position(59.4138, 5.2677);

    @Test
    void distanceToItselfIsZero() {
        assertThat(ENGEBO.distanceMetersTo(ENGEBO)).isZero();
    }

    @Test
    void distanceIsMeasuredInMeters() {
        Position oneDegreeNorth = new Position(62.487982, 5.442754);

        // One degree of latitude is ~111.2 km anywhere on the globe.
        assertThat(ENGEBO.distanceMetersTo(oneDegreeNorth)).isCloseTo(111_195, within(500.0));
    }

    @Test
    void distanceIsSymmetric() {
        assertThat(ENGEBO.distanceMetersTo(HAUGESUND))
                .isCloseTo(HAUGESUND.distanceMetersTo(ENGEBO), within(0.001));
    }

    @Test
    void engeboToHaugesundIsRoughlyTwoHundredKilometers() {
        assertThat(ENGEBO.distanceMetersTo(HAUGESUND)).isCloseTo(230_000, within(5_000.0));
    }
}
