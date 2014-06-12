package com.nguyenmp.csilstatus.app;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nguyenmp.csil.things.User;
import com.nguyenmp.csilstatus.app.dao.ComputerUserContract;
import com.nguyenmp.csilstatus.app.dao.ComputerUserDbHelper;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends BaseAdapter {
    List<User> data = new ArrayList<User>();
    private final Context context;
    private final String hostname;

    UserAdapter(Context context) {
        this(context, null);
    }

    UserAdapter(Context context, String hostname) {
        this.context = context;
        this.hostname = hostname;
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
        String table = ComputerUserContract.ComputerUserEntry.TABLE_NAME;
        String[] columns = {ComputerUserContract.ComputerUserEntry.COLUMN_NAME_USERNAME};
        String selection = hostname == null ? null : ComputerUserContract.ComputerUserEntry.COLUMN_NAME_HOSTNAME + "='" + hostname + "'";
        String orderBy = ComputerUserContract.ComputerUserEntry.COLUMN_NAME_USERNAME + " ASC";
        String limit = "99999";

        Cursor cursor = database.query(true, table, columns, selection, null, null, null, orderBy, limit);

        List<User> users = new ArrayList<User>();
        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndex(ComputerUserContract.ComputerUserEntry.COLUMN_NAME_USERNAME));

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