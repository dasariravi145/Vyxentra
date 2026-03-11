package com.vyxentra.vehicle.mapper;

import com.vyxentra.vehicle.dto.response.WalletResponse;
import com.vyxentra.vehicle.dto.response.WalletTransactionResponse;
import com.vyxentra.vehicle.entity.Wallet;
import com.vyxentra.vehicle.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletMapper {

    @Mapping(target = "walletId", source = "id")
    WalletResponse toResponse(Wallet wallet);

    @Mapping(target = "transactionId", source = "id")
    WalletTransactionResponse toTransactionResponse(WalletTransaction transaction);

    List<WalletTransactionResponse> toTransactionResponseList(List<WalletTransaction> transactions);
}
