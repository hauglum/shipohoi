package no.hauglum.ship_o_hoi.repository;

import no.hauglum.ship_o_hoi.model.WatchedShip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchedShipRepository extends JpaRepository<WatchedShip, String> {
}
