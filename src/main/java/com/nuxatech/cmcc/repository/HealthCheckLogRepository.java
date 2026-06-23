package com.nuxatech.cmcc.repository;

import com.nuxatech.cmcc.entity.HealthCheckLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HealthCheckLogRepository extends JpaRepository<HealthCheckLogEntity, UUID> {
}
