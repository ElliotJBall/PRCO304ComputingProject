package com.example.elliot.automatedorderingsystem.Login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.elliot.automatedorderingsystem.R;

public class RegisterCustomerActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editFirstName, editSurname, editAddress, editTelephoneNumber, editMobileNumber, editEmailAddress;
    private Button btnRegister;

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
        editFirstName = (EditText) findViewById(R.id.editFirstName);
        editSurname = (EditText) findViewById(R.id.editSurname);
        editAddress = (EditText) findViewById(R.id.editAddress);
        editTelephoneNumber = (EditText) findViewById(R.id.editTelephoneNumber);
        editMobileNumber = (EditText) findViewById(R.id.editMobileNumber);
        editEmailAddress = (EditText) findViewById(R.id.editEmailAddress);
    }

    private boolean checkEditTextBoxes() {
        boolean editTextAreFull = false;

        // Find the layout and loop through each editText to ensure that it isn't null or empty
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutRegisterUser);

        for (int i = 0; i < relativeLayout.getChildCount(); i++) {
            if (relativeLayout.getChildAt(i) instanceof EditText) {
                if (((EditText) relativeLayout.getChildAt(i)).getText().toString().equals("")) {
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

    private void registerCusomter() {
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegisterCustomer :
                // Check to ensure that no editTexts were left empty before continuing
                boolean canContinue = checkEditTextBoxes();

                // If canContinue is true then begin the creating the customer object and insert the user into the database
                if (canContinue) {
                    registerCusomter();
                }

                break;
            default:
                break;
        }
    }
}
