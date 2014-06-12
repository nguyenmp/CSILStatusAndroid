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

public class ComputerDialogFragment extends DialogFragment implements GetComputersService.Callback {

    private BaseAdapter adapter = null;

    public ComputerDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        GetComputersService.registerCallback(Looper.getMainLooper(), this);
        adapter = new UserAdapter(activity, getArguments().getString("hostname"));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        GetComputersService.unregisterCallback(this);
        adapter = null;
    }

    public static ComputerDialogFragment newInstance(String hostname) {
        ComputerDialogFragment f = new ComputerDialogFragment();

        Bundle args = new Bundle();
        args.putString("hostname", hostname);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(android.R.layout.list_content, container);

        Bundle args = getArguments();

        getDialog().setTitle(args.getString("hostname"));
        ((ListView) view.findViewById(android.R.id.list)).setAdapter(adapter);

        return view;
    }

    @Override
    public void onUpdated() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }
}