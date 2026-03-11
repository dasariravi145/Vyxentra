package com.vyxentra.vehicle.spec;

import com.vyxentra.vehicle.entity.ServiceAddon;
import com.vyxentra.vehicle.entity.ServiceCategory;
import com.vyxentra.vehicle.entity.ServiceDefinition;
import com.vyxentra.vehicle.entity.ServiceVehicleType;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ServiceSpecification {

    /**
     * Filter by category ID
     */
    public static Specification<ServiceDefinition> hasCategory(String categoryId) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(categoryId)) {
                return cb.conjunction();
            }
            Join<ServiceDefinition, ServiceCategory> categoryJoin = root.join("category");
            return cb.equal(categoryJoin.get("id"), categoryId);
        };
    }

    /**
     * Filter by service type
     */
    public static Specification<ServiceDefinition> hasServiceType(ServiceType serviceType) {
        return (root, query, cb) ->
                serviceType != null ? cb.equal(root.get("serviceType"), serviceType) : cb.conjunction();
    }

    /**
     * Filter by provider type
     */
    public static Specification<ServiceDefinition> hasProviderType(ProviderType providerType) {
        return (root, query, cb) ->
                providerType != null ? cb.equal(root.get("providerType"), providerType) : cb.conjunction();
    }

    /**
     * Filter by active status
     */
    public static Specification<ServiceDefinition> isActive(Boolean active) {
        return (root, query, cb) ->
                active != null ? cb.equal(root.get("isActive"), active) : cb.conjunction();
    }

    /**
     * Filter by popular flag
     */
    public static Specification<ServiceDefinition> isPopular(Boolean popular) {
        return (root, query, cb) ->
                popular != null ? cb.equal(root.get("isPopular"), popular) : cb.conjunction();
    }

    /**
     * Filter by recommended flag
     */
    public static Specification<ServiceDefinition> isRecommended(Boolean recommended) {
        return (root, query, cb) ->
                recommended != null ? cb.equal(root.get("isRecommended"), recommended) : cb.conjunction();
    }

    /**
     * Filter by vehicle type (checks if service supports the vehicle type)
     */
    public static Specification<ServiceDefinition> supportsVehicleType(VehicleType vehicleType) {
        return (root, query, cb) -> {
            if (vehicleType == null) {
                return cb.conjunction();
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ServiceVehicleType> vehicleRoot = subquery.from(ServiceVehicleType.class);
            subquery.select(cb.literal(1L));
            subquery.where(
                    cb.equal(vehicleRoot.get("service"), root),
                    cb.equal(vehicleRoot.get("vehicleType"), vehicleType),
                    cb.isTrue(vehicleRoot.get("isActive"))
            );

            return cb.exists(subquery);
        };
    }

    /**
     * Filter by name or description containing search term
     */
    public static Specification<ServiceDefinition> searchByTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(searchTerm)) {
                return cb.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("shortDescription")), pattern),
                    cb.like(cb.lower(root.get("tags")), pattern)
            );
        };
    }

    /**
     * Filter by price range (using min/max price fields)
     */
    public static Specification<ServiceDefinition> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxPrice"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter by category name
     */
    public static Specification<ServiceDefinition> hasCategoryName(String categoryName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(categoryName)) {
                return cb.conjunction();
            }
            Join<ServiceDefinition, ServiceCategory> categoryJoin = root.join("category");
            return cb.equal(cb.lower(categoryJoin.get("name")), categoryName.toLowerCase());
        };
    }

    /**
     * Combined search specification with multiple filters
     * FIXED: Replaced deprecated Specification.where() with manual combination
     */
    public static Specification<ServiceDefinition> withFilters(
            String categoryId,
            String categoryName,
            ServiceType serviceType,
            ProviderType providerType,
            VehicleType vehicleType,
            Boolean isActive,
            Boolean isPopular,
            Boolean isRecommended,
            String searchTerm,
            Double minPrice,
            Double maxPrice) {

        return new Specification<ServiceDefinition>() {
            @Override
            public Predicate toPredicate(Root<ServiceDefinition> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                // Add each predicate only if the condition is present
                if (StringUtils.hasText(categoryId)) {
                    Join<ServiceDefinition, ServiceCategory> categoryJoin = root.join("category");
                    predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
                }

                if (StringUtils.hasText(categoryName)) {
                    Join<ServiceDefinition, ServiceCategory> categoryJoin = root.join("category");
                    predicates.add(cb.equal(cb.lower(categoryJoin.get("name")), categoryName.toLowerCase()));
                }

                if (serviceType != null) {
                    predicates.add(cb.equal(root.get("serviceType"), serviceType));
                }

                if (providerType != null) {
                    predicates.add(cb.equal(root.get("providerType"), providerType));
                }

                if (vehicleType != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<ServiceVehicleType> vehicleRoot = subquery.from(ServiceVehicleType.class);
                    subquery.select(cb.literal(1L));
                    subquery.where(
                            cb.equal(vehicleRoot.get("service"), root),
                            cb.equal(vehicleRoot.get("vehicleType"), vehicleType),
                            cb.isTrue(vehicleRoot.get("isActive"))
                    );
                    predicates.add(cb.exists(subquery));
                }

                if (isActive != null) {
                    predicates.add(cb.equal(root.get("isActive"), isActive));
                }

                if (isPopular != null) {
                    predicates.add(cb.equal(root.get("isPopular"), isPopular));
                }

                if (isRecommended != null) {
                    predicates.add(cb.equal(root.get("isRecommended"), isRecommended));
                }

                if (StringUtils.hasText(searchTerm)) {
                    String pattern = "%" + searchTerm.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("description")), pattern),
                            cb.like(cb.lower(root.get("shortDescription")), pattern),
                            cb.like(cb.lower(root.get("tags")), pattern)
                    ));
                }

                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("minPrice"), minPrice));
                }

                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("maxPrice"), maxPrice));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    /**
     * Alternative approach using Specification composition (non-deprecated)
     */
    public static Specification<ServiceDefinition> withFiltersV2(
            String categoryId,
            String categoryName,
            ServiceType serviceType,
            ProviderType providerType,
            VehicleType vehicleType,
            Boolean isActive,
            Boolean isPopular,
            Boolean isRecommended,
            String searchTerm,
            Double minPrice,
            Double maxPrice) {

        Specification<ServiceDefinition> spec = (root, query, criteriaBuilder) -> null;

        spec = spec.and(hasCategory(categoryId));
        spec = spec.and(hasCategoryName(categoryName));
        spec = spec.and(hasServiceType(serviceType));
        spec = spec.and(hasProviderType(providerType));
        spec = spec.and(supportsVehicleType(vehicleType));
        spec = spec.and(isActive(isActive));
        spec = spec.and(isPopular(isPopular));
        spec = spec.and(isRecommended(isRecommended));
        spec = spec.and(searchByTerm(searchTerm));
        spec = spec.and(priceBetween(minPrice, maxPrice));

        return spec;
    }

    /**
     * Specification for sorting by price
     */
    public static Specification<ServiceDefinition> orderByPrice(boolean ascending) {
        return (root, query, cb) -> {
            if (ascending) {
                query.orderBy(cb.asc(root.get("minPrice")));
            } else {
                query.orderBy(cb.desc(root.get("maxPrice")));
            }
            return cb.conjunction();
        };
    }

    /**
     * Specification for sorting by popularity
     */
    public static Specification<ServiceDefinition> orderByPopularity() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("isPopular")), cb.asc(root.get("displayOrder")));
            return cb.conjunction();
        };
    }

    /**
     * Specification for services with addons
     */
    public static Specification<ServiceDefinition> hasAddons() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ServiceAddon> addonRoot = subquery.from(ServiceAddon.class);
            subquery.select(cb.literal(1L));
            subquery.where(
                    cb.equal(addonRoot.get("service"), root),
                    cb.isTrue(addonRoot.get("isActive"))
            );
            return cb.exists(subquery);
        };
    }
}