package com.LunaLink.application.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(10);
    private static final int MAX_BUCKET_REQUESTS = 10;
    private static final Duration BUCKET_WINDOW = Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, LockoutState> lockouts = new ConcurrentHashMap<>();

    public boolean isLocked(String email) {
        if (email == null) {
            return false;
        }
        LockoutState state = lockouts.get(email);
        if (state == null || state.lockedUntil == null) {
            return false;
        }
        if (state.lockedUntil.isAfter(Instant.now())) {
            return true;
        }
        lockouts.remove(email);
        return false;
    }

    public boolean tryConsume(String clientIp, String email) {
        if (isLocked(email)) {
            return false;
        }
        String key = (clientIp == null ? "unknown" : clientIp) + "|" + email;
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                        .addLimit(Bandwidth.classic(MAX_BUCKET_REQUESTS, Refill.greedy(MAX_BUCKET_REQUESTS, BUCKET_WINDOW)))
                        .build())
                .tryConsume(1);
    }

    public void onAuthenticationFailure(String email) {
        if (email == null) {
            return;
        }
        LockoutState state = lockouts.computeIfAbsent(email, k -> new LockoutState());
        state.failures++;
        if (state.failures >= MAX_ATTEMPTS) {
            state.lockedUntil = Instant.now().plus(LOCKOUT_DURATION);
            state.failures = 0;
        }
    }

    public void onAuthenticationSuccess(String email) {
        if (email != null) {
            lockouts.remove(email);
        }
    }

    private static class LockoutState {
        private int failures;
        private Instant lockedUntil;
    }
}
