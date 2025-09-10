package kr.hhplus.be.server.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class BalanceResponse {
    private String userId;
    private BigDecimal currentBalance;
    private String lastUpdatedAt;
    private List<TransactionInfo> recentTransactions;

    public BalanceResponse() {}

    public BalanceResponse(String userId, BigDecimal currentBalance, 
                         String lastUpdatedAt, List<TransactionInfo> recentTransactions) {
        this.userId = userId;
        this.currentBalance = currentBalance;
        this.lastUpdatedAt = lastUpdatedAt;
        this.recentTransactions = recentTransactions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public List<TransactionInfo> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionInfo> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public static class TransactionInfo {
        private String transactionId;
        private String type;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String createdAt;

        public TransactionInfo() {}

        public TransactionInfo(String transactionId, String type, BigDecimal amount, 
                             BigDecimal balanceAfter, String createdAt) {
            this.transactionId = transactionId;
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balanceAfter;
            this.createdAt = createdAt;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getBalanceAfter() {
            return balanceAfter;
        }

        public void setBalanceAfter(BigDecimal balanceAfter) {
            this.balanceAfter = balanceAfter;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
