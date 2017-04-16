package com.example.elliot.automatedorderingsystem.OrderHistory;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.elliot.automatedorderingsystem.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderHistoryEmptyFragment extends Fragment {

    private View rootView;


    public OrderHistoryEmptyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_order_history_empty, container, false);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_history_empty, container, false);
    }

}
