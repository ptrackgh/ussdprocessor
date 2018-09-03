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
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author ptrack
 */
public class USSDSessionHandler {

    public enum MessageKey {
        //ARAD_SELECT_COMPANY=Please select the company:
//        ARAD_SELECT_ITENARY=Please select travel itenary:
//        ARAD_SELECT_TIME=Please choose travel time:
        INTERNAL_ERROR, INVALID_AMOUNT, UNKNOWN_MERCHANT, ENTER_AMOUNT, WELCOME_MESSAGE, CONFIRM_TRANSACTION, TRANSACTION_ABORTED,
        ENTER_ACCOUNT_NO, ACCOUNT_NOT_FOUND, TRANSACTION_IN_PROGRESS, ARAD_MAIN_MENU, INVALID_OPTION, ARAD_SELECT_COMPANY,ARAD_SELECT_ITENARY, ARAD_SELECT_DEPARTURE,
        ARAD_ENTER_DEPARTURE_DATE, ARAD_SELECT_DEPARTURE_TIME, ARAD_NO_OF_PERSONS, ARAD_CONFIRM_PURCHASE, CANCEL_TRANSACTION,ARAD_SELECT_TIME, ARAD_SELECT_DESTINATION,
        ARAD_TRANSACTION_PROCESSING, DEPARTURE_DATE_IS_BEFORE_NOW, INVALID_DATE, DEPARTURE_AND_DESTINATION_SAME, ARAD_REQUEST_FAILED, TRANSACTION_FAILED,
        TVM_MAIN_MENU,TVM_IMPORT_TYPE,TVM_VEHICLE_REG,TVM_YEAR,TVM_CONFIRMATiON,TVM_CALCULATE_TAX_URL,
        MERCHANT_DETAILS_USERNAME,MERCHANT_DETAILS_PASSWORD,MERCHANT_DETAILS_URL,REQUEST_PAYMENT_URL,ARAD_AGENCY_LIST_URL,ARAD_TRAVEL_ITENARY_URL,ARAD_TRAVEL_TIMES_URL,
        //Saphir menu keys
        SAPHIR_MAIN_MENU,SAPHIR_CHOOSE_METHOD,SAPHIR_ENTER_ACCOUNT,SAPHIR_ENTER_MSISDN,SAPHIR_CHOOSE_FUND,SAPHIR_ENTER_AMOUNT,SAPHIR_CONFIRM_PAYMENT,SAPHIR_NO_ACCOUNT,
        SAPHIR_GET_ACCOUNT_URL,SAPHIR_COMMON_FUND_URL,
        BPS_ENTER_AMOUNT,BPS_MAIN_MENU,BPS_CONFIRM_TRANSACTION,INVALID_PHONE_NO
    }

    public static final ConcurrentHashMap<String, SubscriberInfo> activeSessions = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ZexpressInfo> activeSessions_Zex = new ConcurrentHashMap<>();
//    private static final String merchantDetails_URL = "http://74.208.83.82:8221/QosicBridge/user/merchantsbycode/";
//    private static final String requestPayment_URL = "https://74.208.83.82:8443/QosicBridge/user/requestpayment";
    
    private final UssdRequest request;
    private final Pattern datePattern = Pattern.compile("^\\d{8}$");
    //

    public enum ARAD_MENUS {
        RESERVATION, STATUS, REPORT
    }
    
    public enum ZEXPRESS_MENUS {
        GAZ, REPAS, FLEUR, TICKET_CINEMA
    }

    public USSDSessionHandler(UssdRequest request) {
        this.request = request;
    }

