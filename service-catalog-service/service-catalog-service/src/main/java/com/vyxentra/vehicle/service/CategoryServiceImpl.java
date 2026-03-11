package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.CategoryRequest;
import com.vyxentra.vehicle.dto.response.CategoryResponse;
import com.vyxentra.vehicle.dto.response.ServiceDefinitionResponse;
import com.vyxentra.vehicle.entity.ServiceCategory;
import com.vyxentra.vehicle.entity.ServiceDefinition;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.mapper.CategoryMapper;
import com.vyxentra.vehicle.mapper.ServiceDefinitionMapper;
import com.vyxentra.vehicle.repository.ServiceCategoryRepository;
import com.vyxentra.vehicle.repository.ServiceDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceDefinitionRepository serviceRepository;
    private final CategoryMapper categoryMapper;
    private final ServiceDefinitionMapper serviceMapper;
    private final ServiceCacheManager cacheManager;

    @Override
    @Cacheable(value = "categories", key = "#active", unless = "#result == null")
    public List<CategoryResponse> getAllCategories(Boolean active) {
        log.debug("Getting all categories, active: {}", active);

        List<Object[]> results = categoryRepository.findCategoriesWithServiceCount(active);

        return results.stream()
                .map(result -> {
                    ServiceCategory category = (ServiceCategory) result[0];
                    Long count = (Long) result[1];
                    return categoryMapper.toResponseWithCount(category, count.intValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "category", key = "#categoryId", unless = "#result == null")
    public CategoryResponse getCategory(String categoryId) {
        log.debug("Getting category: {}", categoryId);

        ServiceCategory category = findCategoryById(categoryId);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Cacheable(value = "servicesByCategory", key = "#categoryId + '-' + #vehicleType + '-' + #active")
    public List<ServiceDefinitionResponse> getServicesByCategory(String categoryId, String vehicleType,
                                                                 Boolean active) {
        log.debug("Getting services for category: {}, vehicle: {}", categoryId, vehicleType);

        List<ServiceDefinition> services;

        if (Boolean.TRUE.equals(active)) {
            services = serviceRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        } else {
            services = serviceRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        }

        // Filter by vehicle type if specified
        if (vehicleType != null) {
            services = services.stream()
                    .filter(s -> s.getVehicleTypes().stream()
                            .anyMatch(vt -> vt.getVehicleType().name().equals(vehicleType) && vt.getIsActive()))
                    .collect(Collectors.toList());
        }

        return serviceMapper.toResponseList(services);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "category", "servicesByCategory"}, allEntries = true)
    public CategoryResponse createCategory(String adminId, CategoryRequest request) {
        log.info("Creating new category by admin: {}", adminId);

        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Category name already exists: " + request.getName());
        }

        ServiceCategory category = ServiceCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(adminId)
                .build();

        category = categoryRepository.save(category);

        log.info("Category created successfully with ID: {}", category.getId());

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "category", "servicesByCategory"}, allEntries = true)
    public CategoryResponse updateCategory(String adminId, String categoryId, CategoryRequest request) {
        log.info("Updating category: {} by admin: {}", categoryId, adminId);

        ServiceCategory category = findCategoryById(categoryId);

        // Check name uniqueness if changed
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Category name already exists: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : category.getIsActive());
        category.setUpdatedBy(adminId);

        category = categoryRepository.save(category);

        log.info("Category updated successfully: {}", categoryId);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "category", "servicesByCategory"}, allEntries = true)
    public void deleteCategory(String adminId, String categoryId) {
        log.info("Deleting category: {} by admin: {}", categoryId, adminId);

        ServiceCategory category = findCategoryById(categoryId);

        // Check if category has services
        if (!category.getServices().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Cannot delete category with existing services. Deactivate it instead.");
        }

        categoryRepository.delete(category);

        log.info("Category deleted: {}", categoryId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "category", "servicesByCategory"}, allEntries = true)
    public void toggleCategoryStatus(String adminId, String categoryId, boolean active) {
        log.info("Toggling category: {} to {} by admin: {}", categoryId, active, adminId);

        ServiceCategory category = findCategoryById(categoryId);
        category.setIsActive(active);
        category.setUpdatedBy(adminId);
        categoryRepository.save(category);

        log.info("Category status toggled: {} -> {}", categoryId, active);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public void reorderCategories(String adminId, List<String> categoryIds) {
        log.info("Reordering categories by admin: {}", adminId);

        for (int i = 0; i < categoryIds.size(); i++) {
            String categoryId = categoryIds.get(i);
            ServiceCategory category = findCategoryById(categoryId);
            category.setDisplayOrder(i);
            category.setUpdatedBy(adminId);
            categoryRepository.save(category);
        }

        log.info("Categories reordered successfully");
    }

    private ServiceCategory findCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
}
