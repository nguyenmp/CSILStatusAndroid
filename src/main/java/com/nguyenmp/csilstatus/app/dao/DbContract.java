package com.nguyenmp.csilstatus.app.dao;

import android.provider.BaseColumns;

public final class DbContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String BOOL_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DbContract() {}

    /* Inner class that defines the table contents */
    public static abstract class UsageEntry implements BaseColumns {
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + UsageEntry.TABLE_NAME + " (" +
                        UsageEntry._ID + " INTEGER PRIMARY KEY," +
                        UsageEntry.COLUMN_NAME_IP_ADDRESS + TEXT_TYPE + COMMA_SEP +
                        UsageEntry.COLUMN_NAME_HOSTNAME + TEXT_TYPE + COMMA_SEP +
                        UsageEntry.COLUMN_NAME_USERNAME + TEXT_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + UsageEntry.TABLE_NAME;

        public static final String TABLE_NAME = "usage";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_IP_ADDRESS = "ipaddress";
        public static final String COLUMN_NAME_HOSTNAME = "hostname";
    }

    /* Inner class that defines the table contents */
    public static abstract class ComputerEntry implements BaseColumns {
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ComputerEntry.TABLE_NAME + " (" +
                        ComputerEntry._ID + " INTEGER PRIMARY KEY," +
                        ComputerEntry.COLUMN_NAME_HOSTNAME + TEXT_TYPE + COMMA_SEP +
                        ComputerEntry.COLUMN_NAME_IP_ADDRESS + TEXT_TYPE + COMMA_SEP +
                        ComputerEntry.COLUMN_NAME_IS_ACTIVE + BOOL_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ComputerEntry.TABLE_NAME;

        public static final String TABLE_NAME = "computer";
        public static final String COLUMN_NAME_HOSTNAME = "hostname";
        public static final String COLUMN_NAME_IP_ADDRESS = "ipaddress";
        public static final String COLUMN_NAME_IS_ACTIVE = "isactive";
    }
}