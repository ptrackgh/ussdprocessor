/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.qos.ussd.main.SubscriberInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.validation.ConstraintViolationException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author ptrack
 */
public class HTTPUtil {
    private static final String merchantDetails_URL = "http://10.4.94.1:8221/QosicBridge/user/merchantsbycode/";
    //private static final String merchantDetails_URL = "https://10.4.94.2:8443/QosicBridge/user/merchantsbycode/";
    //private static final String merchantDetails_URL = "http://www.qosic.net:8221/QosicBridge/user/merchantsbycode/";
    private static final String requestPayment_URL = "https://10.4.94.1:8443/QosicBridge/user/requestpayment";
    //private static final String requestPayment_URL = "http://www.qosic.net:8221/QosicBridge/user/requestpayment";
    //private static final String requestPayment_URL = "http://10.4.94.1:8221/QosicBridge/user/requestpayment";
    public static final String agencyList_URL = "http://arad-reservation.com/web/ws/agence.list";
    public static final String travelItenary_URL = "http://arad-reservation.com/web/ws/agence.tarif/";
    public static final String travelTimes_URL = "http://arad-reservation.com/web/ws/agence.time/";

    private static final String encoding = Base64.encodeBase64String((UssdConstants.merchantDetails_Username + ":"
            + UssdConstants.merchantDetails_Password).getBytes());
    
    public String sendRequestPayment(JsonObject payload) {
        String resp = "";
        CloseableHttpClient httpclient = null;// = getHttpClient(requestPayment_URL);
        try {
            httpclient = getHttpClient(requestPayment_URL);
            URI build = new URIBuilder(requestPayment_URL).build();
            final HttpPost httppost = new HttpPost(build);
            httppost.setHeader("Authorization", "Basic " + encoding);
            httppost.addHeader("Content-Type", "application/json");
            Logger.getLogger(this.getClass()).info("HttpPost payload: " + payload);
            HttpEntity entity = new StringEntity(payload.toString());
            httppost.setEntity(entity);
            try (CloseableHttpResponse response1 = httpclient.execute(httppost)) {
                Logger.getLogger(this.getClass()).info("status line: " + response1.getStatusLine());
                HttpEntity entity1 = response1.getEntity();
                Logger.getLogger(this.getClass()).info("Response from merchantDetails_URL: " + response1.getStatusLine());
                resp = EntityUtils.toString(entity1);
                EntityUtils.consume(entity1);
                response1.close();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(this.getClass()).error("URISyntaxException: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            Logger.getLogger(this.getClass()).error("UnsupportedOperationException: " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(this.getClass()).error("IOException: " + ex.getMessage());
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            Logger.getLogger(this.getClass()).error("KeyManagementException: " + ex.getMessage());
        } catch (KeyStoreException ex) {
            Logger.getLogger(this.getClass()).error("KeyStoreException: " + ex.getMessage());
        } finally {
            try {
                if(null != httpclient){
                    httpclient.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(this.getClass()).error("unable to close httpclient: " + ex.getMessage());
            }
        }
        return resp;
    }
    
    public  String retrieveMerchantByCode(String subscriberInput, SubscriberInfo sub) {
        String resp = "";
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            URI build = new URIBuilder(merchantDetails_URL + subscriberInput.trim()).build();
            final HttpPost httppost = new HttpPost(build);
            httppost.setHeader("Authorization", "Basic " + encoding);
            Logger.getLogger(this.getClass()).info("HttpGet string: " + httppost.toString());
            try (CloseableHttpResponse response1 = httpclient.execute(httppost)) {
                Logger.getLogger(this.getClass()).info("status line: " + response1.getStatusLine());
                HttpEntity entity1 = response1.getEntity();
                Logger.getLogger(this.getClass()).info("Response from merchantDetails_URL: " + response1.getStatusLine());
                if (response1.getStatusLine().getStatusCode() == 200) {
                    try {
                        String httpResp = EntityUtils.toString(entity1);
                        Gson gson = new Gson();
                        final MerchantDetailsResp user = gson.fromJson(httpResp, MerchantDetailsResp.class);
                        sub.setMerchantDetails(user);
                        resp = user.getName();
                    } catch (IOException ex) {
                        Logger.getLogger(this.getClass()).error("IOException|ParseException parsing response from smsgh: " + ex.getMessage());
                        Logger.getLogger(this.getClass()).error("IOException | ParseException " + Arrays.toString(ex.getStackTrace()).replaceAll(", ", "\n"));
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(this.getClass()).error("NumberFormatException parsing response from smsgh: " + ex.getMessage());
                    } catch (ConstraintViolationException ex) {
                        Logger.getLogger(this.getClass()).error("ConstraintViolationException " + Arrays.toString(ex.getConstraintViolations().toArray()));
                        Logger.getLogger(this.getClass()).error("ConstraintViolationException " + ex + " by " + Arrays.toString(ex.getStackTrace()).replaceAll(", ", "\n"));
                    }
                }
                EntityUtils.consume(entity1);
                response1.close();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(this.getClass()).error("URISyntaxException: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            Logger.getLogger(this.getClass()).error("UnsupportedOperationException: " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(this.getClass()).error("IOException: " + ex.getMessage());
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(this.getClass()).error("unable to close httpclient: " + ex.getMessage());
            }
        }
        return resp;
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
    
    public String sendGetRequest(String url){
        String resp = "";
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            final HttpGet httppost = new HttpGet(url);
            //httppost.setHeader("Authorization", "Basic " + encoding);
            Logger.getLogger(this.getClass()).info("HttpGet string: " + url);
            try (CloseableHttpResponse response1 = httpclient.execute(httppost)) {
                Logger.getLogger(this.getClass()).info("status line: " + response1.getStatusLine());
                HttpEntity entity1 = response1.getEntity();
                //Logger.getLogger(this.getClass()).info("Response from Get URL: " + response1.getStatusLine());
                if (response1.getStatusLine().getStatusCode() == 200) {
                    try {
                        resp = EntityUtils.toString(entity1);
                        Logger.getLogger(this.getClass()).info("Response from Get URL: " + resp);
                    } catch (IOException ex) {
                        Logger.getLogger(this.getClass()).error("IOException|ParseException parsing response from Get URL: " + ex.getMessage());
                        Logger.getLogger(this.getClass()).error("IOException | ParseException " + Arrays.toString(ex.getStackTrace()).replaceAll(", ", "\n"));
                    }
                }
                EntityUtils.consume(entity1);
                response1.close();
            }
        } catch (UnsupportedOperationException ex) {
            Logger.getLogger(this.getClass()).error("UnsupportedOperationException: " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(this.getClass()).error("IOException: " + ex.getMessage());
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(this.getClass()).error("unable to close httpclient: " + ex.getMessage());
            }
        }
        return resp;
    }
}
