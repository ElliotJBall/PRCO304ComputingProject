package com.example.elliot.automatedorderingsystem.Basket;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.example.elliot.automatedorderingsystem.ClassLibrary.Order;
import com.example.elliot.automatedorderingsystem.ClassLibrary.Restaurant;
import com.example.elliot.automatedorderingsystem.R;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class BasketCheckoutDetailsFragment extends Fragment implements View.OnClickListener {

    private Button btnPurchaseOrder;
    private Customer customer;
    private Restaurant restaurant;
    private View rootView;
    private com.example.elliot.automatedorderingsystem.APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;
    private String urlToUse = "";
    private JSONObject objectToUse;
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
        totalCost.setText("Total:   £" + String.format("%.2f", Order.getInstance().getTotalPrice()));

        // Get all the textBoxes from the view and stor
        setEditTextBoxes();

        // Get the restaurant in which the order is for
        if (getActivity().getIntent().getSerializableExtra("restaurant") != null) {
            restaurant = (Restaurant) getActivity().getIntent().getSerializableExtra("restaurant");
        }

        // Check if there was a serialised object of the customer
        if (getActivity().getIntent().getSerializableExtra("customer") != null) {
            customer = (Customer) getActivity().getIntent().getSerializableExtra("customer");
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
        editFirstName.setText(customer.getFirstname());
        editSurname.setText(customer.getLastname());
        editAddress.setText(customer.getAddress());
        editTelephoneNumber.setText(customer.getTelephoneNumber());
        editMobileNumber.setText(customer.getMobileNumber());
        editEmailAddress.setText(customer.getEmailAddress());
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
        ObjectId orderID = ObjectId.get();
        String foodOrdered = new Gson().toJson(Order.getInstance().getFoodOrdered());

        // Set the URL to the currentOrders dabase
        urlToUse = "http://10.0.2.2:8080/order/currentOrders/";

        // Create the JSON object and put required fields
        objectToUse = new JSONObject();
        objectToUse.put("_id", orderID.toString());
        objectToUse.put("customerID", customer.getUserId());
        objectToUse.put("customerName", customer.getUsername());
        objectToUse.put("foodOrdered", foodOrdered);
        objectToUse.put("totalPrice", String.format("%.2f", Order.getInstance().getTotalPrice()));
        objectToUse.put("restaurant" , restaurant.getRestaurantName());

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

        // Call the Async task and run the post command
        asyncGetData = new asyncGetData();
        asyncGetData.execute().get();
        // Cancel the async task so it no longer runs in the background
        asyncGetData.cancel(true);
    }

    public class asyncGetData extends AsyncTask<Object, Object, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            try {
                APIConnection.postAPIData(urlToUse, objectToUse);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
