package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.RefundRequest;
import com.vyxentra.vehicle.dto.response.RefundResponse;

import java.util.List;

public interface RefundService {

    RefundResponse initiateRefund(String userId, RefundRequest request);

    RefundResponse getRefund(String refundId);

    List<RefundResponse> getPaymentRefunds(String paymentId);

    RefundResponse processRefund(String refundId);

    void cancelRefund(String refundId, String userId);

    List<RefundResponse> getPendingRefunds(String paymentId);

    void processAutoRefunds();
}
