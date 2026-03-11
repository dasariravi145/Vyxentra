package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.TransactionLog;
import com.vyxentra.vehicle.dto.response.TransactionLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionLogMapper {

    @Mapping(target = "logId", source = "id")
    TransactionLogResponse toResponse(TransactionLog transactionLog);


    TransactionLogResponse toResponse(com.vyxentra.vehicle.entity.TransactionLog log);

    List<TransactionLogResponse> toResponseList(List<com.vyxentra.vehicle.entity.TransactionLog> content);
}
