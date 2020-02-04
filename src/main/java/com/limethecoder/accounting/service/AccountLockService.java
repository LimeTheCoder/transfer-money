package com.limethecoder.accounting.service;

import com.limethecoder.accounting.model.MoneyTransferRequest;

import java.util.function.Consumer;

public interface AccountLockService {

    /**
     * Executes provided requests within locks
     *
     * @param consumer consumer
     * @param request request
     */
    void executeWithinLocks(Consumer<MoneyTransferRequest> consumer, MoneyTransferRequest request);
}
