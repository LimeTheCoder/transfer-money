package com.limethecoder.accounting.service;

import com.limethecoder.accounting.model.Account;
import com.limethecoder.accounting.model.MoneyTransferRequest;

public interface AccountingService {
    /**
     * Creates account with provided balance
     *
     * @param balance balance
     * @return created account info
     */
    Account createAccount(Long balance);

    /**
     * Finds account by provided id
     * @param id account identifier
     * @return account if found or {@code null} otherwise
     */
    Account findAccount(Long id);

    /**
     * Transfers money from one account to another
     *
     * @param request money transer request
     */
    void transferMoney(MoneyTransferRequest request);
}
