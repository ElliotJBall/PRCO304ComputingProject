package com.example.elliot.automatedorderingsystem;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.elliot.automatedorderingsystem.Basket.BasketActivity;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Order;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    protected Customer customer;
    protected ArrayList<Restaurant> allRestaurants = new ArrayList<Restaurant>();
    protected String urlToUse, returnedJSON = "", openingTime ="00:00:00", closingTime = "23:00:00", currentTime = "00:00:00";

    private APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getAllRestaurants();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Call method to populate the list view - Get all restaurants from API and display them
        // Checks whether the restaurant is currently open or closed and edits the text accordingly
        populateRestaurantList();

        // Set onClickListener for the listView
        // Get selected restaurant and start new intent saving that restaurant
        registerRestaurantListClick();

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
            getSupportActionBar().setTitle(customer.getUsername());
        } else {
            getSupportActionBar().setTitle("All Restaurants");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get which item was selected and redirect user to the appropriate activity
        switch (item.getItemId()) {
            case R.id.basketIcon:
                if (customer != null) {
                    startActivity(new Intent(MainActivity.this , BasketActivity.class).putExtra("customer" , customer));
                } else {
                    startActivity(new Intent(MainActivity.this , BasketActivity.class));
                }
                break;
            case R.id.orderTotal:
                if (customer != null) {
                    startActivity(new Intent(MainActivity.this , BasketActivity.class).putExtra("customer" , customer));
                } else {
                    startActivity(new Intent(MainActivity.this , BasketActivity.class));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllRestaurants() throws JSONException, ExecutionException, InterruptedException, ParseException {
        urlToUse = "http://10.0.2.2:8080/restaurant/restaurants";
        asyncGetData = new asyncGetData();
        asyncGetData.execute().get();

        // Create a JSON Array that'll hold all the data pulled
        JSONArray jsonArray = new JSONArray(returnedJSON);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        // Loop through the JSON array and get all the restaurants, adding them to the ArrayList<Restaurant>
        for (int i = 0; i < jsonArray.length(); i++) {
            // Create a temporary restaurant menu as we dont need to get the data until the user has selected it from the list
            ArrayList<Food> temporaryMenu = new ArrayList<Food>();
            Document restaurantDocument = Document.parse(jsonArray.get(i).toString());
            Restaurant restaurant = new Restaurant(restaurantDocument.get("_id").toString() , restaurantDocument.get("restaurantName").toString()
            , restaurantDocument.get("address").toString() , restaurantDocument.get("city").toString()
            , restaurantDocument.get("postcode").toString() , restaurantDocument.get("longitude").toString()
            , restaurantDocument.get("latitude").toString() , simpleDateFormat.parse(restaurantDocument.get("openingTime").toString())
            , simpleDateFormat.parse(restaurantDocument.get("closingTime").toString()),temporaryMenu);

            allRestaurants.add(restaurant);
        }
    }

    private void populateRestaurantList() {
        ArrayAdapter<Restaurant> adapter = new RestaurantListAdapter();
        ListView restaurantList = (ListView) findViewById(R.id.restaurantListView);
        restaurantList.setAdapter(adapter);
    }

    private void registerRestaurantListClick() {
        final ListView restaurantListView = (ListView) findViewById(R.id.restaurantListView);
        restaurantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Restaurant selectedRestaurant = allRestaurants.get(position);

                Intent intent = new Intent();
                intent.putExtra("restaurant", selectedRestaurant);

                if (customer != null) {
                    intent = new Intent(MainActivity.this, RestaurantActivity.class);
                    intent.putExtra("customer" , customer);
                    intent.putExtra("restaurant" , selectedRestaurant);
                    startActivity(intent);
                } else {
                    intent = new Intent(MainActivity.this, RestaurantActivity.class);
                    intent.putExtra("restaurant", selectedRestaurant);
                    startActivity(intent);
                }
            }
        });
    }

    private class RestaurantListAdapter extends ArrayAdapter<Restaurant> {
        public RestaurantListAdapter() {
            super(MainActivity.this , R.layout.restaurant_view , allRestaurants);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            View restaurantItemView = convertView;
            if (restaurantItemView == null) {
                restaurantItemView = getLayoutInflater().inflate(R.layout.restaurant_view, parent, false);
            }
            // Get the current restaurant from the array of restaurants
            Restaurant currentRestaurant = allRestaurants.get(position);

            // Fill the images and different TextViews here

            ImageView imageView = (ImageView) restaurantItemView.findViewById(R.id.imgRestaurant);


            TextView restaurantName = (TextView) restaurantItemView.findViewById(R.id.txtRestaurantName);
            restaurantName.setText(currentRestaurant.getRestaurantName());

            // Parse an opening and closing time - display either open or closed
            try {
                Date restaurantDate = originalFormat.parse(currentRestaurant.getOpeningTime().toString());
                openingTime = timeFormat.format(restaurantDate);
                Date openingTimeDate = new SimpleDateFormat("HH:mm:ss").parse(openingTime);
                Calendar openingTimeCalendar = Calendar.getInstance();
                openingTimeCalendar.setTime(openingTimeDate);

                restaurantDate = originalFormat.parse(currentRestaurant.getClosingTime().toString());
                closingTime = timeFormat.format(restaurantDate);
                Date closingTimeDate = new SimpleDateFormat("HH:mm:ss").parse(closingTime);
                Calendar closingTimeCalendar = Calendar.getInstance();
                closingTimeCalendar.setTime(closingTimeDate);

                restaurantDate = new Date();
                String currentDate = timeFormat.format(restaurantDate);
                Date currentTime = new SimpleDateFormat("HH:mm:ss").parse(currentDate);
                Calendar currentTimeCalendar = Calendar.getInstance();
                currentTimeCalendar.setTime(currentTime);

                TextView restaurantOpeningTime = (TextView) restaurantItemView.findViewById(R.id.txtOpeningTime);
                restaurantOpeningTime.setText("Opening hours: " + openingTime + " " + "-" + " " + closingTime);

                // NEED TO CHECK WHETHER TIME IS ALSO BEFORE RESTAURANT IS MEANT TO SHUT
                // CURRENTLY CANNOT GET THE CORRECT CLOSING TIME OF A RESTAURANT - FIX

                if (currentTimeCalendar.getTime().after(openingTimeCalendar.getTime())) {
                    restaurantOpeningTime.setTextColor(Color.GREEN);
                    restaurantOpeningTime.setText("OPEN");
                } else {
                    restaurantOpeningTime.setTextColor(Color.RED);
                    restaurantOpeningTime.setText("CLOSED");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return restaurantItemView;
        }
    }

    public class asyncGetData extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... params) {
            try {
                Thread.sleep(1000);
                returnedJSON = APIConnection.getAPIData(urlToUse);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return returnedJSON;
        }
    } //End of AsyncTask

    @Override
    public void onResume() {
        // If the user presses the back button you must update the order total on the previous activity
        super.onResume();

        if (menuItem != null) {
            menuItem.setTitle("£" + String.format("%.2f", Order.getInstance().getTotalPrice()));
        }
    }
}
