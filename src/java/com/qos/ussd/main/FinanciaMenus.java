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
import com.qos.ussd.dto.UssdRequest;
import com.qos.ussd.dto.UssdResponse;
import static com.qos.ussd.main.USSDSessionHandler.activeSessions;
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import com.qos.ussd.util.eugenio.dto.Bouquet;
import com.qos.ussd.util.eugenio.dto.Subscription;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author Malkiel
 */
public class FinanciaMenus {

    private final Pattern phonePattern = Pattern.compile("^\\d{10,12}$");
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req) {
        Logger.getLogger("qos_ussd_processor").info("calling Financia menu @step: " + sub.getMenuLevel());
        switch (sub.getMenuLevel()) {
            case 2:
                return processFinanciaLevel2Menu(sub, req);
            case 3:
                return processFinanciaLevel3Menu(sub, req);
            case 4:
                return processFinanciaLevel2Menu(sub, req);
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

    private UssdResponse processFinanciaLevel2Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType;
        try {
            requestType = Integer.parseInt(request.getSubscriberInput());
            if (requestType < 1 || requestType > 2) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("requestType", requestType); //requestType
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

        switch (requestType) {
            //Ouverture
            case 1:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Select Account Type\n"
                        + "1. Epargne\n"
                        + "2. Courant\n"
                        + "3. Tontine\n"
                        + "0. retour");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Connexion
            case 2:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Nom de la Microfinance\n"
                        + "1. Tontine\n"
                        + "2. Demande de prêt\n"
                        + "3. Remboursement\n"
                        + "4. FINANCIA PAY\n"
                        + "5. Solde de votre compte\n"
                        + "6. Termes and Conditions\n"
                        + "0. Retour\n"
                        + "\n"
                        + "Choisissez une option puis appuyer sur envoyer");
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

    private UssdResponse processFinanciaLevel3Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level3 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, operation;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                try {
                    final int accountType = Integer.parseInt(request.getSubscriberInput());
                    if (accountType < 1 || accountType > 3) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("accountType", accountType); //accountType
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Epargne\n"
                        + "Entrez votre nom: ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Connexion
            case 2:
                try {
                    operation = Integer.parseInt(request.getSubscriberInput());
                    if (operation < 1 || operation > 6) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("operation", operation); //operation
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                switch (operation) {
                    //Tontine
                    case 1:
                        resp.setApplicationResponse("Epargne\n"
                                + "1. Payement ponctuel\n"
                                + "2. Auto-debit\n"
                                + "3. Annulation de l'Auto-debit\n"
                                + "4. Remboursement de la tontine\n"
                                + "0. Retour\n"
                                + "\n"
                                + "\n"
                                + "\n"
                                + "Choisissez une option puis appuyer sur envoyer ");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    // Demande de pret
                    case 2:
                        resp.setApplicationResponse("Demande de prêt\n"
                                + "Entrer votre identifiant:");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    // Remboursement
                    case 3:
                        resp.setApplicationResponse("Remboursement\n"
                                + "Entrer votre identifiant:");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    // Financia Pay
                    case 4:
                        resp.setApplicationResponse("FINANCIA PAY\n"
                                + "1. Dépôt sur Financia Pay\n"
                                + "2. Financia Pay sur Mobile Money\n"
                                + "0. Retour\n"
                                + "\n"
                                + "\n"
                                + "\n"
                                + "\n"
                                + "\n"
                                + "Choisissez une option puis appuyer sur envoyer");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    //Solde de votre compte
                    case 5:
                        resp.setApplicationResponse("Choisissez le compte à débiter\n"
                                + "1. Mobile Money\n"
                                + "2. Financia");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    case 6:
                        try {
                            /*String[] parts = request.getMsisdn().split("229");
                            String number = parts[1]; // Phone Number without 229
                            sub.getSubParams().put("NUMBER", number);*/
                            JsonObject getAuthorizationPayment = new JsonObject();
                            //getAuthorizationPayment.addProperty("tel", sub.getSubParams().get("NUMBER").toString());
                            //getAuthorizationPayment.addProperty("id", sub.getSubParams().get("identifiantPret").toString());
                            final String message = new HTTPUtil().getList(getAuthorizationPayment, "#url"); //termes
                            sub.getSubParams().put("JSON_RESPONSE", message);
                            resp.setApplicationResponse(message + "\n1. Suivant");
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        } catch (Exception ex) {
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                        }
                        return resp;
                    default:
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

    private UssdResponse processFinanciaLevel4Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level4 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                sub.getSubParams().put("epargneNom", request.getSubscriberInput()); //epargneNom
                resp.setApplicationResponse("Epargne\n"
                        + "Entrez votre prenom: ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Connexion
            case 2:
                operation = Integer.parseInt(sub.getSubParams().get("operation").toString());

                switch (operation) {
                    //Tontine
                    case 1:
                        try {
                            operationType = Integer.parseInt(request.getSubscriberInput());
                            if (operationType < 1 || operationType > 6) {
                                throw new NumberFormatException();
                            }
                            sub.getSubParams().put("operationType", operationType); //operationType
                        } catch (NumberFormatException ex) {
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                            return resp;
                        }

                        switch (operationType) {
                            // Payement ponctuel
                            case 1:
                            // Auto-Debit
                            case 2:
                            // Annulation de l'Auto-debit
                            case 3:
                            // Remboursement de la tontine
                            case 4:
                                resp.setApplicationResponse("Tontine\n"
                                        + "Entrer votre identifiant de tontine:");
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

                    // Demande de pret
                    case 2:
                        sub.getSubParams().put("identifiantPret", request.getSubscriberInput()); //identifiantPret
                        try {
                            /*String[] parts = request.getMsisdn().split("229");
                            String number = parts[1]; // Phone Number without 229
                            sub.getSubParams().put("NUMBER", number);*/
                            JsonObject getAuthorizationPayment = new JsonObject();
                            //getAuthorizationPayment.addProperty("tel", sub.getSubParams().get("NUMBER").toString());
                            getAuthorizationPayment.addProperty("id", sub.getSubParams().get("identifiantPret").toString());
                            final String response = new HTTPUtil().getList(getAuthorizationPayment, "#");
                            sub.getSubParams().put("JSON_RESPONSE", response);
                            /*JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            String[] rm = response.split("\"");
                            String[] data = rm[1].split("-");
                            String[] solde = data[1].split("=");*/
                            try {
                                final int id = Integer.parseInt(response); //id
                                if (id != 0) {
                                    resp.setApplicationResponse("Entrer le montant du prêt:");
                                    resp.setFreeflow(UssdConstants.CONTINUE);
                                    sub.incrementMenuLevel();
                                    activeSessions.put(request.getMsisdn(), sub);
                                } else {
                                    resp.setApplicationResponse("Identifiant incorrect");
                                    resp.setFreeflow(UssdConstants.BREAK);
                                    Logger.getLogger("qos_ussd_processor").info("unauthorized number supplied for financia request: " + request.getMsisdn());
                                    activeSessions.remove(request.getMsisdn());
                                }
                            } catch (NumberFormatException ex) {
                                resp.setApplicationResponse("Invalid Financia details entered");
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("invalid details supplied for financia request: " + request.getMsisdn());
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
                    // Remboursement
                    case 3:
                        sub.getSubParams().put("identifiantPret", request.getSubscriberInput()); //identifiantPret
                        try {
                            /*String[] parts = request.getMsisdn().split("229");
                            String number = parts[1]; // Phone Number without 229
                            sub.getSubParams().put("NUMBER", number);*/
                            JsonObject getAuthorizationPayment = new JsonObject();
                            //getAuthorizationPayment.addProperty("tel", sub.getSubParams().get("NUMBER").toString());
                            getAuthorizationPayment.addProperty("id", sub.getSubParams().get("identifiantPret").toString());
                            final String response = new HTTPUtil().getList(getAuthorizationPayment, "#url");
                            final String balance = new HTTPUtil().getList(getAuthorizationPayment, "#url"); // Solde
                            sub.getSubParams().put("JSON_RESPONSE", response);
                            /*JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            String[] rm = response.split("\"");
                            String[] data = rm[1].split("-");
                            String[] solde = data[1].split("=");*/
                            try {
                                final int id = Integer.parseInt(response); //id (boolean)
                                if (id != 0) { //True
                                    resp.setApplicationResponse("Le solde du prêt courant est de " + balance + " XOF. \n"
                                            + "Entrer le montant à rembourser:");
                                    resp.setFreeflow(UssdConstants.CONTINUE);
                                    sub.incrementMenuLevel();
                                    activeSessions.put(request.getMsisdn(), sub);
                                } else { //False
                                    resp.setApplicationResponse("Identifiant incorrect");
                                    resp.setFreeflow(UssdConstants.BREAK);
                                    Logger.getLogger("qos_ussd_processor").info("unauthorized number supplied for financia request: " + request.getMsisdn());
                                    activeSessions.remove(request.getMsisdn());
                                }
                            } catch (NumberFormatException ex) {
                                resp.setApplicationResponse("Invalid Financia details entered");
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("invalid details supplied for financia request: " + request.getMsisdn());
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
                    // Financia Pay
                    case 4:
                        try {
                            operationType = Integer.parseInt(request.getSubscriberInput());
                            if (operationType < 1 || operationType > 6) {
                                throw new NumberFormatException();
                            }
                            sub.getSubParams().put("operationType", operationType); //operationType
                        } catch (NumberFormatException ex) {
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                            return resp;
                        }
                        switch (operationType) {
                            // Dépôt sur Financia Pay
                            case 1:
                            //Financia Pay sur Mobile Money
                            case 2:
                                resp.setApplicationResponse("Demande de prêt\n"
                                        + "Entrer votre identifiant:");
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
                    //Solde de votre compte
                    case 5:
                        try {
                            final int account = Integer.parseInt(request.getSubscriberInput());
                            if (account < 1 || account > 6) {
                                throw new NumberFormatException();
                            }
                            sub.getSubParams().put("account", account); //account
                        } catch (NumberFormatException ex) {
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                            return resp;
                        }

                        resp.setApplicationResponse("Entrer votre PIN pour consulter le solde de votre compte épargne");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    case 6:
                        resp.setApplicationResponse("Les frais de prêt sont composés d'un montant fixe et journalier. Cela sera montré avant d'accepter le prêt. Le remboursement se fait par déduction automatique à la date d'échéance\n"
                                + "1. Accepter\n"
                                + "2. Refuser");
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

            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }

    private UssdResponse processFinanciaLevel5Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                sub.getSubParams().put("epargnePrenom", request.getSubscriberInput()); //epargneNom
                resp.setApplicationResponse("Epargne\n"
                        + "Entrez votre date de naissance:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Connexion
            case 2:
                operation = Integer.parseInt(sub.getSubParams().get("operation").toString());

                switch (operation) {
                    //Tontine
                    case 1:
                        operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                        switch (operationType) {
                            // Payement ponctuel
                            case 1:
                            // Auto-Debit
                            case 2:
                                sub.getSubParams().put("identifiantTontine", request.getSubscriberInput()); //identifiantPret
                                resp.setApplicationResponse("Entrer le montant:");
                                resp.setFreeflow(UssdConstants.CONTINUE);
                                sub.incrementMenuLevel();
                                activeSessions.put(request.getMsisdn(), sub);
                                return resp;
                            // Annulation de l'Auto-debit
                            case 3:
                            // Remboursement de la tontine
                            case 4:
                                //Message Final if success
                                resp.setMsisdn(request.getMsisdn());
                                try {
                                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                                    //if (pay == 1) {
                                    Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                                    JsonObject requestPayment = new JsonObject();
                                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                                    requestPayment.addProperty("amount", sub.getAmount());
                                    final StringBuilder transref = new StringBuilder();
                                    transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                                            .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                                            .append("operationType=").append(sub.getSubParams().get("operationType").toString())
                                            .append("identifiantTontine=").append(sub.getSubParams().get("identifiantTontine").toString());
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
                                    /*} else {
                                        //cancel transaction
                                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                        resp.setApplicationResponse(respMessage);
                                        resp.setFreeflow(UssdConstants.BREAK);
                                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                        activeSessions.remove(request.getMsisdn());
                                        return resp;
                                    }*/
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

                    // Demande de pret
                    case 2:
                        sub.getSubParams().put("montant", request.getSubscriberInput()); //montant
                        // message final if success
                        resp.setMsisdn(request.getMsisdn());
                        try {
                            //final int pay = Integer.parseInt(request.getSubscriberInput());
                            //if (pay == 1) {
                            Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", sub.getMsisdn());
                            requestPayment.addProperty("amount", sub.getAmount());
                            final StringBuilder transref = new StringBuilder();
                            transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                                    .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                                    .append("operationType=").append(sub.getSubParams().get("operationType").toString())
                                    .append("identifiantPret=").append(sub.getSubParams().get("identifiantPret").toString());
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
                            /*} else {
                                        //cancel transaction
                                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                        resp.setApplicationResponse(respMessage);
                                        resp.setFreeflow(UssdConstants.BREAK);
                                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                        activeSessions.remove(request.getMsisdn());
                                        return resp;
                                    }*/
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

            // Remboursement
            case 3:
                sub.getSubParams().put("montant", request.getSubscriberInput()); //montant
                resp.setApplicationResponse("Choisissez le compte à débiter\n"
                        + "1. Mobile Money\n"
                        + "2. Financia");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;

            // Financia Pay
            case 4:
                operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                switch (operationType) {
                    // Dépôt sur Financia Pay
                    case 1:
                        sub.getSubParams().put("identifiantPret", request.getSubscriberInput()); //identifiantPret
                        resp.setApplicationResponse("Entrer le numero de votre compte Financia Pay Account:");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    //Financia Pay sur Mobile Money
                    case 2:
                        sub.getSubParams().put("identifiantPret", request.getSubscriberInput()); //identifiantPret
                        resp.setApplicationResponse("Entrer votre numero mobile money");
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
            //Solde de votre compte
            case 5:
                // message final if success
                resp.setMsisdn(request.getMsisdn());
                try {
                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                    //if (pay == 1) {
                    Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                    JsonObject requestPayment = new JsonObject();
                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                    requestPayment.addProperty("amount", sub.getAmount());
                    final StringBuilder transref = new StringBuilder();
                    transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                            .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                            .append("pin=").append(request.getSubscriberInput())
                            .append("account=").append(sub.getSubParams().get("account").toString());
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
                    /*} else {
                                //cancel transaction
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                activeSessions.remove(request.getMsisdn());
                                return resp;
                            }*/
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
            // Termes and Conditions
            case 6:
                // message final if success
                resp.setMsisdn(request.getMsisdn());
                sub.getSubParams().put("termes", Integer.parseInt(request.getSubscriberInput())); //termes
                try {
                    //if (pay == 1) {
                    Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                    JsonObject requestPayment = new JsonObject();
                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                    requestPayment.addProperty("amount", sub.getAmount());
                    final StringBuilder transref = new StringBuilder();
                    transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                            .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                            .append("termes=").append(sub.getSubParams().get("termes").toString())
                            .append("account=").append(sub.getSubParams().get("account").toString());
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
                    /*} else {
                                //cancel transaction
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                activeSessions.remove(request.getMsisdn());
                                return resp;
                            }*/
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

    private UssdResponse processFinanciaLevel6Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level6 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                sub.getSubParams().put("epargneDateNaissance", request.getSubscriberInput()); //epargneDateNaissance
                resp.setApplicationResponse("Epargne\n"
                        + "Entrez votre lieu de résidence:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Connexion
            case 2:
                operation = Integer.parseInt(sub.getSubParams().get("operation").toString());

                switch (operation) {
                    //Tontine
                    case 1:
                        operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                        switch (operationType) {
                            // Payement ponctuel
                            case 1:
                            // Auto-Debit
                            case 2:
                                sub.getSubParams().put("montant", request.getSubscriberInput()); //montant
                                resp.setApplicationResponse("Choisissez le compte à débiter\n"
                                        + "1. Mobile Money\n"
                                        + "2. Financia");
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

                    // Remboursement
                    case 3:
                        sub.getSubParams().put("account", request.getSubscriberInput()); //account
                        // message final if success
                        resp.setMsisdn(request.getMsisdn());
                        try {
                            //final int pay = Integer.parseInt(request.getSubscriberInput());
                            //if (pay == 1) {
                            Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", sub.getMsisdn());
                            requestPayment.addProperty("amount", sub.getAmount());
                            final StringBuilder transref = new StringBuilder();
                            transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                                    .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                                    .append("identifiantPret=").append(sub.getSubParams().get("identifiantPret").toString())
                                    .append("montant=").append(sub.getSubParams().get("montant").toString())
                                    .append("account=").append(sub.getSubParams().get("account").toString());
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
                            /*} else {
                                        //cancel transaction
                                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                        resp.setApplicationResponse(respMessage);
                                        resp.setFreeflow(UssdConstants.BREAK);
                                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                        activeSessions.remove(request.getMsisdn());
                                        return resp;
                                    }*/
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

            // Financia Pay
            case 4:
                operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                switch (operationType) {
                    // Dépôt sur Financia Pay
                    case 1:
                        sub.getSubParams().put("accountNumber", request.getSubscriberInput()); //accountNumber
                        resp.setApplicationResponse("Entrer le montant :");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    //Financia Pay sur Mobile Money
                    case 2:
                        sub.getSubParams().put("momoNumber", request.getSubscriberInput()); //momoNumber
                        resp.setApplicationResponse("Entrer le montant :");
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
            default:
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }

    private UssdResponse processFinanciaLevel7Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level7 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                sub.getSubParams().put("epargneLieuResidence", request.getSubscriberInput()); //epargneLieuResidence
                resp.setApplicationResponse("Epargne\n"
                        + "Entrez votre numero de téléphone:");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Connexion
            case 2:
                operation = Integer.parseInt(sub.getSubParams().get("operation").toString());

                switch (operation) {
                    //Tontine
                    case 1:
                        operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                        switch (operationType) {
                            // Payement ponctuel
                            case 1:
                            // Auto-Debit
                            case 2:
                                sub.getSubParams().put("account", request.getSubscriberInput()); //account
                                // message final if success
                                resp.setMsisdn(request.getMsisdn());
                                try {
                                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                                    //if (pay == 1) {
                                    Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                                    JsonObject requestPayment = new JsonObject();
                                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                                    requestPayment.addProperty("amount", sub.getAmount());
                                    final StringBuilder transref = new StringBuilder();
                                    transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                                            .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                                            .append("operationType=").append(sub.getSubParams().get("operationType").toString()).append("|")
                                            .append("identifiantTontine=").append(sub.getSubParams().get("identifiantTontine").toString())
                                            .append("montant=").append(sub.getSubParams().get("montant").toString())
                                            .append("account=").append(sub.getSubParams().get("account").toString());
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
                                    /*} else {
                                        //cancel transaction
                                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                        resp.setApplicationResponse(respMessage);
                                        resp.setFreeflow(UssdConstants.BREAK);
                                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                        activeSessions.remove(request.getMsisdn());
                                        return resp;
                                    }*/
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

                    // Financia Pay
                    case 4:
                        operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                        switch (operationType) {
                            // Dépôt sur Financia Pay
                            case 1:
                                sub.getSubParams().put("montant", request.getSubscriberInput()); //montant
                                // message final if success
                                resp.setMsisdn(request.getMsisdn());
                                try {
                                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                                    //if (pay == 1) {
                                    Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                                    JsonObject requestPayment = new JsonObject();
                                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                                    requestPayment.addProperty("amount", sub.getAmount());
                                    final StringBuilder transref = new StringBuilder();
                                    transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                                            .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                                            .append("operationType=").append(sub.getSubParams().get("operationType").toString()).append("|")
                                            .append("identifiantPret=").append(sub.getSubParams().get("identifiantPret").toString())
                                            .append("montant=").append(sub.getSubParams().get("montant").toString())
                                            .append("accountNumber=").append(sub.getSubParams().get("accountNumber").toString());
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
                                    /*} else {
                                        //cancel transaction
                                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                        resp.setApplicationResponse(respMessage);
                                        resp.setFreeflow(UssdConstants.BREAK);
                                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                        activeSessions.remove(request.getMsisdn());
                                        return resp;
                                    }*/
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
                            //Financia Pay sur Mobile Money
                            case 2:
                                sub.getSubParams().put("montant", request.getSubscriberInput()); //montant
                                // message final if success
                                resp.setMsisdn(request.getMsisdn());
                                try {
                                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                                    //if (pay == 1) {
                                    Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                                    JsonObject requestPayment = new JsonObject();
                                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                                    requestPayment.addProperty("amount", sub.getAmount());
                                    final StringBuilder transref = new StringBuilder();
                                    transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                                            .append("operation=").append(sub.getSubParams().get("operation").toString()).append("|")
                                            .append("operationType=").append(sub.getSubParams().get("operationType").toString()).append("|")
                                            .append("identifiantPret=").append(sub.getSubParams().get("identifiantPret").toString())
                                            .append("montant=").append(sub.getSubParams().get("montant").toString())
                                            .append("momoNumber=").append(sub.getSubParams().get("momoNumber").toString());
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
                                    /*} else {
                                        //cancel transaction
                                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                                        resp.setApplicationResponse(respMessage);
                                        resp.setFreeflow(UssdConstants.BREAK);
                                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                        activeSessions.remove(request.getMsisdn());
                                        return resp;
                                    }*/
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
                    default:
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
    
    private UssdResponse processFinanciaLevel8Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level8 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                sub.getSubParams().put("epargneTelephone", request.getSubscriberInput()); //epargneTelephone
                resp.setApplicationResponse("Inscription\n" +
                                            "Accepter les termes et conditions de Financia.\n" +
                                            "1. Accepter\n" +
                                            "2. Refuser\n" +
                                            "3. Lire les termes et les conditions");
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
    
    private UssdResponse processFinanciaLevel9Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level9 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, option;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                option = Integer.parseInt(request.getSubscriberInput());
                if(option == 1 || option == 2){
                    sub.getSubParams().put("termes", option); //termes  
                    // message final if success
                    resp.setMsisdn(request.getMsisdn());
                    try {
                        //final int pay = Integer.parseInt(request.getSubscriberInput());
                        //if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                                .append("accountType=").append(sub.getSubParams().get("accountType").toString()).append("|")
                                .append("epargneNom=").append(sub.getSubParams().get("epargneNom").toString()).append("|")
                                .append("epargnePrenom=").append(sub.getSubParams().get("epargnePrenom").toString())
                                .append("epargneDateNaissance=").append(sub.getSubParams().get("epargneDateNaissance").toString())
                                .append("epargneTelephone=").append(sub.getSubParams().get("epargneTelephone").toString())
                                .append("termes=").append(sub.getSubParams().get("termes").toString());
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
                        /*} else {
                            //cancel transaction
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                            return resp;
                        }*/
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
                }else{
                    try {
                            /*String[] parts = request.getMsisdn().split("229");
                            String number = parts[1]; // Phone Number without 229
                            sub.getSubParams().put("NUMBER", number);*/
                            JsonObject getAuthorizationPayment = new JsonObject();
                            //getAuthorizationPayment.addProperty("tel", sub.getSubParams().get("NUMBER").toString());
                            //getAuthorizationPayment.addProperty("id", sub.getSubParams().get("identifiantPret").toString());
                            final String message = new HTTPUtil().getList(getAuthorizationPayment, "#url"); //termes
                            sub.getSubParams().put("JSON_RESPONSE", message);
                            resp.setApplicationResponse(message + "\n1. Suivant");
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        } catch (Exception ex) {
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid input entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                        }
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
    
    private UssdResponse processFinanciaLevel10Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Financia menu level9 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int requestType, option;
        final String listString;
        final Gson gson = new Gson();

        requestType = Integer.parseInt(sub.getSubParams().get("requestType").toString());
        switch (requestType) {
            //Ouverture
            case 1:
                option = Integer.parseInt(request.getSubscriberInput());
                sub.getSubParams().put("termes", option); //termes  
                // message final if success
                resp.setMsisdn(request.getMsisdn());
                try {
                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                    //if (pay == 1) {
                    Logger.getLogger("qos_ussd_processor").info("processing financia transaction for: " + request.getMsisdn());
                    JsonObject requestPayment = new JsonObject();
                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                    requestPayment.addProperty("amount", sub.getAmount());
                    final StringBuilder transref = new StringBuilder();
                    transref.append("requestType=").append(sub.getSubParams().get("requestType").toString()).append("|")
                            .append("accountType=").append(sub.getSubParams().get("accountType").toString()).append("|")
                            .append("epargneNom=").append(sub.getSubParams().get("epargneNom").toString()).append("|")
                            .append("epargnePrenom=").append(sub.getSubParams().get("epargnePrenom").toString())
                            .append("epargneDateNaissance=").append(sub.getSubParams().get("epargneDateNaissance").toString())
                            .append("epargneTelephone=").append(sub.getSubParams().get("epargneTelephone").toString())
                            .append("termes=").append(sub.getSubParams().get("termes").toString());
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
                    /*} else {
                        //cancel transaction
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }*/
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
        Logger.getLogger("qos_ussd_processor").info("showing financia main menu to: " + sub.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(sub.getMsisdn());
        //BPS_MAIN_MENU=Welcome to {MERCHANT_NAME}. Enter phone number:
        final String respMessage = "{MERCHANT_NAME}\n"
                + "1. Ouverture de compte\n"
                + "2. Connexion à votre compte\n"
                + "\n"
                + "Choisissez une option puis appuyer sur envoyer"
                        .replace("{MERCHANT_NAME}", sub.getMerchantName());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        //sub.setMerchantName();
        activeSessions.put(sub.getMsisdn(), sub);
        return resp;
    }

}
