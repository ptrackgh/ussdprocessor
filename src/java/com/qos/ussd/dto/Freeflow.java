/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ptrack
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Freeflow {

//    
//    public static final String CONTINUE = "FC";
//    public static final String BREAK = "FB";
    //<freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0</freeflowChargingAmount>
    
    @XmlElement
    private String mode;
    @XmlElement
    private String freeflowState="FB";
    @XmlElement
    private int freeflowChargingAmount=0;
    @XmlElement
    private String freeflowCharging="N";

    //@XmlElement
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Freeflow(String state) {
        this.freeflowState = state;
    }
    
    public Freeflow() {
    }

    @Override
    public String toString() {
        return "Freeflow [" + mode + "|"+ freeflowCharging + "|"+ freeflowChargingAmount + "|"+ freeflowState + "]";
    }

    /**
     * @return the freeflowChargingAmount
     */
    //@XmlElement
    public int getFreeflowChargingAmount() {
        return freeflowChargingAmount;
    }

    /**
     * @param freeflowChargingAmount the freeflowChargingAmount to set
     */
    public void setFreeflowChargingAmount(int freeflowChargingAmount) {
        this.freeflowChargingAmount = freeflowChargingAmount;
    }

    /**
     * @return the freeflowCharging
     */
    //@XmlElement
    public String getFreeflowCharging() {
        return freeflowCharging;
    }

    /**
     * @param freeflowCharging the freeflowCharging to set
     */
    public void setFreeflowCharging(String freeflowCharging) {
        this.freeflowCharging = freeflowCharging;
    }

    /**
     * @return the freeflowState
     */
    //@XmlElement
    public String getFreeflowState() {
        return freeflowState;
    }

    /**
     * @param freeflowState the freeflowState to set
     */
    public void setFreeflowState(String freeflowState) {
        this.freeflowState = freeflowState;
    }
}
