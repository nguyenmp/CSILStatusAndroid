package com.nguyenmp.csilstatus.app.dao;

import android.provider.BaseColumns;

public final class ComputerContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ComputerContract() {}

    /* Inner class that defines the table contents */
    public static abstract class ComputerEntry implements BaseColumns {
        public static final String TABLE_NAME = "computer";
        public static final String COLUMN_NAME_HOSTNAME = "hostname";
        public static final String COLUMN_NAME_IP_ADDRESS = "ipaddress";
        public static final String COLUMN_NAME_IS_ACTIVE = "isactive";
    }
}