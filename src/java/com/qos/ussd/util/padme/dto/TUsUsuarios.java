/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util.sunu.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Malkiel
 */
public class Infocontrat {
    @SerializedName("assure")
    @Expose
    private String assure;
    @SerializedName("produit")
    @Expose
    private String produit;
    @SerializedName("prime")
    @Expose
    private String prime;

    public String getAssure() {
        return assure;
    }

    public void setAssure(String assure) {
        this.assure = assure;
    }

    public String getProduit() {
        return produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public String getPrime() {
        return prime;
    }

    public void setPrime(String prime) {
        this.prime = prime;
    }

    @Override
    public String toString() {
        return "Infocontrat{" + "assure=" + assure + ", produit=" + produit + ", prime=" + prime + '}';
    }
    
}
