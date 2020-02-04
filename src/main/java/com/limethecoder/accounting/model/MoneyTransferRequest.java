package com.limethecoder.accounting.model;

import java.math.BigDecimal;

public class MoneyTransferRequest {
    private long fromAccount;
    private long toAccount;
    private BigDecimal amount;

    public MoneyTransferRequest() {}

    public MoneyTransferRequest(long fromAccount, long toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    public long getFromAccount() {
        return fromAccount;
    }

    public long getToAccount() {
        return toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setFromAccount(long fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setToAccount(long toAccount) {
        this.toAccount = toAccount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
