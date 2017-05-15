package com.example.elliot.automatedorderingsystem.RestaurantAndMenu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.APIConnection;
import com.example.elliot.automatedorderingsystem.Basket.BasketActivity;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;
import com.example.elliot.automatedorderingsystem.Login.LoginActivity;
import com.example.elliot.automatedorderingsystem.OrderHistory.OrderHistoryActivity;
import com.example.elliot.automatedorderingsystem.R;
import com.example.elliot.automatedorderingsystem.Recommendation.RecommendationActivity;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    protected ArrayList<Restaurant> allRestaurants = new ArrayList<Restaurant>();
    protected String urlToUse, returnedJSON = "", openingTime = "00:00:00", closingTime = "23:00:00", currentTime = "00:00:00";

    private APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;
    private MenuItem menuItem;
    private Location customerLocation = new Location("");

    // Variables to deal with gathering the users location
    private double userLongitude = 0.00, userLatitude = 0.00;
    // Variables to get and hold the location of the user
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ArrayAdapter<Restaurant> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for user permissions - This must be done for later versions of android devices - application functionality is serverly hampered without the permisions
        boolean canContinue = checkForPermissions();

        if (canContinue) {
            getCustomerLocation();
            getLocationUpdates();
            // Set the new locations longitude and latitude to those that were just obtained
            customerLocation.setLongitude(userLongitude);
            customerLocation.setLatitude(userLatitude);
        }

        // Set onClickListener for the listView
        // Get selected restaurant and start new intent saving that restaurant
        registerRestaurantListClick();

        // Find the spinner from the layout and create an adapter using the string array
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.locationSpinner, android.R.layout.simple_spinner_item);
        Spinner locationSpinner = (Spinner) findViewById(R.id.sortByOptionsSpinner);
        locationSpinner.setAdapter(adapter);

        // Set an onItemClickListener so we can get what the user selected
        locationSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        menuItem = menu.findItem(R.id.orderTotal);

        if (menuItem != null) {
            menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get which item was selected and redirect user to the appropriate activity
        switch (item.getItemId()) {
            case R.id.basketIcon:
                startActivity(new Intent(MainActivity.this, BasketActivity.class));
                break;
            case R.id.orderTotal:
                startActivity(new Intent(MainActivity.this, BasketActivity.class));
                break;
            case R.id.viewOrderHistory:
                startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
                break;
            case R.id.viewRecommendations:
                startActivity(new Intent(MainActivity.this, RecommendationActivity.class));
                break;
            case R.id.viewAllRestaurants:
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                break;
            case R.id.signOut:
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCustomerLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Attempt to get the users location before the onLocationChange method is called.
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                userLongitude = location.getLatitude();
                userLatitude = location.getLongitude();
        } catch (Exception e) {
            Toast.makeText(this, "Error getting location, please ensure the application has the location permission.", Toast.LENGTH_SHORT).show();
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Update the customers longitude and latitude
                userLongitude = location.getLongitude();
                userLatitude = location.getLatitude();

                customerLocation.setLongitude(userLatitude);
                customerLocation.setLatitude(userLongitude);

                // Loop through all the restaurants in the list and then recalculate the distance to the user as the users location has changed
                for (Restaurant currentRestaurant : allRestaurants) {
                    currentRestaurant.calculateRestaurantDistance(customerLocation, currentRestaurant);
                }

                if (adapter != null) {
                    // Notify that the dataset has changed so it is redrawn
                    // The adapter has to be recreated as the locations to the restaurants will have changed
                    adapter.notifyDataSetChanged();
                    populateRestaurantList();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                // Redirect the user to the location settings so they can enable location for this application to work
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

    }

    private void getLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locationListener);
        }
    }

    private boolean checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, 10);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void getAllRestaurants() throws JSONException, ExecutionException, InterruptedException, ParseException {
        urlToUse = "http://192.168.0.4:8080/restaurant/restaurants";
        asyncGetData = new asyncGetData();
        asyncGetData.execute().get();

        if (!returnedJSON.equals("")) {
            // Get the required part of the JSON string
            returnedJSON = returnedJSON.substring(returnedJSON.indexOf("["), returnedJSON.indexOf("]") + 1);

            // Create a JSON Array that'll hold all the data pulled
            JSONArray jsonArray = new JSONArray(returnedJSON);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

            // Loop through the JSON array and get all the restaurants, adding them to the ArrayList<Restaurant>
            for (int i = 0; i < jsonArray.length(); i++) {
                // Create a temporary restaurant menu as we dont need to get the data until the user has selected it from the list
                ArrayList<Food> temporaryMenu = new ArrayList<Food>();
                Document restaurantDocument = Document.parse(jsonArray.get(i).toString());
                Restaurant restaurant = new Restaurant(restaurantDocument.get("_id").toString(), restaurantDocument.get("restaurantName").toString()
                        , restaurantDocument.get("address").toString(), restaurantDocument.get("city").toString()
                        , restaurantDocument.get("postcode").toString(), restaurantDocument.get("longitude").toString()
                        , restaurantDocument.get("latitude").toString(), simpleDateFormat.parse(restaurantDocument.get("openingTime").toString())
                        , simpleDateFormat.parse(restaurantDocument.get("closingTime").toString()), temporaryMenu);

                allRestaurants.add(restaurant);
            }
        } else {
            Toast.makeText(this, "Error getting restaurants, please ensure the application can use the internet.", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateRestaurantList() {
        adapter = new RestaurantListAdapter();
        ListView restaurantList = (ListView) findViewById(R.id.restaurantListView);
        restaurantList.setAdapter(adapter);


    }

    private void registerRestaurantListClick() {
        ListView restaurantListView = (ListView) findViewById(R.id.restaurantListView);
        restaurantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Restaurant selectedRestaurant = allRestaurants.get(position - 1);

                Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
                intent.putExtra("restaurant", selectedRestaurant);
                startActivity(intent);
            }
        });

        TextView listViewHeader = new TextView(this);
        listViewHeader.setText("List Of Restauants");
        restaurantListView.addHeaderView(listViewHeader);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Go through the different available options in the spinner
        switch (position) {
            case 0:
                // Call method to populate the list view - Get all restaurants from API and display them
                // Checks whether the restaurant is currently open or closed and edits the text accordingly
                allRestaurants = new ArrayList<Restaurant>();
                try {
                    getAllRestaurants();
                    populateRestaurantList();
                } catch (Exception e) {
                    Toast.makeText(this, "Error finding restaurants. Please try again.", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                // Sort the collection based on locations then update the listView
                Collections.sort(allRestaurants);
                populateRestaurantList();
                break;
            case 2:
                // Sort the collection based on the restaurants name then update the listView
                Collections.sort(allRestaurants, new Comparator<Restaurant>() {
                    @Override
                    public int compare(Restaurant r1, Restaurant r2) {
                        return r1.getRestaurantName().compareTo(r2.getRestaurantName());
                    }
                });
                populateRestaurantList();
                break;

            default:
                // Call method to populate the list view - Get all restaurants from API and display them
                // Checks whether the restaurant is currently open or closed and edits the text accordingly
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

            // Parse an opening and closing time - di   splay either open or closed
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
                Toast.makeText(getContext(), "Error finding restaurants. Please try again.", Toast.LENGTH_SHORT).show();
            }

            // Calculate the distance to the user from the restaurant and location given
            currentRestaurant.calculateRestaurantDistance(customerLocation, currentRestaurant);

            // Convert to BIGDECIMAL as float wasnt being displayed correctly
            BigDecimal distanceFinal = new BigDecimal(currentRestaurant.getDistanceToUser()).setScale(2, BigDecimal.ROUND_HALF_UP);

            // Find the textview and set the value
            TextView restaurantDistanceToUser = (TextView) restaurantItemView.findViewById(R.id.txtRestuarantDistance);
            restaurantDistanceToUser.setText(distanceFinal + " Miles");

            return restaurantItemView;
        }
    }

    public class asyncGetData extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... params) {
            Looper.prepare();
            try {
                Thread.sleep(1000);
                returnedJSON = APIConnection.getAPIData(urlToUse);
            } catch (Exception e) {
                Toast.makeText(getWindow().getContext(), "Error finding restaurants. Please try again.", Toast.LENGTH_SHORT).show();
            }
            return returnedJSON;
        }
    } //End of AsyncTask

    @Override
    public void onResume() {
        // If the user presses the back button you must update the order total on the previous activity
        super.onResume();

        if (menuItem != null) {
            // Set the menu title Text to the current users order total
            menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
        }
    }
}
