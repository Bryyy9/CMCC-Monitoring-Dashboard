package com.nuxatech.cmcc.service;

import com.nuxatech.cmcc.dto.CreateServiceRequest;
import com.nuxatech.cmcc.dto.ServiceResponse;
import com.nuxatech.cmcc.dto.UpdateServiceRequest;
import com.nuxatech.cmcc.entity.ServiceEntity;
import com.nuxatech.cmcc.exception.ServiceNotFoundException;
import com.nuxatech.cmcc.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAll().stream()
            .map(ServiceResponse::fromEntity)
            .toList();
    }

    public ServiceResponse getServiceById(UUID id) {
        return serviceRepository.findById(id)
            .map(ServiceResponse::fromEntity)
            .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    public ServiceResponse createService(CreateServiceRequest request) {
        ServiceEntity entity = new ServiceEntity();
        entity.setName(request.getName());
        entity.setUrl(request.getUrl());
        entity.setCategory(request.getCategory());
        entity.setStatus(com.nuxatech.cmcc.entity.ServiceStatus.UNKNOWN);
        return ServiceResponse.fromEntity(serviceRepository.save(entity));
    }

    public ServiceResponse updateService(UUID id, UpdateServiceRequest request) {
        ServiceEntity entity = serviceRepository.findById(id)
            .orElseThrow(() -> new ServiceNotFoundException(id));
        entity.setName(request.getName());
        entity.setUrl(request.getUrl());
        entity.setCategory(request.getCategory());
        return ServiceResponse.fromEntity(serviceRepository.save(entity));
    }

    public void deleteService(UUID id) {
        if (!serviceRepository.existsById(id)) {
            throw new ServiceNotFoundException(id);
        }
        serviceRepository.deleteById(id);
    }

    public ServiceEntity getServiceEntity(UUID id) {
        return serviceRepository.findById(id)
            .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    public List<ServiceEntity> getAllServiceEntities() {
        return serviceRepository.findAll();
    }

    public ServiceEntity saveServiceEntity(ServiceEntity entity) {
        return serviceRepository.save(entity);
    }
}
