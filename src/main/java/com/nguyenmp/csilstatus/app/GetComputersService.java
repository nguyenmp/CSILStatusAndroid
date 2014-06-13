package com.nguyenmp.csilstatus.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nguyenmp.csil.concurrency.CommandExecutor;
import com.nguyenmp.csilstatus.app.dao.DbContract.ComputerEntry;
import com.nguyenmp.csilstatus.app.dao.DbHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.nguyenmp.csilstatus.app.dao.DbContract.UsageEntry;


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

        DbHelper dbHelper = new DbHelper(this);
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
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

		checkForFriends();

        database.close();
    }

	private void checkForFriends() {
		Set<String> friends = FriendsFragment.getFriends(this);
		Set<String> allUsers = new HashSet<String>();

		// Read active users from database
		SQLiteDatabase database = new DbHelper(this).getReadableDatabase();
		String table = UsageEntry.TABLE_NAME;
		String[] columns = {UsageEntry.COLUMN_NAME_USERNAME};
		String orderBy = UsageEntry.COLUMN_NAME_USERNAME + " ASC";
		String limit = "99999";

		Cursor cursor = database.query(true, table, columns, null, null, null, null, orderBy, limit);

		while (cursor.moveToNext()) {
			String username = cursor.getString(cursor.getColumnIndex(UsageEntry.COLUMN_NAME_USERNAME));
			allUsers.add(username);
		}
		database.close();

		allUsers.retainAll(friends);
		if(!allUsers.isEmpty()) {
			int numUsers = allUsers.size();
			NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
			for(String friend : allUsers) {
				style.addLine(friend);
			}
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
					.setStyle(style)
					.setContentTitle(String.format("%d friend%s at CSIL", numUsers, numUsers == 1 ? "" : "s"))
					.setContentText(getResources().getString(R.string.app_name))
					.setContentInfo(String.valueOf(numUsers))
					.setLargeIcon(BitmapFactory.decodeResource(null, R.drawable.ic_launcher))
					.setSmallIcon(R.drawable.ic_launcher);
			NotificationManager mNotificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, mBuilder.build());
		}
		else Log.i("", "No friends found");
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
                SQLiteDatabase database = new DbHelper(context).getWritableDatabase();
                database.delete(UsageEntry.TABLE_NAME, UsageEntry.COLUMN_NAME_IP_ADDRESS + "='" + ipAddress + "'", null);

                String[] users = s.split("\\n");
                for (String user : users) {
                    if (user.length() == 0) continue;
                    String[] split = user.split("\\s+");
                    if (split.length < 6) Log.d(TAG, user.replace('\n', ' '));
                    String table = UsageEntry.TABLE_NAME;
                    ContentValues values = new ContentValues();
                    values.put(UsageEntry.COLUMN_NAME_IP_ADDRESS, ipAddress);
                    values.put(UsageEntry.COLUMN_NAME_HOSTNAME, hostname);
                    values.put(UsageEntry.COLUMN_NAME_USERNAME, user.split("\\s+")[0]);
                    database.insert(table, null, values);
                }

                database.close();

                notifyCallbacks();
            }
        }

        @Override
        public void onError (Exception e){
            synchronized (context) {
                SQLiteDatabase database = new DbHelper(context).getWritableDatabase();
                database.delete(ComputerEntry.TABLE_NAME, ComputerEntry.COLUMN_NAME_IP_ADDRESS + "='" + ipAddress + "'", null);
                database.close();

                database = new DbHelper(context).getWritableDatabase();
                database.delete(UsageEntry.TABLE_NAME, UsageEntry.COLUMN_NAME_IP_ADDRESS + "='" + ipAddress + "'", null);
                database.close();

                notifyCallbacks();
            }
        }
    }
}
