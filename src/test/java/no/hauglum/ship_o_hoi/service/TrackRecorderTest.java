package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.config.TrackingProperties;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.repository.ShipPositionRepository;
import no.hauglum.ship_o_hoi.repository.WatchedShipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrackRecorderTest {

    private ShipPositionRepository repository;
    private WatchedShipRepository watchedShipRepository;
    private TrackRecorder recorder;

    @BeforeEach
    void setUp() {
        repository = mock(ShipPositionRepository.class);
        watchedShipRepository = mock(WatchedShipRepository.class);
        when(watchedShipRepository.findAll()).thenReturn(List.of());
        TrackingProperties props = new TrackingProperties();
        props.setMmsiWatchlist(List.of());
        recorder = new TrackRecorder(repository, watchedShipRepository, props);
        recorder.loadWatchlist();
    }

    @Test
    void firstPositionIsAlwaysStored() {
        recorder.record(ship("111", 60.0, 5.0));
        verify(repository, times(1)).save(any());
    }

    @Test
    void samePositionIsNotStoredAgain() {
        recorder.record(ship("111", 60.0, 5.0));
        recorder.record(ship("111", 60.0, 5.0));
        verify(repository, times(1)).save(any());
    }

    @Test
    void gpsJitterWithin50mIsNotStored() {
        recorder.record(ship("111", 60.0, 5.0));
        recorder.record(ship("111", 60.00027, 5.0)); // ~30 m north
        verify(repository, times(1)).save(any());
    }

    @Test
    void movementBeyond50mIsStored() {
        recorder.record(ship("111", 60.0, 5.0));
        recorder.record(ship("111", 60.0018, 5.0)); // ~200 m north
        verify(repository, times(2)).save(any());
    }

    @Test
    void differentMmsiTrackedIndependently() {
        recorder.record(ship("111", 60.0, 5.0));
        recorder.record(ship("222", 60.0, 5.0));
        verify(repository, times(2)).save(any());
    }

    @Test
    void newShipIsAutoWatchlisted() {
        recorder.addToWatchlistIfNew(ship("333", 60.0, 5.0));
        verify(watchedShipRepository, times(1)).save(any());
    }

    @Test
    void alreadyWatchlistedShipIsNotSavedAgain() {
        recorder.addToWatchlistIfNew(ship("333", 60.0, 5.0));
        recorder.addToWatchlistIfNew(ship("333", 60.0, 5.0));
        verify(watchedShipRepository, times(1)).save(any());
    }

    private AISShip ship(String mmsi, double lat, double lon) {
        return new AISShip(mmsi, "TEST", lat, lon, null, null, null, null, null);
    }
}
