package com.limethecoder.accounting;

import act.Act;
import com.limethecoder.accounting.util.DbMigrationUtil;

public class AppEntry {

    public static void main(String[] args) throws Exception {
        AppEntry appEntry = new AppEntry();
        appEntry.start();
    }

    public void start() throws Exception {
        DbMigrationUtil.migrate();
        Act.start();
    }
}
