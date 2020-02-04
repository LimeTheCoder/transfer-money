package com.limethecoder.accounting.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.internal.configuration.ConfigUtils;

import java.util.Properties;

public final class DbMigrationUtil {

    private static Flyway FLYWAY;

    static {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.configure(flywayProperties());

        FLYWAY = new Flyway(configuration);
    }

    private DbMigrationUtil() {}

    public static void migrate() {
        FLYWAY.clean();
        FLYWAY.migrate();
    }

    private static Properties flywayProperties() {
        Properties flywayProperties = new Properties();
        flywayProperties.setProperty(ConfigUtils.URL, PropertiesUtil.getProperty(PropertiesUtil.DB_URL));
        flywayProperties.setProperty(ConfigUtils.DRIVER, PropertiesUtil.getProperty(PropertiesUtil.DB_DRIVER));
        flywayProperties.setProperty(ConfigUtils.USER, PropertiesUtil.getProperty(PropertiesUtil.DB_USERNAME));
        flywayProperties.setProperty(ConfigUtils.PASSWORD, PropertiesUtil.getProperty(PropertiesUtil.DB_PASSWORD));
        return flywayProperties;
    }
}
