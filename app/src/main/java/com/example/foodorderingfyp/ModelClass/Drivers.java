package com.example.foodorderingfyp.ModelClass;

public class Drivers {
    private String driverAddress,driverName,driverPhone;

    public Drivers() {
    }

    public Drivers(String driverAddress, String driverName, String driverPhone) {
        this.driverAddress = driverAddress;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
    }

    public String getDriverAddress() {
        return driverAddress;
    }

    public void setDriverAddress(String driverAddress) {
        this.driverAddress = driverAddress;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }
}