    public UssdResponse processRequest() {
        if ((null != request.getNewRequest() && request.getNewRequest().equals(UssdConstants.NEW_REQUEST))
                || !activeSessions.containsKey(request.getMsisdn())) {
            return sendWelcomeMessage();
        }
        //get the subscriber info if this is an existing subc
        final SubscriberInfo sub = activeSessions.get(request.getMsisdn());
        final ZexpressInfo zex = activeSessions_Zex.get(request.getMsisdn());
//        final UssdResponse resp = new UssdResponse();
        //resp.setMsisdn(request.getMsisdn());
        if (sub.isIsAradMenu()) {
            return new AradMenus().processRequest(sub, request);
            //return new ZexpressMenus().processRequest(sub, request);
//            switch (sub.getMenuLevel()) {
//                case 2:
//                    return processAradLevel2Menu(sub);
//                case 3:
//                    return processAradLevel3Menu(sub);
//                case 4:
//                    return processAradLevel4Menu(sub);
//                case 5:
//                    return processAradLevel5Menu(sub);
//                case 6:
//                    return processAradLevel6Menu(sub);
//                case 7:
//                    return processAradLevel7Menu(sub);
//                case 8:
//                    return processAradLevel8Menu(sub);
//                case 9:
//                    return processAradLevel9Menu(sub);
//                default:
//                    final UssdResponse resp = new UssdResponse();
//                    resp.setMsisdn(request.getMsisdn());
//                    final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.INTERNAL_ERROR.toString());
//                    resp.setApplicationResponse(respMessage);
//                    resp.setFreeflow(UssdConstants.BREAK);
//                    activeSessions.remove(request.getMsisdn());
//                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
//                    return resp;
//            }
        } else if(null != sub.getMerchantCode() && sub.getMerchantCode().equalsIgnoreCase("Saphir")){
            return new SaphirMenus().processRequest(sub, request);
        } else if(null != sub.getMerchantCode() && sub.getMerchantCode().equalsIgnoreCase("TVM")){
            return new TaxMenus().processRequest(sub, request);
        } else if(null != sub.getMerchantCode() && sub.getMerchantCode().equalsIgnoreCase("AAVIE")){
            return new AfricaineMenus().processRequest(sub, request);
        }else if(null != sub.getMerchantCode() && sub.getMerchantCode().equalsIgnoreCase("ZEXPRESS")){
            return new ZexpressMenus().processRequest(zex, request);
        } else if(null != sub.getMerchantCode() && sub.getMerchantCode().equalsIgnoreCase("BPS")){
            return new BPS().processRequest(sub, request);
        }else if(null != sub.getMerchantCode() && sub.getMerchantCode().equalsIgnoreCase("EG")){
            return new EugenioMenus().processRequest(sub, request);
        }
        else {
            switch (sub.getMenuLevel()) {
                case 1:
                    return processLevel1Menu(sub, zex);
                case 2:
                    return processLevel2Menu(sub);
                case 3:
                    return processLevel3Menu(sub);
                default:
                    final UssdResponse resp = new UssdResponse();
                    resp.setMsisdn(request.getMsisdn());
                    final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.INTERNAL_ERROR.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }
    }

    private UssdResponse sendWelcomeMessage() {
        //final SubscriberInfo sub = createNewSubscriber(request.getMsisdn());
        Logger.getLogger("qos_ussd_processor").info("sending welcome message to: " + request.getMsisdn());
        final SubscriberInfo sub = new SubscriberInfo();
        sub.setMenuLevel(1);
        sub.setMsisdn(request.getMsisdn());
        activeSessions.put(request.getMsisdn(), sub);
        final ZexpressInfo zex = new ZexpressInfo();
        zex.setMenuLevel(1);
        zex.setMsisdn(request.getMsisdn());
        activeSessions_Zex.put(request.getMsisdn(), zex);
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(MessageKey.WELCOME_MESSAGE.toString()));
        resp.setFreeflow(UssdConstants.CONTINUE);
        return resp;
    }

    private UssdResponse processLevel1Menu(SubscriberInfo sub, ZexpressInfo zex) {
        //check the merchant code entered by the user:
        if (request.getSubscriberInput().equalsIgnoreCase("ARAD")) {
            return processAradLevel1Menu(sub);
        }
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String merchantName = new HTTPUtil().retrieveMerchantByCode(request.getSubscriberInput(), sub);
        sub.setMerchantName(merchantName.toUpperCase());
        sub.setMerchantCode(request.getSubscriberInput().toUpperCase());
        //sub.setMsisdn(re);
        if (merchantName.equals("")) {
            Logger.getLogger("qos_ussd_processor").info("could not find merchant with code: " + request.getSubscriberInput());
            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.UNKNOWN_MERCHANT.toString())
                    .replace("{MERCHANT_CODE}", request.getSubscriberInput());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        } else if (request.getSubscriberInput().equalsIgnoreCase("BPS")) {
            sub.setMerchantName(merchantName.toUpperCase());
            sub.setMerchantCode(request.getSubscriberInput().toUpperCase()); 
            return new BPS().showMainMenu(sub);
        } else if (request.getSubscriberInput().equalsIgnoreCase("TVM")) {
            return new TaxMenus().showMainMenu(sub);
        } else if (request.getSubscriberInput().equalsIgnoreCase("AAVIE")) {
            return new AfricaineMenus().showMainMenu(sub);
        } else if (request.getSubscriberInput().equalsIgnoreCase("Saphir")) {
            return new SaphirMenus().showMainMenu(sub);
        } else if (request.getSubscriberInput().equalsIgnoreCase("ZEXPRESS")) {
            zex.setMerchantName(merchantName.toUpperCase());
            zex.setMerchantCode(request.getSubscriberInput().toUpperCase());
            return new ZexpressMenus().showMainMenu(zex);
        }else if(request.getSubscriberInput().equalsIgnoreCase("EG")){
            sub.setMerchantName(merchantName.toUpperCase());
            sub.setMerchantCode(request.getSubscriberInput().toUpperCase());
            return new EugenioMenus().showMainMenu(sub);
        } else {
            Logger.getLogger("qos_ussd_processor").info(String.format("{%s} found merchant {%s} with code {%s}",
                    request.getMsisdn(), merchantName, request.getSubscriberInput()));
//            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.ENTER_ACCOUNT_NO.toString())
//                    .replace("{MERCHANT_NAME}", merchantName);
            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.ENTER_AMOUNT.toString())
                    .replace("{MERCHANT_NAME}", merchantName);
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.setMerchantCode(request.getSubscriberInput());
            sub.setMerchantName(merchantName);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
            return resp;
        }
    }

