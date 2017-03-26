package com.example.elliot.automatedorderingsystem;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        // Check if there was a customer object passed
        if (getIntent().getSerializableExtra("customer") != null) {
            customer = (Customer) getIntent().getSerializableExtra("customer");
        }

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
}
