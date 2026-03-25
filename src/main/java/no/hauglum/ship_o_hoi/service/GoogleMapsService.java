package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Service
public class GoogleMapsService implements MapService {
    private static final Logger log = LoggerFactory.getLogger(GoogleMapsService.class);

    private static final DecimalFormatSymbols US_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);
    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("#.######", US_SYMBOLS);

    @Override
    public String generateMapUrl(Position destinationPos, Position shipPos) {
        double centerLat = (destinationPos.latitude() + shipPos.latitude()) / 2;
        double centerLng = (destinationPos.longitude() + shipPos.longitude()) / 2;

        double latDiff = Math.abs(destinationPos.latitude() - shipPos.latitude());
        double lngDiff = Math.abs(destinationPos.longitude() - shipPos.longitude());
        double maxDiff = Math.max(latDiff, lngDiff);

        int zoom = calculateZoom(maxDiff);

        String markers = String.format(
                "%s,%s|%s,%s",
                COORD_FORMAT.format(destinationPos.latitude()), COORD_FORMAT.format(destinationPos.longitude()),
                COORD_FORMAT.format(shipPos.latitude()), COORD_FORMAT.format(shipPos.longitude())
        );

        return String.format(
                "https://www.google.com/maps?z=%d&center=%s,%s&q=%s,%s&markers=%s",
                zoom,
                COORD_FORMAT.format(centerLat), COORD_FORMAT.format(centerLng),
                COORD_FORMAT.format(centerLat), COORD_FORMAT.format(centerLng),
                markers
        );
    }

    @Override
    public byte[] generateStaticMapImage(Position destinationPos, Position shipPos, int shipHeading, double shipSpeed, String accessToken) {
        log.warn("GoogleMapsService does not support static image generation. Use MapboxStaticMapService instead.");
        return null;
    }

    @Override
    public Path saveMapImage(byte[] imageData, String filename) throws IOException {
        if (imageData == null) {
            throw new IOException("No image data to save");
        }
        Path tempFile = Files.createTempFile("map_", filename);
        Files.write(tempFile, imageData);
        log.info("Saved map image to: {}", tempFile);
        return tempFile;
    }

    private int calculateZoom(double degreesDiff) {
        if (degreesDiff <= 0.001) return 18;
        if (degreesDiff <= 0.005) return 16;
        if (degreesDiff <= 0.02) return 14;
        if (degreesDiff <= 0.05) return 12;
        if (degreesDiff <= 0.2) return 10;
        if (degreesDiff <= 0.5) return 8;
        if (degreesDiff <= 2) return 6;
        if (degreesDiff <= 5) return 4;
        return 3;
    }
}
