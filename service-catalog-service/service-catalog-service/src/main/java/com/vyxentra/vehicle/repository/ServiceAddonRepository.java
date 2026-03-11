package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ServiceAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceAddonRepository extends JpaRepository<ServiceAddon, String> {

    List<ServiceAddon> findByServiceIdOrderByDisplayOrderAsc(String serviceId);

    List<ServiceAddon> findByServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(String serviceId);

    @Query("SELECT a FROM ServiceAddon a WHERE a.service.id = :serviceId AND a.isMandatory = true")
    List<ServiceAddon> findMandatoryAddons(@Param("serviceId") String serviceId);

    @Modifying
    @Query("UPDATE ServiceAddon a SET a.displayOrder = :displayOrder WHERE a.id = :addonId")
    void updateDisplayOrder(@Param("addonId") String addonId, @Param("displayOrder") Integer displayOrder);
}