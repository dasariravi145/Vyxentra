package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.request.ProviderApprovalRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "provider-service", fallback = ProviderServiceClientFallback.class)
public interface ProviderServiceClient {

    @GetMapping("/api/v1/providers/pending")
    ApiResponse<Object> getPendingProviders(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/api/v1/providers/{providerId}")
    ApiResponse<Object> getProviderDetails(@PathVariable("providerId") String providerId);

    @PostMapping("/api/v1/providers/{providerId}/approve")
    ApiResponse<Void> approveProvider(@PathVariable("providerId") String providerId,
                                      @RequestParam("adminId") String adminId,
                                      @RequestBody ProviderApprovalRequest request);

    @PostMapping("/api/v1/providers/{providerId}/reject")
    ApiResponse<Void> rejectProvider(@PathVariable("providerId") String providerId,
                                     @RequestParam("adminId") String adminId,
                                     @RequestParam("reason") String reason);

    @PostMapping("/api/v1/providers/{providerId}/suspend")
    ApiResponse<Void> suspendProvider(@PathVariable("providerId") String providerId,
                                      @RequestParam("adminId") String adminId,
                                      @RequestParam("reason") String reason);

    @PostMapping("/api/v1/providers/{providerId}/activate")
    ApiResponse<Void> activateProvider(@PathVariable("providerId") String providerId,
                                       @RequestParam("adminId") String adminId);
}
