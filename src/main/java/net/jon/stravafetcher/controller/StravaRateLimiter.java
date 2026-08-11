package net.jon.stravafetcher.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Tracks Strava's live rate-limit usage, as reported on every API response,
 * so callers can stop issuing requests before hitting a 429 rather than
 * reacting to one after the fact.
 */
@Component
public class StravaRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(StravaRateLimiter.class);
    // This service only issues non-upload (read) requests, so track the
    // read-specific limit rather than the overall (read+upload) one.
    private static final String LIMIT_HEADER = "X-ReadRateLimit-Limit";
    private static final String USAGE_HEADER = "X-ReadRateLimit-Usage";

    // Stop short of Strava's published limits to leave headroom for
    // concurrent traffic (token refreshes, manual debugging, etc.).
    private static final int SAFETY_MARGIN = 20;

    // Strava's default non-upload limits: 100 per 15 min, 1000 per day.
    private volatile int shortTermUsage = 0;
    private volatile int shortTermLimit = 100;
    private volatile int dailyUsage = 0;
    private volatile int dailyLimit = 1000;
    private volatile Instant rateLimitedUntil;

    public void updateFromHeaders(HttpHeaders headers) {
        if (headers == null) {
            return;
        }
        int[] usage = parsePair(headers.getFirst(USAGE_HEADER));
        int[] limit = parsePair(headers.getFirst(LIMIT_HEADER));
        if (usage != null) {
            shortTermUsage = usage[0];
            dailyUsage = usage[1];
        }
        if (limit != null) {
            shortTermLimit = limit[0];
            dailyLimit = limit[1];
        }
        log.debug("Strava rate limit usage: {}/{} (15-min), {}/{} (daily)",
                shortTermUsage, shortTermLimit, dailyUsage, dailyLimit);
    }

    public void markRateLimited(Duration retryAfter) {
        this.rateLimitedUntil = Instant.now().plus(retryAfter);
        log.warn("Strava rate limit hit, backing off until {}", rateLimitedUntil);
    }

    public boolean isExhausted() {
        return notHasBudgetFor(1);
    }

    public boolean notHasBudgetFor(int requestCount) {
        if (rateLimitedUntil != null && Instant.now().isBefore(rateLimitedUntil)) {
            return true;
        }
        return shortTermUsage + requestCount > shortTermLimit - SAFETY_MARGIN
                || dailyUsage + requestCount > dailyLimit - SAFETY_MARGIN;
    }

    public int getShortTermUsage() {
        return shortTermUsage;
    }

    public int getShortTermLimit() {
        return shortTermLimit;
    }

    public int getDailyUsage() {
        return dailyUsage;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    private int[] parsePair(String headerValue) {
        if (headerValue == null) {
            return null;
        }
        String[] parts = headerValue.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new int[] { Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()) };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
