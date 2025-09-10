package kr.hhplus.be.server.dto.response;

import java.math.BigDecimal;

public class BalanceChargeResponse {
    private String userId;
    private BigDecimal previousBalance;
    private BigDecimal chargeAmount;
    private BigDecimal newBalance;
    private String transactionId;
    private String chargedAt;

    public BalanceChargeResponse() {}

    public BalanceChargeResponse(String userId, BigDecimal previousBalance, 
                               BigDecimal chargeAmount, BigDecimal newBalance,
                               String transactionId, String chargedAt) {
        this.userId = userId;
        this.previousBalance = previousBalance;
        this.chargeAmount = chargeAmount;
        this.newBalance = newBalance;
        this.transactionId = transactionId;
        this.chargedAt = chargedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getChargedAt() {
        return chargedAt;
    }

    public void setChargedAt(String chargedAt) {
        this.chargedAt = chargedAt;
    }
}
