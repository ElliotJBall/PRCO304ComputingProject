package com.example.elliot.automatedorderingsystem.ClassLibrary;

import java.util.ArrayList;

/**
 * Created by Elliot on 24/02/2017.
 */

public class Order {

    protected String orderId;
    protected String orderNumber;
    protected float totalPrice;
    protected ArrayList<Food> foodOrdered = new ArrayList<Food>();
    protected OrderStatus orderStatus;

    public Order() {

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
}
