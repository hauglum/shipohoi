package no.hauglum.ship_o_hoi.model;

import java.util.Set;

public record DestinationProfile(
        String name,
        Set<String> aliases
) {}

