package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.RefundResponse;
import com.vyxentra.vehicle.entity.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefundMapper {

    @Mapping(target = "refundId", source = "id")
    @Mapping(target = "paymentId", source = "payment.id")
    RefundResponse toResponse(Refund refund);

    List<RefundResponse> toResponseList(List<Refund> refunds);
}