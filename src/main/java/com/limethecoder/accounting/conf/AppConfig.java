package com.limethecoder.accounting.conf;

import com.limethecoder.accounting.repository.AccountingRepository;
import com.limethecoder.accounting.repository.AccountingRepositoryImpl;
import com.limethecoder.accounting.service.AccountLockService;
import com.limethecoder.accounting.service.AccountLockServiceImpl;
import com.limethecoder.accounting.service.AccountingService;
import com.limethecoder.accounting.service.AccountingServiceImpl;
import com.limethecoder.accounting.util.PropertiesUtil;
import org.h2.jdbcx.JdbcDataSource;
import org.osgl.inject.annotation.Provides;

import javax.sql.DataSource;

public class AppConfig extends org.osgl.inject.Module {

    @Override
    protected void configure() {
        bind(AccountingRepository.class).to(AccountingRepositoryImpl.class);
        bind(AccountingService.class).to(AccountingServiceImpl.class);
        bind(AccountLockService.class).to(AccountLockServiceImpl.class);
    }

    @Provides
    public DataSource h2DataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(PropertiesUtil.getProperty(PropertiesUtil.DB_URL));
        dataSource.setUser(PropertiesUtil.getProperty(PropertiesUtil.DB_USERNAME));
        dataSource.setPassword(PropertiesUtil.getProperty(PropertiesUtil.DB_PASSWORD));
        return dataSource;
    }
}
