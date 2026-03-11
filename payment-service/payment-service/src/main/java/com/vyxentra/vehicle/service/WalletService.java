package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.WalletTopupRequest;
import com.vyxentra.vehicle.dto.response.WalletResponse;
import com.vyxentra.vehicle.dto.response.WalletTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WalletService {

    WalletResponse createWallet(String userId, String userType);

    WalletResponse getWallet(String walletId);

    WalletResponse getWalletByUserId(String userId);

    WalletResponse topupWallet(String userId, WalletTopupRequest request);

    WalletResponse debitWallet(String walletId, Double amount, String referenceId,
                               String referenceType, String description);

    WalletResponse creditWallet(String walletId, Double amount, String referenceId,
                                String referenceType, String description);

    List<WalletTransactionResponse> getWalletTransactions(String userId, Pageable pageable);

    Double getWalletBalance(String userId);

    void blockWallet(String walletId, String reason);
}
