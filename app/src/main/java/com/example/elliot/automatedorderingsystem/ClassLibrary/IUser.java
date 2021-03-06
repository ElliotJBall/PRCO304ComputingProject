package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.util.Date;

/**
 * Created by Elliot on 24/02/2017.
 */

public interface IUser {

    String getUsername();
    String getPassword();
    String getUserId();
    String getFirstname();
    String getLastname();
    Date getDateOfBirth();
    String getAddress();
    String getCountry();
    String getCity();
    String getTelephoneNumber();
    String getMobileNumber();
    String getEmailAddress();
    Order getUserOrder();
    String getPostcode();
    CustomerGender getGender();

    void setUsername(String username);
    void setPassword(String password);
    void setUserId(String userId);
    void setFirstname(String firstname);
    void setLastname(String lastname);
    void setDateOfBirth(Date dateOfBirth);
    void setAddress(String address);
    void setCountry(String country);
    void setCity(String city);
    void setTelephoneNumber(String telephoneNumber);
    void setMobileNumber(String mobileNumber);
    void setEmailAddress(String emailAddress);
    void setUserOrder(Order userOrder);
    void setPostcode(String postcode);
    void setGender(CustomerGender gender);
}
