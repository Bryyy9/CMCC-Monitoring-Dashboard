package com.nuxatech.cmcc.service;

import com.nuxatech.cmcc.entity.HealthCheckLogEntity;
import com.nuxatech.cmcc.entity.ServiceEntity;
import com.nuxatech.cmcc.entity.ServiceStatus;
import com.nuxatech.cmcc.repository.HealthCheckLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final RestTemplate restTemplate;
    private final HealthCheckLogRepository healthCheckLogRepository;

    public HealthCheckService(RestTemplate restTemplate, HealthCheckLogRepository healthCheckLogRepository) {
        this.restTemplate = restTemplate;
        this.healthCheckLogRepository = healthCheckLogRepository;
    }

    public HealthCheckLogEntity checkService(ServiceEntity service) {
        long start = System.currentTimeMillis();
        HealthCheckLogEntity logEntry = new HealthCheckLogEntity();
        logEntry.setServiceId(service.getId());

        try {
            var response = restTemplate.getForEntity(service.getUrl(), String.class);
            long latency = System.currentTimeMillis() - start;

            if (response.getStatusCode().is2xxSuccessful()) {
                logEntry.setStatus(ServiceStatus.UP);
                logEntry.setLatencyMs(latency);
                logEntry.setErrorMessage(null);
            } else {
                setDownResult(logEntry, latency, "HTTP " + response.getStatusCode().value());
            }
        } catch (ResourceAccessException e) {
            setDownResult(logEntry, System.currentTimeMillis() - start, "Timeout or connection error: " + e.getMessage());
        } catch (Exception e) {
            setDownResult(logEntry, System.currentTimeMillis() - start, e.getMessage());
        }

        logEntry.setCheckedAt(Instant.now());
        healthCheckLogRepository.save(logEntry);

        service.setStatus(logEntry.getStatus());
        service.setLastCheckedAt(logEntry.getCheckedAt());
        service.setLatencyMs(logEntry.getLatencyMs());

        log.debug("Checked service {} ({}): {} in {}ms",
            service.getName(), service.getUrl(), logEntry.getStatus(), logEntry.getLatencyMs());

        return logEntry;
    }

    private void setDownResult(HealthCheckLogEntity logEntry, long latency, String errorMessage) {
        logEntry.setStatus(ServiceStatus.DOWN);
        logEntry.setLatencyMs(latency);
        logEntry.setErrorMessage(errorMessage);
    }
}
