package com.example.elliot.automatedorderingsystem;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Order;

public class BasketActivity extends AppCompatActivity {

    protected Customer customer;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);

        menuItem = menu.findItem(R.id.orderTotal);
        menuItem.setTitle("£" + String.format("%.2f", Order.getInstance().getTotalPrice()));

        // If there is a customer object get it and create a new customer object
        // Only need to create the guest object when the user is finalizing an order
        if (getIntent().getSerializableExtra("customer") != null) {
            customer = (Customer) getIntent().getSerializableExtra("customer");
            getSupportActionBar().setTitle("Basket");
        } else {
            getSupportActionBar().setTitle("Basket");
        }
        return true;
    }

    @Override
    public void onResume() {
        // If the user presses the back button you must update the order total on the previous activity
        super.onResume();

        if (menuItem != null) {
            menuItem.setTitle("£" + String.format("%.2f", Order.getInstance().getTotalPrice()));
        }
    }
}
