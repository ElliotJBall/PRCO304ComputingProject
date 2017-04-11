package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Elliot on 24/02/2017.
 */

public class Order implements Serializable {

    protected String orderId;
    protected float totalPrice = 0.00f;
    protected ArrayList<Food> foodOrdered = new ArrayList<Food>();
    protected OrderStatus orderStatus;
    protected Date dateOrdered;

    public Order() {

    }

    public Order(String orderId, float totalPrice, ArrayList<Food> foodOrdered, OrderStatus orderStatus, Date dateOrdered) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.foodOrdered = foodOrdered;
        this.orderStatus = orderStatus;
        this.dateOrdered = dateOrdered;
    }

    protected void GenerateOrder(Order order, User user, Restaurant restaurant) {

    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public Date getDateOrdered() {
        return dateOrdered;
    }

    public void setDateOrdered(Date dateOrdered) {
        this.dateOrdered = dateOrdered;
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
