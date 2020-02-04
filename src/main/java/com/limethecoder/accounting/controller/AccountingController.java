package com.limethecoder.accounting.controller;

import act.controller.Controller;
import com.limethecoder.accounting.model.Account;
import com.limethecoder.accounting.model.MoneyTransferRequest;
import com.limethecoder.accounting.service.AccountingService;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import org.osgl.mvc.result.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AccountingController extends Controller.Util {

    @Inject
    private AccountingService accountingService;

    @GetAction("/account/{accountId}")
    public Account findAccount(Long accountId) {
        return accountingService.findAccount(accountId);
    }

    @PostAction("/account")
    public Account createAccount(long balance) {
        return accountingService.createAccount(balance);
    }

    @PostAction("/account/transfer")
    public Result transferMoney(MoneyTransferRequest request) {
        accountingService.transferMoney(request);
        return ok();
    }
}
