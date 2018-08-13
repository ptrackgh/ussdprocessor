/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.qos.ussd.dto.UssdRequest;
import com.qos.ussd.dto.UssdResponse;
import static com.qos.ussd.main.AradMenus.arad_agency_list_url;
import static com.qos.ussd.main.USSDSessionHandler.activeSessions;
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import com.qos.ussd.util.arad.dto.Agency;
import com.qos.ussd.util.eugenio.dto.Bouquet;
import com.qos.ussd.util.eugenio.dto.Company;
import com.qos.ussd.util.eugenio.dto.Destination;
import com.qos.ussd.util.eugenio.dto.Subscription;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author Malkiel
 */
public class EugenioMenus {
    private final Pattern datePattern = Pattern.compile("^\\d{8}$");
    private final Pattern phonePattern = Pattern.compile("^\\d{8}$");
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req) {
        Logger.getLogger("qos_ussd_processor").info("calling BPS menu @step: "+ sub.getMenuLevel());
        switch (sub.getMenuLevel()) {
            case 2:
                return processEugenioLevel2Menu(sub, req);
            case 3:
                return processEugenioLevel3Menu(sub, req);
            case 4:
                return processEugenioLevel4Menu(sub, req);
            case 5:
                return processEugenioLevel5Menu(sub, req);
            case 6:
                return processEugenioLevel6Menu(sub, req);
            case 7:
                return processEugenioLevel7Menu(sub, req);
            case 8:
                return processEugenioLevel8Menu(sub, req);
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
    
    private UssdResponse processEugenioLevel2Menu(SubscriberInfo sub, UssdRequest request){
        Logger.getLogger("qos_ussd_processor").info("Eugenio menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 8) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("OPTION", option);
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
        
        final String listString;
        final Gson gson = new Gson();
        
        switch (option) {
            case 1:
            case 2:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Veuillez entrer le numéro de téléphone:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 3:
                listString = new HTTPUtil().sendGetRequest("http://eugeniobusness.com/mtn/app/view/users/iwiApi.php");
                //logs = gson.fromJson(agencyListString, new TypeToken<List<Agency>>(){}.getType());
                //final ArrayList<Agency> user = gson.fromJson(agencyListString, ArrayList.class);
                try{
                    final ArrayList<Bouquet> user = gson.fromJson(listString, new TypeToken<List<Bouquet>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder("Veuillez choisir le bouquet qui vous convent: ");
                    int i=0;
                    for(final Bouquet bouquet: user){
                        msgResp.append("\n").append(++i).append(". ").append(bouquet.getDuree()).append(" => ").append(bouquet.getAmount()).append("F");
                    }
                    sub.getSubParams().put("BOUQUET_IWI", user);
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel2Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 4:
                listString = new HTTPUtil().sendGetRequest("http://eugeniobusness.com/mtn/app/view/users/canalApi.php");
                //logs = gson.fromJson(agencyListString, new TypeToken<List<Agency>>(){}.getType());
                //final ArrayList<Agency> user = gson.fromJson(agencyListString, ArrayList.class);
                try{
                    final ArrayList<Bouquet> user = gson.fromJson(listString, new TypeToken<List<Bouquet>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder("Veuillez choisir le bouquet qui vous convent: ");
                    int i=0;
                    for(final Bouquet bouquet: user){
                        msgResp.append("\n").append(++i).append(". ").append(bouquet.getDuree()).append(" => ").append(bouquet.getAmount()).append("F");
                    }
                    sub.getSubParams().put("BOUQUET_CANAL", user);
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel2Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 5:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Veuillez entrer le numéro compteur:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 6:
            case 7:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Veuillez entrer le numéro de police:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 8:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Veuillez entrer votre nom et prenom:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }
    
    private UssdResponse processEugenioLevel3Menu(SubscriberInfo sub, UssdRequest request){
        Logger.getLogger("qos_ussd_processor").info("Eugenio menu level3 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        final String listString;
        final Gson gson = new Gson();
        
        option = Integer.parseInt(sub.getSubParams().get("OPTION").toString());
        switch (option) {
            case 1:
            case 2:
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    if (phonePattern.matcher(request.getSubscriberInput()).matches()) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("PHONE", input);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Veuillez confirmer le numéro de téléphone: ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 3:
                final ArrayList<Bouquet> options = (ArrayList<Bouquet>) sub.getSubParams().get("BOUQUET_IWI");
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    if (input < 1 || input > options.size()) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("BQ_IWI_OPTION", options.get(option - 1));
                    sub.getSubParams().put("AMOUNT", options.get(option - 1).getAmount());
                    final Double amount = Double.parseDouble(sub.getSubParams().get("AMOUNT").toString());
                    sub.getSubParams().remove("AMOUNT");
                    final String msg = "Montant de votre commande: {AMOUNT}F. Voulez vous procéder au paiement?\n" +
                                       "1. Oui \n" +
                                       "2. Non"
                            .replace("{AMOUNT}", df.format(amount));
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.setAmount(new BigDecimal(amount));
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    
                } catch (NumberFormatException ex) {
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 4:
                final ArrayList<Bouquet> bq = (ArrayList<Bouquet>) sub.getSubParams().get("BOUQUET_CANAL");
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    if (input < 1 || input > bq.size()) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("BQ_CANAL_OPTION", bq.get(option - 1));
                    final String msg = "Choisir le nombre de mois:";
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                } catch (NumberFormatException ex) {
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 5:
                try {
                    //final int input = Integer.parseInt(request.getSubscriberInput());
                    sub.getSubParams().put("NUMERO_COMPTEUR", request.getSubscriberInput());
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Veuillez confirmer le nom et prénoms d'abonné:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 6:
            case 7:
                try {
                    //final int input = Integer.parseInt(request.getSubscriberInput());
                    sub.getSubParams().put("NUMERO_POLICE", request.getSubscriberInput());
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Veuillez confirmer le numéro de police:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 8:
                try {
                    //final int input = Integer.parseInt(request.getSubscriberInput());
                    sub.getSubParams().put("NAME", request.getSubscriberInput());
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Veuillez renseigner la date (JJMMAAAA):");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }
    
    private UssdResponse processEugenioLevel4Menu(SubscriberInfo sub, UssdRequest request){
        Logger.getLogger("qos_ussd_processor").info("Eugenio menu level4 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        final String listString;
        final Gson gson = new Gson();
        
        option = Integer.parseInt(sub.getSubParams().get("OPTION").toString());
        switch (option) {
            case 1:
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    final int phone = Integer.parseInt(sub.getSubParams().get("PHONE").toString());
                    if (phonePattern.matcher(request.getSubscriberInput()).matches() && input == phone) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                listString = new HTTPUtil().sendGetRequest("http://eugeniobusness.com/mtn/app/view/users/internetApi.php");
                try{
                    final ArrayList<Subscription> user = gson.fromJson(listString, new TypeToken<List<Subscription>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder("Veuillez choisir le bouquet qui vous convent: ");
                    int i=0;
                    for(final Subscription subscription: user){
                        msgResp.append("\n").append(++i).append(". ").append(subscription.getBouquet()).append(" => ").append(subscription.getAmount()).append("F");
                    }
                    sub.getSubParams().put("SUBSCRIPTION_IWI", user);
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                }catch (JsonSyntaxException ex) {
                    Logger.getLogger("processAradLevel2Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 2:
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    final int phone = Integer.parseInt(sub.getSubParams().get("PHONE").toString());
                    if (phonePattern.matcher(request.getSubscriberInput()).matches() && input == phone) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                resp.setApplicationResponse("Veuillez insérer le montant à recharger: ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 3:
                resp.setMsisdn(request.getMsisdn());
                //String respMessage;
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing eugenio transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final Bouquet bouquet = (Bouquet) sub.getSubParams().get("BOUQUET_IWI");
                        final StringBuilder transref = new StringBuilder();
                        transref.append("bouquet=").append(bouquet.getId()).append("|")
                                .append("referenceTransaction=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 4:
                try {
                    //final int input = Integer.parseInt(request.getSubscriberInput());
                    sub.getSubParams().put("MONTH", request.getSubscriberInput());
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel4Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                try {
                    final Bouquet bq = (Bouquet) sub.getSubParams().get("BQ_CANAL_OPTION");
                    sub.getSubParams().put("AMOUNT", bq.getAmount());
                    final Double amount = Double.parseDouble(sub.getSubParams().get("AMOUNT").toString());
                    sub.getSubParams().remove("AMOUNT");
                    final String msg = "Montant de votre commande: {AMOUNT}F. Voulez vous procéder au paiement?\n" +
                                       "1. Oui \n" +
                                       "2. Non"
                            .replace("{AMOUNT}", df.format(amount));
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.setAmount(new BigDecimal(amount));
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    
                } catch (NumberFormatException ex) {
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel4Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 5:
                try {
                    sub.getSubParams().put("NAME", request.getSubscriberInput());
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Veuillez insérer le montant à recharger: ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 6:
            case 7:
                try {
                    if(!sub.getSubParams().get("NUMERO_POLICE").equals(request.getSubscriberInput())){
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }
                    sub.getSubParams().put("NUMERO_POLICE", request.getSubscriberInput());
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Veuillez insérer le montant à payer: ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case 8:
                if (datePattern.matcher(request.getSubscriberInput()).matches()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                    final Date depatureDate;
                    try {
                        depatureDate = sdf.parse(request.getSubscriberInput());
                        Calendar now = Calendar.getInstance();
                        now.set(Calendar.HOUR_OF_DAY, 23);
                        if (depatureDate.before(now.getTime())) {//compares date portions only
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("date entered{" + depatureDate + "} is before now():" + now);
                            activeSessions.remove(request.getMsisdn());
                            return resp;
                        } else {
                            sub.getAradDetails().setDepartureDate(depatureDate);
                            final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                            if (fmt.format(now.getTime()).equals(fmt.format(depatureDate))) {
                                sub.setIsDepartureToday(true);
                            }
                        }
                    } catch (ParseException ex) {
                        Logger.getLogger("qos_ussd_processor").info(ex);
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }
                } else {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                listString = new HTTPUtil().sendGetRequest("http://eugeniobusness.com/mtn/app/view/users/companyApi.php");
                try{
                    final ArrayList<Company> user = gson.fromJson(listString, new TypeToken<List<Company>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder("Veuillez choisir la compagnie de transport: ");
                    int i=0;
                    for(final Company company: user){
                        msgResp.append("\n").append(++i).append(". ").append(company.getCompany());
                    }
                    sub.getSubParams().put("COMPANIES", user);
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                }catch (JsonSyntaxException ex) {
                    Logger.getLogger("processAradLevel2Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }
    
    private UssdResponse processEugenioLevel5Menu(SubscriberInfo sub, UssdRequest request){
        Logger.getLogger("qos_ussd_processor").info("Eugenio menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        final String listString;
        final Gson gson = new Gson();
        
        option = Integer.parseInt(sub.getSubParams().get("OPTION").toString());
        switch (option) {
            case 1:
                final ArrayList<Subscription> subs = (ArrayList<Subscription>) sub.getSubParams().get("SUBSCRIPTION_IWI");
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    if (input < 1 || input > subs.size()) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("SUBSCRIPTION_IWI_OPTION", subs.get(option - 1));
                } catch (NumberFormatException ex) {
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                try {
                    final Subscription su = (Subscription) sub.getSubParams().get("SUBSCRIPTION_IWI_OPTION");
                    sub.getSubParams().put("AMOUNT", su.getAmount());
                    final Double amount = Double.parseDouble(sub.getSubParams().get("AMOUNT").toString());
                    sub.getSubParams().remove("AMOUNT");
                    final String msg = "Montant de votre commande: {AMOUNT}F. Voulez vous procéder au paiement?\n" +
                                       "1. Oui \n" +
                                       "2. Non"
                            .replace("{AMOUNT}", df.format(amount));
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.setAmount(new BigDecimal(amount));
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    
                } catch (NumberFormatException ex) {
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }catch (Exception ex) {
                    Logger.getLogger("processAradLevel5Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 2:
                try {
                    sub.getSubParams().put("MONTANT", request.getSubscriberInput());
                    final Double amount = Double.parseDouble(sub.getSubParams().get("MONTANT").toString());
                    sub.getSubParams().remove("AMOUNT");
                    final String msg = "Montant de votre commande: {AMOUNT}F. Voulez vous procéder au paiement?\n" +
                                       "1. Oui \n" +
                                       "2. Non"
                            .replace("{AMOUNT}", df.format(amount));
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.setAmount(new BigDecimal(amount));
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 4:
                resp.setMsisdn(request.getMsisdn());
                //String respMessage;
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing eugenio transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final Bouquet bouquet = (Bouquet) sub.getSubParams().get("BOUQUET_CANAL");
                        final StringBuilder transref = new StringBuilder();
                        transref.append("bouquet=").append(bouquet.getId()).append("|")
                                .append("mois=").append(sub.getSubParams().get("MONTH").toString()).append("|")
                                .append("referenceTransaction=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 5:
                try {
                    sub.getSubParams().put("MONTANT", request.getSubscriberInput());
                    final Double amount = Double.parseDouble(sub.getSubParams().get("MONTANT").toString());
                    sub.getSubParams().remove("AMOUNT");
                    final String msg = "Montant de votre commande: {AMOUNT}F. Voulez vous procéder au paiement?\n" +
                                       "1. Oui \n" +
                                       "2. Non"
                            .replace("{AMOUNT}", df.format(amount));
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.setAmount(new BigDecimal(amount));
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 6:
            case 7:
                try {
                    sub.getSubParams().put("MONTANT", request.getSubscriberInput());
                    final Double amount = Double.parseDouble(sub.getSubParams().get("MONTANT").toString());
                    sub.getSubParams().remove("AMOUNT");
                    final String msg = "Montant de votre commande: {AMOUNT}F. Voulez vous procéder au paiement?\n" +
                                       "1. Oui \n" +
                                       "2. Non"
                            .replace("{AMOUNT}", df.format(amount));
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.setAmount(new BigDecimal(amount));
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 8:
                final ArrayList<Company> options = (ArrayList<Company>) sub.getSubParams().get("COMPANIES");
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    if (input < 1 || input > options.size()) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("COMPANY_OPTION", options.get(input - 1));
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                } catch (Exception ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                final Company com = (Company) sub.getSubParams().get("COMPANY_OPTION");
                listString = new HTTPUtil().sendGetRequest("http://eugeniobusness.com/mtn/app/view/users/destinationApi.php?token="+com.getId());
                try{
                    final ArrayList<Destination> user = gson.fromJson(listString, new TypeToken<List<Destination>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder("Veuillez choisir la destination: ");
                    int i=0;
                    for(final Destination destination: user){
                        msgResp.append("\n").append(++i).append(". ").append(destination.getDeparture()).append(" - ").append(destination.getDestination()).append(" => ").append(destination.getAmount());
                    }
                    sub.getSubParams().put("DESTINATIONS", user);
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                }catch (JsonSyntaxException ex) {
                    Logger.getLogger("processAradLevel2Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }
    
    private UssdResponse processEugenioLevel6Menu(SubscriberInfo sub, UssdRequest request){
        Logger.getLogger("qos_ussd_processor").info("Eugenio menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        final String listString;
        final Gson gson = new Gson();
        
        option = Integer.parseInt(sub.getSubParams().get("OPTION").toString());
        switch (option) {
            case 1:
                resp.setMsisdn(request.getMsisdn());
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing eugenio transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final Subscription su = (Subscription) sub.getSubParams().get("SUBSCRIPTION_IWI_OPTION");
                        final StringBuilder transref = new StringBuilder();
                        transref.append("phone=").append(sub.getSubParams().get("PHONE").toString()).append("|")
                                .append("subscription=").append(su.getId()).append("|")
                                .append("referenceTransaction=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 2:
                resp.setMsisdn(request.getMsisdn());
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing eugenio transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final Subscription su = (Subscription) sub.getSubParams().get("SUBSCRIPTION_IWI_OPTION");
                        final StringBuilder transref = new StringBuilder();
                        transref.append("phone=").append(sub.getSubParams().get("PHONE").toString()).append("|")
                                .append("montant=").append(sub.getSubParams().get("MONTANT").toString()).append("|")
                                .append("referenceTransaction=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 5:
                resp.setMsisdn(request.getMsisdn());
                //String respMessage;
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing eugenio transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("nocompteur=").append(sub.getSubParams().get("NUMERO_COMPTEUR").toString()).append("|")
                                .append("name=").append(sub.getSubParams().get("NAME").toString()).append("|")
                                .append("montant=").append(sub.getSubParams().get("MONTANT").toString()).append("|")
                                .append("referenceTransaction=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 6:
            case 7:
                resp.setMsisdn(request.getMsisdn());
                //String respMessage;
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing eugenio transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("nopolice=").append(sub.getSubParams().get("NUMERO_POLICE").toString()).append("|")
                                .append("montant=").append(sub.getSubParams().get("MONTANT").toString()).append("|")
                                .append("referenceTransaction=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            case 8:
                final ArrayList<Destination> options = (ArrayList<Destination>) sub.getSubParams().get("DESTINATIONS");
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    if (input < 1 || input > options.size()) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("DESTINATION_OPTION", options.get(input - 1));
                    final StringBuilder msgResp = new StringBuilder("Veuillez choisir l'heure de départ: ");
                    if(!options.get(input - 1).getHour().isEmpty()) msgResp.append("\n1. ").append(options.get(input - 1).getHour());
                    if(!options.get(input - 1).getHour2().isEmpty()) msgResp.append("\n2. ").append(options.get(input - 1).getHour2());
                    if(!options.get(input - 1).getHour3().isEmpty()) msgResp.append("\n3. ").append(options.get(input - 1).getHour3());
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                } catch (Exception ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                /*final Company com = (Company) sub.getSubParams().get("COMPANY_OPTION");
                listString = new HTTPUtil().sendGetRequest("http://eugeniobusness.com/mtn/app/view/users/destinationApi.php?token="+com.getId());
                try{
                    final ArrayList<Destination> user = gson.fromJson(listString, new TypeToken<List<Destination>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder("Veuillez choisir l'heure de départ: \n1. 07h00 ");
                    int i=0;
                    for(final Destination destination: user){
                        msgResp.append("\n").append(++i).append(". ").append(destination.getDeparture()).append(" - ").append(destination.getDestination()).append(" => ").append(destination.getAmount());
                    }
                    sub.getSubParams().put("DESTINATIONS", user);
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                }catch (JsonSyntaxException ex) {
                    Logger.getLogger("processAradLevel2Menu").info("Exception encountered. Reason: " + ex.getMessage());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }*/
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }
    
    private UssdResponse processEugenioLevel7Menu(SubscriberInfo sub, UssdRequest request){
        Logger.getLogger("qos_ussd_processor").info("Eugenio menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        final String listString;
        final Gson gson = new Gson();
        
        option = Integer.parseInt(sub.getSubParams().get("OPTION").toString());
        switch (option) {
            case 8:
                try {
                    final int input = Integer.parseInt(request.getSubscriberInput());
                    final Destination des = (Destination) sub.getSubParams().get("DESTINATION_OPTION");
                    if (input < 1 || input > 1) {
                        throw new NumberFormatException();
                    }
                    switch(input){
                        case 1: sub.getSubParams().put("DEPARTURE_TIME", des.getHour());
                        case 2: sub.getSubParams().put("DEPARTURE_TIME", des.getHour2());
                        case 3: sub.getSubParams().put("DEPARTURE_TIME", des.getHour3());
                    }
                    
                    sub.getSubParams().put("AMOUNT", des.getAmount());
                    final Double amount = Double.parseDouble(sub.getSubParams().get("AMOUNT").toString());
                    sub.getSubParams().remove("AMOUNT");
                    final String msg = "Montant de votre commande: {AMOUNT}F. Voulez vous procéder au paiement?\n" +
                                       "1. Oui \n" +
                                       "2. Non"
                            .replace("{AMOUNT}", df.format(amount));
                    resp.setApplicationResponse(msg);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.setAmount(new BigDecimal(amount));
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                } catch (Exception ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }
    
    private UssdResponse processEugenioLevel8Menu(SubscriberInfo sub, UssdRequest request){
        Logger.getLogger("qos_ussd_processor").info("Eugenio menu level8 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        final String listString;
        final Gson gson = new Gson();
        
        option = Integer.parseInt(sub.getSubParams().get("OPTION").toString());
        switch (option) {
            case 8:
                resp.setMsisdn(request.getMsisdn());
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    final Company com = (Company) sub.getSubParams().get("COMPANY_OPTION");
                    final Destination des = (Destination) sub.getSubParams().get("DESTINATION_OPTION");
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing eugenio transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("name=").append(sub.getSubParams().get("NAME").toString()).append("|")
                                .append("date=").append(new SimpleDateFormat("yyyyMMdd").format(sub.getAradDetails().getDepartureDate())).append("|")
                                .append("company=").append(com.getId()).append("|")
                                .append("destination=").append(des.getId()).append("|")
                                .append("departure.time=").append(sub.getSubParams().get("DEPARTURE_TIME").toString()).append("|")
                                .append("referenceTransaction=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }
    
    UssdResponse showMainMenu(SubscriberInfo sub) {
        Logger.getLogger("qos_ussd_processor").info("showing tvm main menu to: " + sub.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(sub.getMsisdn());
        //BPS_MAIN_MENU=Welcome to {MERCHANT_NAME}. Enter phone number:
        final String respMessage = "Menu Principal\n" +
                                   "1. Activation internet Bénin Télécom\n" +
                                   "2. Achats de crédit Bénin Télécom\n" +
                                   "3. Crédits IWI\n" + 
                                   "4. Canal\n" + 
                                   "5. Achats de crédit SBEE\n" + 
                                   "6. Payements de facture SBEE\n" + 
                                   "7. Payements de facture SONEB\n" +
                                   "8. Réservations de billet de transport";
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        //sub.setMerchantName();
        activeSessions.put(sub.getMsisdn(), sub);
        return resp;
    }
}
