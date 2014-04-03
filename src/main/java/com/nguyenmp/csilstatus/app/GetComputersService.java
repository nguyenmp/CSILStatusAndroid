package com.nguyenmp.csilstatus.app;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class GetComputersService extends IntentService {
    private static final String TAG = "GetComputersService";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_COMPUTERS = "com.nguyenmp.csilstatus.app.action.GET_COMPUTERS";
    private static final String ACTION_INVALIDATE_CACHE = "com.nguyenmp.csilstatus.app.action.INVALIDATE_CACHE";

    private static final String EXTRA_USERNAME = "com.nguyenmp.csilstatus.app.extra.USERNAME";
    private static final String EXTRA_PASSWORD = "com.nguyenmp.csilstatus.app.extra.PASSWORD";

    private static final Collection<Callback> CALLBACKS = new CopyOnWriteArraySet<Callback>();
    private static final Set<Computer> COMPUTERS = new CopyOnWriteArraySet<Computer>();

    public static final int STATE_IN_PROGRESS = 0;
    public static final int STATE_COMPLETE = 1;
    public static final int STATE_NOT_STARTED = 2;
    public static final int STATE_ERROR = 4;

    public static final int PROGRESS_FINDING_COMPUTERS = 0;
    public static final int TESTING_COMPUTERS = 1;

    private static int STATE = STATE_NOT_STARTED;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetComputers(Context context, String username, String password) {
        Intent intent = new Intent(context, GetComputersService.class);
        intent.setAction(ACTION_GET_COMPUTERS);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_PASSWORD, password);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionInvalidateCache(Context context) {
        Intent intent = new Intent(context, GetComputersService.class);
        intent.setAction(ACTION_INVALIDATE_CACHE);
        context.startService(intent);
    }

    public GetComputersService() {
        super("GetComputersService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_COMPUTERS.equals(action)) {
                final String username = intent.getStringExtra(EXTRA_USERNAME);
                final String password = intent.getStringExtra(EXTRA_PASSWORD);
                handleActionGetComputers(username, password);
            } else if (ACTION_INVALIDATE_CACHE.equals(action)) {
                handleActionInvalidateCache();
            } else Log.w(TAG, "Received unknown action: " + intent.getAction());
        } else Log.w(TAG, "Received null intent");
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetComputers(String username, String password) {
        STATE = STATE_IN_PROGRESS;
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInvalidateCache() {
        STATE = STATE_NOT_STARTED;
        COMPUTERS.clear();
    }

    private static void notifyCallbacks(Exception e) {
        for (Callback callback : CALLBACKS) {
            callback.onError(e);
        }
    }

    private static void notifyCallbacks(int progressType, float percentage) {
        for (Callback callback : CALLBACKS) {
            callback.onProgress(progressType, percentage);
        }
    }

    private static void notifyCallbacks(Collection<Computer> computers) {
        for (Callback callback : CALLBACKS) {
            callback.onComplete(computers);
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
    public static void registerCallback(Callback callback) {
        CALLBACKS.add(callback);
    }

    /**
     * Releases reference to the callback to allow the resources to be freed
     * @param callback the callback to release
     */
    public static void unregisterCallback(Callback callback) {
        CALLBACKS.remove(callback);
    }

    private static interface Callback {
        public void onProgress(int progressType, double percentage);
        public void onComplete(Collection<Computer> computers);
        public void onError(Exception e);
    }
}
