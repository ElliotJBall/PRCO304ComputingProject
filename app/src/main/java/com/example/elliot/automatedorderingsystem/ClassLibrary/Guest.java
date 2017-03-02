package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.util.Date;

/**
 * Created by Elliot on 24/02/2017.
 */

public class Guest extends User {

    public Guest() {

    }

    public Guest(String userId, String firstname, String lastname, TypeOfUser typeOfUser, Date dateOfBirth, String address, String county, String city, String telephoneNumber, String mobileNumber, String emailAddress) {
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
