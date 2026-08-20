package com.seatsure.seatsure.security;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

// One bucket per user email, held in memory. ConcurrentHashMap is used
// deliberately - multiple requests from different users can hit this
// map concurrently, and it needs to be thread-safe without us manually
// synchronizing anything.
@Component
public class RateLimiterService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    // computeIfAbsent: if a bucket already exists for this key, return it;
    // otherwise, atomically create and store a new one. This is how we get
    // "one bucket per user, created lazily on first request" without any
    // race condition between two of that user's requests arriving at once.
    public Bucket resolveBucket(String userEmail) {
        return buckets.computeIfAbsent(userEmail, key -> createNewBucket());
    }

    private Bucket createNewBucket() {
        // Current Bucket4j builder-style API: capacity 5, refilling 5
        // tokens every 1 minute, greedy refill (tokens trickle back
        // proportionally over time, not all at once at the minute mark).
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
                .build();
    }
}