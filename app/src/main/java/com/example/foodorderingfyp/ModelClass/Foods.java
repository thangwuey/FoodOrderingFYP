package com.example.foodorderingfyp.ModelClass;

public class Foods {

    //follow firebase name that u want to retrieve all the information
   private String foodName,foodDescription,foodPrice,foodImage;

    public Foods()
    {
        //OPEN CONSTRUCTOR
    }

    public Foods(String foodName, String foodDescription, String foodPrice, String foodImage) {
        this.foodName = foodName;
        this.foodDescription = foodDescription;
        this.foodPrice = foodPrice;
        this.foodImage = foodImage;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public String getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }
}
