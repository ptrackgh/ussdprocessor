/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util.arad.dto;

/**
 *
 * @author ptrack
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TravelItenary {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("departure")
    @Expose
    private String departure;
    @SerializedName("destination")
    @Expose
    private String destination;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("agence")
    @Expose
    private String agence;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

}
