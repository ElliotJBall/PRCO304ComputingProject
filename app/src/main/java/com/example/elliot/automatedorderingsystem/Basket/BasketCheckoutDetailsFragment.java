package com.example.elliot.automatedorderingsystem.Basket;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.APIConnection;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.OrderStatus;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;
import com.example.elliot.automatedorderingsystem.MainActivity;
import com.example.elliot.automatedorderingsystem.OrderHistory.OrderHistoryActivity;
import com.example.elliot.automatedorderingsystem.R;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class BasketCheckoutDetailsFragment extends Fragment implements View.OnClickListener {

    private Button btnPurchaseOrder;
    private Restaurant restaurant;
    private View rootView;
    private APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;
    private String urlToUse = "";
    private JSONObject objectToUse;
    private int responseCode = 0;
    private AVLoadingIndicatorView loadingIndicator;

    // EditTexts for all the fragments parts, need to get these to get and set data
    private EditText editFirstName, editSurname, editAddress, editTelephoneNumber, editMobileNumber, editEmailAddress;

    public BasketCheckoutDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_basket_checkout_details, container, false);

        // Get the loading indicator so it can be displayed when the user clicks to add order
        loadingIndicator = (AVLoadingIndicatorView) rootView.findViewById(R.id.loadingIndicator);

        // Set the totalCost textview with the order total
        TextView totalCost = (TextView) rootView.findViewById(R.id.txtTotalCost);
        totalCost.setText("Total:   £" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));

        // Get all the textBoxes from the view and stor
        setEditTextBoxes();

        // Get the restaurant in which the order is for
        if (getActivity().getIntent().getSerializableExtra("restaurant") != null) {
            restaurant = (Restaurant) getActivity().getIntent().getSerializableExtra("restaurant");
        }

        // Get the user details from the customer Instance
        if (Customer.getInstance().getUsername() != null) {
            addCustomerAccountDetails();
        }

        // Find the button and set onClickListener to check when user wants to complete purchase
        btnPurchaseOrder = (Button) rootView.findViewById(R.id.btnPurchaseOrder);
        btnPurchaseOrder.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPurchaseOrder:
                // Boolean to use when checking the editText boxes
                boolean canContinue = checkEditTextBoxes();

                // Check if canContinue is true - means there is data in the textBoxes and order can be placed
                if (canContinue == true) {
                    try {
                        // Insert the order into the database using information from the customer and order object
                        // Make use of the APIConnection class which holds the POST method
                        insertOrder();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setEditTextBoxes() {
        // Get all the textboxes through the rootView
        editFirstName = (EditText) rootView.findViewById(R.id.editFirstName);
        editSurname = (EditText) rootView.findViewById(R.id.editSurname);
        editAddress = (EditText) rootView.findViewById(R.id.editAddress);
        editTelephoneNumber = (EditText) rootView.findViewById(R.id.editTelephoneNumber);
        editMobileNumber = (EditText) rootView.findViewById(R.id.editMobileNumber);
        editEmailAddress = (EditText) rootView.findViewById(R.id.editEmailAddress);
    }

    private void addCustomerAccountDetails() {
        // Set all the different information to that of the customer object
        editFirstName.setText(Customer.getInstance().getFirstname());
        editSurname.setText(Customer.getInstance().getLastname());
        editAddress.setText(Customer.getInstance().getAddress());
        editTelephoneNumber.setText(Customer.getInstance().getTelephoneNumber());
        editMobileNumber.setText(Customer.getInstance().getMobileNumber());
        editEmailAddress.setText(Customer.getInstance().getEmailAddress());
    }

    private boolean checkEditTextBoxes() {
        boolean editTextAreFull = false;

        // Get all elements of the relative layout and loop through disabling them one by one
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutBasketDetailsFragment);

        // Loop through all the parts of the fragment view
        // Check if the part is an edit text or not
        // if it is an editText box check to ensure there is data in the textBox otherwise return false and get the user to try again
        for (int i = 0; i < relativeLayout.getChildCount(); i++) {
            if (relativeLayout.getChildAt(i) instanceof EditText) {
                if (((EditText) relativeLayout.getChildAt(i)).getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Incorrect details. Please ensure all the information is filled out and try again.", Toast.LENGTH_SHORT).show();
                    return editTextAreFull;
                } else {
                    // Otherwise the textBox has information therefore set the boolean to true
                    editTextAreFull = true;
                }
            }
        }
        return editTextAreFull;
    }

    private void insertOrder() throws JSONException, ExecutionException, InterruptedException {
        // Create a new object ID and get all the different elements required for the BSON document
        final ObjectId orderID = ObjectId.get();

        // Add the other required details to the Order object before creating the JSON
        Customer.getInstance().getUserOrder().setOrderId(orderID.toString());
        Customer.getInstance().getUserOrder().setOrderStatus(OrderStatus.PREPARING);
        Customer.getInstance().getUserOrder().setDateOrdered(new Date());

        // Create the JSON for the customers ORDER so it can be added to the database
        String order = new Gson().toJson(Customer.getInstance().getUserOrder());

        // Set the URL to the currentOrders database
        urlToUse = "http://10.0.2.2:8080/order/currentOrders/";

        // Create the JSON object and put required fields
        objectToUse = new JSONObject();
        objectToUse.put("_id", orderID.toString());

        // Check if customer ID is null - if it is generate an ID for them
        if (Customer.getInstance().getUserId().equals("")) {
            ObjectId customerID = ObjectId.get();
        } else {
            objectToUse.put("customerID", Customer.getInstance().getUserId());
        }

        // Add the other required data to the JSON object - require customer ID and firstname to link back to user and restaurant
        // so it can be selected by the Java application
        objectToUse.put("customerID", Customer.getInstance().getUserId());
        objectToUse.put("customerFirstName", editFirstName.getText().toString());
        objectToUse.put("customerOrder", order);
        objectToUse.put("restaurant" , restaurant.getRestaurantName());

        // Disable all the elements from the VIEW and add the loading icon
        disableTextAndEditboxes();

        // Get the APIConnection class and call the POST method passing the JSON
        asyncGetData = new asyncGetData();
        asyncGetData.execute().get();
        asyncGetData.cancel(true);

        // Wait three seconds - Check whether the insert was successful and if it was load new activity and display the order
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                // If the response code indicates the order was successfuly added display popup showing order info
                // Providing user feedback about their action
                if (responseCode == 201) {
                    // Create an alert dialog to display to the user
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    View mView = rootView.inflate(getActivity(), R.layout.order_confirmation_popup, null);
                    alertDialogBuilder.setView(mView);

                    // Find the EditText boxes and append the required data (OrderID and total price)
                    AppCompatTextView editOrderNumber = (AppCompatTextView) mView.findViewById(R.id.txtOrderNumber);
                    editOrderNumber.append(orderID.toString());
                    AppCompatTextView editTotalPrice = (AppCompatTextView) mView.findViewById(R.id.txtOrderTotal);
                    editTotalPrice.setText("Order total: £" + String.format("%.2f", Customer.getInstance().getUserOrder().getTotalPrice()));
                    AppCompatTextView editRestaurantOrderedTo = (AppCompatTextView) mView.findViewById(R.id.txtRestaurantOrderedTo);
                    editRestaurantOrderedTo.append(" " + restaurant.getRestaurantName());

                    // Find the button and set an onClickListener to take the customer to their orders
                    Button btnViewAllOrders = (Button) mView.findViewById(R.id.btnViewUsersOrderHistory);

                    btnViewAllOrders.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Start a new activity where the user can view their order history
                            startActivity(new Intent(getActivity() , OrderHistoryActivity.class));
                        }
                    });

                    // Find the clickable textView for the Guest users to go to the home activity
                    AppCompatTextView txtContinueGuest = (AppCompatTextView) mView.findViewById(R.id.txtContinueGuest);
                    txtContinueGuest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getActivity() , MainActivity.class));
                        }
                    });

                    // Set the dialog to the newly built AlertDialog - Display it
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.show();

                }
            }
        }, 3000);
    }

    private void disableTextAndEditboxes() throws ExecutionException, InterruptedException {
        // Get all elements of the relative layout and loop through disabling them one by one
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutBasketDetailsFragment);

        for (int i = 0; i < relativeLayout.getChildCount(); i++) {
            View view = relativeLayout.getChildAt(i);
            view.setVisibility(View.INVISIBLE);
        }

        // Enable the loading icon to show to the user the application is currently processing their actions
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void enableTextAndEditboxes() {
        // Get all elements of the relative layout and loop through disabling them one by one
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutBasketDetailsFragment);

        for (int i = 0; i < relativeLayout.getChildCount(); i++) {
            View view = relativeLayout.getChildAt(i);
            view.setVisibility(View.VISIBLE);
        }

        // Enable the loading icon to show to the user the application is currently processing their actions
        loadingIndicator.setVisibility(View.INVISIBLE);
    }

    private boolean checkInsertOrderSuccessful() {
        boolean wasSuccessful = false;
        return wasSuccessful;
    }

    public class asyncGetData extends AsyncTask<Object, Object, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            try {
                responseCode = APIConnection.postAPIData(urlToUse, objectToUse);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
