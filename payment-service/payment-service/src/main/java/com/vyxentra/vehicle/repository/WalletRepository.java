package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

    Optional<Wallet> findByUserId(String userId);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount, " +
            "w.totalCredited = w.totalCredited + :amount, " +
            "w.lastTransactionAt = :now WHERE w.id = :walletId")
    void creditBalance(@Param("walletId") String walletId,
                       @Param("amount") Double amount,
                       @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount, " +
            "w.totalDebited = w.totalDebited + :amount, " +
            "w.lastTransactionAt = :now WHERE w.id = :walletId AND w.balance >= :amount")
    int debitBalance(@Param("walletId") String walletId,
                     @Param("amount") Double amount,
                     @Param("now") LocalDateTime now);

    @Query("SELECT w.balance FROM Wallet w WHERE w.id = :walletId")
    Double getBalance(@Param("walletId") String walletId);
}
