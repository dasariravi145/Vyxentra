package com.vyxentra.vehicle.spec;


import com.vyxentra.vehicle.dto.request.BookingSearchRequest;
import com.vyxentra.vehicle.entity.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    public static Specification<Booking> withFilters(BookingSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getCustomerId())) {
                predicates.add(cb.equal(root.get("customerId"), request.getCustomerId()));
            }

            if (StringUtils.hasText(request.getProviderId())) {
                predicates.add(cb.equal(root.get("providerId"), request.getProviderId()));
            }

            if (StringUtils.hasText(request.getEmployeeId())) {
                predicates.add(cb.equal(root.get("employeeId"), request.getEmployeeId()));
            }

            if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(request.getStatuses()));
            }

            if (request.getVehicleType() != null) {
                predicates.add(cb.equal(root.get("vehicleType"), request.getVehicleType()));
            }

            if (request.getServiceType() != null) {
                predicates.add(cb.equal(root.get("serviceType"), request.getServiceType()));
            }

            if (request.getIsEmergency() != null) {
                predicates.add(cb.equal(root.get("isEmergency"), request.getIsEmergency()));
            }

            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate()));
            }

            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getToDate()));
            }

            if (request.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), request.getMinAmount()));
            }

            if (request.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), request.getMaxAmount()));
            }

            if (StringUtils.hasText(request.getSearchTerm())) {
                String pattern = "%" + request.getSearchTerm().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("bookingNumber")), pattern),
                        cb.like(cb.lower(root.get("locationAddress")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}