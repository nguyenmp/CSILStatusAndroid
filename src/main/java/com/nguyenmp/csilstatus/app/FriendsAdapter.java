package com.nguyenmp.csilstatus.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nguyenmp.csil.things.User;
import com.nguyenmp.csilstatus.app.dao.DbContract;
import com.nguyenmp.csilstatus.app.dao.DbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FriendsAdapter extends BaseAdapter {
    List<String> data = new ArrayList<String>();
    private final String hostname;
    private final Context context;

    FriendsAdapter(Context context) {
        this(context, null);
    }

    FriendsAdapter(Context context, String hostname) {
        this.context = context;
        this.hostname = hostname;
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

        TextView title = (TextView) view.findViewById(android.R.id.text1);
        title.setText(data.get(i));

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        Set<String> friends = FriendsFragment.getFriends(context);

        data.clear();
        data.addAll(friends);

        super.notifyDataSetChanged();
    }
}