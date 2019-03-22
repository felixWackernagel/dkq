package de.wackernagel.dkq.utils;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

import androidx.collection.ArrayMap;

/**
 * Utility class that decides whether we should fetch some data or not.
 */
public class RateLimiter<KEY> {

    private final ArrayMap<KEY, Long> timestamps = new ArrayMap<>();
    private final long timeout;

    public RateLimiter( final long timeout, final TimeUnit timeUnit ) {
        this.timeout = timeUnit.toMillis( timeout );
    }

    public synchronized boolean shouldFetch(KEY key) {
        final Long lastFetched = timestamps.get(key);
        final long now = SystemClock.uptimeMillis();
        if (lastFetched == null) {
            timestamps.put(key, now);
            return true;
        }
        if( now - lastFetched > timeout) {
            timestamps.put(key, now);
            return true;
        }
        return false;
    }

    public synchronized void reset( KEY key ) {
        timestamps.remove(key);
    }
}