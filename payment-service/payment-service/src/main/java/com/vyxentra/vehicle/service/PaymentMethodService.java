package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.response.PaymentMethodResponse;

import java.util.List;
import java.util.Map;

public interface PaymentMethodService {

    PaymentMethodResponse addPaymentMethod(String userId, PaymentMethodRequest request);

    List<PaymentMethodResponse> getUserPaymentMethods(String userId);

    PaymentMethodResponse getPaymentMethod(String methodId, String userId);

    void deletePaymentMethod(String methodId, String userId);

    void setDefaultPaymentMethod(String methodId, String userId);

    PaymentMethodResponse verifyPaymentMethod(String methodId, Map<String, Object> verificationData);

    boolean isPaymentMethodValid(String methodId);

    PaymentMethodResponse getDefaultPaymentMethod(String userId);

    void updatePaymentMethodMetadata(String methodId, Map<String, Object> metadata);

    List<PaymentMethodResponse> getExpiringMethods(int monthsThreshold);
}
