package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.util.Date;

/**
 * Created by Elliot on 24/02/2017.
 */

public class Customer extends User{

    protected String username;
    protected String password;

    public Customer() {

    }

    public Customer(String username, String password, String userId, String firstname, String lastname, TypeOfUser typeOfUser, Date dateOfBirth
                    , String address, String county, String city, String telephoneNumber, String mobileNumber, String emailAddress) {
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.typeOfUser = typeOfUser;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.county = county;
        this.city = city;
        this.telephoneNumber = telephoneNumber;
        this.mobileNumber = mobileNumber;
        this.emailAddress = emailAddress;
    }
}
