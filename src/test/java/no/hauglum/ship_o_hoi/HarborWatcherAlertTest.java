package no.hauglum.ship_o_hoi;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.model.DestinationProfile;
import no.hauglum.ship_o_hoi.model.HarbourArea;
import no.hauglum.ship_o_hoi.model.Position;
import no.hauglum.ship_o_hoi.service.HarbourPresence;
import no.hauglum.ship_o_hoi.service.ShipAlertService;
import no.hauglum.ship_o_hoi.service.TrackRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Covers the alerting decision in {@code handleShip}: a ship is alerted on while it is
 * inbound, announced once when it moors, and left alone afterwards.
 */
class HarborWatcherAlertTest {

    private static final Position ENGEBO = new Position(61.487982, 5.442754);
    private static final DestinationProfile DESTINATION =
            new DestinationProfile("Engebø", Set.of("engebo"), ENGEBO);
    private static final String MMSI = "304011027";

    private ShipAlertService alertService;
    private TrackRecorder trackRecorder;
    private HarborWatcher watcher;

    @BeforeEach
    void setUp() {
        alertService = mock(ShipAlertService.class);
        trackRecorder = mock(TrackRecorder.class);
        HarbourPresence presence = new HarbourPresence(new HarbourArea(ENGEBO, 1000, 0.5));
        watcher = new HarborWatcher(null, null, alertService, null, trackRecorder, presence);
    }

    @Test
    void inboundShipIsAlertedOn() throws Exception {
        handle(inbound());

        verify(alertService).sendShipAlert(any(), anyString(), any());
        verify(alertService, never()).sendArrivalAlert(any(), anyString());
    }

    @Test
    void berthedShipIsAnnouncedOnceAndThenLeftAlone() throws Exception {
        handle(inbound());
        handle(berthed());
        handle(berthed());
        handle(berthed());

        verify(alertService, times(1)).sendArrivalAlert(any(), anyString());
    }

    @Test
    void berthedShipStopsProducingApproachAlerts() throws Exception {
        handle(berthed());
        handle(berthed());

        verify(alertService, never()).sendShipAlert(any(), anyString(), any());
    }

    @Test
    void shipStoppedOutsideTheHarbourStillProducesApproachAlerts() throws Exception {
        handle(ship(ENGEBO.latitude() + 0.018, ENGEBO.longitude(), 0.0, "ENGEBO"));

        verify(alertService).sendShipAlert(any(), anyString(), any());
        verify(alertService, never()).sendArrivalAlert(any(), anyString());
    }

    @Test
    void unrelatedShipIsIgnored() throws Exception {
        handle(ship(60.0, 5.0, 12.0, "ROTTERDAM"));

        verify(alertService, never()).sendShipAlert(any(), anyString(), any());
        verify(alertService, never()).sendArrivalAlert(any(), anyString());
        verify(trackRecorder, never()).record(any());
    }

    private void handle(AISShip ship) throws Exception {
        Method method = HarborWatcher.class.getDeclaredMethod(
                "handleShip", AISShip.class, DestinationProfile.class);
        method.setAccessible(true);
        method.invoke(watcher, ship, DESTINATION);
    }

    private AISShip inbound() {
        return ship(61.6, 5.5, 12.0, "ENGEBO");
    }

    private AISShip berthed() {
        return ship(ENGEBO.latitude(), ENGEBO.longitude(), 0.1, "ENGEBO");
    }

    private AISShip ship(double latitude, double longitude, Double speed, String destination) {
        return new AISShip(MMSI, "THESEUS", latitude, longitude, speed, null, null, destination, null);
    }
}
