package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.dto.BookingSnapshotDTO;
import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent extends BaseEvent {

    private String bookingId;
    private String bookingReference;
    private String customerId;
    private String providerId;
    private ProviderType providerType;
    private VehicleType vehicleType;
    private BookingStatus status;
    private boolean isEmergency;
    private BigDecimal estimatedPrice;
    private BookingSnapshotDTO bookingSnapshot;
    private String requestedServiceId;
    private GeoLocation pickupLocation;
}
