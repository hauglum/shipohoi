package no.hauglum.ship_o_hoi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class WatchedShip {

    @Id
    private String mmsi;
    private String shipName;
    @Column(nullable = false)
    private Instant addedAt;

    protected WatchedShip() {}

    public WatchedShip(String mmsi, String shipName, Instant addedAt) {
        this.mmsi = mmsi;
        this.shipName = shipName;
        this.addedAt = addedAt;
    }

    public String getMmsi() { return mmsi; }
    public String getShipName() { return shipName; }
    public Instant getAddedAt() { return addedAt; }
}
