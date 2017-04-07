package com.example.elliot.automatedorderingsystem;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.elliot.automatedorderingsystem.Basket.BasketActivity;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RestaurantActivity extends AppCompatActivity {

    protected Customer customer;
    protected Restaurant restaurant;
    protected ArrayList<Food> menu = new ArrayList<Food>();

    private APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;
    private String urlToUse, returnedJSON = "";
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        // Get the selected restaurant and create an object
        restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");

        // Get the items on the menu, add them to array and set the array
        try {
            getMenu();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get the updated array menu and add the elements to the listview
        populateFoodList();
        // Register the clicks on the list view - once clicked add them to the order
        registerMenuListClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Get the menu layout and inflate it setting it on the activity
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        getSupportActionBar().setTitle(restaurant.getRestaurantName());
        menuItem = menu.findItem(R.id.orderTotal);

        menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get which item was selected and redirect user to the appropriate activity
        switch (item.getItemId()) {
            case R.id.basketIcon:
                if (customer != null) {
                    startActivity(new Intent(RestaurantActivity.this , BasketActivity.class).putExtra("restaurant" , restaurant));
                } else {
                    startActivity(new Intent(RestaurantActivity.this , BasketActivity.class).putExtra("restaurant" , restaurant));
                }
                break;
            case R.id.orderTotal:
                if (customer != null) {
                    startActivity(new Intent(RestaurantActivity.this , BasketActivity.class).putExtra("restaurant" , restaurant));
                } else {
                    startActivity(new Intent(RestaurantActivity.this , BasketActivity.class).putExtra("restaurant" , restaurant));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMenu() throws ExecutionException, InterruptedException, JSONException {
        // Pass the URL to the apiConnection class to return the relevant items of food
        // Gets all food which matches the menuID
        // Add the required spacing for the api to work (change white space to %20)
        String restaurantName = restaurant.getRestaurantName().replaceAll(" ", "%20");
        urlToUse = "http://10.0.2.2:8080/food/food/?filter={%27restaurantName%27:%20'"+restaurantName+"'}";

        asyncGetData = new asyncGetData();
        asyncGetData.execute().get();

        // Create a JSON Array that'll hold all the data pulled
        JSONArray jsonArray = new JSONArray(returnedJSON);

        // Loop through the JSON array and get all the items of food, add them to the array list so they can be displayed later
        for (int i = 0; i < jsonArray.length(); i++) {
            Document foodDocument = Document.parse(jsonArray.get(i).toString());
            Food food = new Food(foodDocument.get("_id").toString(), foodDocument.get("foodName").toString()
            , Float.valueOf(foodDocument.get("price").toString()), foodDocument.get("restaurantName").toString());

            menu.add(food);
        }
    }

    private void populateFoodList() {
        // Create an adapter for the list view
        ArrayAdapter<Food> adapter = new foodListAdapter();
        ListView foodMenu = (ListView) findViewById(R.id.foodListView);
        foodMenu.setAdapter(adapter);
    }

    private class foodListAdapter extends ArrayAdapter<Food> {
        public foodListAdapter() {
            super(RestaurantActivity.this, R.layout.restaurant_view, menu);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the view, if view is null inflate the food view
            View foodMenuView = convertView;
            if (foodMenuView == null) {
                foodMenuView = getLayoutInflater().inflate(R.layout.food_view, parent, false);
            }

            // Get the current food from the menu to display
            Food currentFood = menu.get(position);

            // Fill the text views with the information gathered from the current food object
            TextView foodName = (TextView) foodMenuView.findViewById(R.id.txtFoodName);
            foodName.setText(currentFood.getFoodName());

            TextView foodDescription = (TextView) foodMenuView.findViewById(R.id.txtFoodDescription);
            foodDescription.setText(currentFood.getFoodName());

            TextView foodPrice = (TextView) foodMenuView.findViewById(R.id.txtFoodPrice);
            foodPrice.setText(String.valueOf("£" + currentFood.getPrice()));

            return foodMenuView;
        }
    }

    private void registerMenuListClick() {
        final ListView menuListView = (ListView) findViewById(R.id.foodListView);
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the item of food that was selected
                Food selectedFood = menu.get(position);

                // Add the item selected to the customers order
                Customer.getInstance().getUserOrder().addToOrder(selectedFood);
                // Get the total price of the order so far
                float totalPrice = Customer.getInstance().getUserOrder().calculateTotalPrice(Customer.getInstance().getUserOrder().getFoodOrdered());
                // Format the string to two decimal places so it can be displayed
                // Update the total displayed to the user
                menuItem.setTitle("£" + String.format("%.2f", totalPrice));
            }
        });
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
    }

    @Override
    public void onResume() {
        // If the user presses the back button you must update the order total on the previous activity
        super.onResume();

        if (menuItem != null) {
            // Set the menu item text to the current total
            menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
        }
    }
}
