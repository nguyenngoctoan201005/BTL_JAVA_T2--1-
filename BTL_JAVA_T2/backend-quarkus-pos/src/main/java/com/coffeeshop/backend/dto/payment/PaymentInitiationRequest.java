package com.coffeeshop.backend.dto.payment;

import lombok.Data;

@Data
public class PaymentInitiationRequest {
    private long amount;
    private String orderInfo;
    private String orderId;
    private String bankCode;
}
