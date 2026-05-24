package com.igirepay.idempotency_gateway;

public class PaymentRequest {
    private int amount;
    private String currency;

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}