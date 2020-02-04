package com.limethecoder.accounting.service;

import com.limethecoder.accounting.model.MoneyTransferRequest;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Singleton
public class AccountLockServiceImpl implements AccountLockService {

    private final Map<Long, Lock> accountLocks = new ConcurrentHashMap<>();

    @Override
    public void executeWithinLocks(Consumer<MoneyTransferRequest> consumer, MoneyTransferRequest request) {
        try {
            lockAccounts(request.getFromAccount(), request.getToAccount());
            consumer.accept(request);
        } finally {
            unlockAccounts(request.getFromAccount(), request.getToAccount());
        }
    }

    private void lockAccounts(long firstAccount, long secondAccount) {
        Lock firstAccountLock = getAccountLock(firstAccount);
        Lock secondAccountLock = getAccountLock(secondAccount);

        boolean locksAcquired = false;
        do {
            if (firstAccountLock.tryLock()) {
                if (secondAccountLock.tryLock()) {
                    locksAcquired = true;
                } else {
                    firstAccountLock.unlock();
                }
            }
        } while (!locksAcquired);
    }

    private void unlockAccounts(long firstAccount, long secondAccount) {
        Lock firstAccountLock = getAccountLock(firstAccount);
        Lock secondAccountLock = getAccountLock(secondAccount);

        secondAccountLock.unlock();
        firstAccountLock.unlock();
    }

    private Lock getAccountLock(long id) {
        return accountLocks.computeIfAbsent(id, ignored -> new ReentrantLock());
    }
}
