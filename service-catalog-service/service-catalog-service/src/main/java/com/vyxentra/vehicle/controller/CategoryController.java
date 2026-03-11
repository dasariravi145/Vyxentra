package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.CategoryRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.CategoryResponse;
import com.vyxentra.vehicle.dto.response.ServiceDefinitionResponse;
import com.vyxentra.vehicle.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false) Boolean active) {
        log.info("Getting all categories, active: {}", active);
        List<CategoryResponse> responses = categoryService.getAllCategories(active);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @PathVariable String categoryId) {
        log.info("Getting category: {}", categoryId);
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{categoryId}/services")
    public ResponseEntity<ApiResponse<List<ServiceDefinitionResponse>>> getServicesByCategory(
            @PathVariable String categoryId,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) Boolean active) {
        log.info("Getting services for category: {}, vehicle: {}", categoryId, vehicleType);
        List<ServiceDefinitionResponse> responses = categoryService.getServicesByCategory(
                categoryId, vehicleType, active);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestHeader("X-User-ID") String adminId,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Creating new category by admin: {}", adminId);
        CategoryResponse response = categoryService.createCategory(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }

    @PutMapping("/admin/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Updating category: {} by admin: {}", categoryId, adminId);
        CategoryResponse response = categoryService.updateCategory(adminId, categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Category updated successfully"));
    }

    @DeleteMapping("/admin/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String categoryId) {
        log.info("Deleting category: {} by admin: {}", categoryId, adminId);
        categoryService.deleteCategory(adminId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }

    @PatchMapping("/admin/{categoryId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleCategoryStatus(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String categoryId,
            @RequestParam boolean active) {
        log.info("Toggling category: {} to {} by admin: {}", categoryId, active, adminId);
        categoryService.toggleCategoryStatus(adminId, categoryId, active);
        return ResponseEntity.ok(ApiResponse.success(null,
                active ? "Category activated" : "Category deactivated"));
    }

    @PostMapping("/admin/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderCategories(
            @RequestHeader("X-User-ID") String adminId,
            @RequestBody List<String> categoryIds) {
        log.info("Reordering categories by admin: {}", adminId);
        categoryService.reorderCategories(adminId, categoryIds);
        return ResponseEntity.ok(ApiResponse.success(null, "Categories reordered successfully"));
    }
}
