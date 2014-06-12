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

import com.nguyenmp.csilstatus.app.dao.ComputerUserDbHelper;

import java.util.ArrayList;
import java.util.List;

import static com.nguyenmp.csilstatus.app.ShowDetailsCallback.Type;
import static com.nguyenmp.csilstatus.app.dao.ComputerUserContract.ComputerUserEntry;

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
            if (view == null) view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, viewGroup, false);

            Computer computer = (Computer) getItem(i);

            TextView title = (TextView) view.findViewById(android.R.id.text1);
            title.setText(String.format("%s (%d)", computer.hostname, computer.users));

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            SQLiteDatabase database = new ComputerUserDbHelper(context).getReadableDatabase();
            String table = ComputerUserEntry.TABLE_NAME;
            String[] columns = {ComputerUserEntry.COLUMN_NAME_IP_ADDRESS, ComputerUserEntry.COLUMN_NAME_HOSTNAME, "COUNT(*) AS users"};
            String orderBy = "users DESC, " + ComputerUserEntry.COLUMN_NAME_HOSTNAME + " ASC";

            String groupBy = ComputerUserEntry.COLUMN_NAME_IP_ADDRESS;
            Cursor cursor = database.query(table, columns, null, null, groupBy, null, orderBy);

            List<Computer> computers = new ArrayList<Computer>();
            while (cursor.moveToNext()) {
                int users = cursor.getInt(cursor.getColumnIndex("users"));
                String ipAddress = cursor.getString(cursor.getColumnIndex(ComputerUserEntry.COLUMN_NAME_IP_ADDRESS));
                String hostname = cursor.getString(cursor.getColumnIndex(ComputerUserEntry.COLUMN_NAME_HOSTNAME));

                Computer computer = new Computer();
                computer.users = users;
                computer.ipAddress = ipAddress;
                computer.hostname = hostname;
                computers.add(computer);
            }

            data.clear();
            data.addAll(computers);

            super.notifyDataSetChanged();
        }
    }
}
