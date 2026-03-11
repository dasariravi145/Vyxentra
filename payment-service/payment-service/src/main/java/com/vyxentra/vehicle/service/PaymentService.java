package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.CreatePaymentRequest;
import com.vyxentra.vehicle.dto.request.ProcessPaymentRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.PaymentDetailResponse;
import com.vyxentra.vehicle.dto.response.PaymentResponse;
import com.vyxentra.vehicle.dto.response.TransactionResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(String customerId, CreatePaymentRequest request);

    PaymentResponse processPayment(String paymentId, ProcessPaymentRequest request);

    PaymentDetailResponse getPayment(String paymentId);

    PaymentDetailResponse getPaymentByNumber(String paymentNumber);

    List<PaymentResponse> getBookingPayments(String bookingId);

    List<PaymentResponse> getCustomerPayments(String customerId, LocalDateTime fromDate, LocalDateTime toDate);

    boolean verifyPayment(String paymentId);

    void handleWebhook(String gateway, String payload, String signature);

    PageResponse<TransactionResponse> getUserTransactions(String userId, Pageable pageable);

    String getPaymentStatus(String bookingId);

    void processExpiredPayments();
}
