package com.meenu.safedrive;

/**
 * Created by Jobin on 1/28/2018.
 */

public class Category {
    private int type;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    private long timeStamp;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    private String categoryId;

    public Category(int type, double latitude, double longitude, String name,long timeStamp) {

        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.timeStamp=timeStamp;
    }

    public int getType() {
        return type;

    }

    public void setType(int type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private double latitude,longitude;
    private String name;
}
