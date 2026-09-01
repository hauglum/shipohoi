package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.HarbourArea;
import no.hauglum.ship_o_hoi.model.Position;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which ships are currently berthed in the harbour.
 * <p>
 * Whether a ship is berthed follows from a single AIS message — position and speed — so
 * the verdict survives a restart: the first message from a moored ship re-establishes it.
 * The verdict is cached because one of the feeds (AISStream) delivers static data without
 * a speed, and those messages must not be mistaken for a ship that has left.
 * <p>
 * Safe for concurrent callers: when both feeds deliver the same arrival at the same time,
 * exactly one caller is told the ship has just arrived.
 */
public class HarbourPresence {

    private final Set<String> shipsInHarbour = ConcurrentHashMap.newKeySet();
    private final HarbourArea harbour;

    public HarbourPresence(HarbourArea harbour) {
        this.harbour = harbour;
    }

    /**
     * @return true exactly once per arrival, on the first message confirming the ship berthed.
     */
    public boolean hasJustArrived(AISShip ship) {
        if (isUnableToJudge(ship)) {
            return false;
        }
        if (harbour.isBerthed(new Position(ship.latitude(), ship.longitude()), ship.speed())) {
            return shipsInHarbour.add(ship.mmsi());
        }
        shipsInHarbour.remove(ship.mmsi());
        return false;
    }

    public boolean isInHarbour(String mmsi) {
        return shipsInHarbour.contains(mmsi);
    }

    /** Without a position or a speed the message says nothing, so the last verdict stands. */
    private boolean isUnableToJudge(AISShip ship) {
        return ship.latitude() == null || ship.longitude() == null || ship.speed() == null;
    }
}
