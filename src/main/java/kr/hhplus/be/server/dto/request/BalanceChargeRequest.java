package kr.hhplus.be.server.dto.request;

import java.math.BigDecimal;

public class BalanceChargeRequest {
    private BigDecimal amount;

    public BalanceChargeRequest() {}

    public BalanceChargeRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
