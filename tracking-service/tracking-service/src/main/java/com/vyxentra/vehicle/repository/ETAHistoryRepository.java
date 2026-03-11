package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ETAHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ETAHistoryRepository extends JpaRepository<ETAHistory, String> {

    List<ETAHistory> findByTrackingSessionIdOrderByCalculatedAtDesc(String trackingSessionId);

    List<ETAHistory> findTop10ByTrackingSessionIdOrderByCalculatedAtDesc(String trackingSessionId);
}
