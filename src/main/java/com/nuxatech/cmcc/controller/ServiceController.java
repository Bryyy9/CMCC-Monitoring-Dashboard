package com.nuxatech.cmcc.controller;

import com.nuxatech.cmcc.dto.CheckResponse;
import com.nuxatech.cmcc.dto.CreateServiceRequest;
import com.nuxatech.cmcc.dto.ServiceResponse;
import com.nuxatech.cmcc.dto.UpdateServiceRequest;
import com.nuxatech.cmcc.service.HealthCheckService;
import com.nuxatech.cmcc.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final HealthCheckService healthCheckService;

    public ServiceController(ServiceService serviceService, HealthCheckService healthCheckService) {
        this.serviceService = serviceService;
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    public List<ServiceResponse> getAllServices() {
        return serviceService.getAllServices();
    }

    @GetMapping("/{id}")
    public ServiceResponse getService(@PathVariable UUID id) {
        return serviceService.getServiceById(id);
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ServiceResponse updateService(@PathVariable UUID id, @Valid @RequestBody UpdateServiceRequest request) {
        return serviceService.updateService(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check")
    public CheckResponse forceCheck(@PathVariable UUID id) {
        var entity = serviceService.getServiceEntity(id);
        var result = healthCheckService.checkService(entity);
        return new CheckResponse(
            result.getServiceId(),
            result.getStatus(),
            result.getLatencyMs(),
            result.getCheckedAt()
        );
    }
}
