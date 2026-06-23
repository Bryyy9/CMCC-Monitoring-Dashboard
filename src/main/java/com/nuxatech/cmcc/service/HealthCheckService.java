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
    private final ServiceService serviceService;

    public HealthCheckService(RestTemplate restTemplate,
                              HealthCheckLogRepository healthCheckLogRepository,
                              ServiceService serviceService) {
        this.restTemplate = restTemplate;
        this.healthCheckLogRepository = healthCheckLogRepository;
        this.serviceService = serviceService;
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
                logEntry.setStatus(ServiceStatus.DOWN);
                logEntry.setLatencyMs(latency);
                logEntry.setErrorMessage("HTTP " + response.getStatusCode().value());
            }
        } catch (ResourceAccessException e) {
            long latency = System.currentTimeMillis() - start;
            logEntry.setStatus(ServiceStatus.DOWN);
            logEntry.setLatencyMs(latency);
            logEntry.setErrorMessage("Timeout or connection error: " + e.getMessage());
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            logEntry.setStatus(ServiceStatus.DOWN);
            logEntry.setLatencyMs(latency);
            logEntry.setErrorMessage(e.getMessage());
        }

        logEntry.setCheckedAt(Instant.now());
        healthCheckLogRepository.save(logEntry);

        service.setStatus(logEntry.getStatus());
        service.setLastCheckedAt(logEntry.getCheckedAt());
        service.setLatencyMs(logEntry.getLatencyMs());
        serviceService.saveServiceEntity(service);

        log.debug("Checked service {} ({}): {} in {}ms",
            service.getName(), service.getUrl(), logEntry.getStatus(), logEntry.getLatencyMs());

        return logEntry;
    }
}
