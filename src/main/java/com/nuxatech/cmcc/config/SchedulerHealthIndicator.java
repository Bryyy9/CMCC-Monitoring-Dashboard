package com.nuxatech.cmcc.config;

import com.nuxatech.cmcc.scheduler.HealthCheckScheduler;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SchedulerHealthIndicator implements HealthIndicator {

    private final HealthCheckScheduler scheduler;

    public SchedulerHealthIndicator(HealthCheckScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Health health() {
        if (scheduler.isRunning()) {
            return Health.up().withDetail("scheduler", "running").build();
        }
        return Health.up().withDetail("scheduler", "idle").build();
    }
}
