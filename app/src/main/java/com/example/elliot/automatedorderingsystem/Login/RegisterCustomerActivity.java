package com.example.elliot.automatedorderingsystem.Login;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.MainActivity;
import com.example.elliot.automatedorderingsystem.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.bson.types.ObjectId;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RegisterCustomerActivity extends AppCompatActivity implements View.OnClickListener {

    private String _id, username, password, firstName, lastName, dateOfBirth, address, postcode, countyName, city, telephoneNumber, mobileNumber, emailAddress;
    private EditText editUsername, editPassword, editFirstName, editSurname, editDateOfBirth, editAddress, editPostcode, editTelephoneNumber, editMobileNumber, editEmailAddress;
    private Button btnRegister;
    private boolean doesExist = false;
    private AVLoadingIndicatorView loadingIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_customer);

        // Get and set all the editTexts so the data can be gathered
        setEditTextBoxes();

        // Find the button to set an onClickListener
        btnRegister = (Button) findViewById(R.id.btnRegisterCustomer);
        btnRegister.setOnClickListener(this);
    }

    private void setEditTextBoxes() {
        // Get all the textboxes and set them to the already declared variables so the customer object can be built
        editUsername = (EditText) findViewById(R.id.editUsername);
        editPassword = (EditText) findViewById(R.id.editPassword);

        editFirstName = (EditText) findViewById(R.id.editFirstName);
        editSurname = (EditText) findViewById(R.id.editSurname);

        editDateOfBirth = (EditText) findViewById(R.id.editDateOfBirth);

        editAddress = (EditText) findViewById(R.id.editAddress);
        editPostcode = (EditText) findViewById(R.id.editAddress);
        editTelephoneNumber = (EditText) findViewById(R.id.editTelephoneNumber);
        editMobileNumber = (EditText) findViewById(R.id.editMobileNumber);
        editEmailAddress = (EditText) findViewById(R.id.editEmailAddress);

        loadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.registerLoadingIndicator);
    }

    private boolean checkEditTextBoxes() {
        boolean editTextAreFull = false;

        // Find the layout and loop through each editText to ensure that it isn't null or empty
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.constraintRegisterCustomer);

        for (int i = 0; i < layout.getChildCount(); i++) {
            if (layout.getChildAt(i) instanceof EditText) {
                if (((EditText) layout.getChildAt(i)).getText().toString().equals("")) {
                    Toast.makeText(this, "Please ensure all details required have been added.", Toast.LENGTH_SHORT).show();
                    return editTextAreFull;
                } else {
                    // Otherwise the textBox has information therefore set the boolean to true
                    editTextAreFull = true;
                }
            }
        }
            return editTextAreFull;
    }

    private void getDataFromEditTexts() throws InterruptedException {
        // Grab the data and add it to strings to add the customer data into the database
        // Generate a BSON id for the _id variable
        final ObjectId orderID = ObjectId.get();

        // SimpleDateFormater to get the date in the formart required from the user
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Go through all the editTexts and add the data to the strings
        _id = orderID.toString();
        username = editUsername.getText().toString();

        // Check whether that username exists in the database before continuing
        if (checkUsernameDoesntExist(username) == false) {
            password = editPassword.getText().toString();
            firstName = editFirstName.getText().toString();
            lastName = editSurname.getText().toString();
            try {
                dateOfBirth = String.valueOf(dateFormat.parse(editDateOfBirth.getText().toString()));
            } catch (ParseException e) {
                loadingIndicatorView.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Please enter a valid date and try again.", Toast.LENGTH_SHORT).show();
            }

            address = editAddress.getText().toString();
            postcode = editPostcode.getText().toString();
            telephoneNumber = editTelephoneNumber.getText().toString();
            mobileNumber = editMobileNumber.getText().toString();
            emailAddress = editEmailAddress.getText().toString();

            // Use the address inserted to get the county and city of the user
            getUsersAddressDetails();

            // Add the user into the database then redirect them to a new activity
            insertUserIntoDatabase();


        } else {
            loadingIndicatorView.setVisibility(View.INVISIBLE);
            // That username exists in the database so get the user to choose a different one
            Toast.makeText(this, "Sorry that username already exists, please try again with a different one.", Toast.LENGTH_SHORT).show();
        }


    }

    private void getUsersAddressDetails() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Create a list of addresses to hold the addresses returned from the geocoder
        // If the listOfAddresses is not null then get the first address and use the locality and set it to county
        try {
            List<Address> listOfAddresses = geocoder.getFromLocationName(address, 1);

            if (listOfAddresses != null || listOfAddresses.size() > 0) {
                if (!listOfAddresses.get(0).getLocality().equals("") || listOfAddresses.get(0).getLocality() != null) {
                    city = listOfAddresses.get(0).getLocality();
                    countyName = listOfAddresses.get(0).getCountryName();
                }
            }

        } catch (IOException e) {
            Toast.makeText(this, "Error getting your location, please ensure the correct permissions are granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkUsernameDoesntExist(final String username) throws InterruptedException {
        // Create and run on the new thread to ensure the application doesnt bomb trying to run on the mainUI thread
        final Thread checkUserCredentials = new Thread(new Runnable() {
                @Override
                public void run() {
                // Connect to the Neo4j Database through the Rest API
                Driver driver = GraphDatabase.driver("bolt://10.0.2.2:7687", AuthTokens.basic("neo4j", "password"));
                Session session = driver.session();

                // Statement to run to check whether the username exists in the database
                StatementResult result = session.run("MATCH (n:Customer) WHERE n.username = {username}"
                                + "RETURN (n)"
                        , Values.parameters("username", username));

                if (result.hasNext()) {
                    doesExist = true;
                } else {
                    doesExist = false;
                }
            }
            });

        // Run the thread to check whether the username exists in the database (returns a boolean based upon the result)
        checkUserCredentials.start();

        // Wait for the response from the Neo4j database
        checkUserCredentials.join();

        return doesExist;
    }

    private void insertUserIntoDatabase() {
        // Create and run on the new thread to ensure the application doesnt bomb trying to run on the mainUI thread
        final Thread insertNewUser = new Thread(new Runnable() {
            @Override
            public void run() {
                // Connect to the Neo4j Database through the Rest API
                Driver driver = GraphDatabase.driver("bolt://10.0.2.2:7687" , AuthTokens.basic("neo4j" , "password"));
                Session session = driver.session();

                // Statement to insert the new user into the database
                session.run("CREATE (n:Customer {_id: {_id}, username: {username}, password: {password}, " +
                                "firstName: {firstName}, lastName: {lastName}, dateOfBirth: {dateOfBirth}, address: {address}, " +
                                "telephoneNumber: {telephoneNumber}, mobileNumber: {mobileNumber}, postcode: {postcode}})" ,
                        Values.parameters("_id", _id, "username", username, "password", password, "firstName", firstName, "lastName", lastName,
                                "dateOfBirth", dateOfBirth, "address", address, "telephoneNumber", telephoneNumber, "mobileNumber", mobileNumber,
                                "emailAddress", emailAddress, "postcode", postcode, "city", city, "countryName", countyName));
                session.close();
                driver.close();
            }
        });

        // Run the thread and wait for it to be completed
        insertNewUser.start();
        try {
            insertNewUser.join();

            // Check the user was successfully inserted into the database, if they were add the details to the customer instance and continue onto the mainActivity
            if (checkUsernameDoesntExist(username)) {
                // If it is true the user was successfully created, add the data to the customer instance and continue
                initialiseCustomerInstance();

                // Create a popup to indicate the user creating their account was successful, provide option to continue onto main activity

                startActivity(new Intent(RegisterCustomerActivity.this, MainActivity.class));
            } else {
                Toast.makeText(this, "Error creating account, please try again.", Toast.LENGTH_SHORT).show();
            }

        } catch (InterruptedException e) {
            Toast.makeText(this, "Error creating account, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initialiseCustomerInstance() {
        // Add the strings to the customer instance and then return
        Customer.getInstance().setUserId(_id);
        Customer.getInstance().setUsername(username);
        Customer.getInstance().setPassword(password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegisterCustomer :
                loadingIndicatorView.setVisibility(View.VISIBLE);
                // Check the user can continue which ensures that no editTexts are empty or null
                if (checkEditTextBoxes()) {
                    // Grab the data from the edit text boxes and add it to the strings
                    try {
                        getDataFromEditTexts();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    loadingIndicatorView.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                break;
        }
    }
}
