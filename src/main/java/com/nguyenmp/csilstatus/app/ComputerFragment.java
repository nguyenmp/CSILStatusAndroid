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

import com.nguyenmp.csilstatus.app.dao.ComputerContract.ComputerEntry;
import com.nguyenmp.csilstatus.app.dao.ComputerDbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the
 * {@link com.nguyenmp.csilstatus.app.ComputerFragment.OnFragmentInteractionListener}
 * interface.
 */
public class ComputerFragment extends ListFragment implements GetComputersService.Callback {
    private static final String TAG = "ComputerFragment";
    private OnFragmentInteractionListener mListener;

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
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }

        GetComputersService.registerCallback(Looper.getMainLooper(), this);
        GetComputersService.refresh(activity);
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
            adapter = new ComputerAdapter(getActivity());
            setListAdapter(adapter);
            setListShown(true);
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
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    private static class ComputerAdapter extends BaseAdapter {
        List<Computer> data = new ArrayList<Computer>();
        private final Context context;

        ComputerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, viewGroup, false);

            Computer computer = (Computer) getItem(i);

            TextView title = (TextView) view.findViewById(android.R.id.text1);
            title.setText(computer.hostname);

            TextView subtitle = (TextView) view.findViewById(android.R.id.text2);
            subtitle.setText(computer.ipAddress);

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            SQLiteDatabase database = new ComputerDbHelper(context).getReadableDatabase();
            String table = ComputerEntry.TABLE_NAME;
            String[] columns = {ComputerEntry.COLUMN_NAME_HOSTNAME, ComputerEntry.COLUMN_NAME_IP_ADDRESS};

            Cursor cursor = database.query(table, columns, null, null, null, null, null);

            List<Computer> computers = new ArrayList<Computer>();
            while (cursor.moveToNext()) {
                String hostname = cursor.getString(cursor.getColumnIndex(ComputerEntry.COLUMN_NAME_HOSTNAME));
                String ipAddress = cursor.getString(cursor.getColumnIndex(ComputerEntry.COLUMN_NAME_IP_ADDRESS));

                Computer computer = new Computer();
                computer.hostname = hostname;
                computer.ipAddress = ipAddress;
                computers.add(computer);
            }

            data.clear();
            data.addAll(computers);

            super.notifyDataSetChanged();
        }
    }
}
