/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util.zexpress.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Malkiel
 */
public class PaidOrders {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("transref")
    @Expose
    private String transref;
    @SerializedName("specialfield1")
    @Expose
    private String specialfield1;
    @SerializedName("status")
    @Expose
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransref() {
        return transref;
    }

    public void setTransref(String transref) {
        this.transref = transref;
    }

    public String getSpecialfield1() {
        return specialfield1;
    }

    public void setSpecialfield1(String specialfield1) {
        this.specialfield1 = specialfield1;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
}
