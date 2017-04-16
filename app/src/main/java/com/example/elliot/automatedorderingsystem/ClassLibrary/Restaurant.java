package com.example.elliot.automatedorderingsystem.ClassLibrary;

import android.location.Location;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Elliot on 24/02/2017.
 */

public class Restaurant implements Serializable, Comparable<Restaurant> {

    protected String restaurantId;
    protected String restaurantName;
    protected String address;
    protected String city;
    protected String postcode;
    protected String longitude;
    protected String latitude;
    protected Date openingTime;
    protected Date closingTime;
    protected ArrayList<Food> menu = new ArrayList<Food>();
    protected float distanceToUser = 0.00f;

    public ArrayList<Food> getMenu() {
        return menu;
    }

    public void setMenu(ArrayList<Food> menu) {
        this.menu = menu;
    }

    public Restaurant() {

    }

    public Restaurant(String restaurantId, String restaurantName, String address, String city, String postcode, String longitude
                      , String latitude, Date openingTime, Date closingTime, ArrayList<Food> menu) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.city = city;
        this.postcode = postcode;
        this.longitude = longitude;
        this.latitude = latitude;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.menu = menu;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public Date getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(Time openingTime) {
        this.openingTime = openingTime;
    }

    public Date getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(Time closingTime) {
        this.closingTime = closingTime;
    }

    public float getDistanceToUser() { return distanceToUser; }

    public void setDistanceToUser(float distanceToUser) {
        this.distanceToUser = distanceToUser;
    }

    @Override
    public int compareTo(Restaurant another) {
        if (this.getDistanceToUser() > another.getDistanceToUser()) {
            return 1;
        } else if (this.getDistanceToUser() < another.getDistanceToUser()) {
            return -1;
        } else {
            return 0;
        }
    }

}
