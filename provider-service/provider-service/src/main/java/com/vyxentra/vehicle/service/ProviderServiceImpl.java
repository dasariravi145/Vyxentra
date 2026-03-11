package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.client.UserServiceClient;
import com.vyxentra.vehicle.dto.request.ProviderRegistrationRequest;
import com.vyxentra.vehicle.dto.request.ProviderUpdateRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderDetailResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import com.vyxentra.vehicle.dto.response.UserProfileResponse;
import com.vyxentra.vehicle.entity.Provider;
import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.exception.BusinessException;
import com.vyxentra.vehicle.exception.ErrorCode;
import com.vyxentra.vehicle.exception.ProviderNotFoundException;
import com.vyxentra.vehicle.exception.ProviderSuspendedException;
import com.vyxentra.vehicle.kafka.ProviderEventProducer;
import com.vyxentra.vehicle.mapper.ProviderMapper;
import com.vyxentra.vehicle.repository.ProviderRepository;

import com.vyxentra.vehicle.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;
    private final ProviderEventProducer eventProducer;
    private final UserServiceClient userServiceClient;
    private final ValidationUtils validationUtils;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public ProviderResponse registerProvider(String userId, ProviderRegistrationRequest request) {
        log.info("Registering provider for user: {}", userId);

        validationUtils.validateProviderRegistration(request);

        if (providerRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.PROVIDER_ALREADY_EXISTS, "Provider already registered for this user");
        }

        if (providerRepository.existsByGstNumber(request.getGstNumber())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "GST number already registered");
        }

        ApiResponse<UserProfileResponse> userResponse = userServiceClient.getUserProfile(userId);
        if (!userResponse.isSuccess() || userResponse.getData() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User not found");
        }

        Provider provider = Provider.builder()
                .userId(userId)
                .businessName(request.getBusinessName())
                .providerType(request.getProviderType())
                .ownerName(request.getOwnerName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .registrationNumber(request.getRegistrationNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .businessDescription(request.getBusinessDescription())
                .website(request.getWebsite())
                .yearOfEstablishment(request.getYearOfEstablishment())
                .employeeCount(request.getEmployeeCount())
                .supportsBike(request.getSupportsBike())
                .supportsCar(request.getSupportsCar())
                .status(ProviderStatus.PENDING_APPROVAL)
                .isVerified(false)
                .createdBy(userId)
                .build();

        if (request.getOperatingHours() != null) {
            provider.setOperatingHours(convertRegistrationOperatingHours(request.getOperatingHours()));
            if (request.getOperatingHours().isTwentyFourSeven()) {
                provider.setTwentyFourSeven(true);
            }
        }

        if (request.getBankDetails() != null) {
            provider.setBankDetails(convertRegistrationBankDetails(request.getBankDetails()));
        }

        provider = providerRepository.save(provider);
        eventProducer.publishProviderRegistered(provider);

        log.info("Provider registered successfully with ID: {}", provider.getId());

        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "providerProfile", key = "#providerId", unless = "#result == null")
    public ProviderDetailResponse getProviderProfile(String providerId) {
        Provider provider = findProviderById(providerId);
        ProviderDetailResponse response = providerMapper.toDetailResponse(provider);

        if (provider.getStatus() == ProviderStatus.SUSPENDED) {
            response.setSuspensionReason(provider.getSuspensionReason());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDetailResponse getProviderProfileByUserId(String userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ProviderNotFoundException("user", userId));
        return getProviderProfile(provider.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "providerProfile", key = "#result.providerId")
    public ProviderResponse updateProviderProfile(String userId, ProviderUpdateRequest request) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ProviderNotFoundException("user", userId));

        if (request.getBusinessName() != null) provider.setBusinessName(request.getBusinessName());
        if (request.getOwnerName() != null) provider.setOwnerName(request.getOwnerName());
        if (request.getEmail() != null) {
            validationUtils.validateEmail(request.getEmail());
            provider.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            validationUtils.validatePhone(request.getPhone());
            provider.setPhone(request.getPhone());
        }
        if (request.getAlternatePhone() != null) provider.setAlternatePhone(request.getAlternatePhone());
        if (request.getAddressLine1() != null) provider.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) provider.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) provider.setCity(request.getCity());
        if (request.getState() != null) provider.setState(request.getState());
        if (request.getPostalCode() != null) provider.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) provider.setCountry(request.getCountry());
        if (request.getLatitude() != null && request.getLongitude() != null) {
            provider.setLatitude(request.getLatitude());
            provider.setLongitude(request.getLongitude());
        }
        if (request.getBusinessDescription() != null) provider.setBusinessDescription(request.getBusinessDescription());
        if (request.getWebsite() != null) provider.setWebsite(request.getWebsite());
        if (request.getEmployeeCount() != null) provider.setEmployeeCount(request.getEmployeeCount());
        if (request.getSupportsBike() != null) provider.setSupportsBike(request.getSupportsBike());
        if (request.getSupportsCar() != null) provider.setSupportsCar(request.getSupportsCar());

        // Fix: Use update-specific converter methods
        if (request.getOperatingHours() != null) {
            provider.setOperatingHours(convertUpdateOperatingHours(request.getOperatingHours()));
        }

        if (request.getBankDetails() != null) {
            provider.setBankDetails(convertUpdateBankDetails(request.getBankDetails()));
        }

        provider.setUpdatedBy(userId);
        provider = providerRepository.save(provider);

        eventProducer.publishProviderUpdated(provider);
        log.info("Provider profile updated: {}", provider.getId());

        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponse> getProvidersByStatus(ProviderStatus status) {
        return providerRepository.findByStatus(status).stream()
                .map(providerMapper::toResponse)
                .toList();
    }

    @Override
    public boolean isProviderAvailable(String providerId) {
        Provider provider = findProviderById(providerId);

        if (provider.getStatus() != ProviderStatus.ACTIVE) {
            return false;
        }

        if (Boolean.TRUE.equals(provider.getTwentyFourSeven())) {
            return true;
        }

        if (provider.getOpeningTime() != null && provider.getClosingTime() != null) {
            LocalTime now = LocalTime.now();
            return !now.isBefore(provider.getOpeningTime()) && !now.isAfter(provider.getClosingTime());
        }

        return true;
    }

    @Override
    @Transactional
    public void incrementBookingCount(String providerId) {
        providerRepository.incrementBookingCount(providerId);
        evictProviderCache(providerId);
    }

    @Override
    @Transactional
    public void updateProviderRating(String providerId, Double rating) {
        Provider provider = findProviderById(providerId);

        double totalRating = (provider.getAverageRating() * provider.getTotalReviews()) + rating;
        int newReviewCount = provider.getTotalReviews() + 1;
        double newAverage = totalRating / newReviewCount;

        providerRepository.updateRating(providerId, newAverage, newReviewCount);
        evictProviderCache(providerId);
    }

    @Override
    public void validateProviderForBooking(String providerId) {
        Provider provider = findProviderById(providerId);

        if (provider.getStatus() == ProviderStatus.SUSPENDED) {
            throw new ProviderSuspendedException(providerId, provider.getSuspensionReason());
        }

        if (provider.getStatus() != ProviderStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.PROVIDER_NOT_APPROVED);
        }
    }

    private Provider findProviderById(String providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
    }

    private void evictProviderCache(String providerId) {
        redisTemplate.delete("providerProfile::" + providerId);
    }

    // Converter methods for ProviderRegistrationRequest
    private Map<String, Object> convertRegistrationOperatingHours(ProviderRegistrationRequest.OperatingHours hours) {
        return Map.of(
                "monday", hours.getMonday(),
                "tuesday", hours.getTuesday(),
                "wednesday", hours.getWednesday(),
                "thursday", hours.getThursday(),
                "friday", hours.getFriday(),
                "saturday", hours.getSaturday(),
                "sunday", hours.getSunday(),
                "timezone", hours.getTimezone(),
                "twentyFourSeven", hours.isTwentyFourSeven()
        );
    }

    private Map<String, Object> convertRegistrationBankDetails(ProviderRegistrationRequest.BankDetails bankDetails) {
        return Map.of(
                "accountHolderName", bankDetails.getAccountHolderName(),
                "accountNumber", maskAccountNumber(bankDetails.getAccountNumber()),
                "ifscCode", bankDetails.getIfscCode(),
                "bankName", bankDetails.getBankName(),
                "branchName", bankDetails.getBranchName(),
                "accountType", bankDetails.getAccountType(),
                "upiId", bankDetails.getUpiId()
        );
    }

    // Converter methods for ProviderUpdateRequest
    private Map<String, Object> convertUpdateOperatingHours(ProviderUpdateRequest.OperatingHours hours) {
        if (hours == null) return null;

        return Map.of(
                "monday", hours.getMonday(),
                "tuesday", hours.getTuesday(),
                "wednesday", hours.getWednesday(),
                "thursday", hours.getThursday(),
                "friday", hours.getFriday(),
                "saturday", hours.getSaturday(),
                "sunday", hours.getSunday(),
                "twentyFourSeven", hours.isTwentyFourSeven()
        );
    }

    private Map<String, Object> convertUpdateBankDetails(ProviderUpdateRequest.BankDetails bankDetails) {
        if (bankDetails == null) return null;

        return Map.of(
                "accountHolderName", bankDetails.getAccountHolderName(),
                "accountNumber", maskAccountNumber(bankDetails.getAccountNumber()),
                "ifscCode", bankDetails.getIfscCode(),
                "bankName", bankDetails.getBankName(),
                "branchName", bankDetails.getBranchName(),
                "accountType", bankDetails.getAccountType(),
                "upiId", bankDetails.getUpiId()
        );
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }
}