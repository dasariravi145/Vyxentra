package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.ApprovalStatistics;
import com.vyxentra.vehicle.dto.request.ProviderApprovalRequest;
import com.vyxentra.vehicle.dto.response.ProviderApprovalResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import com.vyxentra.vehicle.entity.Provider;
import com.vyxentra.vehicle.entity.ProviderApproval;
import com.vyxentra.vehicle.entity.ProviderDocument;
import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.exception.BusinessException;
import com.vyxentra.vehicle.exception.ErrorCode;
import com.vyxentra.vehicle.exception.ProviderApprovalException;
import com.vyxentra.vehicle.exception.ProviderNotFoundException;
import com.vyxentra.vehicle.kafka.ProviderEventProducer;
import com.vyxentra.vehicle.mapper.ProviderApprovalMapper;
import com.vyxentra.vehicle.mapper.ProviderMapper;
import com.vyxentra.vehicle.repository.ProviderApprovalRepository;
import com.vyxentra.vehicle.repository.ProviderDocumentRepository;
import com.vyxentra.vehicle.repository.ProviderRepository;
import com.vyxentra.vehicle.service.ProviderApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderApprovalServiceImpl implements ProviderApprovalService {

    private final ProviderRepository providerRepository;
    private final ProviderApprovalRepository approvalRepository;
    private final ProviderDocumentRepository documentRepository;
    private final ProviderApprovalMapper approvalMapper;
    private final ProviderEventProducer eventProducer;
    private final ProviderMapper providerMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderApprovalResponse> getPendingApprovals(Pageable pageable) {
        return approvalRepository.findPendingApprovals(pageable)
                .map(approvalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderApprovalResponse getApprovalDetails(String providerId) {
        ProviderApproval approval = approvalRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Approval record not found for provider: " + providerId));
        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    @CacheEvict(value = "providerProfile", key = "#request.providerId")
    public ProviderResponse approveProvider(String adminId, ProviderApprovalRequest request) {
        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ProviderNotFoundException(request.getProviderId()));

        if (provider.getStatus() != ProviderStatus.PENDING_APPROVAL) {
            throw new ProviderApprovalException(provider.getId(), provider.getStatus().name(),
                    "Provider is not in pending approval state");
        }

        List<ProviderDocument> pendingDocs = documentRepository.findPendingDocuments(provider.getId());
        if (!pendingDocs.isEmpty()) {
            throw new BusinessException(ErrorCode.PROVIDER_DOCUMENTS_MISSING,
                    "Please verify all documents before approval");
        }

        provider.setStatus(ProviderStatus.ACTIVE);
        provider.setApprovedBy(adminId);
        provider.setApprovedAt(Instant.now());
        provider.setUpdatedBy(adminId);
        provider = providerRepository.save(provider);

        ProviderApproval approval = approvalRepository.findByProviderId(provider.getId())
                .orElse(new ProviderApproval());
        approval.setProvider(provider);
        approval.setReviewedBy(adminId);
        approval.setReviewedAt(Instant.now());
        approval.setComments(request.getComments());
        approval.setCustomCommission(request.getCustomCommission());
        approvalRepository.save(approval);

        cacheProviderStatus(provider.getId(), "ACTIVE");
        eventProducer.publishProviderApproved(provider, adminId);

        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional
    public ProviderResponse rejectProvider(String adminId, String providerId, String reason) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));

        if (provider.getStatus() != ProviderStatus.PENDING_APPROVAL) {
            throw new ProviderApprovalException(providerId, provider.getStatus().name());
        }

        provider.setStatus(ProviderStatus.REJECTED);
        provider.setRejectionReason(reason);
        provider.setUpdatedBy(adminId);
        provider = providerRepository.save(provider);

        ProviderApproval approval = approvalRepository.findByProviderId(providerId)
                .orElse(new ProviderApproval());
        approval.setProvider(provider);
        approval.setReviewedBy(adminId);
        approval.setReviewedAt(Instant.now());
        approval.setRejectionReason(reason);
        approvalRepository.save(approval);

        eventProducer.publishProviderRejected(provider, adminId, reason);

        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional
    public ProviderResponse suspendProvider(String adminId, String providerId, String reason) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));

        provider.setStatus(ProviderStatus.SUSPENDED);
        provider.setSuspensionReason(reason);
        provider.setUpdatedBy(adminId);
        provider = providerRepository.save(provider);

        cacheProviderStatus(providerId, "SUSPENDED");
        eventProducer.publishProviderSuspended(provider, adminId, reason);

        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional
    public ProviderResponse activateProvider(String adminId, String providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));

        if (provider.getStatus() != ProviderStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.PROVIDER_INVALID_STATUS,
                    "Provider is not suspended");
        }

        provider.setStatus(ProviderStatus.ACTIVE);
        provider.setSuspensionReason(null);
        provider.setUpdatedBy(adminId);
        provider = providerRepository.save(provider);

        redisTemplate.delete("suspended:provider:" + providerId);
        redisTemplate.delete("provider:status:" + providerId);
        eventProducer.publishProviderActivated(provider, adminId);

        return providerMapper.toResponse(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalStatistics getApprovalStatistics() {
        long pending = providerRepository.countByStatus(ProviderStatus.PENDING_APPROVAL);
        long approvedToday = providerRepository.findByStatus(ProviderStatus.ACTIVE).stream()
                .filter(p -> p.getApprovedAt() != null &&
                        p.getApprovedAt().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
                .count();
        long rejectedToday = providerRepository.findByStatus(ProviderStatus.REJECTED).stream()
                .filter(p -> p.getUpdatedAt() != null &&
                        p.getUpdatedAt().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
                .count();
        long suspended = providerRepository.countByStatus(ProviderStatus.SUSPENDED);
        long pendingDocs = providerRepository.findAll().stream()
                .filter(p -> documentRepository.countPendingByProviderId(p.getId()) > 0)
                .count();

        return ApprovalStatistics.builder()
                .pendingApprovals(pending)
                .approvedToday(approvedToday)
                .rejectedToday(rejectedToday)
                .suspendedCount(suspended)
                .averageApprovalTimeHours(24.5)
                .pendingDocumentVerification(pendingDocs)
                .build();
    }

    @Override
    @Transactional
    public void verifyDocuments(String adminId, String providerId, List<String> documentIds, boolean verified) {
        for (String documentId : documentIds) {
            ProviderDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

            if (verified) {
                document.setVerified(true);
                document.setVerifiedBy(adminId);
                document.setVerifiedAt(Instant.now());
            } else {
                document.setVerified(false);
                document.setRemarks("Document rejected during verification");
            }
            documentRepository.save(document);
        }

        long pendingCount = documentRepository.countPendingByProviderId(providerId);
        if (pendingCount == 0) {
            Provider provider = providerRepository.findById(providerId).orElse(null);
            if (provider != null && provider.getStatus() == ProviderStatus.DOCUMENTS_PENDING) {
                provider.setStatus(ProviderStatus.PENDING_APPROVAL);
                providerRepository.save(provider);
            }
        }
    }

    @Override
    public boolean isProviderApproved(String providerId) {
        String cachedStatus = redisTemplate.opsForValue().get("provider:status:" + providerId);
        if (cachedStatus != null) {
            return "ACTIVE".equals(cachedStatus);
        }

        Provider provider = providerRepository.findById(providerId).orElse(null);
        boolean isApproved = provider != null && provider.getStatus() == ProviderStatus.ACTIVE;

        if (isApproved) {
            redisTemplate.opsForValue().set("provider:status:" + providerId, "ACTIVE", 1, TimeUnit.HOURS);
        }

        return isApproved;
    }

    @Override
    public List<ProviderApprovalResponse> getApprovalHistory(String providerId) {
        return List.of();
    }

    @Override
    @Transactional
    public void assignApproval(String providerId, String adminId) {
        approvalRepository.assignApproval(providerId, adminId, Instant.now());
    }

    private void cacheProviderStatus(String providerId, String status) {
        String key = "provider:status:" + providerId;
        redisTemplate.opsForValue().set(key, status, 1, TimeUnit.HOURS);

        if ("SUSPENDED".equals(status)) {
            redisTemplate.opsForValue().set("suspended:provider:" + providerId, "true", 1, TimeUnit.HOURS);
        }
    }
}
