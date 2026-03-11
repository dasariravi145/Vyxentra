package com.vyxentra.vehicle.mapper;

import com.vyxentra.vehicle.dto.response.BookingTimelineResponse;
import com.vyxentra.vehicle.entity.BookingTimeline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimelineMapper {

    @Mapping(target = "timelineId", source = "id")
    @Mapping(target = "bookingId", source = "booking.id")
    BookingTimelineResponse toResponse(BookingTimeline timeline);

    List<BookingTimelineResponse> toResponseList(List<BookingTimeline> timelines);
}
