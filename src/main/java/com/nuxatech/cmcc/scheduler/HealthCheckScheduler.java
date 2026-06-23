package com.nuxatech.cmcc.scheduler;

import com.nuxatech.cmcc.service.HealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final HealthCheckService healthCheckService;

    public HealthCheckScheduler(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    public boolean isRunning() {
        return running.get();
    }

    @Scheduled(fixedRate = 60_000)
    public void runHealthChecks() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Previous health check cycle still in progress, skipping");
            return;
        }

        try {
            healthCheckService.checkAllServices();
            log.info("Health check cycle completed");
        } catch (Exception e) {
            log.error("Health check cycle failed", e);
        } finally {
            running.set(false);
        }
    }
}
