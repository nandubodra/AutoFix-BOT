package com.nandubodra.sitesentinel.security;

import com.nandubodra.sitesentinel.config.SiteSentinelProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final SiteSentinelProperties properties;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(SiteSentinelProperties properties) {
        this.properties = properties;
    }

    public boolean isBlocked(String key) {
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }
        cleanup(key, state);
        return state.count >= properties.getSecurity().getMaxLoginAttempts();
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, state) -> {
            Instant now = Instant.now();
            if (state == null || state.windowStart.plusSeconds(properties.getSecurity().getLoginWindowSeconds()).isBefore(now)) {
                return new AttemptState(1, now);
            }
            return new AttemptState(state.count + 1, state.windowStart);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private void cleanup(String key, AttemptState state) {
        if (state.windowStart.plusSeconds(properties.getSecurity().getLoginWindowSeconds()).isBefore(Instant.now())) {
            attempts.remove(key);
        }
    }

    private record AttemptState(int count, Instant windowStart) {}
}
