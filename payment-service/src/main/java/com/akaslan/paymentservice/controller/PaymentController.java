package com.akaslan.paymentservice.controller;

import com.akaslan.common.dto.ApiResponse;
import com.akaslan.paymentservice.dto.PaymentRequest;
import com.akaslan.paymentservice.entity.Payment;
import com.akaslan.paymentservice.entity.PaymentStatus;
import com.akaslan.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @AuthenticationPrincipal Jwt jwt, 
            @Valid @RequestBody PaymentRequest request) {
        
        Payment payment = paymentService.processPayment(request);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return ResponseEntity.ok(ApiResponse.success("Ödeme başarıyla tamamlandı", payment));
        } else {
            // Ödeme başarısızsa Exception fırlatılabilir veya badRequest dönülebilir.
            // SAGA pattern için genellikle exception fırlatılıp Order iptal edilir.
            throw new IllegalArgumentException("Ödeme işlemi banka tarafından reddedildi.");
        }
    }
}
