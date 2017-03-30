package com.example.elliot.automatedorderingsystem;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.ClassLibrary.Customer;
import com.example.elliot.automatedorderingsystem.ClassLibrary.TypeOfUser;

import org.bson.Document;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    protected TextView txtUsername, txtPassword;
    protected Customer customer = new Customer();
    protected String username, password, urlToUse, returnedJSON = "";

    private APIConnection APIConnection = new APIConnection();
    private asyncGetData asyncGetData;
    private Button btnSignIn, btnCreateAnAccount, btnContinueAsGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set onClickListeners on the buttons on the login activity
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(this);

        btnCreateAnAccount = (Button) findViewById(R.id.btnRegister);
        btnCreateAnAccount.setOnClickListener(this);

        btnContinueAsGuest = (Button) findViewById(R.id.btnContinueAsGuest);
        btnContinueAsGuest.setOnClickListener(this);

        txtUsername = (TextView) findViewById(R.id.txtUsername);
        txtPassword = (TextView) findViewById(R.id.txtPassword);
    }

    @Override
    public void onClick(View v) {
        // Switch with cases for each button, get which button was pressed and run the appropriate code
        switch (v.getId()) {
            case R.id.btnSignIn:
                // Get the username and password
                username = txtUsername.getText().toString();
                password = txtPassword.getText().toString();
                try {
                    checkSignIn();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnRegister:
                break;
            case R.id.btnContinueAsGuest:
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                break;
            default:
                break;
        }
    }

    // Ensure there are inputs for username and password
    // run the async method to grab the user data
    // if no customer is returned provide information to user and ask them to try again
    // If there is a valid user create initialise the Mainactivity and open it
    private void checkSignIn() throws ParseException, ExecutionException, InterruptedException {
        if (txtUsername.getText().toString().isEmpty() || txtPassword.getText().toString().isEmpty() == true) {
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
        } else {
            urlToUse = "http://10.0.2.2:8080/customer/customerDetails/?filter={%27username%27:%20'"+username+"'}&filter={%27password%27:%20'"+password+"'}";
            asyncGetData = new asyncGetData();
            asyncGetData.execute().get();

            if (returnedJSON.equals("")) {
                Toast.makeText(this, "Incorrect username or password. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                initiateCustomer();
                asyncGetData.cancel(true);
                startActivity(new Intent(LoginActivity.this , MainActivity.class).putExtra("customer" , customer));
            }
            asyncGetData.cancel(true);
        }
    }

    private void initiateCustomer() throws ParseException {
        // Generate BSON document and and create a new customer to store the details
        returnedJSON = returnedJSON.substring(returnedJSON.indexOf("[") +1 , returnedJSON.indexOf("]"));
        Document customerDetails = Document.parse(returnedJSON);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Get valus from BSON document and add to customer object then return customer
        customer.setUserId(customerDetails.get("_id").toString());
        customer.setUsername(customerDetails.get("username").toString());
        customer.setPassword(customerDetails.get("password").toString());
        customer.setFirstname(customerDetails.get("firstName").toString());
        customer.setLastname(customerDetails.get("lastName").toString());
        customer.setTypeOfUser(TypeOfUser.CUSTOMER);
        customer.setDateOfBirth(dateFormat.parse(customerDetails.get("dateOfBirth").toString()));
        customer.setAddress(customerDetails.get("address").toString());
        customer.setCounty(customerDetails.get("county").toString());
        customer.setCity(customerDetails.get("city").toString());
        customer.setTelephoneNumber(customerDetails.get("telephoneNumber").toString());
        customer.setMobileNumber(customerDetails.get("mobileNumber").toString());
        customer.setEmailAddress(customerDetails.get("emailAddress").toString());
    }

    // Check to see whether the login details entered by the user corrospond to a record in the MongoDB database
    //
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
    } //End of AsyncTask
}
