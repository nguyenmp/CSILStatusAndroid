package com.nguyenmp.csilstatus.app;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nguyenmp.csil.things.User;
import com.nguyenmp.csilstatus.app.dao.ComputerUserDbHelper;

import java.util.ArrayList;
import java.util.List;

import static com.nguyenmp.csilstatus.app.dao.ComputerUserContract.ComputerUserEntry;

/**
 * A fragment representing a list of Items.
 */
public class UserFragment extends ListFragment implements GetComputersService.Callback {
    private static final String TAG = "UserFragment";
    private ShowDetailsCallback mListener;

    private BaseAdapter adapter = null;

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ShowDetailsCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        GetComputersService.registerCallback(Looper.getMainLooper(), this);

        adapter = new UserAdapter(getActivity());
        setListAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        adapter = null;
        setListAdapter(null);
        GetComputersService.unregisterCallback(this);
    }

    @Override
    public void onUpdated() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }
}
