package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.Position;

public interface ShipAlertService {
    void sendShipAlert(AISShip ship, String destination, Position destinationPosition);
}
