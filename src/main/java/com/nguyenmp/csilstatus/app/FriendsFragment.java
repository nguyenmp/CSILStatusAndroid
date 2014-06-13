package com.nguyenmp.csilstatus.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A fragment representing a list of Items.
 */
public class FriendsFragment extends ListFragment implements GetComputersService.Callback {
    public static final String KEY_FRIENDS = "friends";

    private BaseAdapter adapter = null;

    private Callback callback;

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        GetComputersService.registerCallback(Looper.getMainLooper(), this);

        adapter = new FriendsAdapter(getActivity());
        setListAdapter(adapter);

        callback = new Callback() {
            @Override
            public void onChange() {
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        };
        addCallback(Looper.getMainLooper(), callback);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter = null;
        setListAdapter(null);
        GetComputersService.unregisterCallback(this);
        if (callback != null) removeCallback(callback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friends, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_friend:
                final Context context = getActivity();
                if (context == null) return false;

                final EditText input = new EditText(context);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add a friend:");
                builder.setView(input);

                builder.setPositiveButton("ADD THEM!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (input.getText().toString().trim().length() == 0) {
                            input.setError(getString(R.string.error_field_required));
                            input.requestFocus();
                        } else {
                            FriendsFragment.addFriend(context, input.getText().toString());
                        }
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        input.requestFocus();

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(input, 0);
                    }
                });

                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onUpdated() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final Context context = getActivity();
        if (context == null) return;

        final String friend = (String) adapter.getItem(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete " + friend + "?");
        builder.setPositiveButton("Unfollow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FriendsFragment.removeFriend(context, friend);
            }
        });
        builder.show();
    }

    public static interface Callback {
        public void onChange();
    }

    private static class CallbackHandler extends Handler {
        private final FriendsFragment.Callback callback;

        public CallbackHandler(Looper looper, FriendsFragment.Callback callback) {
            super(looper);
            this.callback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            callback.onChange();
        }
    }

    private static List<CallbackHandler> callbacks = new CopyOnWriteArrayList<CallbackHandler>();

    private static void notifyCallbacks() {
        for (CallbackHandler handler : callbacks) {
            handler.callback.onChange();
        }
    }

    public static void addCallback(Looper looper, Callback callback) {
        callbacks.add(new CallbackHandler(looper, callback));
    }

    public static void removeCallback(Callback callback) {
        for (CallbackHandler handler : callbacks) {
            if (handler.callback == callback) callbacks.remove(handler);
        }
    }

    private static final Object lock = new Object();
    public static Set<String> getFriends(Context context) {
        synchronized (lock) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            return preferences.getStringSet(KEY_FRIENDS, new HashSet<String>());
        }
    }

    private static void setFriends(Context context, Set<String> friends) {
        synchronized (lock) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit().putStringSet(KEY_FRIENDS, friends).commit();
            notifyCallbacks();
        }
    }

    public static void addFriend(Context context, String friend) {
        synchronized (lock) {
            Set<String> friends = getFriends(context);
            friends.add(friend);
            setFriends(context, friends);
        }
    }

    public static void removeFriend(Context context, String friend) {
        synchronized (lock) {
            Set<String> friends = getFriends(context);
            friends.remove(friend);
            setFriends(context, friends);
        }
    }
}
