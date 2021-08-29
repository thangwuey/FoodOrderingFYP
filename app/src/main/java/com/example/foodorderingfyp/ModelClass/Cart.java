package com.example.foodorderingfyp.ModelClass;

public class Cart {

    private String foodName,quantity,foodPrice;

    public Cart() {
    }

    public Cart(String foodName, String quantity, String foodPrice) {
        this.foodName = foodName;
        this.quantity = quantity;
        this.foodPrice = foodPrice;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }
}
