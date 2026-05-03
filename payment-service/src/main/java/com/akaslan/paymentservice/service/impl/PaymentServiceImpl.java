package com.akaslan.paymentservice.service.impl;

import com.akaslan.paymentservice.adapter.PaymentProvider;
import com.akaslan.paymentservice.dto.PaymentRequest;
import com.akaslan.paymentservice.entity.Payment;
import com.akaslan.paymentservice.entity.PaymentStatus;
import com.akaslan.paymentservice.repository.PaymentRepository;
import com.akaslan.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider; // Adapter Pattern devrede! IyzicoAdapter otomatik enjekte edilir.

    @Override
    @Transactional
    public Payment processPayment(PaymentRequest request) {
        // aynı  ödeme kaydı varsa tekrar provider çağırma engel iyorum.
        var existingPayment = paymentRepository.findByOrderId(request.orderId());
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

      
        boolean isSuccess = paymentProvider.pay(request);

       //log
        Payment payment = Payment.builder()
            .orderId(request.orderId())
            .amount(request.amount())
            .status(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
            .build();

        return paymentRepository.save(payment);
    }
}
