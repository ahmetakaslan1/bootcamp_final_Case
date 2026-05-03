package com.akaslan.paymentservice.adapter;

import com.akaslan.paymentservice.dto.PaymentRequest;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IyzicoPaymentAdapter implements PaymentProvider {

    private final Options options;

    @Override
    public boolean pay(PaymentRequest request) {
        CreatePaymentRequest iyzicoRequest = new CreatePaymentRequest();
        iyzicoRequest.setLocale(Locale.TR.getValue());
        iyzicoRequest.setConversationId(UUID.randomUUID().toString());
        iyzicoRequest.setPrice(request.amount());
        iyzicoRequest.setPaidPrice(request.amount());
        iyzicoRequest.setCurrency(Currency.TRY.name());
        iyzicoRequest.setInstallment(1);
        iyzicoRequest.setBasketId(request.orderId());
        iyzicoRequest.setPaymentChannel(PaymentChannel.WEB.name());
        iyzicoRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(request.cardHolderName());
        paymentCard.setCardNumber(request.cardNumber());
        paymentCard.setExpireMonth(request.expireMonth());
        paymentCard.setExpireYear(request.expireYear());
        paymentCard.setCvc(request.cvc());
        paymentCard.setRegisterCard(0);
        iyzicoRequest.setPaymentCard(paymentCard);

        Buyer buyer = new Buyer();
        buyer.setId("BY789");
        buyer.setName("Akaslan");
        buyer.setSurname("Customer");
        buyer.setGsmNumber("+905350000000");
        buyer.setEmail("email@email.com");
        buyer.setIdentityNumber("74300864791");
        buyer.setLastLoginDate("2015-10-05 12:43:35");
        buyer.setRegistrationDate("2013-04-21 15:12:09");
        buyer.setRegistrationAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        buyer.setIp("85.34.78.112");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34732");
        iyzicoRequest.setBuyer(buyer);

        Address shippingAddress = new Address();
        shippingAddress.setContactName("Akaslan Customer");
        shippingAddress.setCity("Istanbul");
        shippingAddress.setCountry("Turkey");
        shippingAddress.setAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        shippingAddress.setZipCode("34742");
        iyzicoRequest.setShippingAddress(shippingAddress);

        Address billingAddress = new Address();
        billingAddress.setContactName("Akaslan Customer");
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        billingAddress.setZipCode("34742");
        iyzicoRequest.setBillingAddress(billingAddress);

        List<BasketItem> basketItems = new ArrayList<>();
        BasketItem item = new BasketItem();
        item.setId("BI101");
        item.setName("Genel Siparis Sepeti");
        item.setCategory1("Electronics");
        item.setItemType(BasketItemType.PHYSICAL.name());
        item.setPrice(request.amount());
        basketItems.add(item);
        iyzicoRequest.setBasketItems(basketItems);

        // İyzico'ya İsteği Gönder (Gerçek Sandbox API Çağrısı)
        Payment payment = Payment.create(iyzicoRequest, options);

        if ("failure".equalsIgnoreCase(payment.getStatus())) {
            System.err.println("İyzico Ödeme Hatası: " + payment.getErrorMessage());
        }

        return "success".equalsIgnoreCase(payment.getStatus());
    }
}
