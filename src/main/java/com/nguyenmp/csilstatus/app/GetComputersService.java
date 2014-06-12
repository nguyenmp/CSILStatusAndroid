package com.nguyenmp.csilstatus.app;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.nguyenmp.csil.concurrency.CommandExecutor;
import com.nguyenmp.csilstatus.app.dao.ComputerContract.ComputerEntry;
import com.nguyenmp.csilstatus.app.dao.ComputerDbHelper;
import com.nguyenmp.csilstatus.app.dao.ComputerUserDbHelper;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.nguyenmp.csilstatus.app.dao.ComputerUserContract.ComputerUserEntry;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class GetComputersService extends IntentService {
    private static final String TAG = "GetComputersService";

    private static final Collection<CallbackHandler> CALLBACKS = new CopyOnWriteArraySet<CallbackHandler>();

    public GetComputersService() {
        super("GetComputersService");
    }

    public static void refresh(Context context) {
        if (context == null) return;

        Intent intent = new Intent(context, GetComputersService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Refreshing!
        int numThreads = 40;
        ExecutorService exec = Executors.newFixedThreadPool(numThreads);

        // Documetation says we always return an array of two strings
        // Content is null if not logged in
        String[] credentials = LoginActivity.getCredentials(this);
        String username = credentials[0];
        String password = credentials[1];

        // Sanity check to make sure we are logged in
        if (username == null || password == null) return;

        ComputerDbHelper computerDbHelper = new ComputerDbHelper(this);
        final SQLiteDatabase database = computerDbHelper.getWritableDatabase();
        String table = ComputerEntry.TABLE_NAME;
        String[] columns = new String[] {ComputerEntry.COLUMN_NAME_HOSTNAME, ComputerEntry.COLUMN_NAME_IP_ADDRESS};
        Cursor cursor = database.query(table, columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            final String hostname = cursor.getString(cursor.getColumnIndex(ComputerEntry.COLUMN_NAME_HOSTNAME));
            final String ipAddress = cursor.getString(cursor.getColumnIndex(ComputerEntry.COLUMN_NAME_IP_ADDRESS));
            exec.execute(new WhoRunner(this, username, password, hostname, ipAddress));
        }

        try {
            exec.shutdown();
            exec.awaitTermination(999999, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        database.close();
    }

    private static void notifyCallbacks() {
        for (CallbackHandler callback : CALLBACKS) {
            callback.sendEmptyMessage(0);
        }
    }


    /**
     * The callback to add to our reference list to notify whenever something
     * changes.
     *
     * This should be called before sending the intent to the service to prevent
     * this callback from being skipped if it was added after the operation
     * was performed.
     * @param callback the callback to add
     */
    public static void registerCallback(Looper looper, Callback callback) {
        if (callback != null) {
            CALLBACKS.add(new CallbackHandler(looper, callback));
            notifyCallbacks();
        }
    }

    /**
     * Releases reference to the callback to allow the resources to be freed
     * @param callback the callback to release
     */
    public static void unregisterCallback(Callback callback) {
        for (CallbackHandler handler : CALLBACKS) {
            if (handler.callback == callback) {
                CALLBACKS.remove(handler);
            }
        }
    }

    public static interface Callback {
        public void onUpdated();
    }

    private static class CallbackHandler extends Handler {
        private final GetComputersService.Callback callback;

        CallbackHandler(Looper looper, GetComputersService.Callback callback) {
            super(looper);
            this.callback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            callback.onUpdated();
        }
    }

    private static class WhoRunner extends CommandExecutor {
        private final String ipAddress;
        private final String hostname;
        private final Context context;

        WhoRunner(Context context, String username, String password, String hostname, String ipAddress) {
            super(username, password, hostname, "who");
            this.ipAddress = ipAddress;
            this.context = context;
            this.hostname = hostname;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            super.run();
        }

        @Override
        public void onSuccess (String s){
            synchronized (context) {
                SQLiteDatabase database = new ComputerUserDbHelper(context).getWritableDatabase();
                database.delete(ComputerUserEntry.TABLE_NAME, ComputerUserEntry.COLUMN_NAME_IP_ADDRESS + "='" + ipAddress + "'", null);

                String[] users = s.split("\\n");
                for (String user : users) {
                    Log.d(TAG, user.split("\\s+")[0]);
                    String table = ComputerUserEntry.TABLE_NAME;
                    ContentValues values = new ContentValues();
                    values.put(ComputerUserEntry.COLUMN_NAME_IP_ADDRESS, ipAddress);
                    values.put(ComputerUserEntry.COLUMN_NAME_HOSTNAME, hostname);
                    values.put(ComputerUserEntry.COLUMN_NAME_USERNAME, user.split("\\s+")[0]);
                    database.insert(table, null, values);
                }

                database.close();

                notifyCallbacks();
            }
        }

        @Override
        public void onError (Exception e){
            synchronized (context) {
                SQLiteDatabase database = new ComputerDbHelper(context).getWritableDatabase();
                database.delete(ComputerEntry.TABLE_NAME, ComputerEntry.COLUMN_NAME_IP_ADDRESS + "='" + ipAddress + "'", null);
                database.close();

                database = new ComputerUserDbHelper(context).getWritableDatabase();
                database.delete(ComputerUserEntry.TABLE_NAME, ComputerUserEntry.COLUMN_NAME_IP_ADDRESS + "='" + ipAddress + "'", null);
                database.close();

                notifyCallbacks();
            }
        }
    }
}
