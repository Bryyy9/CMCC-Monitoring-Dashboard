package com.nuxatech.cmcc.dto;

import com.nuxatech.cmcc.entity.ServiceStatus;

import java.time.Instant;
import java.util.UUID;

public class CheckResponse {

    private UUID serviceId;
    private ServiceStatus status;
    private Long latencyMs;
    private Instant checkedAt;

    public CheckResponse(UUID serviceId, ServiceStatus status, Long latencyMs, Instant checkedAt) {
        this.serviceId = serviceId;
        this.status = status;
        this.latencyMs = latencyMs;
        this.checkedAt = checkedAt;
    }

    public UUID getServiceId() { return serviceId; }
    public ServiceStatus getStatus() { return status; }
    public Long getLatencyMs() { return latencyMs; }
    public Instant getCheckedAt() { return checkedAt; }
}
