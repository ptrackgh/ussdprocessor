/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util;

import java.util.Date;

/**
 *
 * @author ptrack
 */
public class AradHelper {
    private String company;
    private String departure;
    private Date departureDate;
    private String departureTime;
    private String destination;
    private int places;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPlaces() {
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }
    
    @Override
    public String toString() {
        return "AradHelper{" + "company=" + company + ", departure=" + departure + ", departureDate=" + departureDate + ", departureTime=" + 
                departureTime + ", destination=" + destination + ", places=" + places + '}';
    }
    
}
