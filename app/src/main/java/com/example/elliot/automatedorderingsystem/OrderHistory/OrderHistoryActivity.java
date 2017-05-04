package com.example.elliot.automatedorderingsystem.OrderHistory;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.elliot.automatedorderingsystem.APIConnection;
import com.example.elliot.automatedorderingsystem.Basket.BasketActivity;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Order;
import com.example.elliot.automatedorderingsystem.R;
import com.example.elliot.automatedorderingsystem.Recommendation.RecommendationActivity;
import com.example.elliot.automatedorderingsystem.RestaurantAndMenu.MainActivity;
import com.google.gson.Gson;

import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class OrderHistoryActivity extends AppCompatActivity {

    // ArrayList of Orders to add all the current and previous orders gathered from database
    private ArrayList<Order> currentCustomerOrders = new ArrayList<Order>();
    private ArrayList<Order> previousCustomerOrders = new ArrayList<Order>();

    // Create the variables that will enable this to connect to theAPI
    protected String urlToUse, returnedJSON = "";
    private com.example.elliot.automatedorderingsystem.APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;
    private MenuItem menuItem;
    private FragmentManager fragmentManager = getFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Check if the Customer name is not null - if it isnt then set the title of the acitvity to the user
        if (Customer.getInstance().getFirstname() != null) {
            getSupportActionBar().setTitle(Customer.getInstance().getFirstname() + "'s Order history");

            // Run the APIConnection method to get the required data to display the users orders
            try {
                getCustomersOrders();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Check if the arrays are empty- if they are display the nocurrentOrders frament instead of empty listViews
            if (currentCustomerOrders.isEmpty() && previousCustomerOrders.isEmpty()) {
                disableAllElements();
                getSupportFragmentManager().beginTransaction().add(R.id.orderHistoryFragmentContainer, new OrderHistoryEmptyFragment()).commit();
            }

        } else {
            disableAllElements();
            getSupportFragmentManager().beginTransaction().add(R.id.orderHistoryFragmentContainer, new OrderHistoryEmptyFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Get the menu layout and inflate it setting it on the activity
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        menuItem = menu.findItem(R.id.orderTotal);

        menuItem.setTitle("£" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get which item was selected and redirect user to the appropriate activity
        switch (item.getItemId()) {
            case R.id.basketIcon:
                startActivity(new Intent(OrderHistoryActivity.this , BasketActivity.class));
                break;
            case R.id.orderTotal:
                startActivity(new Intent(OrderHistoryActivity.this , BasketActivity.class));
                break;
            case R.id.viewOrderHistory:
                startActivity(new Intent(OrderHistoryActivity.this, OrderHistoryActivity.class));
                break;
            case R.id.viewRecommendations:
                startActivity(new Intent(OrderHistoryActivity.this, RecommendationActivity.class));
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCustomersOrders() throws ExecutionException, InterruptedException, JSONException {
        // Get the customer ID and add it to the URL
        String customerID = Customer.getInstance().getUserId();
        urlToUse = "http://10.0.2.2:8080/order/currentOrders/?filter={%27customerID%27:%20'"+customerID +"'}";
        // Create the asyncTask and GET the required data - then cancel after its completed to stop the asynctask
        asyncGetData = new asyncGetData();
        asyncGetData.execute().get();
        asyncGetData.cancel(true);

        // HACKY WORKAROUND THAT WORKS - NEED TO FIND A BETTER ALTERNATIVE TO PARSING HAL/JSON
        int index = returnedJSON.lastIndexOf("]") + 1;

        // If the string does not equal nothing - Add any Current orders to the array
        if (!returnedJSON.equals("")) {
            // Reformat the returned JSON as it comes back in HAL/JSON not just JSON
            returnedJSON = returnedJSON.substring(returnedJSON.indexOf("[") , index);

            // Parse the returned BSON data into a BSON document
            BsonArray orderDetails = BsonArray.parse(returnedJSON);

            // Loop through all the current orders - Realistically there shouldn't be more than one but just in case
            // Get BSON value from the returned JSON - convert to document to access only the customerOrder
            // Convert it into an order Object then add it to the list.
            Order order;
            Gson gson = new Gson();

            for (BsonValue bsonValueOrder : orderDetails) {
                Document bsonDocument = Document.parse(bsonValueOrder.toString());
                JSONObject jsonOrder = new JSONObject(String.valueOf(bsonDocument.getString("customerOrder")));
                order = gson.fromJson(String.valueOf(jsonOrder), Order.class);
                currentCustomerOrders.add(order);
            }

            // Now attempt to get the data from the previousOrders collection
            // Return the JSON string to "" to ensure it doesnt use the previous data
            returnedJSON = "";
            urlToUse = "http://10.0.2.2:8080/order/orderHistory/?filter={%27customerID%27:%20'"+customerID +"'}";
            // Create the asyncTask and GET the required data - then cancel after its completed to stop the asynctask
            asyncGetData = new asyncGetData();
            asyncGetData.execute().get();
            asyncGetData.cancel(true);

            // HACKY WORKAROUND THAT WORKS - NEED TO FIND A BETTER ALTERNATIVE TO PARSING HAL/JSON
            index = returnedJSON.lastIndexOf("]") + 1;
            // Reformat the returned JSON as it comes back in HAL/JSON not just JSON
            returnedJSON = returnedJSON.substring(returnedJSON.indexOf("[") , index);

            // Parse the returned BSON data into a BSON document
            orderDetails = BsonArray.parse(returnedJSON);

            // Loop through all the current orders - Realistically there shouldn't be more than one but just in case
            // Get BSON value from the returned JSON - convert to document to access only the customerOrder
            // Convert it into an order Object then add it to the list.
            for (BsonValue bsonValueOrder : orderDetails) {
                 Document bsonDocument = Document.parse(bsonValueOrder.toString());
                 JSONObject jsonOrder = new JSONObject(String.valueOf(bsonDocument.getString("customerOrder")));
                 order = gson.fromJson(String.valueOf(jsonOrder), Order.class);
                 previousCustomerOrders.add(order);
            }
        } // End IF statement

        // Check if the arrays are empty - no point running code if they aren't full
        if (!currentCustomerOrders.isEmpty()) {
            populateCurrentOrdersListView();
        }

        // Check if the previous orders array is empty before attempting to add elements to the view
        if (!previousCustomerOrders.isEmpty()) {
            populatePreviousOrderesListView();
        }
    }

    private void populateCurrentOrdersListView() {
        ArrayAdapter<Order> adapter = new OrderListAdapter();
        ListView currentOrdersList = (ListView) findViewById(R.id.currentOrderList);
        currentOrdersList.setAdapter(adapter);
    }

    private void populatePreviousOrderesListView() {
        ArrayAdapter<Order> adapter = new PreviousOrderListAdapter();
        ListView previousOrdersList = (ListView) findViewById(R.id.previousOrderList);
        previousOrdersList.setAdapter(adapter);
    }

    private void disableAllElements() {
        // Get all elements of the relative layout and loop through disabling them one by one
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.orderHistoryFragmentContainer);

        for (int i = 0; i < relativeLayout.getChildCount(); i++) {
            View view = relativeLayout.getChildAt(i);
            view.setVisibility(View.INVISIBLE);
        }
    }

    private class OrderListAdapter extends ArrayAdapter<Order> {
        public OrderListAdapter() {
            super(OrderHistoryActivity.this, R.layout.order_view, currentCustomerOrders);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Create formatters to enable us to get the date in the correct format
            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyy");

            View currentOrderItemView = convertView;
            if (currentOrderItemView == null) {
                currentOrderItemView = getLayoutInflater().inflate(R.layout.order_view, parent, false);
            }

            // Get the current order from the array
            Order currentOrder = currentCustomerOrders.get(position);

            // Get all the textviews and set set the relevant information using the order object
            TextView timeOrdered = (TextView) currentOrderItemView.findViewById(R.id.txtOrderDate);

            try {
                Date orderDate = originalFormat.parse(String.valueOf(currentOrder.getDateOrdered()));
                timeOrdered.setText(new SimpleDateFormat("dd/MM/yyyy").format(orderDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Set the number of items ordered
            TextView totalItemsOrdered = (TextView) currentOrderItemView.findViewById(R.id.txtNumberOfItemsOrdered);
            totalItemsOrdered.setText("Items Ordered: " + currentOrder.getFoodOrdered().size());

            // Set the cost here
            TextView orderCost = (TextView) currentOrderItemView.findViewById(R.id.txtOrderCost);
            orderCost.setText("£" + String.format("%.2f", currentCustomerOrders.get(position).getTotalPrice()));

            // Set the current status of the order here
            TextView orderStatus = (TextView) currentOrderItemView.findViewById(R.id.txtOrderStatus);
            orderStatus.setText(currentCustomerOrders.get(position).getOrderStatus().toString());

            return currentOrderItemView;
        }
    }

    private class PreviousOrderListAdapter extends ArrayAdapter<Order> {
        public PreviousOrderListAdapter() {
            super(OrderHistoryActivity.this, R.layout.order_view, currentCustomerOrders);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Create formatters to enable us to get the date in the correct format
            SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyy");

            View currentOrderItemView = convertView;
            if (currentOrderItemView == null) {
                currentOrderItemView = getLayoutInflater().inflate(R.layout.order_view, parent, false);
            }

            // Get the current order from the array
            Order currentOrder = previousCustomerOrders.get(position);

            // Get all the textviews and set set the relevant information using the order object
            TextView timeOrdered = (TextView) currentOrderItemView.findViewById(R.id.txtOrderDate);

            try {
                Date orderDate = originalFormat.parse(String.valueOf(currentOrder.getDateOrdered()));
                timeOrdered.setText(new SimpleDateFormat("dd/MM/yyyy").format(orderDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Set the number of items ordered
            TextView totalItemsOrdered = (TextView) currentOrderItemView.findViewById(R.id.txtNumberOfItemsOrdered);
            totalItemsOrdered.setText("Items Ordered: " + currentOrder.getFoodOrdered().size());

            // Set the cost here
            TextView orderCost = (TextView) currentOrderItemView.findViewById(R.id.txtOrderCost);
            orderCost.setText("£" + String.format("%.2f", previousCustomerOrders.get(position).getTotalPrice()));

            // Set the current status of the order here
            TextView orderStatus = (TextView) currentOrderItemView.findViewById(R.id.txtOrderStatus);
            orderStatus.setText(currentCustomerOrders.get(position).getOrderStatus().toString());

            return currentOrderItemView;
        }
    }

    public class asyncGetData extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... params) {
            try {
                returnedJSON = APIConnection.getAPIData(urlToUse);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return returnedJSON;
        }
    } //End of AsyncTask
}
