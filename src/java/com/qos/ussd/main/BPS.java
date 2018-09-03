/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qos.ussd.dto.UssdRequest;
import com.qos.ussd.dto.UssdResponse;
import static com.qos.ussd.main.USSDSessionHandler.activeSessions;
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author ptrack
 */
public class BPS {

    private final Pattern phonePattern = Pattern.compile("^\\d{10,12}$");
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req) {
        Logger.getLogger("qos_ussd_processor").info("calling BPS menu @step: "+ sub.getMenuLevel());
        switch (sub.getMenuLevel()) {
            case 2:
                return handleEnterPhoneNumber(sub, req);
            case 3:
                return handleEnterAmount(sub, req);
            case 4:
                return handleConfirmTransaction(sub, req);
            default:
                final UssdResponse resp = new UssdResponse();
                resp.setMsisdn(req.getMsisdn());
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions.remove(req.getMsisdn());
                Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", req.getMsisdn()));
                return resp;
        }
    }

    private UssdResponse handleEnterPhoneNumber(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("processTaxImportTypeMenu for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        try {
            final String number = normalize(request.getSubscriberInput());
            if (null==number) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("PHONE_NUMBER", number);
            sub.setAccountNo(request.getSubscriberInput());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.BPS_ENTER_AMOUNT.toString()));
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_PHONE_NO.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        return resp;
    }

    private UssdResponse handleEnterAmount(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("handleEnterAmount for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        try {
            double amount = Double.parseDouble(request.getSubscriberInput());
            sub.setAmount(new BigDecimal(amount));
            //BPS_CONFIRM_TRANSACTION=Debit of {AMOUNT} from {PHONE_NUMBER}. Enter 1 to confirm:
            Logger.getLogger("qos_ussd_processor").info(String.format("{%s} entered {%s} amount",
                    request.getMsisdn(), request.getSubscriberInput()));
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.BPS_CONFIRM_TRANSACTION.toString())
                    .replace("{AMOUNT}", amount + " XOF").replace("{PHONE_NUMBER}", sub.getSubParams().get("PHONE_NUMBER").toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
            return resp;
        } catch (NumberFormatException ex) {
            Logger.getLogger("qos_ussd_processor").info(request.getMsisdn() + " entered invalid amount : " + request.getSubscriberInput());
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_AMOUNT.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }

    private UssdResponse handleConfirmTransaction(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("processTexEnterVehicleRegMenu for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        if (!request.getSubscriberInput().equals("1")) {
            Logger.getLogger("qos_ussd_processor").info("user has declined transaction by entering: " + request.getSubscriberInput());
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TRANSACTION_ABORTED.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        } else {
            Logger.getLogger("qos_ussd_processor").info(String.format("{%s} has confirmed this transaction. Going to process request",
                    request.getMsisdn()));
            final String respMessage = processPayment(sub, request.getSessionId());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private String processPayment(SubscriberInfo sub, String sessionid) {
        //final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.TRANSACTION_IN_PROGRESS.toString());
        Logger.getLogger("qos_ussd_processor").info("processing BPS transaction " + sub.getMsisdn());
        JsonObject requestPayment = new JsonObject();
        requestPayment.addProperty("msisdn", sub.getSubParams().get("PHONE_NUMBER").toString());
        requestPayment.addProperty("amount", sub.getAmount());
        requestPayment.addProperty("transref", sessionid);
        requestPayment.addProperty("clientid", sub.getMerchantCode());
        Logger.getLogger("qos_ussd_processor").info("sendRequestPayment body: "+requestPayment.toString());
        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
        if (response.equals("")) {
            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
            return UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TRANSACTION_FAILED.toString());
        } else {
            JsonParser parseResponse = new JsonParser();
            JsonObject jo = (JsonObject) parseResponse.parse(response);
            if (jo.get("responsecode").getAsString().equals("01")) {
                return UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TRANSACTION_IN_PROGRESS.toString())
                        .replace("{AMOUNT}", sub.getAmount().toString()).replace("{MERCHANT_NAME}", sub.getMerchantName());
            } else {
                return UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TRANSACTION_FAILED.toString());

            }
        }
    }
    
    UssdResponse showMainMenu(SubscriberInfo sub) {
        Logger.getLogger("qos_ussd_processor").info("showing tvm main menu to: " + sub.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(sub.getMsisdn());
        //BPS_MAIN_MENU=Welcome to {MERCHANT_NAME}. Enter phone number:
        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.BPS_MAIN_MENU.toString()) 
                .replace("{MERCHANT_NAME}", sub.getMerchantName());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        //sub.setMerchantName();
        activeSessions.put(sub.getMsisdn(), sub);
        return resp;
    }
    
    public String normalize(String number){
        if(number.length()==11 && number.startsWith("229")){
            return number;
        }else if(number.length()==8){
            return "229"+number;
        }else if(number.startsWith("00") && number.length()==13){
            return number.substring(2);
        }else if(number.startsWith("+") && number.length()==12){
            return number.substring(1);
        }else if(number.startsWith("0") && number.length()==9){
            return "229"+number.substring(1);
        }else{
            return null;
        }
    }

}
