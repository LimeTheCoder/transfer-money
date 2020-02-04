package com.limethecoder.accounting;

import com.limethecoder.accounting.conf.AppConfig;
import com.limethecoder.accounting.model.Account;
import com.limethecoder.accounting.model.MoneyTransferRequest;
import com.limethecoder.accounting.repository.AccountingRepository;
import com.limethecoder.accounting.repository.AccountingRepositoryImpl;
import com.limethecoder.accounting.service.AccountLockService;
import com.limethecoder.accounting.service.AccountLockServiceImpl;
import com.limethecoder.accounting.service.AccountingService;
import com.limethecoder.accounting.service.AccountingServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LoadingTest {

    private static final long INITIAL_VALUE = 1_000_000L;

    private AccountingService accountingService;
    private ExecutorService executorService;
    private Long fromAccountId;
    private Long toAccountId;

    @Before
    public void setUp() {
        AccountingRepository accountingRepository = new AccountingRepositoryImpl(new AppConfig().h2DataSource());
        AccountLockService accountLockService = new AccountLockServiceImpl();
        accountingService = new AccountingServiceImpl(accountingRepository, accountLockService);
        executorService = Executors.newFixedThreadPool(2);

        fromAccountId = accountingService.createAccount(INITIAL_VALUE).getId();
        toAccountId = accountingService.createAccount(INITIAL_VALUE).getId();
    }

    @Test(timeout = 30000)
    public void shouldAvoidDeadLocks() {
        List<Callable<Object>> tasks = new ArrayList<>();
        BigDecimal amount = BigDecimal.valueOf(50);
        for (int i = 0; i < 30; i++) {
            MoneyTransferRequest transferRequest = new MoneyTransferRequest(fromAccountId, toAccountId, amount);
            MoneyTransferRequest returnRequest = new MoneyTransferRequest(toAccountId, fromAccountId, amount);
            tasks.add(() -> { accountingService.transferMoney(transferRequest); return null; });
            tasks.add(() -> { accountingService.transferMoney(returnRequest); return null; });
        }
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Account fromAccount = accountingService.findAccount(fromAccountId);
        assertThat(fromAccount.getBalance().longValue(), is(INITIAL_VALUE));

        Account toAccount = accountingService.findAccount(toAccountId);
        assertThat(toAccount.getBalance().longValue(), is(INITIAL_VALUE));
    }
}
