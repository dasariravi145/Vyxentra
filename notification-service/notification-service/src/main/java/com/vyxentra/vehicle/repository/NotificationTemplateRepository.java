package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByNameAndChannel(String name, String channel);

    Optional<NotificationTemplate> findByNameAndChannelAndIsActiveTrue(String name, String channel);
}
