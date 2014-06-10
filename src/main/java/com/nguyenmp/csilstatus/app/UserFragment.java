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
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the
 * {@link com.nguyenmp.csilstatus.app.ComputerFragment.OnFragmentInteractionListener}
 * interface.
 */
public class UserFragment extends ListFragment implements GetComputersService.Callback {
    private static final String TAG = "UserFragment";
    private OnFragmentInteractionListener mListener;

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
            adapter = new UserAdapter(getActivity());
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

    private static class UserAdapter extends BaseAdapter {
        List<User> data = new ArrayList<User>();
        private final Context context;

        UserAdapter(Context context) {
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

            User user = (User) getItem(i);

            TextView title = (TextView) view.findViewById(android.R.id.text1);
            title.setText(user.name);

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            SQLiteDatabase database = new ComputerUserDbHelper(context).getReadableDatabase();
            String table = ComputerUserEntry.TABLE_NAME;
            String[] columns = {ComputerUserEntry.COLUMN_NAME_USERNAME};

            Cursor cursor = database.query(true, table, columns, null, null, null, null, null, "99999");

            List<User> users = new ArrayList<User>();
            while (cursor.moveToNext()) {
                String username = cursor.getString(cursor.getColumnIndex(ComputerUserEntry.COLUMN_NAME_USERNAME));

                User user = new User();
                user.name = username;
                users.add(user);
            }

            database.close();

            data.clear();
            data.addAll(users);

            super.notifyDataSetChanged();
        }
    }
}
