/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ptrack
 */

@XmlRootElement(name = "request")
public class UssdRequest {

    private String msisdn;
    private Freeflow freeflow;
    private String type;
    private String sessionId;
    private String newRequest;
    private String subscriberInput;

    @XmlElement
    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    @XmlElement
    public Freeflow getFreeflow() {
        return freeflow;
    }

    public void setFreeflow(Freeflow freeflow) {
        this.freeflow = freeflow;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the newRequest
     */
    @XmlElement
    public String getNewRequest() {
        return newRequest;
    }

    /**
     * @param newRequest the newRequest to set
     */
    public void setNewRequest(String newRequest) {
        this.newRequest = newRequest;
    }
    
    @Override
    public String toString() {
        return type + "|" + msisdn + "|" + freeflow + "|" + sessionId+ "|" + newRequest +"|"+subscriberInput;
    }

    /**
     * @return the subscriberInput
     */
    @XmlElement
    public String getSubscriberInput() {
        return subscriberInput;
    }

    /**
     * @param subscriberInput the subscriberInput to set
     */
    public void setSubscriberInput(String subscriberInput) {
        this.subscriberInput = subscriberInput;
    }
}
