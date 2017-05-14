package com.example.elliot.automatedorderingsystem.Basket;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;
import com.example.elliot.automatedorderingsystem.Login.LoginActivity;
import com.example.elliot.automatedorderingsystem.OrderHistory.OrderHistoryActivity;
import com.example.elliot.automatedorderingsystem.R;
import com.example.elliot.automatedorderingsystem.Recommendation.RecommendationActivity;
import com.example.elliot.automatedorderingsystem.RestaurantAndMenu.MainActivity;

import java.util.Collections;

public class BasketActivity extends AppCompatActivity implements View.OnClickListener {

    protected Restaurant restaurant;
    private MenuItem menuItem;
    private TextView orderTotal;
    private Button btnCheckout;
    private ListView basketListView;
    private FragmentManager fragmentManager = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        // Get all the elements from the XML file and
        orderTotal = (TextView) findViewById(R.id.txtTotalCost);
        btnCheckout = (Button) findViewById(R.id.btnCheckout);
        basketListView = (ListView) findViewById(R.id.basketListView);

        basketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the piece of food that was selected and remove it from the customers orders
                // Redraw the list to ensure it's removed from the listview
                Food foodToRemove = (Food) basketListView.getAdapter().getItem(position);
                Customer.getInstance().getUserOrder().getFoodOrdered().remove(foodToRemove);

                // Recalculate the total price of the users basket
                Customer.getInstance().getUserOrder().calculateTotalPrice(Customer.getInstance().getUserOrder().getFoodOrdered());

                if (menuItem != null) {
                    menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
                    orderTotal.setText("Total:  £" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
                }

                ((BaseAdapter) basketListView.getAdapter()).notifyDataSetChanged();

                // Check if the customers order is empty - if empty display empty basket sign ect
                // IF statement checks if order array is empty - if true then display the empty basket - false means get the first fragment and display it
                if (Customer.getInstance().getUserOrder().getFoodOrdered().isEmpty()) {
                    // Order total is empty - disable all elements and display the fragment which shows an empty basket
                    disableBasketElements();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new BasketCheckoutEmptyFragment()).commit();
                }
            }
        });

        // Get the restaurant that was serialized so it can be passed to the checkout fragment
        if (getIntent().getSerializableExtra("restaurant") != null) {
            restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");
        }

        // Set the currentTotal text to the customers order total
        orderTotal.setText("Total:  £" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
        // Set the button onClickListener to check when the user wants to complete their order
        btnCheckout.setOnClickListener(this);

        // Populate the list view with the relevant data from database - display it
        populateBasketList();

        // Check if the customers order is empty - if empty display empty basket sign ect
        // IF statement checks if order array is empty - if true then display the empty basket - false means get the first fragment and display it
        if (Customer.getInstance().getUserOrder().getFoodOrdered().isEmpty()) {
            // Order total is empty - disable all elements and display the fragment which shows an empty basket
            disableBasketElements();
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new BasketCheckoutEmptyFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);

        menuItem = menu.findItem(R.id.orderTotal);

        if (menuItem != null) {
            menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
        }

        getSupportActionBar().setTitle("Basket");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get which item was selected and redirect user to the appropriate activity
        switch (item.getItemId()) {
            case R.id.viewOrderHistory:
                startActivity(new Intent(BasketActivity.this, OrderHistoryActivity.class));
                break;
            case R.id.viewRecommendations:
                startActivity(new Intent(BasketActivity.this, RecommendationActivity.class));
                break;
            case R.id.viewAllRestaurants:
                startActivity(new Intent(BasketActivity.this, MainActivity.class));
                break;
            case R.id.signOut:
                startActivity(new Intent(BasketActivity.this, LoginActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        // If the user presses the back button you must update the order total on the previous activity
        super.onResume();

        if (menuItem != null) {
            menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
        }
    }

    @Override
    public void onClick(View v) {
        // Switch for all the possible button options that can be clicked
        switch (v.getId()){
            case R.id.btnCheckout:
                // Disable the previous elements
                disableBasketElements();
                // Display the fragment with the user details on
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new BasketCheckoutDetailsFragment()).commit();
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
            super(BasketActivity.this, R.layout.basket_view, Customer.getInstance().getUserOrder().getFoodOrdered());

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View orderItemView = convertView;
            if (orderItemView == null) {
                orderItemView = getLayoutInflater().inflate(R.layout.basket_view, parent, false);
            }

            // Create the variables for the food and select it from the correct Ojbect (Customer / Guest)
            // Create INT for occurences to see how many of that object the user has ordered
            int occurrences = 1;

            Food currentFood = Customer.getInstance().getUserOrder().getFoodOrdered().get(position);
            occurrences = Collections.frequency(Customer.getInstance().getUserOrder().getFoodOrdered(), currentFood);

            // Fill the text views with the information gathered from the current food object
            TextView foodName = (TextView) orderItemView.findViewById(R.id.txtFoodName);
            foodName.setText(currentFood.getFoodName());

            TextView foodQuantity = (TextView) orderItemView.findViewById(R.id.txtQuantity);
            foodQuantity.setText("1");

            TextView foodPrice = (TextView) orderItemView.findViewById(R.id.txtFoodPrice);
            foodPrice.setText(String.valueOf("£" + currentFood.getPrice()));

            ImageView deleteFoodImage = (ImageView) orderItemView.findViewById(R.id.imgDeleteFood);
            deleteFoodImage.setVisibility(View.VISIBLE);

            return orderItemView;
        }
    }

    private void disableBasketElements() {
        // Disable all the activities elements by setting them to invisible
        orderTotal.setVisibility(View.INVISIBLE);
        btnCheckout.setVisibility(View.INVISIBLE);
        basketListView.setVisibility(View.INVISIBLE);
    }
}
