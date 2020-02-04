package com.limethecoder.accounting.service;

import com.limethecoder.accounting.model.Account;
import com.limethecoder.accounting.model.MoneyTransferRequest;
import com.limethecoder.accounting.repository.AccountingRepository;
import org.osgl.mvc.result.NotFound;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;

@Singleton
public class AccountingServiceImpl implements AccountingService {

    private AccountingRepository accountingRepository;
    private AccountLockService accountLockService;

    @Inject
    public AccountingServiceImpl(AccountingRepository accountingRepository,
                                 AccountLockService accountLockService) {
        this.accountingRepository = accountingRepository;
        this.accountLockService = accountLockService;
    }

    @Override
    public Account createAccount(Long balance) {
        BigDecimal amount = Optional.ofNullable(balance).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        return accountingRepository.create(amount);
    }

    @Override
    public Account findAccount(Long id) {
        if (id == null) {
            throw new IllegalArgumentException();
        }

        return accountingRepository.find(id)
                .orElseThrow(NotFound::new);
    }

    @Override
    public void transferMoney(MoneyTransferRequest request) {
        validateTransferRequest(request);
        accountLockService.executeWithinLocks(this::processMoneyTransfer, request);
    }

    private void processMoneyTransfer(MoneyTransferRequest request) {
        BigDecimal amount = request.getAmount();
        Account fromAccount = findAccount(request.getFromAccount());
        Account toAccount = findAccount(request.getToAccount());

        ensureHasEnoughMoney(fromAccount, amount);
        updateBalance(toAccount, amount);
        updateBalance(fromAccount, amount.negate());
    }

    private void validateTransferRequest(MoneyTransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException();
        }

        if (request.getFromAccount() == request.getToAccount()) {
            throw new IllegalArgumentException("Accounts shouldn't be the same");
        }

        if (request.getAmount() == null || isLessOrEqualZero(request.getAmount())) {
            throw new IllegalArgumentException("Transfer amount is less or equal to 0");
        }
    }

    private boolean isLessOrEqualZero(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) <= 0;
    }

    private void ensureHasEnoughMoney(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Account has not sufficient balance for processing operation");
        }
    }

    private void updateBalance(Account account, BigDecimal diff) {
        account.setBalance(account.getBalance().add(diff));
        accountingRepository.updateBalance(account);
    }
}
