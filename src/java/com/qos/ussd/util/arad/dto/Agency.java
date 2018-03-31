/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util.arad.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Agency{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("libelle")
    @Expose
    private String libelle;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    
    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", libelle = "+libelle+"]";
    }

}
