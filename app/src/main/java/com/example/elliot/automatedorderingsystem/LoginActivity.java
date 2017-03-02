package com.example.elliot.automatedorderingsystem;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

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

        MongoClient
    }

    @Override
    public void onClick(View v) {
        // Switch with cases for each button, get which button was pressed and run the appropriate code
        switch (v.getId()) {
            case R.id.btnSignIn:
                break;
            case R.id.btnRegister:
                break;
            case R.id.btnContinueAsGuest:
                break;
            default:
                break;
        }
    }
}
