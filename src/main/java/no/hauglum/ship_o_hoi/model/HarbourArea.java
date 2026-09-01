package no.hauglum.ship_o_hoi.model;

/**
 * The area around a destination in which a ship counts as having arrived:
 * close enough to the quay and no longer making way.
 */
public record HarbourArea(Position centre, double radiusMeters, double maxBerthedSpeedKnots) {

    public HarbourArea {
        if (centre == null) {
            throw new IllegalArgumentException("Harbour area needs a centre position");
        }
    }

    public boolean isBerthed(Position position, double speedKnots) {
        return isWithinArea(position) && isStopped(speedKnots);
    }

    private boolean isWithinArea(Position position) {
        return centre.distanceMetersTo(position) <= radiusMeters;
    }

    private boolean isStopped(double speedKnots) {
        return speedKnots <= maxBerthedSpeedKnots;
    }
}
