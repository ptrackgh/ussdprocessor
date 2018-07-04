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
import static com.qos.ussd.main.USSDSessionHandler.activeSessions;
import com.qos.ussd.util.AradHelper;
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import com.qos.ussd.util.arad.dto.Agency;
import com.qos.ussd.util.arad.dto.TravelItenary;
import com.qos.ussd.util.arad.dto.TravelTime;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

/**
 *
 * @author ptrack
 */
public class AradMenus {
    
    private final Pattern datePattern = Pattern.compile("^\\d{8}$");
    private final DecimalFormat df = new DecimalFormat("#,##0");//new DecimalFormat("#,##0.00");
    final static String arad_agency_list_url = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_AGENCY_LIST_URL.toString());
    final static String arad_travel_iternary_url = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRAVEL_ITENARY_URL.toString());
    final static String arad_travel_times_url = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRAVEL_TIMES_URL.toString());
    
    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req){
        switch (sub.getMenuLevel()) {
                case 2:
                    return processAradLevel2Menu(sub, req);
                case 3:
                    return processAradLevel3Menu(sub, req);
                case 4:
                    return processAradLevel4Menu(sub, req);
                case 5:
                    return processAradLevel5Menu(sub, req);
                case 6:
                    return processAradLevel6Menu(sub, req);
                case 7:
                    return processAradLevel7Menu(sub, req);
                case 8:
                    return processAradLevel8Menu(sub, req);
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
    
    private UssdResponse processAradLevel2Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 3) {
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
        final String respMessage;
        switch (option) {
            case 1:
                sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                sub.setAradDetails(new AradHelper());
                //final ArrayList agencyList
                final String agencyListString = new HTTPUtil().sendGetRequest(arad_agency_list_url);
                final Gson gson = new Gson();
                //logs = gson.fromJson(agencyListString, new TypeToken<List<Agency>>(){}.getType());
                //final ArrayList<Agency> user = gson.fromJson(agencyListString, ArrayList.class);
                try{
                    final ArrayList<Agency> user = gson.fromJson(agencyListString, new TypeToken<List<Agency>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_SELECT_COMPANY.toString()));
                    int i=0;
                    for(final Agency agency: user){
                        msgResp.append("\n").append(++i).append(". ").append(agency.getLibelle());
                    }
                    sub.setAgencyList(user);
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
                
            //break;
            case 2:
                sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.STATUS);
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_MAIN_MENU.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            default:
                sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.REPORT);
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_MAIN_MENU.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
        }
    }

    private UssdResponse processAradLevel3Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String respMessage;
        //if (null != transactionType) {
        switch (sub.getTransactionType()) {
            case RESERVATION:
                final int option;
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    if (option < 1 || option > sub.getAgencyList().size()) {
                        throw new NumberFormatException();
                    }
                    //sub.getAradDetails().setCompany(UssdConstants.ARAD_COMPANIES[option]);
                    sub.setSelectedAgency(option-1);
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
//                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_SELECT_DEPARTURE.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.CONTINUE);
//                sub.incrementMenuLevel();
//                activeSessions.put(request.getMsisdn(), sub);
//                return resp;
                final String agencyListString = new HTTPUtil().sendGetRequest(arad_travel_iternary_url+sub.getAgencyList().get(sub.getSelectedAgency()).getId());
                final Gson gson = new Gson();
                //logs = gson.fromJson(agencyListString, new TypeToken<List<Agency>>(){}.getType());
                //final ArrayList<Agency> user = gson.fromJson(agencyListString, ArrayList.class);
                try{
                    final ArrayList<TravelItenary> user = gson.fromJson(agencyListString, new TypeToken<List<TravelItenary>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_SELECT_ITENARY.toString()));
                    int i=0;
                    for(final TravelItenary itenary: user){
                        msgResp.append("\n").append(++i).append(". ").append(itenary.getDeparture())
                                .append(" -> ").append(itenary.getDestination()).append(" ").append(itenary.getPrice());
                    }
                    //sub.setMerchantDetails(user);
                    //respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_SELECT_COMPANY.toString());
                    //resp.setApplicationResponse(respMessage);
                    sub.setTravelItenaryList(user);
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
            case STATUS:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
            default:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
        //}
    }

    private UssdResponse processAradLevel4Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level4 {Select Departure} for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String respMessage;
        //if (null != transactionType) {
        switch (sub.getTransactionType()) {
            case RESERVATION:
                final int option;
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    if (option < 1 || option > sub.getTravelItenaryList().size()) {
                        throw new NumberFormatException();
                    }
                    //sub.getAradDetails().setDeparture(UssdConstants.ARAD_DEPARTURE_LOCATIONS[option]);
                    sub.setSelectedTravelItenary(option - 1);
                    Logger.getLogger("qos_ussd_processor").info("selected itenary {" + (option - 1) + "}");
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
//                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_SELECT_DESTINATION.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.CONTINUE);
//                sub.incrementMenuLevel();
//                activeSessions.put(request.getMsisdn(), sub);
//                return resp;
                final String agencyListString = new HTTPUtil().sendGetRequest(arad_travel_times_url+sub.getAgencyList().get(sub.getSelectedAgency()).getId());
                final Gson gson = new Gson();
                //logs = gson.fromJson(agencyListString, new TypeToken<List<Agency>>(){}.getType());
                //final ArrayList<Agency> user = gson.fromJson(agencyListString, ArrayList.class);
                try{
                    final ArrayList<TravelTime> user = gson.fromJson(agencyListString, new TypeToken<List<TravelTime>>(){}.getType());
                    final StringBuilder msgResp = new StringBuilder(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_SELECT_TIME.toString()));
                    int i=0;
                    for(final TravelTime itenary: user){
                        msgResp.append("\n").append(++i).append(". ").append(itenary.getTime());
                    }
                    sub.setTravelTimeList(user);
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
            case STATUS:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
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

    private UssdResponse processAradLevel5Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level5 {SELECT DESTINATION} for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        String respMessage;
        switch (sub.getTransactionType()) {
            case RESERVATION:
                final int option;
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    if (option < 1 || option > sub.getTravelTimeList().size()) {
                        throw new NumberFormatException();
                    }
//                    final String destination = UssdConstants.ARAD_DESTINATION_LOCATIONS[option];
//                    if (sub.getAradDetails().getDeparture().equals(destination)) {
//                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_AND_DESTINATION_SAME.toString());
//                        resp.setApplicationResponse(respMessage);
//                        resp.setFreeflow(UssdConstants.BREAK);
//                        Logger.getLogger("qos_ussd_processor").info("detination {" + destination + "} is same as departure" + sub.getAradDetails().getDeparture());
//                        activeSessions.remove(request.getMsisdn());
//                        return resp;
//                    }
                    //sub.getAradDetails().setDeparture(UssdConstants.ARAD_DESTINATION_LOCATIONS[option]);
                    sub.setSelectedTravelTime(option - 1);
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_ENTER_DEPARTURE_DATE.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case STATUS:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
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

    private UssdResponse processAradLevel6Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level6 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        String respMessage;
        //if (null != transactionType) {
        switch (sub.getTransactionType()) {
            case RESERVATION:
                final int option;
                if (datePattern.matcher(request.getSubscriberInput()).matches()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                    final Date depatureDate;
                    try {
                        depatureDate = sdf.parse(request.getSubscriberInput());
                        Calendar now = Calendar.getInstance();
                        now.set(Calendar.HOUR_OF_DAY, 23);
                        if (depatureDate.before(now.getTime())) {//compares date portions only
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
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
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }
                } else {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
//                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_SELECT_DEPARTURE_TIME.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.CONTINUE);
//                sub.incrementMenuLevel();
//                activeSessions.put(request.getMsisdn(), sub);
//                return resp;
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_NO_OF_PERSONS.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case STATUS:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
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

//    private UssdResponse processAradLevel7Menu(SubscriberInfo sub, UssdRequest request) {
//        Logger.getLogger("qos_ussd_processor").info("Arad menu level7 for " + request.getMsisdn());
//        final UssdResponse resp = new UssdResponse();
//        resp.setMsisdn(request.getMsisdn());
//        final String respMessage;
//        //if (null != transactionType) {
//        switch (sub.getTransactionType()) {
//            case RESERVATION:
//                final int option;
//                try {
//                    option = Integer.parseInt(request.getSubscriberInput());
//                    if (option < 1 || option > 5) {
//                        throw new NumberFormatException();
//                    }
//                    if (sub.isIsDepartureToday()) {
//                        //Integer departTime = Integer.parseInt(UssdConstants.ARAD_DEPARTURE_TIMES[option].split(":")[0]);
//                        Integer departTime = Integer.parseInt(UssdConstants.ARAD_DEPARTURE_TIMES[option].split(":")[0]);
//                        Calendar now = Calendar.getInstance();
//                        if (departTime < now.get(Calendar.HOUR_OF_DAY)) {
//                            //respMessage = UssdConstants.MESSAGES.getProperty(MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
//                            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString()));
//                            resp.setFreeflow(UssdConstants.BREAK);
//                            Logger.getLogger("qos_ussd_processor").info("hour entered{" + departTime + "} is before current hour:" + now.get(Calendar.HOUR_OF_DAY));
//                            activeSessions.remove(request.getMsisdn());
//                            return resp;
//                        }
//                    }
//                    sub.getAradDetails().setDepartureTime(UssdConstants.ARAD_DEPARTURE_TIMES[option]);
//                } catch (NumberFormatException ex) {
//                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
//                    resp.setApplicationResponse(respMessage);
//                    resp.setFreeflow(UssdConstants.BREAK);
//                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
//                    activeSessions.remove(request.getMsisdn());
//                    return resp;
//                }
//                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_NO_OF_PERSONS.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.CONTINUE);
//                sub.incrementMenuLevel();
//                activeSessions.put(request.getMsisdn(), sub);
//                return resp;
//            case STATUS:
//                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.BREAK);
//                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
//                activeSessions.remove(request.getMsisdn());
//                return resp;
//            default:
//                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
//                resp.setApplicationResponse(respMessage);
//                resp.setFreeflow(UssdConstants.BREAK);
//                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
//                activeSessions.remove(request.getMsisdn());
//                return resp;
//        }
//    }

    //private UssdResponse processAradLevel8Menu(SubscriberInfo sub, UssdRequest request) {
    private UssdResponse processAradLevel7Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level8 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String respMessage;
        //if (null != transactionType) {
        switch (sub.getTransactionType()) {
            case RESERVATION:
                final int option;
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    sub.getAradDetails().setPlaces(option);
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
                String msg1 = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_CONFIRM_PURCHASE.toString());
                msg1 = msg1.replace("{SEATS}", df.format(new BigDecimal(250).multiply(new BigDecimal(sub.getAradDetails().getPlaces())).doubleValue()));
                msg1 = msg1.replace("{FEE}", df.format(new BigDecimal(sub.getTravelItenaryList().get(sub.getSelectedTravelItenary()).getPrice())
                                .multiply(new BigDecimal(sub.getAradDetails().getPlaces()))));
                sub.setAmount(
                                new BigDecimal(sub.getTravelItenaryList().get(sub.getSelectedTravelItenary()).getPrice())
                                .multiply(new BigDecimal(sub.getAradDetails().getPlaces()))
                                .add(new BigDecimal(250).multiply(new BigDecimal(sub.getAradDetails().getPlaces())))
                );
                msg1 = msg1.replace("{AMOUNT}", df.format(sub.getAmount().doubleValue()));
                Logger.getLogger("qos_ussd_processor").info("configured msg: " + msg1);
                
                //respMessage = UssdConstants.MESSAGES.getProperty(msg1);
                resp.setApplicationResponse(msg1);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            case STATUS:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
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

    //private UssdResponse processAradLevel9Menu(SubscriberInfo sub, UssdRequest request) {
    private UssdResponse processAradLevel8Menu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level9 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        String respMessage;
        //if (null != transactionType) {
        switch (sub.getTransactionType()) {
            case RESERVATION:
                final int option;
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    if (option == 1) {
                        //make reservation

                        Logger.getLogger("qos_ussd_processor").info("processing arad transaction for{" + sub.getAradDetails().toString() + "} by" + request.getMsisdn());
                        JsonObject requestPayment = new JsonObject();
                        requestPayment.addProperty("msisdn", sub.getMsisdn());
                        requestPayment.addProperty("amount", sub.getAmount());
                        //requestPayment.addProperty("amount", sub.getTravelItenaryList().get(sub.getSelectedTravelItenary()).getPrice());
                        final StringBuilder transref = new StringBuilder();
                        transref.append("agence=").append(sub.getAgencyList().get(sub.getSelectedAgency()).getId()).append("|")
                                .append("agence.tarif=").append(sub.getTravelItenaryList().get(sub.getSelectedTravelItenary()).getId()).append("|")
                                .append("agence.time=").append(sub.getTravelTimeList().get(sub.getSelectedTravelTime()).getId()).append("|")
                                .append("person=").append(sub.getAradDetails().getPlaces()).append("|")
                                .append("agence.date=").append(new SimpleDateFormat("yyyyMMdd").format(sub.getAradDetails().getDepartureDate())).append("|")
                                .append("msisdn=").append(sub.getMsisdn()).append("|")
                                .append("sessionid=").append(request.getSessionId());
                        requestPayment.addProperty("transref", request.getSessionId());
                        //requestPayment.addProperty("transref", transref.toString());
                        requestPayment.addProperty("specialfield1", transref.toString());
                        requestPayment.addProperty("clientid", sub.getMerchantCode());

                        final String response = new HTTPUtil().sendRequestPayment(requestPayment);
                        if (response.equals("")) {
                            Logger.getLogger("qos_ussd_processor").info("sendRequestPayment returned empty response");
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            JsonParser parseResponse = new JsonParser();
                            JsonObject jo = (JsonObject) parseResponse.parse(response);
                            if (jo.get("responsecode").getAsString().equals("01")) {
                                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRANSACTION_PROCESSING.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            }
                        }
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    } else {
                        //cancel transaction
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions.remove(request.getMsisdn());
                        return resp;
                    }
                    //aradDetails.setDepartureTime(UssdConstants.ARAD_DEPARTURE_TIMES[option]);
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                } catch (JsonSyntaxException ex) {
                    Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions.remove(request.getMsisdn());
                    return resp;
                }
            //break;
            case STATUS:
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
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

    public static CloseableHttpClient getHttpClient(String s)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        if (s.toLowerCase().startsWith("https")) {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
                    .build();
            return HttpClients.custom().setSslcontext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
        } else {
            return HttpClients.createDefault();
        }
    }
    
}
