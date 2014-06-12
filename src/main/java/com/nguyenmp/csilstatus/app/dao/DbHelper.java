package com.nguyenmp.csilstatus.app.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.nguyenmp.csilstatus.app.dao.DbContract.ComputerEntry;
import static com.nguyenmp.csilstatus.app.dao.DbContract.UsageEntry;

public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ComputerUser.db";
    private static final String[] SQL_CREATE_ENTRIES = {ComputerEntry.SQL_CREATE_ENTRIES, UsageEntry.SQL_CREATE_ENTRIES};
    private static final String[] SQL_DELETE_ENTRIES = {ComputerEntry.SQL_DELETE_ENTRIES, UsageEntry.SQL_DELETE_ENTRIES};

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        for (String statement : SQL_CREATE_ENTRIES) {
            db.execSQL(statement);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        for (String statement : SQL_DELETE_ENTRIES) {
            db.execSQL(statement);
        }
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}