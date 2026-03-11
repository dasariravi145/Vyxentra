package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.BankAccountDetails;
import com.vyxentra.vehicle.dto.KycDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderResponse {

    private String providerId;
    private String userId;
    private String businessName;
    private String providerType;
    private String ownerName;
    private String email;
    private String phone;
    private String alternatePhone;
    private String status;

    // Bank details for payout
    private BankAccountDetails bankAccount;
    private KycDetails kycDetails;

    // Financial details
    private Double totalEarnings;
    private Double pendingPayouts;
    private Double totalPayouts;
    private Double commissionRate;
    private String payoutFrequency;
    private Instant lastPayoutDate;

    // Verification status
    private Boolean isVerified;
    private Boolean isPayoutEligible;
    private String kycStatus;
    private String panNumber;
    private String gstNumber;

    // Additional metadata
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isAvailable;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean hasValidBankAccount() {
        return bankAccount != null &&
                Boolean.TRUE.equals(bankAccount.getIsVerified()) &&
                bankAccount.getAccountNumber() != null &&
                bankAccount.getIfscCode() != null;
    }
}
