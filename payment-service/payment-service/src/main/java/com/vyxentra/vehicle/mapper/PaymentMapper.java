package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.PaymentDetailResponse;
import com.vyxentra.vehicle.dto.response.PaymentResponse;
import com.vyxentra.vehicle.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {RefundMapper.class})
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "id")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "refunds", source = "refunds")
    PaymentDetailResponse toDetailResponse(Payment payment);
}
