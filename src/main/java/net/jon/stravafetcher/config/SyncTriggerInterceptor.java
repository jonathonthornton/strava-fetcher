package net.jon.stravafetcher.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.jon.stravafetcher.service.SyncTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Treats any incoming API request as a signal that someone's actually using
 * the app, and uses that to decide whether a Strava sync is due. Never
 * blocks or fails the request itself — {@link SyncTriggerService#trigger()}
 * is a cheap, cooldown-gated check that at most submits a background sync.
 */
@Component
public class SyncTriggerInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SyncTriggerInterceptor.class);

    private final SyncTriggerService syncTriggerService;

    public SyncTriggerInterceptor(SyncTriggerService syncTriggerService) {
        this.syncTriggerService = syncTriggerService;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        try {
            syncTriggerService.trigger();
        } catch (Exception e) {
            log.warn("Sync trigger check failed; continuing request unaffected", e);
        }
        return true;
    }
}
