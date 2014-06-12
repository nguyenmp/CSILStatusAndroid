package com.nguyenmp.csilstatus.app;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nguyenmp.csilstatus.app.dao.DbContract;
import com.nguyenmp.csilstatus.app.dao.DbHelper;

import java.util.ArrayList;
import java.util.List;

public class ComputerAdapter extends BaseAdapter {
    List<Computer> data = new ArrayList<Computer>();
    private final Context context;
    private final String username;

    ComputerAdapter(Context context) {
        this(context, null);
    }

    ComputerAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
        notifyDataSetChanged();
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

        SQLiteDatabase database = new DbHelper(context).getReadableDatabase();
        String table = DbContract.UsageEntry.TABLE_NAME;
        String[] columns = {DbContract.UsageEntry.COLUMN_NAME_IP_ADDRESS, DbContract.UsageEntry.COLUMN_NAME_HOSTNAME, "COUNT(*) AS users"};
        String orderBy = "users DESC, " + DbContract.UsageEntry.COLUMN_NAME_HOSTNAME + " ASC";
        String selection = username == null ? null : DbContract.UsageEntry.COLUMN_NAME_USERNAME + "='" + username + "'";

        String groupBy = DbContract.UsageEntry.COLUMN_NAME_IP_ADDRESS;
        Cursor cursor = database.query(table, columns, selection, null, groupBy, null, orderBy);

        List<Computer> computers = new ArrayList<Computer>();
        while (cursor.moveToNext()) {
            int users = cursor.getInt(cursor.getColumnIndex("users"));
            String ipAddress = cursor.getString(cursor.getColumnIndex(DbContract.UsageEntry.COLUMN_NAME_IP_ADDRESS));
            String hostname = cursor.getString(cursor.getColumnIndex(DbContract.UsageEntry.COLUMN_NAME_HOSTNAME));

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
