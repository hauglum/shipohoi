package no.hauglum.ship_o_hoi.repository;

import no.hauglum.ship_o_hoi.model.ShipPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShipPositionRepository extends JpaRepository<ShipPosition, Long> {

    List<ShipPosition> findByMmsiOrderByRecordedAtAsc(String mmsi);

    @Query("SELECT DISTINCT s.mmsi FROM ShipPosition s")
    List<String> findDistinctMmsi();
}
