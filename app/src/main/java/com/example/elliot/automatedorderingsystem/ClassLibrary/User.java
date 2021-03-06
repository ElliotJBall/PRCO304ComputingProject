package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Elliot on 24/02/2017.
 */

public abstract class User implements IUser, Serializable {

    protected String userId;
    protected String username;
    protected String password;
    protected String firstname;
    protected String lastname;
    protected Date dateOfBirth;
    protected String address;
    protected String country;
    protected String city;
    protected String telephoneNumber;
    protected String mobileNumber;
    protected String emailAddress;
    protected Order userOrder = new Order();
    protected String postcode;
    protected CustomerGender gender;

    @Override
    public CustomerGender getGender() {
        return gender;
    }

    @Override
    public void setGender(CustomerGender gender) {
        this.gender = gender;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Order getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(Order userOrder) {
        this.userOrder = userOrder;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getFirstname() {
        return firstname;
    }

    @Override
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    @Override
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public void setCountry(String county) {
        this.country = county;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    @Override
    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    @Override
    public String getMobileNumber() {
        return mobileNumber;
    }

    @Override
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
