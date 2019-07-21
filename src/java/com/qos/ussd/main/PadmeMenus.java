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
import com.qos.ussd.util.padme.dto.TUsUsuarios;
import com.qos.ussd.util.sunu.dto.Infocontrat;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author Malkiel
 */
public class PadmeMenus {

    private final Pattern phonePattern = Pattern.compile("^\\d{10,12}$");
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req) {
        Logger.getLogger("qos_ussd_processor").info("calling Padme menu @step: " + sub.getMenuLevel());
        switch (sub.getMenuLevel()) {
            case 2:
                return processPadmeLevel2Menu(sub, req);
            case 3:
                return processPadmeLevel3Menu(sub, req);
            case 4:
                return processPadmeLevel4Menu(sub, req);
            case 5:
                return processPadmeLevel5Menu(sub, req);
            case 6:
                return processPadmeLevel6Menu(sub, req);
            case 7:
                return processPadmeLevel7Menu(sub, req);
            case 8:
                return processPadmeLevel8Menu(sub, req);
            case 9:
                return processPadmeLevel9Menu(sub, req);
            case 10:
                return processPadmeLevel10Menu(sub, req);
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

    private UssdResponse processPadmeLevel2Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId;
        try {
            itemId = Integer.parseInt(request.getSubscriberInput());
            if (itemId < 1 || itemId > 6) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("itemId", itemId); //itemId
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
        
        final Gson gson = new Gson();
        final String listString;

        switch (itemId) {
            //Depôt
            case 1:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Dépôt sur :\n"
                        + "1. Epargne à vue\n"
                        + "2. Plan tontine\n"
                        + "3. Compte courant\n"
                        + "0. Retour"
                        + "\n"
                        + "Sélectionner un numéro puis appuyer sur envoyer");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Retrait
            case 2:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Retrait du compte :\n"
                        + "1. Epargne à vue\n"
                        + "2. Compte courant\n"
                        + "0. Retour"
                        + "\n"
                        + "Sélectionner un numéro puis appuyer sur envoyer");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Prêt
            case 3:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Prêt PADME\n" 
                        + "1. Remboursement \n"
                        + "2. Demande de prêt \n"
                        + "3. Etat du prêt \n"
                        + "0. Retour"
                        + "\n"
                        + "Sélectionner un numéro puis appuyer sur envoyer");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Transfert
            case 4:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Transférer sur votre compte : \n" 
                        + "1. Epargne à vue \n"
                        + "2. Courant \n"
                        + "\n"
                        + "Sélectionner un numéro puis appuyer sur envoyer");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Gestion du compte
            case 5:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Gestion des comptes:\n" 
                        + "1. Solde\n" 
                        + "2. Termes et condition"
                        + "\n"
                        + "Sélectionner un numéro puis appuyer sur envoyer");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Gestion du compte
            case 6:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse("Opérations pour tiers\n" 
                        +   "1. Depôt sur compte de tiers\n" 
                        +   "2. Remboursement sur compte de tiers\n"
                        + "Sélectionner un numéro puis appuyer sur envoyer");
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

    private UssdResponse processPadmeLevel3Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level3 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, operation, compteType1;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
            //Depôt
            case 1:
                try {
                    compteType1 = Integer.parseInt(request.getSubscriberInput());
                    if (compteType1 < 1 || compteType1 > 3) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("compteType1", compteType1); //compteType1
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                switch(compteType1){
                    //Epargne à vue
                    case 1:
                        final String AccountEpargneString = new HTTPUtil().sendGetRequest("http://74.208.84.251:8222/padme-digitalfinance-api/cuenta/103/" + sub.getMsisdn());
                        String[] AcctEpargne = AccountEpargneString.split("ï»¿");
                        String jsonEpargne = AcctEpargne[1];
                        //TUsUsuarios[] user = gson.fromJson(jsonEpargne, TUsUsuarios[].class);
                        //sub.getSubParams().put("TUsUsuarios", user[0]);
                    //Plan tontine
                    case 2:
                        final String AccountTontineString = new HTTPUtil().sendGetRequest("http://74.208.84.251:8222/padme-digitalfinance-api/cuenta/105/" + sub.getMsisdn());
                        String[] AcctTontine = AccountTontineString.split("ï»¿");
                        String jsonTontine = AcctTontine[1];
                        //TUsUsuarios[] user = gson.fromJson(json, TUsUsuarios[].class);
                        //sub.getSubParams().put("TUsUsuarios", user[0]);
                    //Compte Courant
                    case 3:
                        final String AccountCourantString = new HTTPUtil().sendGetRequest("http://74.208.84.251:8222/padme-digitalfinance-api/cuenta/104/" + sub.getMsisdn());
                        String[] AcctCourant = AccountCourantString.split("ï»¿");
                        String jsonCourant = AcctCourant[1];
                        //TUsUsuarios[] user = gson.fromJson(jsonCourant, TUsUsuarios[].class);
                        //sub.getSubParams().put("TUsUsuarios", user[0]);
                    default:
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                }
                
                resp.setApplicationResponse("Epargne\n"
                        + "Veuillez saisir le montant : ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Retrait
            case 2:
                try {
                    compteType1 = Integer.parseInt(request.getSubscriberInput());
                    if (compteType1 < 1 || compteType1 > 2) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("compteType1", compteType1); //compteType1
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                switch(compteType1){
                    //Epargne à vue
                    case 1:
                        final String AccountEpargneString = new HTTPUtil().sendGetRequest("http://74.208.84.251:8222/padme-digitalfinance-api/cuenta/103/" + sub.getMsisdn());
                        String[] AcctEpargne = AccountEpargneString.split("ï»¿");
                        String jsonEpargne = AcctEpargne[1];
                        //TUsUsuarios[] user = gson.fromJson(jsonEpargne, TUsUsuarios[].class);
                        //sub.getSubParams().put("TUsUsuarios", user[0]);
                    //Compte Courant
                    case 2:
                        final String AccountCourantString = new HTTPUtil().sendGetRequest("http://74.208.84.251:8222/padme-digitalfinance-api/cuenta/104/" + sub.getMsisdn());
                        String[] AcctCourant = AccountCourantString.split("ï»¿");
                        String jsonCourant = AcctCourant[1];
                        //TUsUsuarios[] user = gson.fromJson(jsonCourant, TUsUsuarios[].class);
                        //sub.getSubParams().put("TUsUsuarios", user[0]);
                    default:
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                }
                
                resp.setApplicationResponse("Epargne\n"
                        + "Veuillez saisir le montant : ");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            //Pret
            case 3:
                try {
                    operation = Integer.parseInt(request.getSubscriberInput());
                    if (operation < 1 || operation > 3) {
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
                String respMessage = null;
                switch(operation){
                    case 1:
                        respMessage = "Remboursement de prêt\n" 
                                +     "1. Montant à payer pour régulariser : 100 fcfa  \n" 
                                +     "2. Prochaine échéance  : 50 fcfa (Exigible au plus tard le jj-mm-aaaa) \n" 
                                +     "3. Autre montant à payer\n" 
                                +     "Sélectionner un numéro puis appuyer sur envoyer \n";
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    case 2:
                        respMessage = "Veuillez saisir le montant sollicité: \n";
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    case 3:
                        respMessage = "Etat du prêt :\n" 
                                +     "Montant du crédit : xxxxxxx\n" 
                                +     "Montant échéance : xxxxxxx\n" 
                                +     "Montant impayé : xxxxxxx\n" 
                                +     "Reste à solder : xxxxxxx\n" 
                                +     "Nombre d'échances restant : XX\n" 
                                +     "Date de la dernière échance : xx/xx/xxxx ";
                        
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    default:
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        
                }
                resp.setApplicationResponse(respMessage);
                return resp;
            //Transfert
            case 4:
                try {
                    compteType1 = Integer.parseInt(request.getSubscriberInput());
                    if (compteType1 < 1 || compteType1 > 2) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("compteType1", compteType1); //compteType1
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                switch(compteType1){
                    case 1:
                        respMessage = "A partir de votre compte :\n"
                                +     "1. Courant  \n" ;
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    case 2:
                        respMessage = "A partir de votre compte :\n"
                                +     "1. Epargne à vue  \n";
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    default:
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        
                }
                resp.setApplicationResponse(respMessage);
                return resp;
            //Gestion du Compte
            case 5:
                final int compteType;
                try {
                    compteType = Integer.parseInt(request.getSubscriberInput());
                    if (compteType < 1 || compteType > 2) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("compteType", compteType); //compteType
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                
                switch(compteType){
                    case 1:
                        respMessage = "Solde :\n" 
                                +     "1. Compte epargne\n" 
                                +     "2. Compte plan tontine\n" 
                                +     "3. Compte courant\n" ;
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    case 2:
                        respMessage = "Les 4 prochaines pages afficheront les termes et les conditions.\n" 
                                +     "1. Suivant\n";
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                    default:
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        
                }
                resp.setApplicationResponse(respMessage);
                return resp;
            //Operations des tiers
            case 6:
                try {
                    final int operationId = Integer.parseInt(request.getSubscriberInput());
                    if (operationId < 1 || operationId > 2) {
                        throw new NumberFormatException();
                    }
                    sub.getSubParams().put("operationId", operationId); //operationId
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                resp.setApplicationResponse("Veuillez saisir le numero de téléphone du tiers");
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
                
            default:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }

    private UssdResponse processPadmeLevel4Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level4 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, operation, operationType, compteType1, option;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
            //Depôt
            case 1:
                compteType1  = Integer.parseInt(sub.getSubParams().get("compteType1").toString());
                try {
                    double amount = Double.parseDouble(request.getSubscriberInput());
                    sub.getSubParams().put("montant", amount); //montant
                    sub.setAmount(new BigDecimal(amount));
                    //BPS_CONFIRM_TRANSACTION=Debit of {AMOUNT} from {PHONE_NUMBER}. Enter 1 to confirm:
                    Logger.getLogger("qos_ussd_processor").info(String.format("{%s} entered {%s} amount",
                            request.getMsisdn(), request.getSubscriberInput()));
                    final String respMessage = "Transfert de xxxxx fcfa de votre compte momo sur votre compte padme épargne à vue.\nFrais : xxxxxxxx fcfa"
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
            //Retrait
            case 2:
                compteType1  = Integer.parseInt(sub.getSubParams().get("compteType1").toString());
                try {
                    double amount = Double.parseDouble(request.getSubscriberInput());
                    sub.getSubParams().put("montant", amount); //montant
                    sub.setAmount(new BigDecimal(amount));
                    //BPS_CONFIRM_TRANSACTION=Debit of {AMOUNT} from {PHONE_NUMBER}. Enter 1 to confirm:
                    Logger.getLogger("qos_ussd_processor").info(String.format("{%s} entered {%s} amount",
                            request.getMsisdn(), request.getSubscriberInput()));
                    final String respMessage = "Transfert de xxxxx fcfa de votre compte momo sur votre compte padme épargne à vue.\nFrais : xxxxxxxx fcfa"
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

                //Pret
                case 3:
                    operation = Integer.parseInt(sub.getSubParams().get("operation").toString());
                    String respMessage = null;
                    switch(operation){
                        //Remboursement de prêt
                        case 1:
                            final int compteType;
                            try {
                                compteType = Integer.parseInt(request.getSubscriberInput());
                                if (compteType < 1 || compteType > 3) {
                                    throw new NumberFormatException();
                                }
                                sub.getSubParams().put("compteType", compteType); //compteType
                            } catch (NumberFormatException ex) {
                                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                activeSessions.remove(request.getMsisdn());
                                return resp;
                            }

                            switch(compteType){
                                case 1:
                                case 2:
                                    respMessage = "Rembourser de votre compte :\n" +
                                                  "1. MoMo\n" +
                                                  "2. Epargne à vue" ;
                                    resp.setFreeflow(UssdConstants.CONTINUE);
                                    sub.incrementMenuLevel();
                                    activeSessions.put(request.getMsisdn(), sub);
                                case 3:
                                    respMessage = "Veuillez saisir le montant à rembourser :";
                                    resp.setFreeflow(UssdConstants.CONTINUE);
                                    sub.incrementMenuLevel();
                                    activeSessions.put(request.getMsisdn(), sub);
                                default:
                                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                                    resp.setFreeflow(UssdConstants.BREAK);
                                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                    activeSessions.remove(request.getMsisdn());

                            }
                        case 2:
                            respMessage = "Veuillez saisir le montant sollicité: \n";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        case 3:
                            respMessage = "Etat du prêt :\n" 
                                    +     "Montant du crédit : xxxxxxx\n" 
                                    +     "Montant échéance : xxxxxxx\n" 
                                    +     "Montant impayé : xxxxxxx\n" 
                                    +     "Reste à solder : xxxxxxx\n" 
                                    +     "Nombre d'échances restant : XX\n" 
                                    +     "Date de la dernière échance : xx/xx/xxxx ";

                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        default:
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());

                    }
                    resp.setApplicationResponse(respMessage);
                    return resp;
                // Transfert
                case 4:
                    final int compteType;
                    try {
                        compteType = Integer.parseInt(request.getSubscriberInput());
                        if (compteType < 1 || compteType > 2) {
                            throw new NumberFormatException();
                        }
                        sub.getSubParams().put("compteType", compteType); //compteType
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }

                    switch(compteType){
                        case 1:
                            respMessage = "A partir de votre compte :\n" +
                                          "1. Courant";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        case 2:
                            respMessage = "A partir de votre compte :\n" +
                                          "1. Epargne à vue";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        default:
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());

                    }
                    return resp;
                // Gestion du compte
                case 5:
                    try {
                        compteType = Integer.parseInt(request.getSubscriberInput());
                        if (compteType < 1 || compteType > 2) {
                            throw new NumberFormatException();
                        }
                        sub.getSubParams().put("compteType", compteType); //compteType
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }

                    switch(compteType){
                        case 1:
                            respMessage = "Solde :\n" +
                                        "1. Compte epargne\n" +
                                        "2. Compte plan tontine\n" +
                                        "3. Compte courant";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        case 2:
                            respMessage = "Les 4 prochaines pages afficheront les termes et les conditions.\n" +
                                            "1. Suivant\n" +
                                            "0. Retour";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        default:
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                    }
                    return resp;
                //Operations pour tiers
                case 6:
                    try {
                        compteType = Integer.parseInt(request.getSubscriberInput());
                        if (compteType < 1 || compteType > 2) {
                            throw new NumberFormatException();
                        }
                        sub.getSubParams().put("compteType", compteType); //compteType
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }

                    resp.setApplicationResponse("Veuillez saisir le numero de téléphone du tiers");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                default:
                    final String respMsg = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMsg);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
            }
    }

    private UssdResponse processPadmeLevel5Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, operation, operationType, option;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
            //Depôt
            case 1:
                // message final if success
                resp.setMsisdn(request.getMsisdn());
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
                                .append("compteType1=").append(sub.getSubParams().get("compteType1").toString()).append("|")
                                .append("numCompte1=").append(sub.getSubParams().get("numCompte1").toString()).append("|")
                                .append("montant=").append(sub.getSubParams().get("montant").toString())
                                .append("date=").append(sub.getSubParams().get("date").toString());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = "Transfert de xxxxx fcfa de votre compte momo sur votre compte padme épargne à vue.\nFrais : xxxxxxxx fcfa"; //UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
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
                    }else{
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
            //Retrait
            case 2:
                // message final if success
                resp.setMsisdn(request.getMsisdn());
                try {
                    final int pay = Integer.parseInt(request.getSubscriberInput());
                    if (pay == 1) {
                        Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
                                .append("compteType1=").append(sub.getSubParams().get("compteType1").toString()).append("|")
                                .append("numCompte1=").append(sub.getSubParams().get("numCompte1").toString()).append("|")
                                .append("montant=").append(sub.getSubParams().get("montant").toString())
                                .append("date=").append(sub.getSubParams().get("date").toString());
                        requestPayment.addProperty("transref", request.getSessionId());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            final String respMessage = "Transfert de xxxxx fcfa de votre compte momo sur votre compte padme épargne à vue.\nFrais : xxxxxxxx fcfa"; //UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
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
                    }else{
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
            //Pret
                case 3:
                    operation = Integer.parseInt(sub.getSubParams().get("operation").toString());
                    String respMessage = null;
                    switch(operation){
                        //Remboursement de prêt
                        case 1:
                            final int compteType;
                            try {
                                compteType = Integer.parseInt(request.getSubscriberInput());
                                if (compteType < 1 || compteType > 3) {
                                    throw new NumberFormatException();
                                }
                                sub.getSubParams().put("compteType", compteType); //compteType
                            } catch (NumberFormatException ex) {
                                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                activeSessions.remove(request.getMsisdn());
                                return resp;
                            }

                            switch(compteType){
                                case 1:
                                case 2:
                                    final int RemcompteType;
                                    try {
                                        RemcompteType = Integer.parseInt(request.getSubscriberInput());
                                        if (RemcompteType < 1 || RemcompteType > 3) {
                                            throw new NumberFormatException();
                                        }
                                        sub.getSubParams().put("RemcompteType", RemcompteType); //Rembourser de votre compte
                                    } catch (NumberFormatException ex) {
                                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                                        resp.setApplicationResponse(respMessage);
                                        resp.setFreeflow(UssdConstants.BREAK);
                                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                        activeSessions.remove(request.getMsisdn());
                                        return resp;
                                    }
                                    
                                    switch(compteType){
                                        case 1:
                                            respMessage = "A partir de votre compte :\n" +
                                                          "1. Courant";
                                            resp.setFreeflow(UssdConstants.CONTINUE);
                                            sub.incrementMenuLevel();
                                            activeSessions.put(request.getMsisdn(), sub);
                                        case 2:
                                            respMessage = "A partir de votre compte :\n" +
                                                          "1. Epargne à vue";
                                            resp.setFreeflow(UssdConstants.CONTINUE);
                                            sub.incrementMenuLevel();
                                            activeSessions.put(request.getMsisdn(), sub);
                                        default:
                                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                                            resp.setFreeflow(UssdConstants.BREAK);
                                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                            activeSessions.remove(request.getMsisdn());

                                    }
                                    return resp;
                                    
                                case 3:
                                    respMessage = "Veuillez saisir le montant à rembourser :";
                                    resp.setFreeflow(UssdConstants.CONTINUE);
                                    sub.incrementMenuLevel();
                                    activeSessions.put(request.getMsisdn(), sub);
                                default:
                                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                                    resp.setFreeflow(UssdConstants.BREAK);
                                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                                    activeSessions.remove(request.getMsisdn());

                            }
                        case 2:
                            respMessage = "Veuillez saisir le montant sollicité: \n";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        case 3:
                            respMessage = "Etat du prêt :\n" 
                                    +     "Montant du crédit : xxxxxxx\n" 
                                    +     "Montant échéance : xxxxxxx\n" 
                                    +     "Montant impayé : xxxxxxx\n" 
                                    +     "Reste à solder : xxxxxxx\n" 
                                    +     "Nombre d'échances restant : XX\n" 
                                    +     "Date de la dernière échance : xx/xx/xxxx ";

                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        default:
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());

                    }
                    resp.setApplicationResponse(respMessage);
                    return resp;
                // Transfert
                case 4:
                    final int compteType;
                    try {
                        compteType = Integer.parseInt(request.getSubscriberInput());
                        if (compteType < 1 || compteType > 2) {
                            throw new NumberFormatException();
                        }
                        sub.getSubParams().put("compteType", compteType); //compteType
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }

                    switch(compteType){
                        case 1:
                            respMessage = "A partir de votre compte :\n" +
                                          "1. Courant";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        case 2:
                            respMessage = "A partir de votre compte :\n" +
                                          "1. Epargne à vue";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        default:
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());

                    }
                    return resp;
                // Gestion du compte
                case 5:
                    try {
                        compteType = Integer.parseInt(request.getSubscriberInput());
                        if (compteType < 1 || compteType > 2) {
                            throw new NumberFormatException();
                        }
                        sub.getSubParams().put("compteType", compteType); //compteType
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }

                    switch(compteType){
                        case 1:
                            respMessage = "Solde :\n" +
                                        "1. Compte epargne\n" +
                                        "2. Compte plan tontine\n" +
                                        "3. Compte courant";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        case 2:
                            respMessage = "Les 4 prochaines pages afficheront les termes et les conditions.\n" +
                                            "1. Suivant\n" +
                                            "0. Retour";
                            resp.setFreeflow(UssdConstants.CONTINUE);
                            sub.incrementMenuLevel();
                            activeSessions.put(request.getMsisdn(), sub);
                        default:
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions.remove(request.getMsisdn());
                    }
                    return resp;
                //Operations pour tiers
                case 6:
                    try {
                        compteType = Integer.parseInt(request.getSubscriberInput());
                        if (compteType < 1 || compteType > 2) {
                            throw new NumberFormatException();
                        }
                        sub.getSubParams().put("compteType", compteType); //compteType
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }

                    resp.setApplicationResponse("Veuillez saisir le numero de téléphone du tiers");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    sub.incrementMenuLevel();
                    activeSessions.put(request.getMsisdn(), sub);
                    return resp;
                default:
                    final String respMsg = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMsg);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
            }
        
    }

    private UssdResponse processPadmeLevel6Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level6 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
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
                                        + "2. Padme");
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
                            Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", sub.getMsisdn());
                            requestPayment.addProperty("amount", sub.getAmount());
                            final StringBuilder transref = new StringBuilder();
                            transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
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

            // Padme Pay
            case 4:
                operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                switch (operationType) {
                    // Dépôt sur Padme Pay
                    case 1:
                        sub.getSubParams().put("accountNumber", request.getSubscriberInput()); //accountNumber
                        resp.setApplicationResponse("Entrer le montant :");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        sub.incrementMenuLevel();
                        activeSessions.put(request.getMsisdn(), sub);
                        return resp;
                    //Padme Pay sur Mobile Money
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

    private UssdResponse processPadmeLevel7Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level7 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
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
                                    Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                                    JsonObject requestPayment = new JsonObject();
                                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                                    requestPayment.addProperty("amount", sub.getAmount());
                                    final StringBuilder transref = new StringBuilder();
                                    transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
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

                    // Padme Pay
                    case 4:
                        operationType = Integer.parseInt(sub.getSubParams().get("operationType").toString());

                        switch (operationType) {
                            // Dépôt sur Padme Pay
                            case 1:
                                sub.getSubParams().put("montant", request.getSubscriberInput()); //montant
                                // message final if success
                                resp.setMsisdn(request.getMsisdn());
                                try {
                                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                                    //if (pay == 1) {
                                    Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                                    JsonObject requestPayment = new JsonObject();
                                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                                    requestPayment.addProperty("amount", sub.getAmount());
                                    final StringBuilder transref = new StringBuilder();
                                    transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
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
                            //Padme Pay sur Mobile Money
                            case 2:
                                sub.getSubParams().put("montant", request.getSubscriberInput()); //montant
                                // message final if success
                                resp.setMsisdn(request.getMsisdn());
                                try {
                                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                                    //if (pay == 1) {
                                    Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                                    JsonObject requestPayment = new JsonObject();
                                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                                    requestPayment.addProperty("amount", sub.getAmount());
                                    final StringBuilder transref = new StringBuilder();
                                    transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
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
    
    private UssdResponse processPadmeLevel8Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level8 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, operation, operationType;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
            //Ouverture
            case 1:
                sub.getSubParams().put("epargneTelephone", request.getSubscriberInput()); //epargneTelephone
                resp.setApplicationResponse("Inscription\n" +
                                            "Accepter les termes et conditions de Padme.\n" +
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
    
    private UssdResponse processPadmeLevel9Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level9 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, option;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
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
                        Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
                                .append("compteType1=").append(sub.getSubParams().get("compteType1").toString()).append("|")
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
    
    private UssdResponse processPadmeLevel10Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Padme menu level9 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int itemId, option;
        final String listString;
        final Gson gson = new Gson();

        itemId = Integer.parseInt(sub.getSubParams().get("itemId").toString());
        switch (itemId) {
            //Ouverture
            case 1:
                option = Integer.parseInt(request.getSubscriberInput());
                sub.getSubParams().put("termes", option); //termes  
                // message final if success
                resp.setMsisdn(request.getMsisdn());
                try {
                    //final int pay = Integer.parseInt(request.getSubscriberInput());
                    //if (pay == 1) {
                    Logger.getLogger("qos_ussd_processor").info("processing padme transaction for: " + request.getMsisdn());
                    JsonObject requestPayment = new JsonObject();
                    requestPayment.addProperty("msisdn", sub.getMsisdn());
                    requestPayment.addProperty("amount", sub.getAmount());
                    final StringBuilder transref = new StringBuilder();
                    transref.append("itemId=").append(sub.getSubParams().get("itemId").toString()).append("|")
                            .append("compteType1=").append(sub.getSubParams().get("compteType1").toString()).append("|")
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
        Logger.getLogger("qos_ussd_processor").info("showing padme main menu to: " + sub.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        final String respMessage;
        final Gson gson = new Gson();
        resp.setMsisdn(sub.getMsisdn());
        //BPS_MAIN_MENU=Welcome to {MERCHANT_NAME}. Enter phone number:
        final String AllowedUserString = new HTTPUtil().sendGetRequest("http://74.208.84.251:8222/padme-digitalfinance-api/tususuariodireccion/client/" + sub.getMsisdn());
        String[] AllowedUser = AllowedUserString.split("ï»¿");
                String json = AllowedUser[1];
                TUsUsuarios[] user = gson.fromJson(json, TUsUsuarios[].class);
                sub.getSubParams().put("TUsUsuarios", user[0]);
        if(user[0].getEmail().isEmpty()){
            respMessage = "Vous n'êtes pas authorisé";
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(sub.getMsisdn());
        }else{
            respMessage = "{MERCHANT_NAME}\n"
                    + "1. Dépôt\n" 
                    + "2. Retrait\n"
                    + "3. Prêt\n"
                    + "4. Transfert\n"
                    + "5. Gestion des comptes\n"
                    + "6. Opérations pour tiers\n"
                    + "0. Retour\n"
                    + "\n"
                    + "Sélectionner un numéro puis appuyer sur envoyer"
                            .replace("{MERCHANT_NAME}", sub.getMerchantName());
            
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(sub.getMsisdn(), sub);
        }
        resp.setApplicationResponse(respMessage);
        //sub.setMerchantName();
        return resp;
    }

}
