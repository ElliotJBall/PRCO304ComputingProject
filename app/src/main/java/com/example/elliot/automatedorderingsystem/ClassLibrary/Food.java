package com.example.elliot.automatedorderingsystem.ClassLibrary;

/**
 * Created by Elliot on 24/02/2017.
 */
public class Food {

    protected String foodId;
    protected String foodName;
    protected float price;
    protected String restaurantName;

    public Food(String foodId, String foodName, float price, String restaurantName) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
        this.restaurantName = restaurantName;
    }

    public Food() {

    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
