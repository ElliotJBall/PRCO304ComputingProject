package com.example.elliot.automatedorderingsystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.Basket.BasketActivity;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Food;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Order;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;
import com.example.elliot.automatedorderingsystem.Login.LoginActivity;
import com.example.elliot.automatedorderingsystem.OrderHistory.OrderHistoryActivity;
import com.example.elliot.automatedorderingsystem.Recommendation.RecommendationActivity;
import com.example.elliot.automatedorderingsystem.RestaurantAndMenu.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Location customerLocation = new Location("");

    // Variables to deal with gathering the users location
    private double userLongitude = 0.00, userLatitude = 0.00;
    // Variables to get and hold the location of the user
    private LocationManager locationManager;
    private LocationListener locationListener;

    protected ArrayList<Restaurant> allRestaurants = new ArrayList<Restaurant>();
    protected String urlToUse, returnedJSON = "", openingTime = "00:00:00", closingTime = "23:00:00", currentTime = "00:00:00";
    private MenuItem menuItem;
    private APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Check for user permissions - This must be done for later versions of android devices - application functionality is serverly hampered without the permisions
        boolean canContinue = checkForPermissions();

        if (canContinue) {
            getCustomerLocation();
            getLocationUpdates();
            // Set the new locations longitude and latitude to those that were just obtained
            customerLocation.setLongitude(userLongitude);
            customerLocation.setLatitude(userLatitude);

            try {
                getAllRestaurants();
            } catch (Exception e) {
                Toast.makeText(this, "Error getting restaurants, please ensure the application can use the internet.", Toast.LENGTH_SHORT).show();
            }
        }

        // Add a marker in Sydney and move the camera
        LatLng userLocation = new LatLng(customerLocation.getLongitude(), customerLocation.getLatitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title("You").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        //mMap.setMinZoomPreference(20);

        // Loop through all restaurants adding a marker onto the map
        for (Restaurant restaurant : allRestaurants) {
            // Convert to BIGDECIMAL as float wasnt being displayed correctly
            restaurant.calculateRestaurantDistance(customerLocation, restaurant);
            BigDecimal distanceFinal = new BigDecimal(restaurant.getDistanceToUser()).setScale(2, BigDecimal.ROUND_HALF_UP);
            
            LatLng restaurantLocation = new LatLng(Float.valueOf(restaurant.getLongitude()),Float.valueOf(restaurant.getLatitude()));
            mMap.addMarker(new MarkerOptions().position(restaurantLocation).title(restaurant.getRestaurantName() + " " + String.valueOf(distanceFinal) + " Miles away").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
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
                startActivity(new Intent(MapsActivity.this, BasketActivity.class));
                break;
            case R.id.orderTotal:
                startActivity(new Intent(MapsActivity.this, BasketActivity.class));
                break;
            case R.id.viewOrderHistory:
                startActivity(new Intent(MapsActivity.this, OrderHistoryActivity.class));
                break;
            case R.id.viewRecommendations:
                startActivity(new Intent(MapsActivity.this, RecommendationActivity.class));
                break;
            case R.id.viewAllRestaurants:
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
                break;
            case R.id.signOut:
                Customer.getInstance().setUsername(null);
                Order newOrder = new Order();
                Customer.getInstance().setUserOrder(newOrder);
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                break;
            case R.id.viewMap:
                startActivity(new Intent(MapsActivity.this, MapsActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCustomerLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Attempt to get the users location before the onLocationChange method is called.

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
        Location location = locationManager.getLastKnownLocation("gps");

        if (location != null) {
            userLongitude = location.getLatitude();
            userLatitude = location.getLongitude();
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
            locationManager.requestLocationUpdates("gps", 30000, 0, locationListener);
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
        urlToUse = "http://192.168.0.2:8080/restaurant/restaurants";
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




    public class asyncGetData extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... params) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
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
