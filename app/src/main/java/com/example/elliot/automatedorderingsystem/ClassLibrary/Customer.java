package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Elliot on 24/02/2017.
 */

public class Customer extends User implements Serializable {

    protected static Customer mInstance;

    public synchronized static Customer getInstance() {
        if (mInstance == null) {
            mInstance = new Customer();
        }
        return mInstance;
    }

    public Customer(String id, String username, String password, String firstname, String lastname, Date dateOfBirth
                    , String address, String city, String telephoneNumber, String mobileNumber, String emailAddress, Order userOrder, String postcode) {
        this.userId = id;
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.city = city;
        this.telephoneNumber = telephoneNumber;
        this.mobileNumber = mobileNumber;
        this.emailAddress = emailAddress;
        this.userOrder = userOrder;
        this.postcode = postcode;
    }

    public Customer() {

    }
}
