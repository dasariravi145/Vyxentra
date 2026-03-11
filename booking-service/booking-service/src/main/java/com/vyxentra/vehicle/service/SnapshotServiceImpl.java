package com.vyxentra.vehicle.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.entity.BookingSnapshot;
import com.vyxentra.vehicle.repository.BookingSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {

    private final BookingSnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void createSnapshot(Booking booking, String snapshotType, String createdBy) {
        log.info("Creating {} snapshot for booking: {}", snapshotType, booking.getId());

        Map<String, Object> snapshotData = new HashMap<>();
        snapshotData.put("bookingId", booking.getId());
        snapshotData.put("bookingNumber", booking.getBookingNumber());
        snapshotData.put("customerId", booking.getCustomerId());
        snapshotData.put("providerId", booking.getProviderId());
        snapshotData.put("employeeId", booking.getEmployeeId());
        snapshotData.put("vehicleType", booking.getVehicleType());
        snapshotData.put("serviceType", booking.getServiceType());
        snapshotData.put("status", booking.getStatus());
        snapshotData.put("scheduledTime", booking.getScheduledTime());
        snapshotData.put("totalAmount", booking.getTotalAmount());
        snapshotData.put("approvedAmount", booking.getApprovedAmount());
        snapshotData.put("paidAmount", booking.getPaidAmount());
        snapshotData.put("isEmergency", booking.getIsEmergency());
        snapshotData.put("emergencyType", booking.getEmergencyType());
        snapshotData.put("location", Map.of(
                "lat", booking.getLocationLat(),
                "lng", booking.getLocationLng(),
                "address", booking.getLocationAddress()
        ));
        snapshotData.put("createdAt", booking.getCreatedAt());

        BookingSnapshot snapshot = BookingSnapshot.builder()
                .booking(booking)
                .snapshotData(snapshotData)
                .snapshotType(snapshotType)
                .createdBy(createdBy)
                .build();

        snapshotRepository.save(snapshot);

        booking.setSnapshotId(snapshot.getId());
    }
}
