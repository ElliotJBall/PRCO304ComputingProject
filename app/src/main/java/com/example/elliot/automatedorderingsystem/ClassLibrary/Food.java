package com.example.elliot.automatedorderingsystem.ClassLibrary;

/**
 * Created by Elliot on 24/02/2017.
 */
public class Food {

    protected String foodId;
    protected String foodName;
    protected float price;

    public Food() {

    }

    public Food(String foodId, String foodName, float price) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
    }
}
