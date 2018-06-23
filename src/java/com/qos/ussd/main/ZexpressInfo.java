/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.main;

import com.qos.ussd.util.zexpress.dto.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Malkiel
 */
public class ZexpressInfo {
    private String msisdn;
    private int menuLevel;
    private BigDecimal amount;
    private BigDecimal frais;
    private String accountNo;
    private String merchantCode;
    private String merchantName;
    private String accountDetails;
    private int quantity;
    private Date addressDate;
    private Date movieDay;
    private PaidOrders paidorders;

    public PaidOrders getPaidorders() {
        return paidorders;
    }

    public void setPaidorders(PaidOrders paidorders) {
        this.paidorders = paidorders;
    }

    public Date getMovieDay() {
        return movieDay;
    }

    public void setMovieDay(Date movieDay) {
        this.movieDay = movieDay;
    }
    private Address address;
    private int selectedAddress;

    public Date getAddressDate() {
        return addressDate;
    }

    public void setAddressDate(Date addressDate) {
        this.addressDate = addressDate;
    }

    public BigDecimal getFrais() {
        return frais;
    }

    public void setFrais(BigDecimal frais) {
        this.frais = frais;
    }
    
    private HashMap<String, Object> subParams = new HashMap<>();
    
    private ArrayList<Address> addressList;
    //private int selectedAddress;
    
    private ArrayList<Delivery> deliveryList;
    private int selectedDelivery;
    
    private ArrayList<Flower> flowerList;
    private int selectedFlower;
    
    private ArrayList<GasBrand> gasBrandList;
    private int selectedGasBrand;
    
    private ArrayList<GasWeight> gasWeightList;
    private int selectedGasWeight;
    
    private ArrayList<Meal> mealList;
    private int selectedMeal;
    
    private ArrayList<MovieTicketHours> movieTicketHoursList;
    private int selectedMovieTicketHours;
    
    private ArrayList<MovieTicketType> movieTicketTypeList;
    private int selectedMovieTicketType;
    
    private ArrayList<PaidOrders> paidOrdersList;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public int getMenuLevel() {
        return menuLevel;
    }
    
    public void incrementMenuLevel() {
        menuLevel++;
    }

    public void setMenuLevel(int menuLevel) {
        this.menuLevel = menuLevel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(String accountDetails) {
        this.accountDetails = accountDetails;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getSelectedAddress() {
        return selectedAddress;
    }

    public void setSelectedAddress(int selectedAddress) {
        this.selectedAddress = selectedAddress;
    }
    
    

    public HashMap<String, Object> getSubParams() {
        return subParams;
    }

    public void setSubParams(HashMap<String, Object> subParams) {
        this.subParams = subParams;
    }

    public ArrayList<Delivery> getDeliveryList() {
        return deliveryList;
    }

    public void setDeliveryList(ArrayList<Delivery> deliveryList) {
        this.deliveryList = deliveryList;
    }

    public int getSelectedDelivery() {
        return selectedDelivery;
    }

    public void setSelectedDelivery(int selectedDelivery) {
        this.selectedDelivery = selectedDelivery;
    }

    public ArrayList<Flower> getFlowerList() {
        return flowerList;
    }

    public void setFlowerList(ArrayList<Flower> flowerList) {
        this.flowerList = flowerList;
    }

    public int getSelectedFlower() {
        return selectedFlower;
    }

    public void setSelectedFlower(int selectedFlower) {
        this.selectedFlower = selectedFlower;
    }

    public ArrayList<GasBrand> getGasBrandList() {
        return gasBrandList;
    }

    public void setGasBrandList(ArrayList<GasBrand> gasBrandList) {
        this.gasBrandList = gasBrandList;
    }

    public int getSelectedGasBrand() {
        return selectedGasBrand;
    }

    public void setSelectedGasBrand(int selectedGasBrand) {
        this.selectedGasBrand = selectedGasBrand;
    }

    public ArrayList<GasWeight> getGasWeightList() {
        return gasWeightList;
    }

    public void setGasWeightList(ArrayList<GasWeight> gasWeightList) {
        this.gasWeightList = gasWeightList;
    }

    public int getSelectedGasWeight() {
        return selectedGasWeight;
    }

    public void setSelectedGasWeight(int selectedGasWeight) {
        this.selectedGasWeight = selectedGasWeight;
    }

    public ArrayList<Meal> getMealList() {
        return mealList;
    }

    public void setMealList(ArrayList<Meal> mealList) {
        this.mealList = mealList;
    }

    public int getSelectedMeal() {
        return selectedMeal;
    }

    public void setSelectedMeal(int selectedMeal) {
        this.selectedMeal = selectedMeal;
    }

    public ArrayList<MovieTicketHours> getMovieTicketHoursList() {
        return movieTicketHoursList;
    }

    public void setMovieTicketHoursList(ArrayList<MovieTicketHours> movieTicketHoursList) {
        this.movieTicketHoursList = movieTicketHoursList;
    }

    public int getSelectedMovieTicketHours() {
        return selectedMovieTicketHours;
    }

    public void setSelectedMovieTicketHours(int selectedMovieTicketHours) {
        this.selectedMovieTicketHours = selectedMovieTicketHours;
    }

    public ArrayList<MovieTicketType> getMovieTicketTypeList() {
        return movieTicketTypeList;
    }

    public void setMovieTicketTypeList(ArrayList<MovieTicketType> movieTicketTypeList) {
        this.movieTicketTypeList = movieTicketTypeList;
    }

    public int getSelectedMovieTicketType() {
        return selectedMovieTicketType;
    }

    public void setSelectedMovieTicketType(int selectedMovieTicketType) {
        this.selectedMovieTicketType = selectedMovieTicketType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ArrayList<Address> getAddressList() {
        return addressList;
    }

    public void setAddressList(ArrayList<Address> addressList) {
        this.addressList = addressList;
    }

    public ArrayList<PaidOrders> getPaidOrdersList() {
        return paidOrdersList;
    }

    public void setPaidOrdersList(ArrayList<PaidOrders> paidOrdersList) {
        this.paidOrdersList = paidOrdersList;
    }
    
    
}
