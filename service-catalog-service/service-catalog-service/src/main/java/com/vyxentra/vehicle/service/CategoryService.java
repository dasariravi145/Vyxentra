package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.CategoryRequest;
import com.vyxentra.vehicle.dto.response.CategoryResponse;
import com.vyxentra.vehicle.dto.response.ServiceDefinitionResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories(Boolean active);

    CategoryResponse getCategory(String categoryId);

    List<ServiceDefinitionResponse> getServicesByCategory(String categoryId, String vehicleType, Boolean active);

    CategoryResponse createCategory(String adminId, CategoryRequest request);

    CategoryResponse updateCategory(String adminId, String categoryId, CategoryRequest request);

    void deleteCategory(String adminId, String categoryId);

    void toggleCategoryStatus(String adminId, String categoryId, boolean active);

    void reorderCategories(String adminId, List<String> categoryIds);
}