//    private UssdResponse processLevel2Menu(SubscriberInfo sub) {
//        final UssdResponse resp = new UssdResponse();
//        resp.setMsisdn(request.getMsisdn());
//        final String accountdetails = retrieveAccountDetails(request.getSubscriberInput());
//        if (accountdetails.equals("")) {
//            Logger.getLogger("qos_ussd_processor").info("could not find merchant with code: " + request.getSubscriberInput());
//            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.ACCOUNT_NOT_FOUND.toString())
//                    .replace("{MERCHANT_NAME}", sub.getMerchantName()).replace("{ACCOUNT_NO}", request.getSubscriberInput());
//            resp.setApplicationResponse(respMessage);
//            resp.setFreeflow(UssdConstants.BREAK);
//            activeSessions.remove(request.getMsisdn());
//            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
//            return resp;
//        } else {
//            Logger.getLogger("qos_ussd_processor").info(String.format("{%s} found accountholder {%s} with accountNo {%s}",
//                    request.getMsisdn(), accountdetails, request.getSubscriberInput()));
//            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.ENTER_AMOUNT.toString())
//                    .replace("{ACCOUNT_DETAILS}", accountdetails);
//            resp.setApplicationResponse(respMessage);
//            resp.setFreeflow(UssdConstants.CONTINUE);
//            sub.setAccountNo(request.getSubscriberInput());
//            sub.setAccountDetails(accountdetails);
//            sub.incrementMenuLevel();
//            activeSessions.put(request.getMsisdn(), sub);
//            return resp;
//        }
//    }

    private UssdResponse processLevel2Menu(SubscriberInfo sub) {
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        try {
            double amount = Double.parseDouble(request.getSubscriberInput());
            sub.setAmount(new BigDecimal(amount));
            Logger.getLogger("qos_ussd_processor").info(String.format("{%s} entered {%s} amount",
                    request.getMsisdn(), request.getSubscriberInput()));
            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.CONFIRM_TRANSACTION.toString())
                    .replace("{AMOUNT}", amount + " XOF").replace("{MERCHANT_NAME}", sub.getMerchantName());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.setAccountNo(request.getSubscriberInput());
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
            return resp;
        } catch (NumberFormatException ex) {
            Logger.getLogger("qos_ussd_processor").info(request.getMsisdn() + " entered invalid amount : " + request.getSubscriberInput());
            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.INVALID_AMOUNT.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }

    private UssdResponse processLevel3Menu(SubscriberInfo sub) {
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        //final String accountdetails= retrieveAccountDetails(request.getSubscriberInput());
        if (!request.getSubscriberInput().equals("1")) {
            Logger.getLogger("qos_ussd_processor").info("user has declined transaction by entering: " + request.getSubscriberInput());
            final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.TRANSACTION_ABORTED.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        } else {
            Logger.getLogger("qos_ussd_processor").info(String.format("{%s} has confirmed this transaction. Going to process request",
                    request.getMsisdn()));
            final String respMessage = processPayment(sub);
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private String retrieveAccountDetails(String subscriberInput) {
        return "Test User " + subscriberInput.toUpperCase() + " (2000 XOF)";
    }

    private String processPayment(SubscriberInfo sub) {
        //final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.TRANSACTION_IN_PROGRESS.toString());
        Logger.getLogger("qos_ussd_processor").info("processing transaction " + request.getMsisdn());
        JsonObject requestPayment = new JsonObject();
        requestPayment.addProperty("msisdn", sub.getMsisdn());
        requestPayment.addProperty("amount", sub.getAmount());
        requestPayment.addProperty("transref", request.getSessionId());
        requestPayment.addProperty("clientid", sub.getMerchantCode());

        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
        if (response.equals("")) {
            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
            return UssdConstants.MESSAGES.getProperty(MessageKey.TRANSACTION_FAILED.toString());
        } else {
            JsonParser parseResponse = new JsonParser();
            JsonObject jo = (JsonObject) parseResponse.parse(response);
            if (jo.get("responsecode").getAsString().equals("01")) {
//                respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.BREAK);
                return UssdConstants.MESSAGES.getProperty(MessageKey.TRANSACTION_IN_PROGRESS.toString())
                        .replace("{AMOUNT}", sub.getAmount().toString()).replace("{MERCHANT_NAME}", sub.getMerchantName());
            } else {
//                respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.ARAD_REQUEST_FAILED.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.BREAK);
                return UssdConstants.MESSAGES.getProperty(MessageKey.TRANSACTION_FAILED.toString());

            }
        }
//        activeSessions.remove(request.getMsisdn());
//        return resp;
//        return respMessage;
    }

    private UssdResponse processAradLevel1Menu(SubscriberInfo sub) {
        sub.setIsAradMenu(true);// = true;
        sub.setMerchantCode("ARAD");
        Logger.getLogger("qos_ussd_processor").info("Arad menu level1 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.ARAD_MAIN_MENU.toString());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        activeSessions.put(request.getMsisdn(), sub);
        return resp;
    }

}
