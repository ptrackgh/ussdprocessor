/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.main;

//import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.qos.ussd.dto.UssdRequest;
import com.qos.ussd.dto.UssdResponse;
import static com.qos.ussd.main.USSDSessionHandler.activeSessions;
//import static com.qos.ussd.main.USSDSessionHandler.activeSessions_Zex;
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
//import com.qos.ussd.util.arad.dto.TVMResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author Malkiel
 */
public class AfricaineMenus {
    private final Pattern datePattern = Pattern.compile("^\\d{8}$");
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req) {
        Logger.getLogger("qos_ussd_processor").info("calling Africaine menu @step: "+ sub.getMenuLevel());
        switch (sub.getMenuLevel()) {
            case 2:
                return processAfricainelevel2Menu(sub, req);
            case 3:
                return processAfricainelevel3Menu(sub, req);
            case 4:
                return processAfricainelevel4Menu(sub, req);
            case 5:
                return processAfricainelevel5Menu(sub, req);
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
    
    private UssdResponse processAfricainelevel2Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Africaine menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 3) {
                throw new NumberFormatException();
            }
            if(option == 3){
                final String respMessage = "Bientôt Disponible";
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions.remove(request.getMsisdn());
                return resp;
            }
            sub.getSubParams().put("MENU_CHOICE", option);
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
        
        final String respMessage = "Veuillez choisir le type de cotisation: \n1. Paiement Mensuel \n2. Paiement Annuel";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_MAIN_MENU.toString());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        activeSessions.put(sub.getMsisdn(), sub);
        return resp;
    }
    
    private UssdResponse processAfricainelevel3Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Africaine menu level3 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 2) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("PAYMENT_TYPE", UssdConstants.AFRICAINE_PAYMENT_TYPE[option]);
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
        
        try {
            String[] parts = request.getMsisdn().split("229");
            String number = parts[1]; // Phone Number without 229
            sub.getSubParams().put("NUMBER", number); 
            JsonObject getAuthorizationPayment = new JsonObject();
            getAuthorizationPayment.addProperty("tel", sub.getSubParams().get("NUMBER").toString());
            getAuthorizationPayment.addProperty("type", sub.getSubParams().get("PAYMENT_TYPE").toString());
            final String response = new HTTPUtil().getList(getAuthorizationPayment, "https://africainevieonline.com/wanrou/public/tel_mm.php");
            sub.getSubParams().put("JSON_RESPONSE", response);
            /*JsonParser parseResponse = new JsonParser();
            JsonObject jo = (JsonObject) parseResponse.parse(response);*/
            String[] rm = response.split("\"");
            String[] data = rm[1].split("-");
            String[] solde = data[1].split("=");
            try {
                final int id = Integer.parseInt(data[0]); //id
                if(id != 0){
                    if(Integer.parseInt(sub.getSubParams().get("MENU_CHOICE").toString()) == 1){
                        resp.setApplicationResponse("Veuillez entrer le montant que vous souhaitez payer:"/*UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_IMPORT_TYPE.toString())*/);
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    }else{
                        final String msg = "Solde: " + solde[1] + "F";
                        resp.setApplicationResponse(msg);
                        resp.setFreeflow(UssdConstants.BREAK);
                        activeSessions.remove(request.getMsisdn());
                    }
                }else{
                    final String msg = data[1];
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("unauthorized number supplied for africaine request: " + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                }
            } catch (NumberFormatException ex) {
                resp.setApplicationResponse("Invalid Africaine details entered");
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid details supplied for africaine request: " + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
            }
        } catch (Exception ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        
        return resp;
    }
    
    private UssdResponse processAfricainelevel4Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Africaine menu level4 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        try {
            sub.getSubParams().put("AMOUNT", request.getSubscriberInput());
            final String response = sub.getSubParams().get("JSON_RESPONSE").toString();
            /*JsonParser parseResponse = new JsonParser();
            JsonObject jo = (JsonObject) parseResponse.parse(response);*/
            String[] rm = response.split("\"");
            String[] data = rm[1].split("-");
            try {
                final int id = Integer.parseInt(data[0]); //id
                if(id != 0){
                    double amount = Integer.parseInt(sub.getSubParams().get("AMOUNT").toString());
                    //sub.getSubParams().put("AMOUNT", amount);
                    sub.setAmount(new BigDecimal(amount));
                    final String msg = "Vous souhaitez effectuer un paiement de {AMOUNT}FCFA. Voulez vous procéder au paiement?\n1. Oui \n2. Non";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_CONFIRMATiON.toString()).replace("{AMOUNT}", response);
                    resp.setApplicationResponse(msg.replace("{AMOUNT}", sub.getSubParams().get("AMOUNT").toString()));
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                }else{
                    final String msg = data[1];
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("unauthorized number supplied for africaine request: " + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                }
            } catch (NumberFormatException ex) {
                resp.setApplicationResponse("Invalid Africaine details entered");
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid details supplied for africaine request: " + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
            }
        } catch (Exception ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        return resp;
    }
    
    private UssdResponse processAfricainelevel5Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Africaine menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        //final String respMessage;

        try {
            final int option = Integer.parseInt(request.getSubscriberInput());
            if (option == 1) {
                //make reservation

                Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + sub.toString() + "} by" + request.getMsisdn());
                JsonObject requestPayment = new JsonObject();
                requestPayment.addProperty("msisdn", sub.getMsisdn());
                requestPayment.addProperty("amount", sub.getAmount());

                final StringBuilder transref = new StringBuilder();
                transref.append("tel=").append(sub.getSubParams().get("NUMBER").toString()).append("|")
                        .append("amount=").append(sub.getAmount()).append("|")
                        .append("type=").append(sub.getSubParams().get("PAYMENT_TYPE").toString()).append("|")
                        .append("sessionid=").append(request.getSessionId());
                requestPayment.addProperty("transref", request.getSessionId());
                requestPayment.addProperty("specialfield1", transref.toString());
                requestPayment.addProperty("clientid", sub.getMerchantCode());

                final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                if (response.equals("")) {
                    Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
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
                Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for zexpress. user:" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
            }
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        } catch (JsonSyntaxException ex) {
            Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
            final String respMessage = "Votre paiement a echouee. Veuillez reessayer plus tard.";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
    }
    
    UssdResponse showMainMenu(SubscriberInfo sub) {
        Logger.getLogger("qos_ussd_processor").info("showing africaine main menu to: " + sub.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(sub.getMsisdn());
        final String respMessage = "Veuillez choisir le type de cotisation: \n1. Depôt \n2. Consulter solde \n3. Retrait";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_MAIN_MENU.toString());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        //sub.setMerchantName();
        activeSessions.put(sub.getMsisdn(), sub);
        return resp;
    }
}
