package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, String> {

    Optional<EmailLog> findByNotificationId(String notificationId);
}
