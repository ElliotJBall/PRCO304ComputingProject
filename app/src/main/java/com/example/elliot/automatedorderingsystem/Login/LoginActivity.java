package com.example.elliot.automatedorderingsystem.Login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.CustomerGender;
import com.example.elliot.automatedorderingsystem.RestaurantAndMenu.MainActivity;
import com.example.elliot.automatedorderingsystem.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // Textviews and strings to hold the customers username and password
    protected TextView txtUsername, txtPassword;
    protected String username, password;

    // Buttons to set onClickListeners for the various options the user has
    private Button btnSignIn, btnCreateAnAccount, btnContinueAsGuest, btnRegister;
    // Boolean that enables the application to wait whilst the users details are collected from the database
    private boolean isCompleted = false;
    // Loading indicator to display when the user attempts to login
    private AVLoadingIndicatorView loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Call method that gets the UI elements and sets onClickListeners
        grabUIElements();

        // Ensure that the loadingIndictor is set to invisible
        loadingIndicator.hide();

        // Check for user permissions - This must be done for later versions of android devices - application functionality is serverly hampered without the permisions
        checkForPermissions();
    }

    @Override
    public void onClick(View v) {
        // Switch with cases for each button, get which button was pressed and run the appropriate code
        switch (v.getId()) {
            case R.id.btnSignIn:
                loadingIndicator.show();
                // Get the username and password
                username = txtUsername.getText().toString();
                password = txtPassword.getText().toString();
                try {
                    checkSignIn();
                } catch (Exception e) {
                    Toast.makeText(this, "Error checking user details. Please try again..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRegister:
                startActivity(new Intent(LoginActivity.this, RegisterCustomerActivity.class));
                break;
            case R.id.btnContinueAsGuest:
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                break;
            default:
                break;
        }
    }

    private void grabUIElements() {
        // Set onClickListeners on the buttons on the login activity
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(this);

        btnCreateAnAccount = (Button) findViewById(R.id.btnRegister);
        btnCreateAnAccount.setOnClickListener(this);

        btnContinueAsGuest = (Button) findViewById(R.id.btnContinueAsGuest);
        btnContinueAsGuest.setOnClickListener(this);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        txtUsername = (TextView) findViewById(R.id.txtUsername);
        txtPassword = (TextView) findViewById(R.id.txtPassword);

        loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.signInLoadingIndicator);
    }

    // Ensure there are inputs for username and password
    // if no customer is returned provide information to user and ask them to try again
    // If there is a valid user create initialise the Mainactivity and open it
    private void checkSignIn() throws ParseException, ExecutionException, InterruptedException {
        if (txtUsername.getText().toString().isEmpty() == true || txtPassword.getText().toString().isEmpty() == true) {
            loadingIndicator.hide();
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
        } else {
            // Check the user credentials agaisnt the Neo4j database to check whether the user exists
            // Execute the task off the UI thread to ensure the application doesn't bomb
            // Create a boolean to change to true if the user does exist so the rest of the user details can be gathered
            final Thread checkUserCredentials = new Thread() {
                @Override
                public void run() {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    try {
                        Driver driver = GraphDatabase.driver("bolt://192.168.0.2:7687", AuthTokens.basic("neo4j", "password"));

                        Session session = driver.session();

                        // Statement to run to check whether the user exists in the database
                        StatementResult result = session.run("MATCH (n:Customer) WHERE n.username = {username} AND n.password = {password} RETURN (n)"
                                , Values.parameters("username", username, "password", password));

                        // Check whether there was a result if not then the user either entered the wrong credentials or the user does not exist in the database
                        // If true then add the credentials to the customer instance and then return to the loginActivity
                        while (result.hasNext()) {
                            Record record = result.next();
                            Node node = record.values().get(0).asNode();

                            // Get the customer details from the node and add them to the customer instance
                            Customer.getInstance().setUsername(username);
                            Customer.getInstance().setPassword(password);
                            Customer.getInstance().setUserId(node.get("_id").asString());
                            Customer.getInstance().setFirstname(node.get("firstName").asString());
                            Customer.getInstance().setLastname(node.get("lastName").asString());
                            Customer.getInstance().setAddress(node.get("address").asString());
                            Customer.getInstance().setCity(node.get("city").asString());
                            Customer.getInstance().setCountry(node.get("countryName").asString());
                            Customer.getInstance().setPostcode(node.get("postcode").asString());
                            // Attempt to parse the date of birth of the user from the string given
                            try {
                                SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyy");
                                Date dateOfBirth = originalFormat.parse(node.get("dateOfBirth").asString());
                                Customer.getInstance().setDateOfBirth(dateOfBirth);
                            } catch (ParseException e) {
                                Toast.makeText(getWindow().getContext(), "Error getting user details. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                            Customer.getInstance().setEmailAddress(node.get("emailAddress").asString());
                            Customer.getInstance().setMobileNumber(node.get("mobileNumber").asString());
                            Customer.getInstance().setTelephoneNumber(node.get("telephoneNumber").asString());

                            String genderString = node.get("gender").asString();

                            if (!genderString.equals("")) {
                                switch (genderString) {
                                    case "MALE" :
                                        Customer.getInstance().setGender(CustomerGender.MALE);
                                        break;
                                    case "FEMALE" :
                                        Customer.getInstance().setGender(CustomerGender.FEMALE);
                                        break;
                                    case "NOTSAY" :
                                        Customer.getInstance().setGender(CustomerGender.NOTSAY);
                                        break;
                                }
                            }
                        }

                        session.close();
                        driver.close();

                    } catch (Exception e) {
                        Toast.makeText(getWindow().getContext(), "Error connecting to database. Please try again.", Toast.LENGTH_SHORT).show();
                    }

                }
            };
            checkUserCredentials.start();
            checkUserCredentials.join();

            if (Customer.getInstance().getUsername() == null && Customer.getInstance().getPassword() == null) {
                isCompleted = false;
                loadingIndicator.hide();
                Toast.makeText(this, "Incorrect username or password. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        }
    }

    private boolean checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                break;
            default:
                break;
        }
    }
}
