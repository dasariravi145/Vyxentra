package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ProviderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderDocumentRepository extends JpaRepository<ProviderDocument, String> {

    List<ProviderDocument> findByProviderId(String providerId);

    List<ProviderDocument> findByProviderIdAndDocumentType(String providerId, String documentType);

    @Query("SELECT pd FROM ProviderDocument pd WHERE pd.provider.id = :providerId AND pd.verified = false")
    List<ProviderDocument> findPendingDocuments(@Param("providerId") String providerId);

    @Query("SELECT COUNT(pd) FROM ProviderDocument pd WHERE pd.provider.id = :providerId AND pd.verified = false")
    long countPendingByProviderId(@Param("providerId") String providerId);

    Optional<ProviderDocument> findByProviderIdAndDocumentTypeAndVerifiedFalse(String providerId, String documentType);

    boolean existsByProviderIdAndDocumentTypeAndVerifiedTrue(String providerId, String documentType);

    @Query("SELECT pd FROM ProviderDocument pd WHERE pd.verified = false AND pd.createdAt < :cutoff")
    List<ProviderDocument> findStalePendingDocuments(@Param("cutoff") Instant cutoff);
}
