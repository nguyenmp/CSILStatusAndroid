package com.nguyenmp.csilstatus.app.dao;

import android.provider.BaseColumns;

public final class ComputerUserContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ComputerUserContract() {}

    /* Inner class that defines the table contents */
    public static abstract class ComputerUserEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_computer";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_IP_ADDRESS = "ipaddress";
        public static final String COLUMN_NAME_HOSTNAME = "hostname";
    }
}