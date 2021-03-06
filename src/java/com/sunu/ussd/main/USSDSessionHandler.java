/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sunu.ussd.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.qos.ussd.dto.UssdRequest;
import com.qos.ussd.dto.UssdResponse;
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import com.qos.ussd.util.sunu.dto.Contrat;
import com.qos.ussd.util.sunu.dto.Infocontrat;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author ptrack
 */
public class USSDSessionHandler {
    
    private UssdResponse processSunulevel2Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("sunu_ussd_processor").info("Sunu menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String listString;
        final Gson gson = new Gson();
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 4) {
                throw new NumberFormatException();
            }
            if(option > 1){
                final String respMessage = "Bientôt Disponible";
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions.remove(request.getMsisdn());
                return resp;
            }
            sub.getSubParams().put("MENU_CHOICE", option);
            
            String[] parts = request.getMsisdn().split("229");
            String number = parts[1]; // Phone Number without 229
            sub.getSubParams().put("NUMBER", number); 
            listString = new HTTPUtil().sendGetRequest("http://benin.vie.sunu-group.com/Site/mtnservice/func.php?r=contrats&number="+number);
            try {
                if(listString.isEmpty()){
                    final String msg = "Ce numéro n'est pas authorisé";
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("sunu_ussd_processor").info("unauthorized number supplied for sunu request: " + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                }else{
                    String[] partslistString = listString.split("ï»¿");
                    String json = partslistString[1];
                    final ArrayList<Contrat> user = gson.fromJson(json, new TypeToken<List<Contrat>>(){}.getType());
                    /*JsonParser parseResponse = new JsonParser();
                    JsonObject jo = (JsonObject) parseResponse.parse(listString);
                    Contrat[] usr = gson.fromJson(jo, Contrat[].class);*/
                    final StringBuilder msgResp = new StringBuilder("Liste de vos contrats :");
                    int i=0;
                    for(final Contrat contrat: user){
                        msgResp.append("\n").append(++i).append(". ").append(contrat.getLabel());
                    }
                    sub.getSubParams().put("CONTRATS", user);
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                }
                    
            } catch (JsonSyntaxException ex) {
                resp.setApplicationResponse("Numéro non authorisé");
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("sunu_ussd_processor").info("invalid details supplied for sunu request: " + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
            }
        } catch (JsonSyntaxException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("sunu_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        } catch (Exception ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        
        return resp;
    }
    
    private UssdResponse processSunulevel3Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Sunu menu level3 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        final Gson gson = new Gson();
        //final int option;
        final ArrayList<Contrat> options = (ArrayList<Contrat>) sub.getSubParams().get("CONTRATS");
        try {
            final int input = Integer.parseInt(request.getSubscriberInput());
            if (input < 1 || input > options.size()) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("CONTRAT_OPTION", options.get(input - 1));
            
            final String listString = new HTTPUtil().sendGetRequest("http://benin.vie.sunu-group.com/Site/mtnservice/func.php?r=infocontrat&id=" + options.get(input - 1).getId());
            
            if(listString.isEmpty()){
                final String msg = "Ce numéro n'est pas authorisé";
                resp.setApplicationResponse(msg);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("unauthorized number supplied for sunu request: " + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
            }else{
                String[] partslistString = listString.split("ï»¿");
                String json = partslistString[1];
                Infocontrat[] user = gson.fromJson(json, Infocontrat[].class);
                sub.getSubParams().put("INFOCONTRAT", user[0]);
                final String msg = "Affichage du détails du contrat "
                    + "\nAssuré : " +  user[0].getAssure()
                    + "\nProduit : " + user[0].getProduit()
                    + "\nPrime : " + user[0].getPrime()
                    + "\n\n#. Suivant";
                resp.setApplicationResponse(msg);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            }

        } catch (NumberFormatException ex) {
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }catch (JsonSyntaxException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }catch (Exception ex) {
            Logger.getLogger("processAradLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
        return resp;
    }
    
    private UssdResponse processSunulevel4Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("sunu_ussd_processor").info("Sunu menu level4 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        //final String respMessage;

        try {
            if (request.getSubscriberInput().equals("#")) {
                final String msg = "Veuillez entrer le montant que vous souhaitez payé:";
                resp.setApplicationResponse(msg);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            }else {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("sunu_ussd_processor").info("user opted to cancel transaction for sunu. user:" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
            }
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        } catch (Exception ex) {
            Logger.getLogger("sunu_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
            final String respMessage = "Votre paiement a echouee. Veuillez reessayer plus tard.";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
    }
    
    private UssdResponse processSunulevel5Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("sunu_ussd_processor").info("Sunu menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        final String listString;
        final Gson gson = new Gson();
        try {
            sub.getSubParams().put("AMOUNT", request.getSubscriberInput());
            final Contrat contrat = (Contrat) sub.getSubParams().get("CONTRAT_OPTION");
            listString = new HTTPUtil().sendGetRequest("http://benin.vie.sunu-group.com/Site/mtnservice/func.php?r=infocontrat&id="+contrat.getId());
            
            String[] partslistString = listString.split("ï»¿");
            String json = partslistString[1];
            try {
                if(json.isEmpty()){
                    resp.setApplicationResponse("Invalid Sunu details entered");
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("sunu_ussd_processor").info("invalid details supplied for sunu request: " + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                }else
                {
                    double amount = Integer.parseInt(sub.getSubParams().get("AMOUNT").toString());
                    final ArrayList<Infocontrat> user = gson.fromJson(json, new TypeToken<List<Infocontrat>>(){}.getType());
                    final Infocontrat infocontrat = user.get(0);
                    sub.getSubParams().put("INFOCONTRAT", infocontrat);
                    sub.setAmount(new BigDecimal(amount));
                    final String msg = "M. {NAME}\nPolice : {CONTRAT}\nMontant : {AMOUNT}FCFA\n\nVoulez vous procéder au paiement?\n1. Oui \n2. Non";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_CONFIRMATiON.toString()).replace("{AMOUNT}", response);
                    resp.setApplicationResponse(msg.replace("{NAME}", infocontrat.getAssure()).replace("{CONTRAT}", contrat.getLabel()).replace("{AMOUNT}", sub.getSubParams().get("AMOUNT").toString()));
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                }
                    
            } catch (JsonSyntaxException | NumberFormatException ex) {
                resp.setApplicationResponse("Invalid Sunu details entered");
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("sunu_ussd_processor").info("invalid details supplied for sunu request: " + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
            }
        }  catch (JsonSyntaxException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        } catch (Exception ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        return resp;
    }
    
    private UssdResponse processSunulevel6Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("sunu_ussd_processor").info("Sunu menu level6 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        //final String respMessage;

        try {
            //retrieve Merchant Name
            final String merchantName = new HTTPUtil().retrieveMerchantByCode("SUNU", sub);
            sub.setMerchantName(merchantName.toUpperCase());
            sub.setMerchantCode("SUNU".toUpperCase());
            
            final int option = Integer.parseInt(request.getSubscriberInput());
            final Contrat contrat = (Contrat) sub.getSubParams().get("CONTRAT_OPTION");
            if (option == 1) {
                //make reservation

                Logger.getLogger("sunu_ussd_processor").info("processing sunu transaction for{" + sub.toString() + "} by" + request.getMsisdn());
                JsonObject requestPayment = new JsonObject();
                requestPayment.addProperty("msisdn", sub.getMsisdn());
                requestPayment.addProperty("amount", sub.getAmount());

                final StringBuilder transref = new StringBuilder();
                transref.append("request=").append(sub.getSubParams().get("MENU_CHOICE").toString()).append("|")
                        .append("request=").append(contrat.getId()).append("|")
                        .append("amount=").append(sub.getAmount()).append("|")
                        .append("sessionid=").append(request.getSessionId());
                requestPayment.addProperty("transref", request.getSessionId());
                requestPayment.addProperty("specialfield1", transref.toString());
                requestPayment.addProperty("clientid", sub.getMerchantCode());

                final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                if (response.equals("")) {
                    Logger.getLogger("sunu_ussd_processor").info("sendRequestPayment returned empty response");
                    final String respMessage = "Votre paiement a echouee. Veuillez reessayer plus tard.";
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                } else {
                    JsonParser parseResponse = new JsonParser();
                    JsonObject jo = (JsonObject) parseResponse.parse(response);
                    if (jo.get("responsecode").getAsString().equals("01")) {
                        final String respMessage = "Votre paiement est en cours. Vous recevrez une demande de confirmation dans un instant.";
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                    } else {
                        final String respMessage = "Votre paiement a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                    }
                }
                activeSessions.remove(request.getMsisdn());
                return resp;
            }else {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("sunu_ussd_processor").info("user opted to cancel transaction for sunu. user:" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
            }
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        } catch (JsonSyntaxException ex) {
            Logger.getLogger("sunu_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
            final String respMessage = "Votre paiement a echouee. Veuillez reessayer plus tard.";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("sunu_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
    }
//    
//    UssdResponse showMainMenu(SubscriberInfo sub) {
//        Logger.getLogger("sunu_ussd_processor").info("showing sunu main menu to: " + sub.getMsisdn());
//        final UssdResponse resp = new UssdResponse();
//        resp.setMsisdn(sub.getMsisdn());
//        final String respMessage = "Veuillez choisir une opération : \n1. Paiement primes\n2. Souscription\n3. Demande de prestation\n4. Autres";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_MAIN_MENU.toString());
//        resp.setApplicationResponse(respMessage);
//        resp.setFreeflow(UssdConstants.CONTINUE);
//        sub.incrementMenuLevel();
//        //sub.setMerchantName();
//        activeSessions.put(sub.getMsisdn(), sub);
//        return resp;
//    }
    
    private UssdResponse showMainMenu() {
        //final SubscriberInfo sub = createNewSubscriber(request.getMsisdn());
        Logger.getLogger("sunu_ussd_processor").info("showing sunu main menu to: " + request.getMsisdn());
        final SubscriberInfo sub = new SubscriberInfo();
        sub.setMenuLevel(1);
        sub.setMsisdn(request.getMsisdn());
        activeSessions.put(request.getMsisdn(), sub);
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String respMessage = "Veuillez choisir une opération : \n1. Paiement primes\n2. Souscription\n3. Demande de prestation\n4. Autres";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_MAIN_MENU.toString());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        return resp;
    }

    public enum MessageKey {
        //ARAD_SELECT_COMPANY=Please select the company:
//        ARAD_SELECT_ITENARY=Please select travel itenary:
//        ARAD_SELECT_TIME=Please choose travel time:
        INTERNAL_ERROR, INVALID_AMOUNT, UNKNOWN_MERCHANT, ENTER_AMOUNT, WELCOME_MESSAGE, CONFIRM_TRANSACTION, TRANSACTION_ABORTED,
        ENTER_ACCOUNT_NO, ACCOUNT_NOT_FOUND, TRANSACTION_IN_PROGRESS, ARAD_MAIN_MENU, INVALID_OPTION, ARAD_SELECT_COMPANY, ARAD_SELECT_ITENARY, ARAD_SELECT_DEPARTURE,
        ARAD_ENTER_DEPARTURE_DATE, ARAD_SELECT_DEPARTURE_TIME, ARAD_NO_OF_PERSONS, ARAD_CONFIRM_PURCHASE, CANCEL_TRANSACTION, ARAD_SELECT_TIME, ARAD_SELECT_DESTINATION,
        ARAD_TRANSACTION_PROCESSING, DEPARTURE_DATE_IS_BEFORE_NOW, INVALID_DATE, DEPARTURE_AND_DESTINATION_SAME, ARAD_REQUEST_FAILED, TRANSACTION_FAILED,
        TVM_MAIN_MENU, TVM_IMPORT_TYPE, TVM_VEHICLE_REG, TVM_YEAR, TVM_CONFIRMATiON, TVM_CALCULATE_TAX_URL,
        MERCHANT_DETAILS_USERNAME, MERCHANT_DETAILS_PASSWORD, MERCHANT_DETAILS_URL, REQUEST_PAYMENT_URL, ARAD_AGENCY_LIST_URL, ARAD_TRAVEL_ITENARY_URL, ARAD_TRAVEL_TIMES_URL,
        //Saphir menu keys
        SAPHIR_MAIN_MENU, SAPHIR_CHOOSE_METHOD, SAPHIR_ENTER_ACCOUNT, SAPHIR_ENTER_MSISDN, SAPHIR_CHOOSE_FUND, SAPHIR_ENTER_AMOUNT, SAPHIR_CONFIRM_PAYMENT, SAPHIR_NO_ACCOUNT,
        SAPHIR_GET_ACCOUNT_URL, SAPHIR_COMMON_FUND_URL,
        BPS_ENTER_AMOUNT, BPS_MAIN_MENU, BPS_CONFIRM_TRANSACTION, INVALID_PHONE_NO
    }

    public static final ConcurrentHashMap<String, SubscriberInfo> activeSessions = new ConcurrentHashMap<>();
//    private static final String merchantDetails_URL = "http://74.208.83.82:8221/QosicBridge/user/merchantsbycode/";
//    private static final String requestPayment_URL = "https://74.208.83.82:8443/QosicBridge/user/requestpayment";

    private final UssdRequest request;
    private final Pattern datePattern = Pattern.compile("^\\d{8}$");

    public USSDSessionHandler(UssdRequest request) {
        this.request = request;
    }

    public UssdResponse processRequest() {
        if ((null != request.getNewRequest() && request.getNewRequest().equals(UssdConstants.NEW_REQUEST))
                || !activeSessions.containsKey(request.getMsisdn())) {
            return showMainMenu();
        }
        //get the subscriber info if this is an existing subc
        final SubscriberInfo sub = activeSessions.get(request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());

        switch (sub.getMenuLevel()) {
            case 1:
                return processSunulevel2Menu(sub, request);
            case 2:
                return processSunulevel3Menu(sub, request);
            case 3:
                return processSunulevel4Menu(sub, request);
            case 4:
                return processSunulevel5Menu(sub, request);
            case 5:
                return processSunulevel6Menu(sub, request);
            default:
                resp.setMsisdn(request.getMsisdn());
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions.remove(request.getMsisdn());
                Logger.getLogger("sunu_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                return resp;
        }
    }
}


