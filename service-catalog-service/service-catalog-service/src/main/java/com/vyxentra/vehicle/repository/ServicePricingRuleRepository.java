package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ServicePricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePricingRuleRepository extends JpaRepository<ServicePricingRule, String> {

    List<ServicePricingRule> findByServiceIdAndIsActiveTrueOrderByPriorityDesc(String serviceId);

    List<ServicePricingRule> findByRuleTypeAndIsActiveTrue(String ruleType);
}
