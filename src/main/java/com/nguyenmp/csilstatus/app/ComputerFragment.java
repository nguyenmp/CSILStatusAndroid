package com.nguyenmp.csilstatus.app;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import static com.nguyenmp.csilstatus.app.ShowDetailsCallback.Type;

/**
 * A fragment representing a list of Items.
 */
public class ComputerFragment extends ListFragment implements GetComputersService.Callback {
    private static final String TAG = "ComputerFragment";
    private ShowDetailsCallback mListener;

    private BaseAdapter adapter = null;

    public static ComputerFragment newInstance() {
        return new ComputerFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComputerFragment() {

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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        GetComputersService.unregisterCallback(this);
    }

    @Override
    public void onUpdated() {
        if (adapter == null) {
            Context context = getActivity();
            if (context != null) {
                adapter = new ComputerAdapter(context);
                setListAdapter(adapter);
            }
        }

        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.showDetails(Type.Computer, ((Computer) adapter.getItem(position)).hostname);
        }
    }
}
