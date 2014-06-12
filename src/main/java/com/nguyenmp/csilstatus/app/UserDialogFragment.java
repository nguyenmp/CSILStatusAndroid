package com.nguyenmp.csilstatus.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class UserDialogFragment extends DialogFragment implements GetComputersService.Callback {

    private BaseAdapter adapter = null;

    public UserDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        GetComputersService.registerCallback(Looper.getMainLooper(), this);
        adapter = new ComputerAdapter(activity, getArguments().getString("username"));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        GetComputersService.unregisterCallback(this);
        adapter = null;
    }

    public static UserDialogFragment newInstance(String username) {
        UserDialogFragment f = new UserDialogFragment();

        Bundle args = new Bundle();
        args.putString("username", username);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(android.R.layout.list_content, container);

        Bundle args = getArguments();

        getDialog().setTitle(args.getString("username"));
        ((ListView) view.findViewById(android.R.id.list)).setAdapter(adapter);

        return view;
    }

    @Override
    public void onUpdated() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }
}