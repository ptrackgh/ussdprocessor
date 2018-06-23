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
import com.qos.ussd.util.arad.dto.TVMResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author ptrack
 */
public class TaxMenus {

    private final Pattern datePattern = Pattern.compile("^\\d{8}$");
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req) {
        Logger.getLogger("qos_ussd_processor").info("calling TVM menu @step: "+ sub.getMenuLevel());
        switch (sub.getMenuLevel()) {
            case 2:
                return processTaxMainMenu(sub, req);
            case 3:
                return processTaxImportTypeMenu(sub, req);
            case 4:
                return processTaxEnterVehicleRegMenu(sub, req);
            case 5:
                return processTaxEnterYearMenu(sub, req);
            case 8:
                return processTaxConfirmPaymentMenu(sub, req);
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

    private UssdResponse processTaxMainMenu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("processTaxMainMenu for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 2) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("TAX_TYPE", UssdConstants.TVM_TAX_TYPE[option]);
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_IMPORT_TYPE.toString()));
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        return resp;
    }

    private UssdResponse processTaxImportTypeMenu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("processTaxImportTypeMenu for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option >= UssdConstants.TVM_PAYER_TYPE.length) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("IMPORT_TYPE", UssdConstants.TVM_PAYER_TYPE[option]);
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_VEHICLE_REG.toString()));
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        return resp;
    }

    private UssdResponse processTaxEnterVehicleRegMenu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("processTexEnterVehicleRegMenu for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        try {
            sub.getSubParams().put("TVM_VEHICLE_REG", request.getSubscriberInput());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_YEAR.toString()));
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
        } catch (Exception ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        return resp;
    }

    private UssdResponse processTaxEnterYearMenu(SubscriberInfo sub, UssdRequest request) {
//        if (datePattern.matcher(request.getSubscriberInput()).matches()) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
//                    final Date depatureDate;
//                    try {
//                        depatureDate = sdf.parse(request.getSubscriberInput());
//                        Calendar now = Calendar.getInstance();
//                        now.set(Calendar.HOUR_OF_DAY, 23);
//                        if (depatureDate.before(now.getTime())) {//compares date portions only
//                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
//                            resp.setApplicationResponse(respMessage);
//                            resp.setFreeflow(UssdConstants.BREAK);
//                            Logger.getLogger("qos_ussd_processor").info("date entered{" + depatureDate + "} is before now():" + now);
//                            activeSessions.remove(request.getMsisdn());
//                            return resp;
//                        } else {
//                            sub.getAradDetails().setDepartureDate(depatureDate);
//                            final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
//                            if (fmt.format(now.getTime()).equals(fmt.format(depatureDate))) {
//                                sub.setIsDepartureToday(true);
//                            }
//                        }
//                    } catch (ParseException ex) {
//                        Logger.getLogger("qos_ussd_processor").info(ex);
//                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
//                        resp.setApplicationResponse(respMessage);
//                        resp.setFreeflow(UssdConstants.BREAK);
//                        Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
//                        activeSessions.remove(request.getMsisdn());
//                        return resp;
//                    }
//                }
        Logger.getLogger("qos_ussd_processor").info("processTaxEnterYearMenu for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        try {
            sub.getSubParams().put("YEAR", request.getSubscriberInput());
            JsonObject getTaxDetails = new JsonObject();
            getTaxDetails.addProperty("tvmAnnee", request.getSubscriberInput());
            getTaxDetails.addProperty("tvmMatricule", sub.getSubParams().get("TVM_VEHICLE_REG").toString());
            getTaxDetails.addProperty("typeTaxe", sub.getSubParams().get("TAX_TYPE").toString());
            getTaxDetails.addProperty("typeContrib", sub.getSubParams().get("IMPORT_TYPE").toString());
            final String response = new HTTPUtil().getTaxDetails(getTaxDetails);
            try {
                Gson resp1 = new Gson();
                TVMResponse tvmdetails = resp1.fromJson(response, TVMResponse.class);
                double amount = tvmdetails.droitsimple + tvmdetails.penalite;
                sub.getSubParams().put("AMOUNT", amount);
                sub.setAmount(new BigDecimal(amount));
                final String msg = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_CONFIRMATiON.toString()).replace("{AMOUNT}", sub.getAmount().toString());
                resp.setApplicationResponse(msg);
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                resp.setFreeflow(UssdConstants.CONTINUE);
            } catch (Exception ex) {
                resp.setApplicationResponse("Invalid TVM details entered");
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid details supplied for TVM request: " + request.getMsisdn());
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

    private UssdResponse processTaxConfirmPaymentMenu(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Arad menu level9 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        String respMessage;

        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option == 1) {
                //make reservation

                Logger.getLogger("qos_ussd_processor").info("processing arad transaction for{" + sub.getAradDetails().toString() + "} by" + request.getMsisdn());
                JsonObject requestPayment = new JsonObject();
                requestPayment.addProperty("msisdn", sub.getMsisdn());
                requestPayment.addProperty("amount", sub.getAmount());

                final StringBuilder transref = new StringBuilder();
                transref.append("tvmAnnee=").append(sub.getSubParams().get("YEAR").toString()).append("|")
                        .append("tvmMatricule=").append(sub.getSubParams().get("TVM_VEHICLE_REG").toString()).append("|")
                        .append("typeTaxe=").append(sub.getSubParams().get("TAX_TYPE").toString()).append("|")
                        .append("typeContrib=").append(sub.getSubParams().get("IMPORT_TYPE").toString());
                requestPayment.addProperty("transref", request.getSessionId());
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
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for TVM. user:" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
            }
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
    }

    UssdResponse showMainMenu(SubscriberInfo sub) {
        Logger.getLogger("qos_ussd_processor").info("showing tvm main menu to: " + sub.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(sub.getMsisdn());
        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.TVM_MAIN_MENU.toString());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        //sub.setMerchantName();
        activeSessions.put(sub.getMsisdn(), sub);
        return resp;
    }

}
