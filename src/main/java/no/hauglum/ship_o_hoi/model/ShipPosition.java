package no.hauglum.ship_o_hoi.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ShipPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mmsi;
    private String shipName;
    private double latitude;
    private double longitude;
    private Double speed;
    private Double course;

    @Column(nullable = false)
    private Instant recordedAt;

    protected ShipPosition() {}

    public ShipPosition(String mmsi, String shipName, double latitude, double longitude,
                        Double speed, Double course, Instant recordedAt) {
        this.mmsi = mmsi;
        this.shipName = shipName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.course = course;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public String getMmsi() { return mmsi; }
    public String getShipName() { return shipName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Double getSpeed() { return speed; }
    public Double getCourse() { return course; }
    public Instant getRecordedAt() { return recordedAt; }
}
