package com.example.elliot.automatedorderingsystem;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Order;

import java.util.Collections;

public class BasketActivity extends AppCompatActivity implements View.OnClickListener {

    protected Customer customer;
    private MenuItem menuItem;
    private TextView orderTotal;
    private Button btnCheckout;
    private ListView basketListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        // Get the total cost of the order so far and set the orderTotal textview
        orderTotal = (TextView) findViewById(R.id.txtTotalCost);
        orderTotal.setText("Total:          " + String.format("%.2f", Order.getInstance().getTotalPrice()));

        // Get the current orders from the arrayList and add all elements to the listView
        populateBasketList();

        // Set the button onClickListener
        btnCheckout = (Button) findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(this);
        // Get the listview and text so it can be hidden
        basketListView = (ListView) findViewById(R.id.basketListView);
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

    @Override
    public void onClick(View v) {
        // Switch for all the possible button options that can be clicked
        switch (v.getId()){
            case R.id.btnCheckout:
                // Display the fragment with the user details on
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                basketListView.setVisibility(View.INVISIBLE);
                orderTotal.setVisibility(View.INVISIBLE);
                btnCheckout.setVisibility(View.INVISIBLE);

                getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new BasketCheckoutDetailsFragment()).commit();

                // If the customer is signed in grab their data and add it to the edit text boxes for them
                // Check if there was a customer object passed
                if (getIntent().getSerializableExtra("customer") != null) {
                    customer = (Customer) getIntent().getSerializableExtra("customer");
                    populateCustomerData();
                }

                break;
            default:
                break;
        }
    }

    private void populateBasketList() {
        // Create an adapter for the list view
        ArrayAdapter<Food> adapter = new orderListAdapter();
        ListView basketList = (ListView) findViewById(R.id.basketListView);
        basketList.setAdapter(adapter);
    }

    private class orderListAdapter extends ArrayAdapter<Food> {
        public orderListAdapter() {
            super(BasketActivity.this, R.layout.basket_view, Order.getInstance().getFoodOrdered());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View orderItemView = convertView;
            if (orderItemView == null) {
                orderItemView = getLayoutInflater().inflate(R.layout.basket_view, parent, false);
            }

            // Get the current food from the menu to display
            Food currentFood = Order.getInstance().getFoodOrdered().get(position);

            int occurnces = Collections.frequency(Order.getInstance().getFoodOrdered(), currentFood);

            // Fill the text views with the information gathered from the current food object
            TextView foodName = (TextView) orderItemView.findViewById(R.id.txtFoodName);
            foodName.setText(currentFood.getFoodName());

            TextView foodQuantity = (TextView) orderItemView.findViewById(R.id.txtQuantity);
            foodQuantity.setText(String.valueOf(occurnces));

            TextView foodPrice = (TextView) orderItemView.findViewById(R.id.txtFoodPrice);
            foodPrice.setText(String.valueOf("£" + currentFood.getPrice()));

            return orderItemView;
        }
    }

    private void populateCustomerData() {
        // Get all the edit texts and add the customer data accordingly
    }
}
