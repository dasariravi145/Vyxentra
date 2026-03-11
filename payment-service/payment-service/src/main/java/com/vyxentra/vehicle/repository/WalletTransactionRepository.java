package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId);

    Page<WalletTransaction> findByWalletId(String walletId, Pageable pageable);

    List<WalletTransaction> findByReferenceId(String referenceId);
}
