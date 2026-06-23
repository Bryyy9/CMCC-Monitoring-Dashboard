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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HealthCheckLogRepository logRepository;
    @Mock
    private ServiceService serviceService;

    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() {
        healthCheckService = new HealthCheckService(restTemplate, logRepository, serviceService);
    }

    @Test
    void checkService_When2xx_ReturnsUp() {
        var service = new ServiceEntity();
        service.setId(UUID.randomUUID());
        service.setUrl("https://example.com/health");

        var responseEntity = new org.springframework.http.ResponseEntity<>("OK", null,
            org.springframework.http.HttpStatus.OK);

        when(restTemplate.getForEntity(service.getUrl(), String.class)).thenReturn(responseEntity);
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(serviceService.saveServiceEntity(any())).thenAnswer(i -> i.getArgument(0));

        HealthCheckLogEntity result = healthCheckService.checkService(service);

        assertThat(result.getStatus()).isEqualTo(ServiceStatus.UP);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getLatencyMs()).isNotNull();
    }

    @Test
    void checkService_WhenNon2xx_ReturnsDown() {
        var service = new ServiceEntity();
        service.setId(UUID.randomUUID());
        service.setUrl("https://example.com/health");

        var responseEntity = new org.springframework.http.ResponseEntity<>("Error", null,
            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(service.getUrl(), String.class)).thenReturn(responseEntity);
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(serviceService.saveServiceEntity(any())).thenAnswer(i -> i.getArgument(0));

        HealthCheckLogEntity result = healthCheckService.checkService(service);

        assertThat(result.getStatus()).isEqualTo(ServiceStatus.DOWN);
        assertThat(result.getErrorMessage()).contains("500");
    }

    @Test
    void checkService_WhenTimeout_ReturnsDown() {
        var service = new ServiceEntity();
        service.setId(UUID.randomUUID());
        service.setUrl("https://example.com/health");

        when(restTemplate.getForEntity(service.getUrl(), String.class))
            .thenThrow(new ResourceAccessException("Connection timed out"));
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(serviceService.saveServiceEntity(any())).thenAnswer(i -> i.getArgument(0));

        HealthCheckLogEntity result = healthCheckService.checkService(service);

        assertThat(result.getStatus()).isEqualTo(ServiceStatus.DOWN);
        assertThat(result.getErrorMessage()).contains("Timeout");
    }

    @Test
    void checkService_UpdatesServiceEntity() {
        var service = new ServiceEntity();
        service.setId(UUID.randomUUID());
        service.setUrl("https://example.com/health");

        var responseEntity = new org.springframework.http.ResponseEntity<>("OK", null,
            org.springframework.http.HttpStatus.OK);

        when(restTemplate.getForEntity(service.getUrl(), String.class)).thenReturn(responseEntity);
        when(logRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(serviceService.saveServiceEntity(any())).thenAnswer(i -> i.getArgument(0));

        healthCheckService.checkService(service);

        assertThat(service.getStatus()).isEqualTo(ServiceStatus.UP);
        assertThat(service.getLastCheckedAt()).isNotNull();
        verify(serviceService).saveServiceEntity(service);
    }
}
