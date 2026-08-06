package net.jon.stravafetcher.exception;

import java.time.Duration;

public class StravaRateLimitException extends RuntimeException {
    private final Duration retryAfter;

    public StravaRateLimitException(Duration retryAfter) {
        super("Strava API rate limit exceeded, retry after " + retryAfter);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
