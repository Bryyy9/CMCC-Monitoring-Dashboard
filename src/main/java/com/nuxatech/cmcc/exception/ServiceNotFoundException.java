package com.nuxatech.cmcc.exception;

import java.util.UUID;

public class ServiceNotFoundException extends RuntimeException {

    private final UUID serviceId;

    public ServiceNotFoundException(UUID serviceId) {
        super("Service not found: " + serviceId);
        this.serviceId = serviceId;
    }

    public UUID getServiceId() { return serviceId; }
}
