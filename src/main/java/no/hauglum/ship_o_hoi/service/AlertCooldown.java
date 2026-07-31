package no.hauglum.ship_o_hoi.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Rate limits alerts to at most one per key within a cooldown window.
 * <p>
 * Safe for concurrent callers: when several AIS feeds deliver the same ship at
 * the same time, exactly one caller is granted the alert.
 */
public class AlertCooldown {

    private final Map<String, Instant> lastAlert = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration cooldown;

    public AlertCooldown(Clock clock, Duration cooldown) {
        this.clock = clock;
        this.cooldown = cooldown;
    }

    public boolean shouldAlert(String key) {
        Instant now = clock.instant();
        AtomicBoolean isAlertGranted = new AtomicBoolean(false);

        // compute() runs the remapping function under the map's per-key lock, so
        // simultaneous callers are serialised and only one can be granted.
        lastAlert.compute(key, (mmsi, previous) -> {
            if (isWithinCooldown(previous, now)) {
                return previous;
            }
            isAlertGranted.set(true);
            return now;
        });

        return isAlertGranted.get();
    }

    private boolean isWithinCooldown(Instant previous, Instant now) {
        return previous != null && previous.isAfter(now.minus(cooldown));
    }
}
