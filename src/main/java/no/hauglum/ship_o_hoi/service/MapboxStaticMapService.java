package no.hauglum.ship_o_hoi.service;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.api.staticmap.v1.models.StaticPolylineAnnotation;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import no.hauglum.ship_o_hoi.model.Position;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Primary
@Service
public class MapboxStaticMapService implements MapService {
    private static final Logger log = LoggerFactory.getLogger(MapboxStaticMapService.class);
    private static final int IMAGE_WIDTH = 600;
    private static final int IMAGE_HEIGHT = 400;
    private static final String MAP_STYLE = "streets-v12";
    private static final double MIN_ARROW_LENGTH_KM = 0.5;
    private static final double MAX_ARROW_LENGTH_KM = 5.0;
    private static final double KNOTS_PER_KM = 4.0;

    private final OkHttpClient httpClient;

    public MapboxStaticMapService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String generateMapUrl(Position destinationPos, Position shipPos) {
        return null;
    }

    @Override
    public byte[] generateStaticMapImage(Position destinationPos, Position shipPos, int shipHeading, double shipSpeed, String accessToken) throws IOException {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Mapbox access token is required");
        }

        Point centerPoint = Point.fromLngLat(
                (shipPos.longitude() + destinationPos.longitude()) / 2,
                (shipPos.latitude() + destinationPos.latitude()) / 2
        );

        int zoom = calculateZoom(shipPos.latitude(), shipPos.longitude(),
                                 destinationPos.latitude(), destinationPos.longitude());

        double arrowLengthKm = calculateArrowLength(shipSpeed);
        Point headingEnd = calculateHeadingEndpoint(shipPos.latitude(), shipPos.longitude(), shipHeading, arrowLengthKm);

        try {
            List<StaticMarkerAnnotation> markers = List.of(
                    StaticMarkerAnnotation.builder()
                            .name("pin-l")
                            .lnglat(Point.fromLngLat(shipPos.longitude(), shipPos.latitude()))
//                            .label(String.valueOf(shipHeading))
                            .color("ff0000")
                            .build(),
                    StaticMarkerAnnotation.builder()
                            .name("pin-l")
                            .lnglat(Point.fromLngLat(destinationPos.longitude(), destinationPos.latitude()))
//                            .label("1")
                            .color("1e5c93")
                            .build()
            );

            List<Point> linePoints = List.of(
                    Point.fromLngLat(shipPos.longitude(), shipPos.latitude()),
                    headingEnd
            );
            String encodedPolyline = PolylineUtils.encode(linePoints, 5);

            List<StaticPolylineAnnotation> polylines = List.of(
                    StaticPolylineAnnotation.builder()
                            .polyline(encodedPolyline)
                            .strokeColor("ff0000")
                            .strokeWidth(4.0)
                            .build()
            );

            MapboxStaticMap staticMap = MapboxStaticMap.builder()
                    .accessToken(accessToken)
                    .styleId(MAP_STYLE)
                    .cameraPoint(centerPoint)
                    .cameraZoom(zoom)
                    .width(IMAGE_WIDTH)
                    .height(IMAGE_HEIGHT)
                    .retina(true)
                    .staticMarkerAnnotations(markers)
                    .staticPolylineAnnotations(polylines)
                    .build();

            String url = staticMap.url().toString();
            log.info("Generated Mapbox static map URL: {}", url);

            return downloadStaticMapImage(url);
        } catch (Exception e) {
            log.error("Failed to generate Mapbox static map", e);
            throw new IOException("Failed to generate Mapbox static map: " + e.getMessage(), e);
        }
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

    private byte[] downloadStaticMapImage(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (okhttp3.Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download map image: " + response);
            }
            return response.body() != null ? response.body().bytes() : null;
        }
    }

    private Point calculateHeadingEndpoint(double lat, double lon, int heading, double lengthKm) {
        double bearingRad = Math.toRadians(heading);
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        double angularDistance = lengthKm / 6371.0;

        double endLatRad = Math.asin(
                Math.sin(latRad) * Math.cos(angularDistance) +
                Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearingRad)
        );

        double endLonRad = lonRad + Math.atan2(
                Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(latRad),
                Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(endLatRad)
        );

        return Point.fromLngLat(Math.toDegrees(endLonRad), Math.toDegrees(endLatRad));
    }

    private double calculateArrowLength(double speedKnots) {
        if (speedKnots <= 0) {
            return MIN_ARROW_LENGTH_KM;
        }
        double length = speedKnots / KNOTS_PER_KM;
        return Math.min(length, MAX_ARROW_LENGTH_KM);
    }

    private int calculateZoom(double lat1, double lon1, double lat2, double lon2) {
        double latDiff = Math.abs(lat1 - lat2);
        double lonDiff = Math.abs(lon1 - lon2);
        double maxDiff = Math.max(latDiff, lonDiff);

        if (maxDiff <= 0.005) return 15;
        if (maxDiff <= 0.01) return 14;
        if (maxDiff <= 0.02) return 13;
        if (maxDiff <= 0.05) return 12;
        if (maxDiff <= 0.1) return 11;
        if (maxDiff <= 0.2) return 10;
        if (maxDiff <= 0.5) return 9;
        if (maxDiff <= 1.0) return 8;
        if (maxDiff <= 2.0) return 7;
        return 6;
    }
}
