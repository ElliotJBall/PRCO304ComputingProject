package com.example.elliot.automatedorderingsystem.ClassLibrary;

import android.location.Location;

import java.sql.Time;

/**
 * Created by Elliot on 24/02/2017.
 */

public class Restaurant {

    protected String restaurantId;
    protected String restaurantName;
    protected String address;
    protected String city;
    protected String county;
    protected String longitude;
    protected String latitude;
    protected Location location;
    protected Time openingTime;
    protected Time closingTime;
    protected RestaurantStatus restaurantStatus;

    public Restaurant() {

    }

    public Restaurant(String restaurantId, String restaurantName, String address, String city, String county, String longitude
                      , String latitude, Location location, Time openingTime, Time closingTime , RestaurantStatus restaurantStatus) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.city = city;
        this.county = county;
        this.longitude = longitude;
        this.latitude = latitude;
        this.location = location;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.restaurantStatus = restaurantStatus;
    }
}
