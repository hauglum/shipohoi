package no.hauglum.ship_o_hoi.model;

public record Position(double latitude, double longitude) {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    public double distanceMetersTo(Position other) {
        double deltaLatitude = Math.toRadians(other.latitude - latitude);
        double deltaLongitude = Math.toRadians(other.longitude - longitude);
        double a = haversine(deltaLatitude)
                 + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude))
                 * haversine(deltaLongitude);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double haversine(double angleRadians) {
        double halfAngleSine = Math.sin(angleRadians / 2);
        return halfAngleSine * halfAngleSine;
    }
}
