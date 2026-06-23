package com.nuxatech.cmcc.dto;

import com.nuxatech.cmcc.entity.ServiceEntity;
import com.nuxatech.cmcc.entity.ServiceStatus;

import java.time.Instant;
import java.util.UUID;

public class ServiceResponse {

    private UUID id;
    private String name;
    private String url;
    private String category;
    private ServiceStatus status;
    private Instant lastCheckedAt;
    private Long latencyMs;
    private Instant createdAt;

    public static ServiceResponse fromEntity(ServiceEntity entity) {
        ServiceResponse res = new ServiceResponse();
        res.id = entity.getId();
        res.name = entity.getName();
        res.url = entity.getUrl();
        res.category = entity.getCategory();
        res.status = entity.getStatus();
        res.lastCheckedAt = entity.getLastCheckedAt();
        res.latencyMs = entity.getLatencyMs();
        res.createdAt = entity.getCreatedAt();
        return res;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getCategory() { return category; }
    public ServiceStatus getStatus() { return status; }
    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public Long getLatencyMs() { return latencyMs; }
    public Instant getCreatedAt() { return createdAt; }
}
