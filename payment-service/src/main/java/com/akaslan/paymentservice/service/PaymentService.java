package com.akaslan.paymentservice.service;

import com.akaslan.paymentservice.dto.PaymentRequest;
import com.akaslan.paymentservice.entity.Payment;

public interface PaymentService {
    Payment processPayment(PaymentRequest request);
}
