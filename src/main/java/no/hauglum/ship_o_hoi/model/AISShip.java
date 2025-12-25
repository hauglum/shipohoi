package no.hauglum.ship_o_hoi.model;

public record AISShip(
        String mmsi,
        String name,
        Double latitude,
        Double longitude,

        Double speed,
        Double course,
        Integer heading,

        String destination,

        String eta
) {}
