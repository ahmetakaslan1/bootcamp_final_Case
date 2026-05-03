package com.akaslan.paymentservice.adapter;

import com.akaslan.paymentservice.dto.PaymentRequest;

public interface PaymentProvider {
    boolean pay(PaymentRequest request);
}
