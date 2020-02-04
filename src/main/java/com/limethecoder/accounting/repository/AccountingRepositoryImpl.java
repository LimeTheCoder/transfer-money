package com.limethecoder.accounting.repository;

import com.limethecoder.accounting.model.Account;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.osgl.mvc.result.InternalServerError;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class AccountingRepositoryImpl implements AccountingRepository {

    private DataSource dataSource;

    @Inject
    public AccountingRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Account create(BigDecimal balance) {
        QueryRunner runner = new QueryRunner(dataSource);
        ResultSetHandler<Long> handler = new ScalarHandler<>();

        try {
            long accountId = runner.insert( "INSERT INTO account (balance) VALUES (?)", handler, balance);
            return new Account(accountId, balance);
        } catch (SQLException e) {
            throw new InternalServerError(e);
        }
    }

    @Override
    public Optional<Account> find(long id) {
        QueryRunner runner = new QueryRunner(dataSource);
        ResultSetHandler<Account> handler = new BeanHandler<>(Account.class);

        try {
            Account account = runner.query("SELECT id, balance FROM account WHERE id=?", handler, id);
            return Optional.ofNullable(account);
        } catch (SQLException e) {
            throw new InternalServerError(e);
        }
    }

    @Override
    public void updateBalance(Account account) {
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update( "UPDATE account SET balance=? WHERE id=?", account.getBalance(), account.getId());
        } catch (SQLException e) {
            throw new InternalServerError(e);
        }
    }
}
