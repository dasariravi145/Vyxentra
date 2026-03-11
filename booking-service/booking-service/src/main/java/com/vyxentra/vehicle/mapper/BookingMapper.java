package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.dto.ServiceItem;
import com.vyxentra.vehicle.dto.TimelineEntry;
import com.vyxentra.vehicle.dto.request.AddonItem;
import com.vyxentra.vehicle.dto.response.BookingDetailResponse;
import com.vyxentra.vehicle.dto.response.BookingResponse;
import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.entity.BookingAddon;
import com.vyxentra.vehicle.entity.BookingService;
import com.vyxentra.vehicle.entity.BookingTimeline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {DamageMapper.class})
public interface BookingMapper {

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "providerName", ignore = true)
    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "serviceName", ignore = true)
    @Mapping(target = "location", expression = "java(mapLocation(booking))")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerPhone", ignore = true)
    @Mapping(target = "providerName", ignore = true)
    @Mapping(target = "providerPhone", ignore = true)
    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "location", expression = "java(mapLocation(booking))")
    @Mapping(target = "services", source = "services")
    @Mapping(target = "addons", source = "services", qualifiedByName = "extractAddons")
    @Mapping(target = "damageReports", ignore = true)
    @Mapping(target = "timeline", source = "timeline")
    BookingDetailResponse toDetailResponse(Booking booking);

    @Named("mapLocation")
    default Location mapLocation(Booking booking) {
        if (booking.getLocationLat() == null || booking.getLocationLng() == null) {
            return null;
        }
        return Location.builder()
                .latitude(booking.getLocationLat())
                .longitude(booking.getLocationLng())
                .address(booking.getLocationAddress())
                .build();
    }

    @Named("extractAddons")
    default List<AddonItem> extractAddons(List<BookingService> services) {
        return services.stream()
                .flatMap(bs -> bs.getAddons().stream())
                .map(this::mapAddon)
                .collect(Collectors.toList());
    }

    default ServiceItem mapService(BookingService service) {
        return ServiceItem.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .quantity(service.getQuantity())
                .unitPrice(service.getUnitPrice())
                .totalPrice(service.getTotalPrice())
                .isApproved(service.getIsApproved())
                .build();
    }

    default AddonItem mapAddon(BookingAddon addon) {
        return AddonItem.builder()
                .addonId(addon.getAddonId())
                .addonName(addon.getAddonName())
                .quantity(addon.getQuantity())
                .unitPrice(addon.getUnitPrice())
                .totalPrice(addon.getTotalPrice())
                .isApproved(addon.getIsApproved())
                .build();
    }

    default TimelineEntry mapTimeline(BookingTimeline timeline) {
        return TimelineEntry.builder()
                .status(timeline.getStatus())
                .notes(timeline.getNotes())
                .changedBy(timeline.getChangedBy())
                .changedAt(timeline.getChangedAt())
                .build();
    }
}