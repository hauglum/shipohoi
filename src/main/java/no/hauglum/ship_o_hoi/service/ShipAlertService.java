package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;

public interface ShipAlertService {
    void sendShipAlert(AISShip ship, String destination);
}
