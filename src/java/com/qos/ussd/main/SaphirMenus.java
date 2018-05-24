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
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import com.qos.ussd.util.saphir.dto.SaphirCommonFundOption;
import com.qos.ussd.util.saphir.dto.SaphirCustomerDetails;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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
public class SaphirMenus {
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    public UssdResponse processRequest(SubscriberInfo sub, UssdRequest req) {
        switch (sub.getMenuLevel()) {
            case 2:
                return handleshowTandC(sub, req);
            case 3:
                return handleChooseIdentityMethod(sub, req);
            case 4:
                return handleEnterAccount(sub, req);
            case 5:
                return handleSelectCommonFund(sub, req);
            case 6:
                return handleEnterAmount(sub, req);
            case 7:
                return handleConfirmPayment(sub, req);
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

    private UssdResponse handleshowTandC(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Saphir menu level2 (handleshowTandC) for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 2) {
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
        switch (option) {
            case 1:
                //sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.RESERVATION);//transactionType = ARAD_MENUS.RESERVATION;
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_CHOOSE_METHOD.toString()));
                resp.setFreeflow(UssdConstants.CONTINUE);
                sub.incrementMenuLevel();
                activeSessions.put(request.getMsisdn(), sub);
                return resp;
            default:
                sub.setTransactionType(USSDSessionHandler.ARAD_MENUS.STATUS);
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_NO_ACCOUNT.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("user does not have a sapphir account" + request.getMsisdn());
                activeSessions.remove(request.getMsisdn());
                return resp;
        }
    }

    private UssdResponse handleChooseIdentityMethod(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Saphir menu level3 (handleChooseIdentityMethod) for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            switch (option) {
                case 1:
                    sub.getSubParams().put("ID_TYPE", "MSISDN");
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_ENTER_MSISDN.toString()));
                    break;
                case 2:
                    sub.getSubParams().put("ID_TYPE", "ACCOUNT");
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_ENTER_ACCOUNT.toString()));
                    break;
                default:
                    throw new NumberFormatException();
            }
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
        } catch (NumberFormatException ex) {
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
        }
        return resp;
    }

    private UssdResponse handleEnterAccount(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Saphir menu level4 {handleEnterAccount} for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        //final String account = ;
        final JsonObject json = new JsonObject();
        json.addProperty("elem", request.getSubscriberInput());
        if (sub.getSubParams().get("ID_TYPE").equals("ACCOUNT")) {
            json.addProperty("request", "getClientByCompte");
        } else {
            json.addProperty("request", "getClientByPhone");
        }
        //[{"Compte":"143","Intitule":"WANOU A. CAMUS MAXIME"}]  
        final String url = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_GET_ACCOUNT_URL.toString());
        final String agencyListString = new HTTPUtil().postUrlWithJson(json, url);
        final Gson gson = new Gson();
        try {
            final ArrayList<SaphirCustomerDetails> user = gson.fromJson(agencyListString, new TypeToken<List<SaphirCustomerDetails>>() {
            }.getType());
            final SaphirCustomerDetails customer = user.get(0);
            sub.getSubParams().put("CUSTOMER", customer);
            //get common fund options
            final String url2 = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_COMMON_FUND_URL.toString());
            final JsonObject json2 = new JsonObject();
            json2.addProperty("request", "getNosFCPAll");
            final String commonfundoptionsString = new HTTPUtil().postUrlWithJson(json2, url2);
            final ArrayList<SaphirCommonFundOption> options = gson.fromJson(commonfundoptionsString, new TypeToken<List<SaphirCommonFundOption>>() {
            }.getType());
            //Please choose commonfund placement for {CUST_NAME}
            final StringBuilder msgResp = new StringBuilder(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_CHOOSE_FUND.toString())
                    .replace("{CUST_NAME}", customer.getIntitule()));
            int i = 0;
            for (final SaphirCommonFundOption opt : options) {
                msgResp.append("\n").append(++i).append(". ").append(opt.getDenominationOpcvm());
            }
            sub.getSubParams().put("CF_OPTIONS", options);
            resp.setApplicationResponse(msgResp.toString());
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
            return resp;
        } catch (Exception ex) {
            Logger.getLogger("processAradLevel2Menu").info("Exception encountered. Reason: " + ex.getMessage());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
    }

    private UssdResponse handleSelectCommonFund(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Saphir menu level5 {handleSelectCommonFund} for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            final ArrayList<SaphirCommonFundOption> options = (ArrayList<SaphirCommonFundOption>) sub.getSubParams().get("CF_OPTIONS");
            if (option < 1 || option > options.size()) {
                throw new NumberFormatException();
            }
            sub.getSubParams().put("CF_OPTION", options.get(option - 1));
            sub.getSubParams().remove("CF_OPTIONS");
        } catch (NumberFormatException ex) {
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
        resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_ENTER_AMOUNT.toString()));
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        activeSessions.put(request.getMsisdn(), sub);
        return resp;
    }

    private UssdResponse handleEnterAmount(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("spahir menu level6 {handleEnterAmount} for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        try {
            final SaphirCommonFundOption option = (SaphirCommonFundOption) sub.getSubParams().get("CF_OPTION");
            final Double amount = Double.parseDouble(request.getSubscriberInput());
            final String msg = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_CONFIRM_PAYMENT.toString())
                    .replace("{AMOUNT}", df.format(amount))
                    .replace("{COMMON_FUND_OPTION}", option.getDenominationOpcvm());
            resp.setApplicationResponse(msg);
            resp.setFreeflow(UssdConstants.CONTINUE);
            sub.setAmount(new BigDecimal(amount));
            sub.incrementMenuLevel();
            activeSessions.put(request.getMsisdn(), sub);
        } catch (NumberFormatException ex) {
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_AMOUNT.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("handleEnterAmount").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions.remove(request.getMsisdn());
            return resp;
        }
        return resp;
    }

    private UssdResponse handleConfirmPayment(SubscriberInfo sub, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Saphir menu level9 (handleConfirmPayment) for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        String respMessage;

        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option == 1) {
                Logger.getLogger("qos_ussd_processor").info("processing saphir transaction for: " + request.getMsisdn());
                JsonObject requestPayment = new JsonObject();
                requestPayment.addProperty("msisdn", sub.getMsisdn());
                requestPayment.addProperty("amount", sub.getAmount());
                final SaphirCustomerDetails customer = (SaphirCustomerDetails) sub.getSubParams().get("CUSTOMER");
                final SaphirCommonFundOption cfOption = (SaphirCommonFundOption) sub.getSubParams().get("CF_OPTION");
                final StringBuilder transref = new StringBuilder();
                transref.append("compte=").append(customer.getCompte()).append("|")
                        .append("idPersonne=1").append("|")
                        .append("montant=").append(sub.getAmount().toString()).append("|")
                        .append("idOpcvm=").append(cfOption.getIdOpcvm()).append("|")
                        .append("referenceTransaction=").append(request.getSessionId());
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
                //cancel transaction
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
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

    public CloseableHttpClient getHttpClient(String s)
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

    UssdResponse showMainMenu(SubscriberInfo sub) {
        Logger.getLogger("qos_ussd_processor").info("showing sapphir main menu to: " + sub.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(sub.getMsisdn());
        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.SAPHIR_MAIN_MENU.toString());
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        sub.incrementMenuLevel();
        activeSessions.put(sub.getMsisdn(), sub);
        return resp;
    }

}
