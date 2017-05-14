package com.example.elliot.automatedorderingsystem.Recommendation;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.example.elliot.automatedorderingsystem.RestaurantAndMenu.MainActivity;
import com.example.elliot.automatedorderingsystem.RestaurantAndMenu.RestaurantActivity;
import com.wang.avi.AVLoadingIndicatorView;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RecommendationActivity extends AppCompatActivity {

    private AVLoadingIndicatorView loadingIndicator;
    private MenuItem menuItem;

    // ArrayLists to hold all the relatedCustomerIDs and restaurantNames that can be used to gather the correct users / restaurants
    private ArrayList<String> relatedCustomerIds = new ArrayList<>();
    private ArrayList<String> relatedRestaurantNames = new ArrayList<>();

    // ArrayList to hold the recommended restaurant objects
    private ArrayList<Restaurant> relatedRestaurants = new ArrayList<>();

    // Ints to get restaurant from the recommended restaurants and Location object to hold the users current location to calculate distance
    private int restaurantCounter = 0;
    private Location customerLocation = new Location("");
    // Variables to deal with gathering the users location
    private double userLongitude = 0.00, userLatitude = 0.00;
    // Variables to get and hold the location of the user
    private LocationManager locationManager;
    private LocationListener locationListener;


    // Variables to connect to the MongoDB database and check for any previous or current orders a user may have
    private String returnedJSON = "", urlToUse = "";
    private APIConnection APIConnection = new APIConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        // Check for user permissions - This must be done for later versions of android devices - application functionality is serverly hampered without the permisions
        boolean canContinue = checkForPermissions();

        if (canContinue) {
            getCustomerLocation();
            getLocationUpdates();
            // Set the new locations longitude and latitude to those that were just obtained
            customerLocation.setLongitude(userLongitude);
            customerLocation.setLatitude(userLongitude);
        }

        // Find the loading icon from the layout and enable it whilst the details about the user are collected
        loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.recommendationLoadingIndicator);
        loadingIndicator.show();

        // Handler to run this whilst loading the rest of the activities contents
        Handler handler = new Handler();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Check to see whether the customer is logged in first before continuing
                if (Customer.getInstance().getUsername() == null && Customer.getInstance().getPassword() == null) {
                    // Customer isnt logged in therefore cannot produce recommendations - display popup adivising to login
                    signInPopup();
                } else {
                    // Get the selected user and attempt to get all the possible relationships that a user can have
                    // Return all the connected users, adding the to an array
                    try {
                        getCustomersRelationships();

                        // Loop through all the recommended restaurants and produce popups for each one
                        // If the relateRestaurants array is not empty then loop through the restaurants and display a popup for each one
                        if (!relatedRestaurants.isEmpty()) {
                            produceRecommendationPopups(restaurantCounter);
                        } else {
                            // No recommendations in the array, redirect user to main restaurant list
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getWindow().getContext());
                            View mView = getWindow().getDecorView().inflate(getWindow().getContext(), R.layout.no_recommendations_remaining_popup, null);

                            TextView noMoreRestaurants = (TextView) mView.findViewById(R.id.txtViewNoRecommendationsText);
                            noMoreRestaurants.setText("Sorry, we couldnt find any recommendations. Please view the list of restaurants.");

                            // Set onclicklistener to check whether the user wants to redirect to view all restaurants
                            Button btnViewAllRestaurants = (Button) mView.findViewById(R.id.btnViewAllRestaurants);
                            btnViewAllRestaurants.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(RecommendationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });

                            alertDialogBuilder.setView(mView);

                            // Set the dialog to the newly built AlertDialog - Display it
                            AlertDialog dialog = alertDialogBuilder.create();
                            dialog.show();
                        }


                    } catch (Exception e) {
                        Toast.makeText(getWindow().getContext(), "Error getting recommendations, please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        // Post the handler after a few seconds delay to load the other elements
        handler.postDelayed(runnable, 3000);

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
            Location location = locationManager.getLastKnownLocation("gps");
            userLongitude = location.getLongitude();
            userLatitude = location.getLatitude();

        } catch (Exception e) {

        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Update the customers longitude and latitude
                userLongitude = location.getLongitude();
                userLatitude = location.getLatitude();

                customerLocation.setLongitude(userLatitude);
                customerLocation.setLatitude(userLongitude);
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
                startActivity(new Intent(RecommendationActivity.this, BasketActivity.class));
                break;
            case R.id.orderTotal:
                startActivity(new Intent(RecommendationActivity.this, BasketActivity.class));
                break;
            case R.id.viewOrderHistory:
                startActivity(new Intent(RecommendationActivity.this, OrderHistoryActivity.class));
                break;
            case R.id.viewRecommendations:
                startActivity(new Intent(RecommendationActivity.this, RecommendationActivity.class));
                break;
            case R.id.viewAllRestaurants:
                startActivity(new Intent(RecommendationActivity.this, MainActivity.class));
                break;
            case R.id.signOut:
                startActivity(new Intent(RecommendationActivity.this, LoginActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signInPopup() {
        // Dialog box to display when the customer isnt logged in, button onClickListener to check when the user wants to return to the login activity
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        View mView = getWindow().getDecorView().inflate(this, R.layout.sign_in_popup_window, null);
        alertDialogBuilder.setView(mView);

        // Set the dialog to the newly built AlertDialog - Display it
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        // Button onClickListener to check for button to link back to login activity
        Button btnRecommendSignIn = (Button) mView.findViewById(R.id.btnSignInPopup);

        btnRecommendSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecommendationActivity.this, LoginActivity.class));
            }
        });
    }

    private void getCustomersRelationships() throws InterruptedException {
        // Connect to the Neo4j Database using the customer credentials, extracting the customers relationships

        final Thread checkUserCredentials = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                // Connect to the Neo4j Database through the Rest API
                Driver driver = GraphDatabase.driver("bolt://10.0.2.2:7687" , AuthTokens.basic("neo4j" , "password"));
                Session session = driver.session();

                // Statement to run to check whether the user exists in the database
                StatementResult result = session.run("MATCH p =(a:Customer)-[r]->(b:Customer) WHERE a.username = {username} RETURN p LIMIT 25"
                        , Values.parameters("username" , Customer.getInstance().getUsername()));


                ArrayList<Relationship> allRelationships = new ArrayList<>();

                // Check whether there was a result if not then the user either entered the wrong credentials or the user does not exist in the database
                // If true then add the credentials to the customer instance and then return to the loginActivity
                while (result.hasNext()) {
                    // Get the record from the next result
                    Record record = result.next();

                    // Get the path of the relationship
                    Path newPath = record.values().get(0).asPath();

                    // Get the name of the relationship and add it to the array so it can be used to describe the recommendation later
                    Relationship relationship = newPath.relationships().iterator().next();

                    // Add the relationship to the array of relationships
                    // Get the node ID and use this to get the corrosponding user ID
                    allRelationships.add(relationship);
                    long nodeId = relationship.endNodeId();

                    // Use the nodeID to get the correct Node, get the customers details use their customerID to find any potential orders they've placed
                    getCustomerOrdersFromNodeID(nodeId);
                }

                // Method to loop through all the different _ids gathered and to any potential orders placed
                checkRelatedCustomersOrderHistory();

                // Using the restaurants name get the restaurants from the database and add them to an arrayList of type restaurant
                getRecommendedRestaurantsFromDatabase();

                // Close the connection to the Neo4j database
                session.close();
                driver.close();
            }
        };
        checkUserCredentials.start();
        checkUserCredentials.join();
    }

    private void getCustomerOrdersFromNodeID(long nodeId) {
        // Connect to the Neo4J database and return the customer details
        // Connect to the Neo4j Database through the Rest API
        Driver driver = GraphDatabase.driver("bolt://10.0.2.2:7687" , AuthTokens.basic("neo4j" , "password"));
        Session session = driver.session();

        // Statement to run to check whether the user exists in the database
        StatementResult result = session.run("MATCH (n:Customer) WHERE id(n) = {nodeId}"
                        + "RETURN (n)"
                , Values.parameters("nodeId" , nodeId));

        while (result.hasNext()) {
            Record record = result.next();
            Node node = record.values().get(0).asNode();

            // Check if the arrayList already contains the _id, if it does skip it and dont add to arrayList
            if (!relatedCustomerIds.contains(node.get("_id").asString())) {
                relatedCustomerIds.add(node.get("_id").asString());
            }
        }

        // Close connection to database
        session.close();
        driver.close();
    }

    private void checkRelatedCustomersOrderHistory() {
        // Go through each string in the related Id's arrayList, using the returned JSON create a list of restaurants user chose to order from
        for (String customerID : relatedCustomerIds) {
            urlToUse = "http://10.0.2.2:8080/order/currentOrders?filter={%27customerID%27%20:%20%27"+customerID+"%27}";

            try {
                returnedJSON = APIConnection.getAPIData(urlToUse);
            } catch (Exception e) {
                Toast.makeText(getWindow().getContext(), "Error getting recommendations, please try again.", Toast.LENGTH_SHORT).show();
            }

            int index = returnedJSON.lastIndexOf("]") + 1;

            // If the string does not equal nothing - Add any Current orders to the array
            if (!returnedJSON.equals("")) {
                // Get the required part of the JSON string
                returnedJSON = returnedJSON.substring(returnedJSON.indexOf("[") , index);

                if (!returnedJSON.equals("[]")) {
                    // Parse the returned BSON data into a usable BSON array
                    BsonArray orderDetails = BsonArray.parse(returnedJSON);

                    // Get the restaurant Name and add it to the array of restaurants
                    // Required to loop through as BSON value needs to be broken down into a document in order to get the value correctly
                    for (BsonValue bsonValue : orderDetails) {
                        Document bsonDocument = Document.parse(bsonValue.toString());
                        if (bsonDocument.get("restaurant") != null) {
                            relatedRestaurantNames.add(String.valueOf(bsonDocument.get("restaurant")));
                        }
                    }
                }

            }
        }

        // Go through each related customers previous orders that have been completed and check for orders from restaurants
        for (String customerID : relatedCustomerIds) {
            urlToUse = "http://10.0.2.2:8080/order/orderHistory?filter={%27customerID%27%20:%20%27"+customerID+"%27}";

            try {
                returnedJSON = APIConnection.getAPIData(urlToUse);
            } catch (Exception e) {
                Toast.makeText(getWindow().getContext(), "Error getting recommendations, please try again.", Toast.LENGTH_SHORT).show();
            }

            int index = returnedJSON.lastIndexOf("]") + 1;

            // If the string does not equal nothing - Add any Current orders to the array
            if (!returnedJSON.equals("")) {
                // Get the required part of the JSON string
                returnedJSON = returnedJSON.substring(returnedJSON.indexOf("[") , index);

                if (!returnedJSON.equals("[]")) {
                    // Parse the returned BSON data into a usable BSON array
                    BsonArray orderDetails = BsonArray.parse(returnedJSON);

                    // Get the restaurant Name and add it to the array of restaurants
                    // Required to loop through as BSON value needs to be broken down into a document in order to get the value correctly
                    for (BsonValue bsonValue : orderDetails) {
                        Document bsonDocument = Document.parse(bsonValue.toString());
                        if (bsonDocument.get("restaurant") != null) {
                            relatedRestaurantNames.add(String.valueOf(bsonDocument.get("restaurant")));
                        }
                    }
                }

            }
        }
    }

    private void getRecommendedRestaurantsFromDatabase() {
        // Check if restaurant names is not null - If it is not empty then continue otherwise provide popup stating no recommendations currently
        if (!relatedRestaurantNames.isEmpty()) {

            // Check for number of occurences of restaurants, if higher than a certain threshold then add them to the list of restaurantNames no loop through
            // Add the restaurant to the list of restaurantNames to recommend if there are greater than 5 orders /
            ArrayList<String> popularRestaurantsToRecommend = new ArrayList<>();

            for (String relatedRestaurant : relatedRestaurantNames) {
                if (Collections.frequency(relatedRestaurantNames, relatedRestaurant) >= 5) {
                    if (!popularRestaurantsToRecommend.contains(relatedRestaurant)) {
                        popularRestaurantsToRecommend.add(relatedRestaurant);
                    }
                }
            }

            // Loop through each restaurant name in the popularRestaurants array, get the restaurant, instantiate the restaurant object and add it to an array
            for (String restaurantName : popularRestaurantsToRecommend) {
                urlToUse = "http://10.0.2.2:8080/restaurant/restaurants/?filter={%27restaurantName%27:%20'" + restaurantName + "'}";

                try {
                    // Run on non ui thread to get the returned JSON, parse string into correct points from HAL/JSON
                    returnedJSON = APIConnection.getAPIData(urlToUse);
                    returnedJSON = returnedJSON.substring(returnedJSON.indexOf("["), returnedJSON.indexOf("]") + 1);

                    // Create a JSON Array that'll hold all the data pulled
                    JSONArray jsonArray = new JSONArray(returnedJSON);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

                    // Create a temporary restaurant menu as we dont need to get the data until the user has selected it from the list
                    ArrayList<Food> temporaryMenu = new ArrayList<Food>();
                    Document restaurantDocument = Document.parse(jsonArray.get(0).toString());
                    Restaurant restaurant = new Restaurant(restaurantDocument.get("_id").toString(), restaurantDocument.get("restaurantName").toString()
                            , restaurantDocument.get("address").toString(), restaurantDocument.get("city").toString()
                            , restaurantDocument.get("postcode").toString(), restaurantDocument.get("longitude").toString()
                            , restaurantDocument.get("latitude").toString(), simpleDateFormat.parse(restaurantDocument.get("openingTime").toString())
                            , simpleDateFormat.parse(restaurantDocument.get("closingTime").toString()), temporaryMenu);

                    relatedRestaurants.add(restaurant);
                } catch (Exception e) {
                    Toast.makeText(getWindow().getContext(), "Error getting recommendations, please try again.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void produceRecommendationPopups(int restaurantCounter) {
        // Generate popup and add the correct text to it
        if (restaurantCounter < relatedRestaurants.size()) {
            // Dialog box to display when the customer isnt logged in, button onClickListener to check when the user wants to return to the login activity
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            View mView = getWindow().getDecorView().inflate(this, R.layout.restaurant_recommendation_popup, null);

            // Method to set all the text for the popup to that of the restaurant
            try {
                initialiseRecommendationPopupText(mView, relatedRestaurants.get(restaurantCounter));
            } catch (ParseException e) {
                Toast.makeText(this, "Error getting recommendations, please try again.", Toast.LENGTH_SHORT).show();
            }

            alertDialogBuilder.setView(mView);

            // Set the dialog to the newly built AlertDialog - Display it
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();

        } else {
            // Run out of recommendations, display no more recommendations popup to the user
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            View mView = getWindow().getDecorView().inflate(this, R.layout.no_recommendations_remaining_popup, null);

            // Set onclicklistener to check whether the user wants to redirect to view all restaurants
            Button btnViewAllRestaurants = (Button) mView.findViewById(R.id.btnViewAllRestaurants);
            btnViewAllRestaurants.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecommendationActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            alertDialogBuilder.setView(mView);

            // Set the dialog to the newly built AlertDialog - Display it
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        }
    }

    private void initialiseRecommendationPopupText(View mView, final Restaurant currentRecommendedRestaurant) throws ParseException {
        // Simple date parser to parse restaurant time into more readable format
        SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        // Get all the elements off the restaurant_recommendation_popup layout and add the relevant elements from the
        TextView recommendedRestaurantText = (TextView) mView.findViewById(R.id.txtViewRestaurantRecommendationText);
        recommendedRestaurantText.setText("People similar to you ordered:");

        TextView recommendedRestaurantName = (TextView) mView.findViewById(R.id.txtViewRestaurantRecommendedName);
        recommendedRestaurantName.setText(currentRecommendedRestaurant.getRestaurantName());

        Date restaurantDate = originalFormat.parse(currentRecommendedRestaurant.getOpeningTime().toString());
        String restaurantOpeningTime = simpleDateFormat.format(restaurantDate);
        TextView recommendedRestaurantOpeningTime = (TextView) mView.findViewById(R.id.txtViewRecommendedOpeningTime);
        recommendedRestaurantOpeningTime.setText(restaurantOpeningTime);

        Date restaurantClosingDate = originalFormat.parse(currentRecommendedRestaurant.getClosingTime().toString());
        String restaurantClosingTime = simpleDateFormat.format(restaurantClosingDate);
        TextView recommendedRestaurantClosingTime = (TextView) mView.findViewById(R.id.txtViewRecommendedClosingTime);
        recommendedRestaurantClosingTime.setText(restaurantClosingTime);


        // Calculate the distance to the user from the restaurant and location given
        currentRecommendedRestaurant.calculateRestaurantDistance(customerLocation, currentRecommendedRestaurant);

        // Convert to BIGDECIMAL as float wasnt being displayed correctly
        BigDecimal distanceFinal = new BigDecimal(currentRecommendedRestaurant.getDistanceToUser()).setScale(2, BigDecimal.ROUND_HALF_UP);

        TextView restaurantDistance = (TextView) mView.findViewById(R.id.txtRecommendedRestaurantDistance);
        restaurantDistance.setText(distanceFinal + " Miles");

        // Find the buttons to set onClickListeners to check whether the user wants a new recommendation or to view that restaurants menu
        Button btnViewRestaurant = (Button) mView.findViewById(R.id.btnViewRecommendedRestaurant);
        btnViewRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecommendationActivity.this, RestaurantActivity.class);
                intent.putExtra("restaurant", currentRecommendedRestaurant);
                startActivity(intent);
            }
        });

        Button btnNextRecommendation = (Button) mView.findViewById(R.id.btnNextRecommendation);
        btnNextRecommendation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restaurantCounter++;
                produceRecommendationPopups(restaurantCounter);
            }
        });

    }


}
