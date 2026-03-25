package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.Position;

import java.io.IOException;
import java.nio.file.Path;

public interface MapService {
    String generateMapUrl(Position destinationPos, Position shipPos);
    byte[] generateStaticMapImage(Position destinationPos, Position shipPos, int shipHeading, double shipSpeed, String accessToken) throws IOException;
    Path saveMapImage(byte[] imageData, String filename) throws IOException;
}
