package com.example.elliot.automatedorderingsystem.Basket;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Order;
import com.example.elliot.automatedorderingsystem.R;

import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class BasketCheckoutEmptyFragment extends Fragment {

    private View rootView;

    public BasketCheckoutEmptyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_basket_checkout, container, false);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basket_checkout, container, false);
    }
}
