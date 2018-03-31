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
public class TravelTime {

    private String id;

    private String time;

    private String agence;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", time = " + time + ", agence = " + agence + "]";
    }
}
