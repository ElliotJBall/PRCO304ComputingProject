package com.example.elliot.automatedorderingsystem.Recommendation;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.APIConnection;
import com.example.elliot.automatedorderingsystem.Basket.BasketActivity;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.Login.LoginActivity;
import com.example.elliot.automatedorderingsystem.OrderHistory.OrderHistoryActivity;
import com.example.elliot.automatedorderingsystem.R;
import com.example.elliot.automatedorderingsystem.RestaurantAndMenu.MainActivity;
import com.wang.avi.AVLoadingIndicatorView;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RecommendationActivity extends AppCompatActivity {

    private AVLoadingIndicatorView loadingIndicator;
    private MenuItem menuItem;

    // ArrayLists to hold all the relatedCustomerIDs and restaurantNames that can be used to gather the correct users / restaurants
    private ArrayList<String> relatedCustomerIds = new ArrayList<>();
    private ArrayList<String> relatedRestaurantNames = new ArrayList<>();


    // Variables to connect to the MongoDB database and check for any previous or current orders a user may have
    private String returnedJSON = "", urlToUse = "";
    private APIConnection APIConnection = new APIConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

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
                    } catch (Exception e) {
                        Toast.makeText(getWindow().getContext(), "Error getting recommendations, please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        // Post the handler after a few seconds delay to load the other elements
        handler.postDelayed(runnable, 3000);

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
                e.printStackTrace();
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


}
