package com.igirepay.idempotency_gateway;

public class PaymentResponse {
    private String message;
    private String transactionId;
    private String status;

    public PaymentResponse(String message, String transactionId, String status) {
        this.message = message;
        this.transactionId = transactionId;
        this.status = status;
    }

    public String getMessage() { return message; }
    public String getTransactionId() { return transactionId; }
    public String getStatus() { return status; }
}