package com.limethecoder.accounting;

import com.limethecoder.accounting.model.Account;
import com.limethecoder.accounting.model.MoneyTransferRequest;
import com.limethecoder.accounting.repository.AccountingRepository;
import com.limethecoder.accounting.service.AccountLockService;
import com.limethecoder.accounting.service.AccountLockServiceImpl;
import com.limethecoder.accounting.service.AccountingServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgl.mvc.result.NotFound;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountingServiceImplTest {

    private static final Account TEST_ACCOUNT = new Account(1L, BigDecimal.TEN);

    @InjectMocks
    private AccountingServiceImpl accountingService;

    @Mock
    private AccountingRepository accountingRepository;

    @Spy
    private AccountLockService accountLockService = new AccountLockServiceImpl();

    @Captor
    private ArgumentCaptor<BigDecimal> balanceCaptor;

    @Captor
    private ArgumentCaptor<Account> accountsCaptor;

    @Before
    public void setUp() {
        when(accountingRepository.create(any(BigDecimal.class))).thenReturn(TEST_ACCOUNT);
    }

    @Test
    public void shouldCreateAccountWithProvidedBalance() {
        Account createdAccount = accountingService.createAccount(5L);

        assertThat(createdAccount, is(TEST_ACCOUNT));
        verify(accountingRepository).create(balanceCaptor.capture());

        BigDecimal value = balanceCaptor.getValue();
        assertThat(value.intValue(), is(5));
    }

    @Test
    public void shouldCreateAccountWithZeroBalanceIfNotProvided() {
        accountingService.createAccount(null);

        verify(accountingRepository).create(balanceCaptor.capture());

        BigDecimal value = balanceCaptor.getValue();
        assertThat(value.intValue(), is(0));
    }

    @Test
    public void shouldSuccessfullyFindAccountIfExists() {
        when(accountingRepository.find(anyLong())).thenReturn(Optional.of(TEST_ACCOUNT));

        long accountId = 1L;
        Account account = accountingService.findAccount(accountId);

        assertThat(account, is(TEST_ACCOUNT));
        verify(accountingRepository).find(accountId);
    }

    @Test(expected = NotFound.class)
    public void shouldThrowNotFoundExceptionIfAccountIsNotExists() {
        when(accountingRepository.find(anyLong())).thenReturn(Optional.empty());
        accountingService.findAccount(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfInvalidAccountIdProvided() {
        accountingService.findAccount(null);
    }

    @Test
    public void shouldTransferMoney() {
        // given
        Account fromAccount = new Account(1L, BigDecimal.TEN);
        Account toAccount = new Account(2L, BigDecimal.ZERO);

        MoneyTransferRequest transferRequest = new MoneyTransferRequest(fromAccount.getId(),
                toAccount.getId(), BigDecimal.TEN);

        BigDecimal fromAccountExpectedBalance = fromAccount.getBalance()
                .subtract(transferRequest.getAmount());
        BigDecimal toAccountExpectedBalance = toAccount.getBalance()
                .add(transferRequest.getAmount());

        when(accountingRepository.find(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountingRepository.find(toAccount.getId())).thenReturn(Optional.of(toAccount));

        // when
        accountingService.transferMoney(transferRequest);

        // verify
        verify(accountingRepository).find(fromAccount.getId());
        verify(accountingRepository).find(toAccount.getId());
        verify(accountLockService).executeWithinLocks(any(), eq(transferRequest));

        verify(accountingRepository, times(2)).updateBalance(accountsCaptor.capture());

        // then
        Map<Long, Account> accounts = accountsCaptor.getAllValues().stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Account updatedFromAccount = accounts.get(fromAccount.getId());
        assertThat(updatedFromAccount.getBalance(), is(fromAccountExpectedBalance));

        Account updatedToAccount = accounts.get(toAccount.getId());
        assertThat(updatedToAccount.getBalance(), is(toAccountExpectedBalance));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfEmptyRequestProvided() {
        accountingService.transferMoney(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfToAndFromAccountsSame() {
        MoneyTransferRequest transferRequest = new MoneyTransferRequest(1L, 1L, BigDecimal.TEN);
        accountingService.transferMoney(transferRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfAmountIsNotProvided() {
        MoneyTransferRequest transferRequest = new MoneyTransferRequest(1L, 2L, null);
        accountingService.transferMoney(transferRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfAmountIsLessOrEqualZero() {
        MoneyTransferRequest transferRequest = new MoneyTransferRequest(1L, 2L, BigDecimal.valueOf(-5));
        accountingService.transferMoney(transferRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfFromAccountHasInsufficientBalance() {
        Account fromAccount = new Account(1L, BigDecimal.TEN);
        Account toAccount = new Account(2L, BigDecimal.ZERO);

        when(accountingRepository.find(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountingRepository.find(toAccount.getId())).thenReturn(Optional.of(toAccount));

        MoneyTransferRequest transferRequest = new MoneyTransferRequest(fromAccount.getId(), toAccount.getId(), BigDecimal.valueOf(50));
        accountingService.transferMoney(transferRequest);
    }
}
