package com.nuxatech.cmcc.service;

import com.nuxatech.cmcc.dto.CreateServiceRequest;
import com.nuxatech.cmcc.dto.ServiceResponse;
import com.nuxatech.cmcc.dto.UpdateServiceRequest;
import com.nuxatech.cmcc.entity.ServiceEntity;
import com.nuxatech.cmcc.entity.ServiceStatus;
import com.nuxatech.cmcc.exception.ServiceNotFoundException;
import com.nuxatech.cmcc.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    private ServiceService serviceService;

    @BeforeEach
    void setUp() {
        serviceService = new ServiceService(serviceRepository);
    }

    @Test
    void getAllServices_ReturnsAllServices() {
        var service = new ServiceEntity();
        service.setId(UUID.randomUUID());
        service.setName("Test Service");
        service.setUrl("https://example.com/health");
        service.setCategory("Internal");
        service.setStatus(ServiceStatus.UNKNOWN);

        when(serviceRepository.findAll()).thenReturn(List.of(service));

        List<ServiceResponse> result = serviceService.getAllServices();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Service");
        verify(serviceRepository).findAll();
    }

    @Test
    void getServiceById_WhenExists_ReturnsService() {
        UUID id = UUID.randomUUID();
        var service = new ServiceEntity();
        service.setId(id);
        service.setName("Test");
        service.setUrl("https://example.com");
        service.setCategory("Internal");

        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));

        ServiceResponse result = serviceService.getServiceById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Test");
    }

    @Test
    void getServiceById_WhenNotExists_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.getServiceById(id))
            .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void createService_SetsUnknownStatus() {
        var request = new CreateServiceRequest();
        request.setName("New Service");
        request.setUrl("https://example.com/health");
        request.setCategory("External");

        var saved = new ServiceEntity();
        saved.setId(UUID.randomUUID());
        saved.setName("New Service");
        saved.setUrl("https://example.com/health");
        saved.setCategory("External");
        saved.setStatus(ServiceStatus.UNKNOWN);

        when(serviceRepository.save(any())).thenReturn(saved);

        ServiceResponse result = serviceService.createService(request);

        assertThat(result.getStatus()).isEqualTo(ServiceStatus.UNKNOWN);
        assertThat(result.getName()).isEqualTo("New Service");
    }

    @Test
    void updateService_WhenExists_UpdatesFields() {
        UUID id = UUID.randomUUID();
        var existing = new ServiceEntity();
        existing.setId(id);
        existing.setName("Old Name");
        existing.setUrl("https://old.com");
        existing.setCategory("Old");

        var request = new UpdateServiceRequest();
        request.setName("New Name");
        request.setUrl("https://new.com");
        request.setCategory("New");

        var updated = new ServiceEntity();
        updated.setId(id);
        updated.setName("New Name");
        updated.setUrl("https://new.com");
        updated.setCategory("New");

        when(serviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(serviceRepository.save(any())).thenReturn(updated);

        ServiceResponse result = serviceService.updateService(id, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getUrl()).isEqualTo("https://new.com");
    }

    @Test
    void deleteService_WhenNotExists_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(serviceRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> serviceService.deleteService(id))
            .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void deleteService_WhenExists_Deletes() {
        UUID id = UUID.randomUUID();
        when(serviceRepository.existsById(id)).thenReturn(true);

        serviceService.deleteService(id);

        verify(serviceRepository).deleteById(id);
    }
}
