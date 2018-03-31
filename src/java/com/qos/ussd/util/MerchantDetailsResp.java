/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util;

/**
 *
 * @author ptrack
 */
public class MerchantDetailsResp {

    private String name;
    private String email;
    private String dateofregistration;
    private String msisdn;
    private String spaccount;
    private String ipaddress;
    private String status;
    private String merchantcode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Object getDateofregistration() {
        return dateofregistration;
    }

    public void setDateofregistration(String dateofregistration) {
        this.dateofregistration = dateofregistration;
    }

    public Object getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getSpaccount() {
        return spaccount;
    }

    public void setSpaccount(String spaccount) {
        this.spaccount = spaccount;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMerchantcode() {
        return merchantcode;
    }

    public void setMerchantcode(String merchantcode) {
        this.merchantcode = merchantcode;
    }

}
