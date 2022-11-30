package com.example.androidapp;

import android.location.Location;

/**
 * Class to represent Canteen
 */
public class Canteen {
    /**
     * name of the canteen
     */
    private final String name;

    /**
     * rating of the canteen
     */
    private final int rating;

    /**
     * capacity of the canteen
     */
    private final int capacity;

    /**
     * latitude of the canteen
     */
    private final double latitude;

    /**
     * longitude of the canteen
     */
    private final double longitude;

    /**
     * opening time of the canteen (Monday - Thursday)
     */
    private final String openingTime;

    /**
     * opening time of the canteen Friday
     */
    private final String fridayOpeningTime;

    /**
     * address of the canteen
     */
    private final String address;

    /**
     * url of the canteen
     */
    private final String url;

    /**
     * distance between user and canteen
     */
    private int distance;

    /**
     * Canteen class constructor
     * @param name {@link #name}
     * @param rating {@link #rating}
     * @param capacity {@link #capacity}
     * @param latitude {@link #latitude}
     * @param longitude {@link #longitude}
     * @param openingTime {@link #openingTime}
     * @param fridayOpeningTime {@link #fridayOpeningTime}
     * @param address {@link #address}
     * @param url {@link #url}
     */
    public Canteen(String name, int rating, int capacity, double latitude, double longitude, String openingTime, String fridayOpeningTime, String address, String url) {
        this.name = name;
        this.rating = rating;
        this.capacity = capacity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openingTime = openingTime;
        this.fridayOpeningTime = fridayOpeningTime;
        this.address = address;
        this.url = url;
    }

    /**
     * method to measure distance between user and canteen {@link #distance}
     * @param userLatitude latitude of the user
     * @param userLongitude longitude of the user
     */
    public void measureDistance (double userLatitude, double userLongitude){
        float[] result = new float[3];
        Location.distanceBetween(userLatitude,userLongitude,latitude,longitude,result);
        distance = (int) result[0];
    }

    /**
     * method to return name
     * @return {@link #name}
     */
    public String getName(){
        return name;
    }

    /**
     * method to return rating
     * @return {@link #rating}
     */
    public int getRating(){
        return rating;
    }

    /**
     * method to return capacity
     * @return {@link #capacity}
     */
    public int getCapacity(){
        return capacity;
    }

    /**
     * method to return openingTime
     * @return {@link #openingTime}
     */
    public String getOpeningTime(){
        return openingTime;
    }

    /**
     * method to return fridayOpeningTime
     * @return {@link #fridayOpeningTime}
     */
    public String getFridayOpeningTime(){
        return fridayOpeningTime;
    }

    /**
     * method to return address of the canteen (used in google maps)
     * @return {@link #address}
     */
    public String getAddress(){
        return address;
    }

    /**
     * method to return address of the canteen (used in web browser)
     * @return {@link #url}
     */
    public String getUrl(){
        return url;
    }

    /**
     * method to return distance between user and canteen
     * @return {@link #distance}
     */
    public int getDistance(){
        return distance;
    }

    /**
     * method to return string value of distance
     * @return {@link #distance}
     */
    public String getDistanceString(){
        return distance + " m";
    }

    /**
     * method to return string value of rating (0-5)
     * @return {@link #rating}
     */
    public String getRatingString(){
        int units = rating % 10;
        int dozens = rating / 10;
        return dozens + "." + units + "/5";
    }

    /**
     * method to return string value of capacity
     * @return {@link #capacity}
     */
    public String getCapacityString(){
        return capacity + " míst";
    }
}
