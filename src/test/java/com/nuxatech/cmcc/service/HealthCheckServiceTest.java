package com.nuxatech.cmcc.service;

import com.nuxatech.cmcc.entity.HealthCheckLogEntity;
import com.nuxatech.cmcc.entity.ServiceEntity;
import com.nuxatech.cmcc.entity.ServiceStatus;
import com.nuxatech.cmcc.repository.HealthCheckLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private HealthCheckLogRepository logRepository;

    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() {
        healthCheckService = new HealthCheckService(restTemplate, logRepository);
    }

    private ServiceEntity serviceWith(String url) {
        var s = new ServiceEntity();
        s.setId(UUID.randomUUID());
        s.setName("Test");
        s.setUrl(url);
        return s;
    }

    @Test
    void checkService_When2xx_ReturnsUp() {
        var service = serviceWith("https://example.com/health");
        when(restTemplate.getForEntity(service.getUrl(), String.class))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HealthCheckLogEntity result = healthCheckService.checkService(service);

        assertThat(result.getStatus()).isEqualTo(ServiceStatus.UP);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getLatencyMs()).isNotNull();
        assertThat(service.getStatus()).isEqualTo(ServiceStatus.UP);
    }

    @Test
    void checkService_WhenNon2xx_ReturnsDown() {
        var service = serviceWith("https://example.com/health");
        when(restTemplate.getForEntity(service.getUrl(), String.class))
            .thenReturn(new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR));
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HealthCheckLogEntity result = healthCheckService.checkService(service);

        assertThat(result.getStatus()).isEqualTo(ServiceStatus.DOWN);
        assertThat(result.getErrorMessage()).contains("500");
    }

    @Test
    void checkService_WhenTimeout_ReturnsDown() {
        var service = serviceWith("https://example.com/health");
        when(restTemplate.getForEntity(service.getUrl(), String.class))
            .thenThrow(new ResourceAccessException("Connection timed out"));
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HealthCheckLogEntity result = healthCheckService.checkService(service);

        assertThat(result.getStatus()).isEqualTo(ServiceStatus.DOWN);
        assertThat(result.getErrorMessage()).contains("Timeout");
    }

    @Test
    void checkService_UpdatesServiceEntityFields() {
        var service = serviceWith("https://example.com/health");
        when(restTemplate.getForEntity(service.getUrl(), String.class))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        healthCheckService.checkService(service);

        assertThat(service.getStatus()).isEqualTo(ServiceStatus.UP);
        assertThat(service.getLastCheckedAt()).isNotNull();
        assertThat(service.getLatencyMs()).isNotNull();
    }
}
