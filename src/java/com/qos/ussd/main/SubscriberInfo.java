/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.main;

import com.qos.ussd.util.AradHelper;
import com.qos.ussd.util.MerchantDetailsResp;
import com.qos.ussd.util.arad.dto.Agency;
import com.qos.ussd.util.arad.dto.TravelItenary;
import com.qos.ussd.util.arad.dto.TravelTime;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 *
 * @author ptrack
 */
public class SubscriberInfo {
    private String msisdn;
    private int menuLevel;
    private BigDecimal amount;
    private String accountNo;
    private String merchantCode;
    private String merchantName;
    private String accountDetails;
    private MerchantDetailsResp merchantDetails;
    
    private ArrayList<Agency> agencyList;
    private int selectedAgency;
    
    private ArrayList<TravelItenary> travelItenaryList;
    private int selectedTravelItenary;
    
    private ArrayList<TravelTime> travelTimeList;
    private int selectedTravelTime;
    
    
    private boolean isAradMenu;
    private boolean isDepartureToday;

    public boolean isIsAradMenu() {
        return isAradMenu;
    }

    public void setIsAradMenu(boolean isAradMenu) {
        this.isAradMenu = isAradMenu;
    }

    public USSDSessionHandler.ARAD_MENUS getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(USSDSessionHandler.ARAD_MENUS transactionType) {
        this.transactionType = transactionType;
    }

    public AradHelper getAradDetails() {
        return aradDetails;
    }

    public void setAradDetails(AradHelper aradDetails) {
        this.aradDetails = aradDetails;
    }
    private USSDSessionHandler.ARAD_MENUS transactionType;
    private AradHelper aradDetails;
    

    /**
     * @return the msisdn
     */
    public String getMsisdn() {
        return msisdn;
    }

    /**
     * @param msisdn the msisdn to set
     */
    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    /**
     * @return the menuLevel
     */
    public int getMenuLevel() {
        return menuLevel;
    }
    
    public void incrementMenuLevel() {
        menuLevel++;
    }

    /**
     * @param menuLevel the menuLevel to set
     */
    public void setMenuLevel(int menuLevel) {
        this.menuLevel = menuLevel;
    }

    /**
     * @return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * @return the accountNo
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * @param accountNo the accountNo to set
     */
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    /**
     * @return the merchantCode
     */
    public String getMerchantCode() {
        return merchantCode;
    }

    /**
     * @param merchantCode the merchantCode to set
     */
    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    /**
     * @return the merchantName
     */
    public String getMerchantName() {
        return merchantName;
    }

    /**
     * @param merchantName the merchantName to set
     */
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    /**
     * @return the accountDetails
     */
    public String getAccountDetails() {
        return accountDetails;
    }

    /**
     * @param accountDetails the accountDetails to set
     */
    public void setAccountDetails(String accountDetails) {
        this.accountDetails = accountDetails;
    }

    /**
     * @return the merchantDetails
     */
    public MerchantDetailsResp getMerchantDetails() {
        return merchantDetails;
    }

    /**
     * @param merchantDetails the merchantDetails to set
     */
    public void setMerchantDetails(MerchantDetailsResp merchantDetails) {
        this.merchantDetails = merchantDetails;
    }

    /**
     * @return the isDepartureToday
     */
    public boolean isIsDepartureToday() {
        return isDepartureToday;
    }

    /**
     * @param isDepartureToday the isDepartureToday to set
     */
    public void setIsDepartureToday(boolean isDepartureToday) {
        this.isDepartureToday = isDepartureToday;
    }

    public ArrayList<Agency> getAgencyList() {
        return agencyList;
    }

    public void setAgencyList(ArrayList<Agency> agencyList) {
        this.agencyList = agencyList;
    }

    public int getSelectedAgency() {
        return selectedAgency;
    }

    public void setSelectedAgency(int selectedAgency) {
        this.selectedAgency = selectedAgency;
    }

    public ArrayList<TravelItenary> getTravelItenaryList() {
        return travelItenaryList;
    }

    public void setTravelItenaryList(ArrayList<TravelItenary> travelItenaryList) {
        this.travelItenaryList = travelItenaryList;
    }

    public int getSelectedTravelItenary() {
        return selectedTravelItenary;
    }

    public void setSelectedTravelItenary(int selectedTravelItenary) {
        this.selectedTravelItenary = selectedTravelItenary;
    }

    public ArrayList<TravelTime> getTravelTimeList() {
        return travelTimeList;
    }

    public void setTravelTimeList(ArrayList<TravelTime> travelTimeList) {
        this.travelTimeList = travelTimeList;
    }

    public int getSelectedTravelTime() {
        return selectedTravelTime;
    }

    public void setSelectedTravelTime(int selectedTravelTime) {
        this.selectedTravelTime = selectedTravelTime;
    }
    
    
}
