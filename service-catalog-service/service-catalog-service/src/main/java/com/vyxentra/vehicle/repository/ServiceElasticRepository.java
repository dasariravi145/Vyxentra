package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.elasticsearch.ServiceDocument;
import com.vyxentra.vehicle.enums.CategoryType;
import com.vyxentra.vehicle.enums.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ServiceElasticRepository extends ElasticsearchRepository<ServiceDocument, Long> {


    /**
     * Find active services by status
     */
    Page<ServiceDocument> findByStatus(ServiceStatus status, Pageable pageable);

    /**
     * Find services by category ID
     */
    Page<ServiceDocument> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find services by category type and status, ordered by popularity
     */
    Page<ServiceDocument> findByCategoryTypeAndStatusOrderByPopularityScoreDesc(
            CategoryType categoryType, ServiceStatus status, Pageable pageable);

    /**
     * Find services by category type and status
     */
    List<ServiceDocument> findByCategoryTypeAndStatus(CategoryType categoryType, ServiceStatus status);

    /**
     * Search services by name using full-text search
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}, {\"term\": {\"status\": \"ACTIVE\"}}]}}")
    Page<ServiceDocument> searchByName(String name, Pageable pageable);

    /**
     * Full-text search across name, description, and tags
     */
    @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"description\": \"?0\"}}, {\"match\": {\"tags\": \"?0\"}}], \"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}}")
    Page<ServiceDocument> fullTextSearch(String keyword, Pageable pageable);

    /**
     * Search with fuzzy matching for typo tolerance
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}], \"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}}")
    Page<ServiceDocument> fuzzySearchByName(String name, Pageable pageable);

    /**
     * Search services by price range
     */
    @Query("{\"bool\": {\"must\": [{\"range\": {\"basePrice\": {\"gte\": ?0, \"lte\": ?1}}}], \"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}}")
    Page<ServiceDocument> findByPriceRange(Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * Search services by minimum rating
     */
    @Query("{\"bool\": {\"must\": [{\"range\": {\"averageRating\": {\"gte\": ?0}}}], \"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}}")
    Page<ServiceDocument> findByMinimumRating(Double minRating, Pageable pageable);

    /**
     * Get top rated services
     */
    @Query("{\"bool\": {\"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}, \"sort\": [{\"averageRating\": {\"order\": \"desc\"}}]}")
    Page<ServiceDocument> findTopRatedServices(Pageable pageable);

    /**
     * Get most popular services
     */
    @Query("{\"bool\": {\"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}, \"sort\": [{\"popularityScore\": {\"order\": \"desc\"}}]}")
    Page<ServiceDocument> findMostPopularServices(Pageable pageable);

    /**
     * Get featured services
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"isFeatured\": true}}], \"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}}")
    List<ServiceDocument> findFeaturedServices();

    /**
     * Count services by category
     */
    long countByCategoryId(Long categoryId);

    /**
     * Count active services by category type
     */
    long countByCategoryTypeAndStatus(CategoryType categoryType, ServiceStatus status);

    /**
     * Delete all services by status
     */
    void deleteByStatus(ServiceStatus status);

    /**
     * Search with multiple filters combined
     */
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}], \"filter\": [{\"term\": {\"categoryType\": \"?1\"}}, {\"range\": {\"basePrice\": {\"gte\": ?2, \"lte\": ?3}}}, {\"range\": {\"averageRating\": {\"gte\": ?4}}}]}}")
    Page<ServiceDocument> searchWithFilters(
            String keyword, String categoryType, Double minPrice, Double maxPrice, Double minRating, Pageable pageable);

    /**
     * Get service suggestions for autocomplete
     */
    @Query("{\"bool\": {\"must\": [{\"prefix\": {\"name\": \"?0\"}}], \"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}, \"size\": 10}")
    List<ServiceDocument> getAutocompleteSuggestions(String prefix);

    /**
     * Get popular search keywords (based on service names)
     */
    @Query("{\"bool\": {\"filter\": {\"term\": {\"status\": \"ACTIVE\"}}}, \"sort\": [{\"popularityScore\": {\"order\": \"desc\"}}], \"size\": ?0}")
    List<ServiceDocument> getPopularServicesByLimit(int limit);
}