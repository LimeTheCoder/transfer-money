package com.limethecoder.accounting.util;

import com.limethecoder.accounting.exception.MissingPropertiesException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {
    public static final String DB_URL = "db.url";
    public static final String DB_DRIVER = "db.driver";
    public static final String DB_USERNAME = "db.user";
    public static final String DB_PASSWORD = "db.password";

    private static final String DB_PROPERTIES_FILE = "db/db.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream propsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(DB_PROPERTIES_FILE)) {
            PROPERTIES.load(propsStream);
        } catch (IOException ex) {
            throw new MissingPropertiesException("Missing properties file", ex);
        }
    }

    private PropertiesUtil() {}

    public static String getProperty(String name) {
        return PROPERTIES.getProperty(name);
    }
}
