package com.stockbroker.backend.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hand-rolled in-memory fixed-window rate limiter (SRS Appendix D). Covers
 * only login/registration abuse, the two most valuable brute-force/spam
 * vectors to defend - the full per-scenario rate-limit matrix (search,
 * writes, password reset) is out of scope. Deliberately dependency-free
 * (no Bucket4j) to avoid adding a new library for this subset.
 *
 * Not distributed - resets on restart and is per-instance only, which is
 * an acceptable simplification for a single-node deployment.
 */
@Component
public class RateLimiter {

    private record Window(AtomicInteger count, Instant windowStart) {
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * @return true if the call is allowed (and is now counted), false if the
     *         caller has exceeded maxAttempts within windowSeconds.
     */
    public boolean tryAcquire(String key, int maxAttempts, long windowSeconds) {

        Instant now = Instant.now();

        Window window = windows.compute(key, (k, existing) -> {

            if (existing == null
                    || now.isAfter(existing.windowStart().plusSeconds(windowSeconds))) {
                return new Window(new AtomicInteger(1), now);
            }

            existing.count().incrementAndGet();
            return existing;
        });

        return window.count().get() <= maxAttempts;
    }
}
