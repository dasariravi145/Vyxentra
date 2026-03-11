package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.SMSLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SMSLogRepository extends JpaRepository<SMSLog, String> {

    Optional<SMSLog> findByNotificationId(String notificationId);
}
