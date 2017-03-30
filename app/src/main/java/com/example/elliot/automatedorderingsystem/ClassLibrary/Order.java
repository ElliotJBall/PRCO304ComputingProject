package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.text.NumberFormat;
import java.util.ArrayList;
/**
 * Created by Elliot on 24/02/2017.
 */

public class Order {

    protected String orderId;
    protected String orderNumber;
    protected float totalPrice = 0.00f;
    protected ArrayList<Food> foodOrdered = new ArrayList<Food>();
    protected OrderStatus orderStatus;

    protected static Order mInstance;

    public Order() {

    }

    public synchronized static Order getInstance() {
        if (mInstance == null) {
            mInstance = new Order();
        }
        return mInstance;
    }

    public Order(String orderId, String orderNumber, float totalPrice, ArrayList<Food> foodOrdered, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.foodOrdered = foodOrdered;
        this.orderStatus = orderStatus;
    }

    protected void GenerateOrder(Order order, User user, Restaurant restaurant) {

    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ArrayList<Food> getFoodOrdered() {
        return foodOrdered;
    }

    public void setFoodOrdered(ArrayList<Food> foodOrdered) {
        this.foodOrdered = foodOrdered;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void addToOrder(Food foodToAdd) {
        // Check if the food to be added is null, if it isn't null add it to the list
        if (foodToAdd != null) {
            foodOrdered.add(foodToAdd);
        }
    }

    public float calculateTotalPrice(ArrayList<Food> totalOrder) {
        // Loop through the array and get the price of each item
        // Add to the total and return the new amount
        totalPrice = 0.0f;
        for (Food itemOfFood : totalOrder) {
            totalPrice = totalPrice + itemOfFood.getPrice();
        }
        return totalPrice;
    }
}
