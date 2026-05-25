package no.hauglum.ship_o_hoi.controller;

import no.hauglum.ship_o_hoi.model.ShipPosition;
import no.hauglum.ship_o_hoi.repository.ShipPositionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TrackController {

    private final ShipPositionRepository repository;

    public TrackController(ShipPositionRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/tracks")
    public List<String> trackedMmsis() {
        return repository.findDistinctMmsi();
    }

    @GetMapping("/api/tracks/{mmsi}")
    public Map<String, Object> track(@PathVariable String mmsi) {
        List<ShipPosition> positions = repository.findByMmsiOrderByRecordedAtAsc(mmsi);

        List<List<Double>> coordinates = positions.stream()
                .map(p -> List.of(p.getLongitude(), p.getLatitude()))
                .toList();

        String shipName = positions.isEmpty() ? mmsi : positions.get(positions.size() - 1).getShipName();

        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("mmsi", mmsi);
        properties.put("name", shipName);
        properties.put("pointCount", positions.size());

        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("geometry", geometry);
        feature.put("properties", properties);

        Map<String, Object> featureCollection = new LinkedHashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", List.of(feature));

        return featureCollection;
    }
}
