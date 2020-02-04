package com.limethecoder.accounting.repository;

import com.limethecoder.accounting.model.Account;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountingRepository {
    Account create(BigDecimal balance);
    Optional<Account> find(long id);
    void updateBalance(Account account);
}
