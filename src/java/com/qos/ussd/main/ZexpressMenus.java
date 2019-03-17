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
//import static com.qos.ussd.main.USSDSessionHandler.activeSessions;
import static com.qos.ussd.main.USSDSessionHandler.activeSessions_Zex;
import com.qos.ussd.util.HTTPUtil;
import com.qos.ussd.util.UssdConstants;
import com.qos.ussd.util.zexpress.dto.*;
import static java.lang.Math.ceil;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
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
 * @author Malkiel
 */
public class ZexpressMenus {
    private final Pattern datePattern = Pattern.compile("^\\d{8}$");
    private final DecimalFormat df = new DecimalFormat("#,##0");
    private final DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");
    private final DateFormat df2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    final static String zexpress_url = "http://74.208.84.251/zexpress/restapi.php";
    //final static String arad_travel_iternary_url = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRAVEL_ITENARY_URL.toString());
    //final static String arad_travel_times_url = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_TRAVEL_TIMES_URL.toString());
    //replace special char
    public static String translate(String src) {
        StringBuilder result = new StringBuilder();
        if(src!=null && src.length()!=0) {
            int index = -1;
            char c = (char)0;
            String chars= "àâäéèêëîïôöùûüç";
            String replace= "aaaeeeeiioouuuc";
            for(int i=0; i<src.length(); i++) {
                c = src.charAt(i);
                if( (index=chars.indexOf(c))!=-1 )
                    result.append(replace.charAt(index));
                else
                    result.append(c);
            }
        }
        return result.toString();
    }
    
    public UssdResponse processRequest(ZexpressInfo zex, UssdRequest req){
        switch (zex.getMenuLevel()) {
                case 1:
                    return processZexpressLevel1Menu(zex, req);
                case 2:
                    return processZexpressLevel2Menu(zex, req);
                case 3:
                    return processZexpressLevel3Menu(zex, req);
                case 4:
                    return processZexpressLevel4Menu(zex, req);
                case 5:
                    return processZexpressLevel5Menu(zex, req);
                case 6:
                    return processZexpressLevel6Menu(zex, req);
                case 7:
                    return processZexpressLevel7Menu(zex, req);
                case 8:
                    return processZexpressLevel8Menu(zex, req);
                case 9:
                    return processZexpressLevel9Menu(zex, req);
                case 10:
                    return processZexpressLevel10Menu(zex, req);
                case 11:
                    return processZexpressLevel11Menu(zex, req);
                case 12:
                    return processZexpressLevel12Menu(zex, req);
                case 13:
                    return processZexpressLevel13Menu(zex, req);
                default:
                    final UssdResponse resp = new UssdResponse();
                    resp.setMsisdn(req.getMsisdn());
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(req.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", req.getMsisdn()));
                    return resp;
            }
    }
    
    private UssdResponse processZexpressLevel1Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level1 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final int option;
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions_Zex.remove(request.getMsisdn());
            return resp;
        }
        final ArrayList<PaidOrders> paidorders;
        final ArrayList<Delivery> user;
        zex.getSubParams().put("FIRST_ORDER", false);
        zex.getSubParams().put("LAST_ORDER", false);
        try{
            //final ArrayList deliveryList
            final String url = zexpress_url;
            final Gson gson = new Gson();
            
            //Delivery
            JsonObject getDeliveryList = new JsonObject();
                    getDeliveryList.addProperty("menu", "deliveryAddress");
            final String deliveryListString = new HTTPUtil().getList(getDeliveryList, url);
            user = gson.fromJson(deliveryListString, new TypeToken<List<Delivery>>(){}.getType());
            zex.setDeliveryList(user);
            
            //Paid Orders
            JsonObject getOrdersList = new JsonObject();
                    getOrdersList.addProperty("menu", "exists");
                    getOrdersList.addProperty("msisdn", request.getMsisdn());
            final String ordersListString = new HTTPUtil().getList(getOrdersList, url);
            paidorders = gson.fromJson(ordersListString, new TypeToken<List<PaidOrders>>(){}.getType());
            zex.setPaidOrdersList(paidorders);
            if(paidorders.isEmpty()) zex.getSubParams().put("FIRST_ORDER", true);
            
            //Address
            JsonObject getAddressList = new JsonObject();
                getAddressList.addProperty("menu", "address");
                getAddressList.addProperty("type", request.getMsisdn());
            final String addressListString = new HTTPUtil().getList(getAddressList, url);
            final ArrayList<Address> addressList = gson.fromJson(addressListString, new TypeToken<List<Address>>(){}.getType());
            zex.setAddressList(addressList);
            //GasWeight
            JsonObject getGasWeightList = new JsonObject();
                getGasWeightList.addProperty("menu", "weight");
            final String gasWeightListString = new HTTPUtil().getList(getGasWeightList, url);
            final ArrayList<GasWeight> gw = gson.fromJson(gasWeightListString, new TypeToken<List<GasWeight>>(){}.getType());
            zex.setGasWeightList(gw);
            //GasBrand
            JsonObject getGasBrandList = new JsonObject();
                getGasBrandList.addProperty("menu", "brand");
            final String gasBrandListString = new HTTPUtil().getList(getGasBrandList, url);
            final ArrayList<GasBrand> gb = gson.fromJson(gasBrandListString, new TypeToken<List<GasBrand>>(){}.getType());
            zex.setGasBrandList(gb);
            //MealType
            JsonObject getMealList = new JsonObject();
                getMealList.addProperty("menu", "mealType");
            final String gasMealListString = new HTTPUtil().getList(getMealList, url);
            final ArrayList<Meal> meal = gson.fromJson(gasMealListString, new TypeToken<List<Meal>>(){}.getType());
            zex.setMealList(meal);
            //MealDish
//            JsonObject getMealDishList = new JsonObject();
//                getMealDishList.addProperty("menu", "mealDish");
//                getMealDishList.addProperty("type", "Petit Dejeuner");
//            final String mealListString = new HTTPUtil().getList(getMealDishList, url);
//            final ArrayList<Meal> mealDish = gson.fromJson(mealListString, new TypeToken<List<Meal>>(){}.getType());
//            zex.setMealList(mealDish);
            //Flower
            JsonObject getFlowerList = new JsonObject();
                getFlowerList.addProperty("menu", "flower");
            final String flowerListString = new HTTPUtil().getList(getFlowerList, url);
            final ArrayList<Flower> flower = gson.fromJson(flowerListString, new TypeToken<List<Flower>>(){}.getType());
            zex.setFlowerList(flower);
            //MovieTicketHours
            JsonObject getMovieTicketHoursList = new JsonObject();
                getMovieTicketHoursList.addProperty("menu", "movieHours");
            final String movieTicketHoursListString = new HTTPUtil().getList(getMovieTicketHoursList, url);
            final ArrayList<MovieTicketHours> mth = gson.fromJson(movieTicketHoursListString, new TypeToken<List<MovieTicketHours>>(){}.getType());
            zex.setMovieTicketHoursList(mth);
            //MovieTicketType
            JsonObject getMovieTicketTypeList = new JsonObject();
                getMovieTicketTypeList.addProperty("menu", "movieType");
            final String movieTicketTypeListString = new HTTPUtil().getList(getMovieTicketTypeList, url);
            final ArrayList<MovieTicketType> mtt = gson.fromJson(movieTicketTypeListString, new TypeToken<List<MovieTicketType>>(){}.getType());
            zex.setMovieTicketTypeList(mtt);
            
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(request.getMsisdn(), zex);
        }catch (JsonSyntaxException ex) {
            Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
            final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
            resp.setApplicationResponse(resMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            return resp;
        }catch (Exception ex) {
            Logger.getLogger("processZexpressLevel1Menu").info("Exception encountered. Reason: " + ex.getMessage());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            return resp;
        }
           
        switch (option) {
            case 1:
                zex.getSubParams().put("MENU", "GAZ");
                if(!paidorders.isEmpty()){
                    for(final PaidOrders orders: paidorders){
                        String[] sf = orders.getSpecialfield1().split("\\|", -1);
                        String[] delivery = sf[2].split("=");
                        String[] gasWeight = sf[8].split("=");
                        String[] gasBrand = sf[9].split("=");
                        String[] quantity = sf[4].split("=");
                        if(gasBrand[1] != null && gasWeight[1] != null && gasWeight[0].equalsIgnoreCase("gasWeight") && delivery[1] != null && quantity[1] != null){
                            zex.setPaidorders(orders);
                            zex.getSubParams().put("LAST_ORDER", true);
                            final String msg = "Voulez-vous renouveler votre commande précédente ? \n1. Oui \n2. Non";
                            resp.setApplicationResponse(msg);
                            return resp;
                        }
                    }
                    //zex.getSubParams().put("FIRST_ORDER", true);
                }
                zex.setMenuLevel(2);
                return processZexpressLevel2Menu(zex, request);
            //break;
            case 2:
                zex.getSubParams().put("MENU", "REPAS");
                if(!paidorders.isEmpty()){
                    for(final PaidOrders orders: paidorders){
                        String[] sf = orders.getSpecialfield1().split("\\|", -1);
                        String[] delivery = sf[2].split("=");
                        String[] mealType = sf[8].split("=");
                        String[] mealDish = sf[9].split("=");
                        String[] quantity = sf[4].split("=");
                        if(!mealType[1].isEmpty() && mealType[0].equalsIgnoreCase("mealType") && !mealDish[1].isEmpty() && !delivery[1].isEmpty() && !quantity[1].isEmpty()){
                            zex.setPaidorders(orders);
                            zex.getSubParams().put("LAST_ORDER", true);
                            final String msg = "Voulez-vous renouveler votre commande précédente ? \n1. Oui \n2. Non";
                            resp.setApplicationResponse(msg);
                            return resp;
                        }
                    }
                    //zex.getSubParams().put("FIRST_ORDER", true);
                }
                zex.setMenuLevel(2);
                return processZexpressLevel2Menu(zex, request);
            case 3:
                zex.getSubParams().put("MENU", "FLEUR");
                if(!paidorders.isEmpty()){
                    for(final PaidOrders orders: paidorders){
                        String[] sf = orders.getSpecialfield1().split("\\|", -1);
                        String[] delivery = sf[2].split("=");
                        String[] flower = sf[8].split("=");
                        String[] quantity = sf[4].split("=");
                        if(!flower[1].isEmpty() && flower[0].equalsIgnoreCase("flower") && !delivery[1].isEmpty() && !quantity[1].isEmpty()){
                            zex.setPaidorders(orders);
                            zex.getSubParams().put("LAST_ORDER", true);
                            final String msg = "Voulez-vous renouveler votre commande précédente ? \n1. Oui \n2. Non";
                            resp.setApplicationResponse(msg);
                            return resp;
                        }
                    }
                    //zex.getSubParams().put("FIRST_ORDER", true);
                }
                zex.setMenuLevel(2);
                return processZexpressLevel2Menu(zex, request);
            case 4:
                zex.getSubParams().put("MENU", "TICKET_CINEMA");
                if(!paidorders.isEmpty()){
                    for(final PaidOrders orders: paidorders){
                        String[] sf = orders.getSpecialfield1().split("\\|", -1);
                        String[] delivery = sf[2].split("=");
                        String[] movieTime = sf[8].split("=");
                        String[] cinemaTime = sf[9].split("=");
                        String[] ticketType = sf[10].split("=");
                        String[] quantity = sf[4].split("=");
                        if(!cinemaTime[1].isEmpty() && !ticketType[1].isEmpty() && !movieTime[1].isEmpty() && movieTime[0].equalsIgnoreCase("movieTime") && !delivery[1].isEmpty() && !quantity[1].isEmpty()){                            
                            zex.setPaidorders(orders);
                            zex.getSubParams().put("LAST_ORDER", true);
                            final String msg = "Voulez-vous renouveler votre commande précédente ? \n1. Oui \n2. Non";
                            resp.setApplicationResponse(msg);
                            return resp;
                        }
                    }
                    //zex.getSubParams().put("FIRST_ORDER", true);
                }
                zex.setMenuLevel(2);
                return processZexpressLevel2Menu(zex, request);
            default:
                zex.getSubParams().put("MENU", "FACTURES");
                /*if(!paidorders.isEmpty()){
                    for(final PaidOrders orders: paidorders){
                        String[] sf = orders.getSpecialfield1().split("\\|", -1);
                        String[] delivery = sf[2].split("=");
                        String[] flower = sf[8].split("=");
                        String[] quantity = sf[4].split("=");
                        if(!flower[1].isEmpty() && flower[0].equalsIgnoreCase("flower") && !delivery[1].isEmpty() && !quantity[1].isEmpty()){
                            zex.setPaidorders(orders);
                            zex.getSubParams().put("LAST_ORDER", true);
                            final String msg = "Voulez-vous renouveler votre commande précédente ? \n1. Oui \n2. Non";
                            resp.setApplicationResponse(msg);
                            return resp;
                        }
                    }
                    //zex.getSubParams().put("FIRST_ORDER", true);
                }*/
                zex.setMenuLevel(2);
                return processZexpressLevel2Menu(zex, request);
        }
        
    }
    
    private UssdResponse processZexpressLevel2Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level2 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        if(zex.getSubParams().get("LAST_ORDER").equals(true)){
            final int option;
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > 2) {
                    throw new NumberFormatException();
                }else if(option == 1){
                    switch (zex.getSubParams().get("MENU").toString()) {
                        case "GAZ":
                            String[] sf = zex.getPaidorders().getSpecialfield1().split("\\|", -1);
                            String[] delivery = sf[2].split("=");
                            String[] gasWeight = sf[8].split("=");
                            String[] gasBrand = sf[9].split("=");
                            String[] quantity = sf[4].split("=");
                            if(!gasBrand[1].isEmpty() && !gasWeight[1].isEmpty() && !delivery[1].isEmpty() && !quantity[1].isEmpty()){
                                //Persist Previous Data
                                for(final Delivery del: zex.getDeliveryList()){ //Delivery
                                    if(delivery[1].equals(del.getName())){
                                       final ArrayList<Delivery> deliv = new ArrayList<>();
                                       deliv.add(del);
                                       zex.setDeliveryList(deliv);
                                       zex.setSelectedDelivery(0);
                                       break;
                                    }
                                }
                                for(final GasWeight gw: zex.getGasWeightList()){ //GasWeight
                                    if(gasWeight[1].equals(gw.getName())){
                                       final ArrayList<GasWeight> gasw = new ArrayList<>();
                                       gasw.add(gw);
                                       zex.setGasWeightList(gasw);
                                       zex.setSelectedGasWeight(0);
                                       break;
                                    }
                                }
                                for(final GasBrand gb: zex.getGasBrandList()){ //GasBrand
                                    if(gasBrand[1].equals(gb.getName())){
                                       final ArrayList<GasBrand> gasb = new ArrayList<>();
                                       gasb.add(gb);
                                       zex.setGasBrandList(gasb);
                                       zex.setSelectedGasBrand(0);
                                       break;
                                    }
                                }
                                zex.setQuantity(Integer.parseInt(quantity[1])); //Quantity
                            }
                            zex.setMenuLevel(6);
                            return processZexpressLevel6Menu(zex, request);
                        //break;
                        case "REPAS":
                            String[] sf2 = zex.getPaidorders().getSpecialfield1().split("\\|", -1);
                            String[] delivery2 = sf2[2].split("=");
                            String[] mealType = sf2[8].split("=");
                            String[] mealDish = sf2[9].split("=");
                            String[] quantity2 = sf2[4].split("=");
                            if(!mealType[1].isEmpty() && !mealDish[1].isEmpty() && !delivery2[1].isEmpty() && !quantity2[1].isEmpty()){
                                //Persist Previous Data
                                for(final Delivery del: zex.getDeliveryList()){ //Delivery
                                    if(delivery2[1].equals(del.getName())){
                                       final ArrayList<Delivery> deliv = new ArrayList<>();
                                       deliv.add(del);
                                       zex.setDeliveryList(deliv);
                                       zex.setSelectedDelivery(0);
                                       break;
                                    }
                                }
                                zex.getSubParams().put("MEAL_TYPE", mealType[1]);
                                for(final Meal meal: zex.getMealList()){ //MealType
                                    if(mealType[1].equalsIgnoreCase(meal.getMealType())){
                                       zex.getSubParams().put("MEAL_TYPE", meal.getMealType());
                                       break;
                                    }
                                }

                                try{ //MealDish
                                    final String url = zexpress_url;
                                    final Gson gson = new Gson();

                                    //MealDish
                                    JsonObject getMealList = new JsonObject();
                                        getMealList.addProperty("menu", "mealDish");
                                        getMealList.addProperty("type", zex.getSubParams().get("MEAL_TYPE").toString());
                                    final String mealListString = new HTTPUtil().getList(getMealList, url);
                                    final ArrayList<Meal> repas = gson.fromJson(mealListString, new TypeToken<List<Meal>>(){}.getType());
                                    zex.setMealList(repas);
                                }catch (JsonSyntaxException ex) {
                                    Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                                    final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(resMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                    activeSessions_Zex.remove(request.getMsisdn());
                                    return resp;
                                }catch (Exception ex) {
                                    Logger.getLogger("processZexpressLevel1Menu").info("Exception encountered. Reason: " + ex.getMessage());
                                    resp.setApplicationResponse("Votre demande a echouee. Veuillez reessayer plus tard.");
                                    resp.setFreeflow(UssdConstants.BREAK);
                                    activeSessions_Zex.remove(request.getMsisdn());
                                    return resp;
                                }
                                
                                for(final Meal ml: zex.getMealList()){ //MealDish
                                    if(mealDish[1].equalsIgnoreCase(ml.getDish())){
                                       final ArrayList<Meal> mea = new ArrayList<>();
                                       mea.add(ml);
                                       zex.setMealList(mea);
                                       zex.setSelectedMeal(0);
                                       break;
                                    }
                                }

                                zex.setQuantity(Integer.parseInt(quantity2[1])); //Quantity
                            }
                            zex.setMenuLevel(6);
                            return processZexpressLevel6Menu(zex, request);
                        case "FLEUR":
                            String[] sf3 = zex.getPaidorders().getSpecialfield1().split("\\|", -1);
                            String[] delivery3 = sf3[2].split("=");
                            String[] flower = sf3[8].split("=");
                            String[] quantity3 = sf3[4].split("=");
                            if(!flower[1].isEmpty() && !delivery3[1].isEmpty() && !quantity3[1].isEmpty()){
                                for(final Delivery del: zex.getDeliveryList()){ //Delivery
                                    if(delivery3[1].equalsIgnoreCase(del.getName())){
                                       final ArrayList<Delivery> deliv = new ArrayList<>();
                                       deliv.add(del);
                                       zex.setDeliveryList(deliv);
                                       zex.setSelectedDelivery(0);
                                       break;
                                    }
                                }

                                for(final Flower flo: zex.getFlowerList()){ //Delivery
                                    if(flower[1].equalsIgnoreCase(flo.getName())){
                                       final ArrayList<Flower> flow = new ArrayList<>();
                                       flow.add(flo);
                                       zex.setFlowerList(flow);
                                       zex.setSelectedFlower(0);
                                       break;
                                    }
                                }

                                zex.setQuantity(Integer.parseInt(quantity3[1])); //Quantity
                            }
                            zex.setMenuLevel(5);
                            return processZexpressLevel5Menu(zex, request);
                        default:
                            String[] sf4 = zex.getPaidorders().getSpecialfield1().split("\\|", -1);
                            String[] delivery4 = sf4[2].split("=");
                            String[] movieTime = sf4[8].split("=");
                            String[] cinemaTime = sf4[9].split("=");
                            String[] ticketType = sf4[10].split("=");
                            String[] quantity4 = sf4[4].split("=");
                            if(!cinemaTime[1].isEmpty() && !ticketType[1].isEmpty() && !movieTime[1].isEmpty() && !delivery4[1].isEmpty() && !quantity4[1].isEmpty()){
                                for(final Delivery del: zex.getDeliveryList()){ //Delivery
                                    if(delivery4[1].equalsIgnoreCase(del.getName())){
                                       final ArrayList<Delivery> deliv = new ArrayList<>();
                                       deliv.add(del);
                                       zex.setDeliveryList(deliv);
                                       zex.setSelectedDelivery(0);
                                       break;
                                    }
                                }
                                for(final MovieTicketHours mth: zex.getMovieTicketHoursList()){ //Delivery
                                    if(movieTime[1].equalsIgnoreCase(mth.getName())){
                                       final ArrayList<MovieTicketHours> movieticketh = new ArrayList<>();
                                       movieticketh.add(mth);
                                       zex.setMovieTicketHoursList(movieticketh);
                                       zex.setSelectedMovieTicketHours(0);
                                       break;
                                    }
                                }
                                for(final MovieTicketType mtt: zex.getMovieTicketTypeList()){ //Delivery
                                    if(ticketType[1].equalsIgnoreCase(mtt.getName())){
                                       final ArrayList<MovieTicketType> movietickett = new ArrayList<>();
                                       movietickett.add(mtt);
                                       zex.setMovieTicketTypeList(movietickett);
                                       zex.setSelectedMovieTicketType(0);
                                       break;
                                    }
                                }
                                try{
                                    zex.setMovieDay(df1.parse(cinemaTime[1]));
                                }catch (Exception ex) {
                                    Logger.getLogger("processZexpressLevel1Menu").info("Exception encountered. Reason: " + ex.getMessage());
                                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                                    resp.setFreeflow(UssdConstants.BREAK);
                                    activeSessions_Zex.remove(request.getMsisdn());
                                    return resp;
                                }
                                zex.setQuantity(Integer.parseInt(quantity4[1])); //Quantity
                            }
                            zex.setMenuLevel(7);
                            return processZexpressLevel7Menu(zex, request);
                    }
                }else{
                    zex.getSubParams().put("LAST_ORDER", false);
                }
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }
        
        final StringBuilder msgResp; int i=0;    
        switch (zex.getSubParams().get("MENU").toString()) {
            case "GAZ":
                msgResp = new StringBuilder("Veuillez choisir la ville de livraison:");
                for(final Delivery delivery: zex.getDeliveryList()){
                    msgResp.append("\n").append(++i).append(". ").append(delivery.getName()).append(" (").append(delivery.getGas()).append("F) ");
                }
                resp.setApplicationResponse(msgResp.toString());
                zex.incrementMenuLevel();
                resp.setFreeflow(UssdConstants.CONTINUE);
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            //break;
            case "REPAS":
                msgResp = new StringBuilder("Veuillez choisir la ville de livraison:");
                for(final Delivery delivery: zex.getDeliveryList()){
                    msgResp.append("\n").append(++i).append(". ").append(delivery.getName()).append(" (").append(delivery.getMeal()).append("F) ");
                }
                resp.setApplicationResponse(msgResp.toString());
                zex.incrementMenuLevel();
                resp.setFreeflow(UssdConstants.CONTINUE);
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            case "FLEUR":
                msgResp = new StringBuilder("Veuillez choisir la ville de livraison:");
                for(final Delivery delivery: zex.getDeliveryList()){
                    msgResp.append("\n").append(++i).append(". ").append(delivery.getName()).append(" (").append(delivery.getFlower()).append("F) ");
                }
                resp.setApplicationResponse(msgResp.toString());
                zex.incrementMenuLevel();
                resp.setFreeflow(UssdConstants.CONTINUE);
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            case "TICKET_CINEMA":
                msgResp = new StringBuilder("Veuillez choisir la ville de livraison:");
                for(final Delivery delivery: zex.getDeliveryList()){
                    msgResp.append("\n").append(++i).append(". ").append(delivery.getName()).append(" (").append(delivery.getMovie()).append("F) ");
                }
                resp.setApplicationResponse(msgResp.toString());
                zex.incrementMenuLevel();
                resp.setFreeflow(UssdConstants.CONTINUE);
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            default:
                msgResp = new StringBuilder("Commande 24H/24 - Code et Quittance 8H-16H/11H W_E. Info. 65159898 \nVeuillez choisir: \n1. Electricité \n2. Eau");
                resp.setApplicationResponse(msgResp.toString());
                zex.incrementMenuLevel();
                resp.setFreeflow(UssdConstants.CONTINUE);
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
        }
        
    }
    
    private UssdResponse processZexpressLevel3Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level3 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse(); 
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        final int option;
        final String url = zexpress_url;
        final Gson gson = new Gson();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            final int choice;
            try {
                choice = Integer.parseInt(request.getSubscriberInput());
                if (choice < 1 || choice > 2) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            //Json to get Previous Compteur
            try{
                JsonObject getMeterList = new JsonObject();
                    getMeterList.addProperty("menu", "meter");
                    getMeterList.addProperty("type", request.getMsisdn());
                    getMeterList.addProperty("factures", choice);
                final String meterListString = new HTTPUtil().getList(getMeterList, url);
                final ArrayList<Meters> user = gson.fromJson(meterListString, new TypeToken<List<Meters>>(){}.getType());
                zex.setMetersList(user);
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            switch(choice){
                case 1:
                    zex.getSubParams().put("FACTURES", "Electricite");
                    if(zex.getMetersList().isEmpty()){
                        final String respMessage = "Veuillez choisir le type de compteur: \n1. Conventionnel \n2. Recharge Code";
                        resp.setApplicationResponse(respMessage);
                        zex.incrementMenuLevel();
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    }else{
                        zex.setMenuLevel(4);
                        return processZexpressLevel4Menu(zex, request);
                    }
                default:
                    zex.getSubParams().put("FACTURES", "Eau");
                    zex.setMenuLevel(4);
                    return processZexpressLevel4Menu(zex, request);
            }
        }
        
        try {
            option = Integer.parseInt(request.getSubscriberInput());
            if (option < 1 || option > zex.getDeliveryList().size()) {
                throw new NumberFormatException();
            }
            zex.setSelectedDelivery(option-1);
        } catch (NumberFormatException ex) {
            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.BREAK);
            Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
            activeSessions_Zex.remove(request.getMsisdn());
            return resp;
        }
        
        if(menu.equalsIgnoreCase("GAZ")){
            //final ArrayList gasWeightList
            try{
                JsonObject getGasWeightList = new JsonObject();
                    getGasWeightList.addProperty("menu", "weight");
                final String gasWeightListString = new HTTPUtil().getList(getGasWeightList, url);
                final ArrayList<GasWeight> user = gson.fromJson(gasWeightListString, new TypeToken<List<GasWeight>>(){}.getType());
                final StringBuilder msgResp = new StringBuilder("Veuillez choisir le poids:");
                int i=0;
                for(final GasWeight gasWeight: user){
                    msgResp.append("\n").append(++i).append(". ").append(gasWeight.getName()).append(" (").append(gasWeight.getPrice()).append("F) ");
                }
                zex.setGasWeightList(user);
                resp.setApplicationResponse(msgResp.toString());
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }else if(menu.equalsIgnoreCase("REPAS")){
            //final ArrayList mealList
            try{
                JsonObject getMealList = new JsonObject();
                    getMealList.addProperty("menu", "mealType");
                final String gasMealListString = new HTTPUtil().getList(getMealList, url);
                final ArrayList<Meal> user = gson.fromJson(gasMealListString, new TypeToken<List<Meal>>(){}.getType());
                final StringBuilder msgResp = new StringBuilder("Veuillez choisir le type de repas:");
                int i=0;
                for(final Meal meal: user){
                    msgResp.append("\n").append(++i).append(". ").append(meal.getMealType());
                }
                zex.setMealList(user);
                resp.setApplicationResponse(msgResp.toString());
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }else if(menu.equalsIgnoreCase("FLEUR")){
            //final ArrayList flowerList
            try{
                JsonObject getFlowerList = new JsonObject();
                    getFlowerList.addProperty("menu", "flower");
                final String flowerListString = new HTTPUtil().getList(getFlowerList, url);
                final ArrayList<Flower> user = gson.fromJson(flowerListString, new TypeToken<List<Flower>>(){}.getType());
                final StringBuilder msgResp = new StringBuilder("Veuillez choisir votre fleur:");
                int i=0;
                for(final Flower flower: user){
                    msgResp.append("\n").append(++i).append(". ").append(flower.getName()).append(" (").append(flower.getPrice()).append("F) ");
                }
                zex.setFlowerList(user);
                resp.setApplicationResponse(msgResp.toString());
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            //final ArrayList movieTicketHoursList
            try{
                JsonObject getMovieTicketHoursList = new JsonObject();
                    getMovieTicketHoursList.addProperty("menu", "movieHours");
                final String movieTicketHoursListString = new HTTPUtil().getList(getMovieTicketHoursList, url);
                final ArrayList<MovieTicketHours> user = gson.fromJson(movieTicketHoursListString, new TypeToken<List<MovieTicketHours>>(){}.getType());
                final StringBuilder msgResp = new StringBuilder("Veuillez choisir l’heure de projection:");
                int i=0;
                for(final MovieTicketHours movieTicketHours: user){
                    msgResp.append("\n").append(++i).append(". ").append(movieTicketHours.getName());
                }
                zex.setMovieTicketHoursList(user);
                resp.setApplicationResponse(msgResp.toString());
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel3Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel4Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level4 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();final int option;
        final String url = zexpress_url;
        final Gson gson = new Gson();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            final int choice;
            if(zex.getSubParams().get("FACTURES").equals("Electricite") && zex.getMetersList().isEmpty()){
                try {
                    choice = Integer.parseInt(request.getSubscriberInput());
                    if (choice < 1 || choice > 2) {
                        throw new NumberFormatException();
                    }
                    if(choice == 1){
                        zex.getSubParams().put("COMPTEUR_TYPE", "Conventionnel");
                    } else {
                        zex.getSubParams().put("COMPTEUR_TYPE", "Recharge Code");
                    }
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            
            //Json to get Previous Compteur
            try{
                final ArrayList<Meters> user = zex.getMetersList();
                
                if(user.isEmpty()){
                    zex.getSubParams().put("COMPTEUR", 1);
                    zex.setMenuLevel(5);
                    return processZexpressLevel5Menu(zex, request);
                }else if(!user.isEmpty()){
                    zex.getSubParams().put("COMPTEUR", 0); //Initialisation
                    final String respMessage = "Nouveau compteur ou choisir: \n1. Ajouter \n2. Choisir";
                    resp.setApplicationResponse(respMessage);
                }

                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(zex.getMsisdn(), zex);
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel4Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }
        
        if(menu.equalsIgnoreCase("GAZ")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > zex.getGasWeightList().size()) {
                    throw new NumberFormatException();
                }
                zex.setSelectedGasWeight(option-1);
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            //final ArrayList gasBrandList
            try{
                JsonObject getGasBrandList = new JsonObject();
                    getGasBrandList.addProperty("menu", "brand");
                final String gasBrandListString = new HTTPUtil().getList(getGasBrandList, url);
                final ArrayList<GasBrand> user = gson.fromJson(gasBrandListString, new TypeToken<List<GasBrand>>(){}.getType());
                final StringBuilder msgResp = new StringBuilder("Veuillez choisir la marque:");
                int i=0;
                for(final GasBrand gasBrand: user){
                    msgResp.append("\n").append(++i).append(". ").append(gasBrand.getName());
                }
                zex.setGasBrandList(user);
                resp.setApplicationResponse(msgResp.toString());
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel4Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }else if(menu.equalsIgnoreCase("REPAS")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > zex.getMealList().size()) {
                    throw new NumberFormatException();
                }
                zex.setSelectedMeal(option-1);
                zex.getSubParams().put("MEAL_TYPE", ZexpressMenus.translate(zex.getMealList().get(option-1).getMealType()));
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            //final ArrayList DishList
            try{
                JsonObject getMealList = new JsonObject();
                    getMealList.addProperty("menu", "mealDish");
                    getMealList.addProperty("type", zex.getSubParams().get("MEAL_TYPE").toString());
                final String mealListString = new HTTPUtil().getList(getMealList, url);
                final ArrayList<Meal> user = gson.fromJson(mealListString, new TypeToken<List<Meal>>(){}.getType());
                final StringBuilder msgResp = new StringBuilder("Veuillez choisir le plat:");
                int i=0;
                for(final Meal meal: user){
                    msgResp.append("\n").append(++i).append(". ").append(meal.getDish()).append(" (").append(meal.getPrice()).append("F) ");
                }
                zex.setMealList(user);
                //final StringBuilder msgResp = new StringBuilder("Veuillez choisir le plat:" + zex.getSubParams().get("MEAL_TYPE").toString());
                resp.setApplicationResponse(msgResp.toString());
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel4Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }else if(menu.equalsIgnoreCase("FLEUR")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > zex.getFlowerList().size()) {
                    throw new NumberFormatException();
                }
                zex.setSelectedFlower(option-1);
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            final String respMessage = "Quantité de fleur(s):";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > zex.getMovieTicketHoursList().size()) {
                    throw new NumberFormatException();
                }
                zex.setSelectedMovieTicketHours(option-1);
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            final String respMessage = "Veuillez entrer la date de projection JJMMAAAA:";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel5Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level5 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            final int choice;
            if(Integer.parseInt(zex.getSubParams().get("COMPTEUR").toString()) != 1){
                try {
                    choice = Integer.parseInt(request.getSubscriberInput());
                    if (choice < 1 || choice > 2) {
                        throw new NumberFormatException();
                    }
                    zex.getSubParams().put("COMPTEUR", choice);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            
            switch(Integer.parseInt(zex.getSubParams().get("COMPTEUR").toString())){
                case 1:
                    final String respMessage;
                    /*if(zex.getSubParams().get("FACTURES").equals("Electricite")){*/
                        respMessage = "Numero Compteur (11 chiffres)";
                    /*}else{
                        respMessage = "Numero de police:";
                    }*/
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(zex.getMsisdn(), zex);
                    return resp;
                case 2: 
                    final StringBuilder msgResp;
                    /*if(zex.getSubParams().get("FACTURES").equals("Electricite")){*/
                        msgResp = new StringBuilder("Numero Compteur (11 chiffres)");
                    /*}else{
                        msgResp = new StringBuilder("Numero de police:");
                    }*/
                    int i=0;
                    for(final Meters meter: zex.getMetersList()){
                        msgResp.append("\n").append(++i).append(". ").append(meter.getNom());
                    }
                    resp.setApplicationResponse(msgResp.toString());
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(zex.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }
        
        final int option;
        final String url = zexpress_url;
        final Gson gson = new Gson();
        if(menu.equalsIgnoreCase("GAZ")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > zex.getGasBrandList().size()) {
                    throw new NumberFormatException();
                }
                zex.setSelectedGasBrand(option-1);
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            final String respMessage = "Nombre de bouteille(s):";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else if(menu.equalsIgnoreCase("REPAS")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > zex.getMealList().size()) {
                    throw new NumberFormatException();
                }
                zex.setSelectedMeal(option-1);
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            /*zex.setMenuLevel(6);
            return processZexpressLevel6Menu(zex, request);*/ 
            final String respMessage = "Nombre de repas:";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else if(menu.equalsIgnoreCase("FLEUR")){
            if(zex.getSubParams().get("LAST_ORDER").equals(false)){
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    zex.setQuantity(option);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            
            //Json to get Previous Adresses
            try{
                JsonObject getMovieTicketTypeList = new JsonObject();
                    getMovieTicketTypeList.addProperty("menu", "address");
                    getMovieTicketTypeList.addProperty("type", request.getMsisdn());
                final String addressListString = new HTTPUtil().getList(getMovieTicketTypeList, url);
                if(!addressListString.isEmpty()){
                    final ArrayList<Address> user = gson.fromJson(addressListString, new TypeToken<List<Address>>(){}.getType());
                    zex.setAddressList(user);
                }
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel5Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            if(zex.getAddressList().isEmpty()){
                zex.getSubParams().put("DELIVERY_OPTION", "1");
                zex.setMenuLevel(6);
                return processZexpressLevel6Menu(zex, request);
            }else{
                final String respMessage = "Votre adresse de livraison: \n1. Ajouter une adresse \n2. Choisir une adresse";
                resp.setApplicationResponse(respMessage);
            }
            
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            final String respMessage;
            if (datePattern.matcher(request.getSubscriberInput()).matches()) {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                final Date cinemaDate;
                try {
                    cinemaDate = sdf.parse(request.getSubscriberInput());
                    Calendar now = Calendar.getInstance();
                    //now.set(Calendar.HOUR_OF_DAY, 23);
                    if (cinemaDate.before(now.getTime())) {//compares date portions only
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("date entered{" + cinemaDate + "} is before now():" + now);
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } else {
                        zex.setMovieDay(cinemaDate);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger("qos_ussd_processor").info(ex);
                    final String responseMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
                    resp.setApplicationResponse(responseMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            } else {
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            //final ArrayList movieTicketTypeList
            try{
                JsonObject getMovieTicketTypeList = new JsonObject();
                    getMovieTicketTypeList.addProperty("menu", "movieType");
                final String movieTicketTypeListString = new HTTPUtil().getList(getMovieTicketTypeList, url);
                final ArrayList<MovieTicketType> user = gson.fromJson(movieTicketTypeListString, new TypeToken<List<MovieTicketType>>(){}.getType());
                final StringBuilder msgResp = new StringBuilder("Veuillez choisir le type de ticket:");
                int i=0;
                for(final MovieTicketType movieTicketType: user){
                    msgResp.append("\n").append(++i).append(". ").append(movieTicketType.getName()).append(" (").append(movieTicketType.getPrice()).append("F) ");
                }
                zex.setMovieTicketTypeList(user);
                resp.setApplicationResponse(msgResp.toString());
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel5Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel6Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level6 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            final int choice;
            switch(Integer.parseInt(zex.getSubParams().get("COMPTEUR").toString())){
                case 1: 
                    try {
                        zex.getSubParams().put("NUMERO_COMPTEUR", request.getSubscriberInput());
                    } catch (Exception ex) {
                        final String message = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(message);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    final String respMessage = "Numero de police:";
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(zex.getMsisdn(), zex);
                    return resp;
                    /*zex.setMenuLevel(7);
                    return processZexpressLevel7Menu(zex, request);*/
                    
                case 2: 
                    try {
                        choice = Integer.parseInt(request.getSubscriberInput());
                        if (choice < 1 || choice > zex.getMetersList().size()) {
                            throw new NumberFormatException();
                        }
                        String str = "@" + zex.getMetersList().get(choice-1).getNom();
                        zex.getSubParams().put("NUMERO_COMPTEUR", str);
                        
                        String[] sf = zex.getMetersList().get(choice-1).getSpecialfield1().split("\\|", -1);
                        String[] compteurType = sf[2].split("=");
                        String[] ville = sf[3].split("=");
                        String[] proprietaire = sf[4].split("=");
                        String[] contact = sf[5].split("=");
                        String[] mail = sf[6].split("=");
                        
                        try{
                            if(!compteurType[1].isEmpty()) zex.getSubParams().put("COMPTEUR_TYPE", compteurType[1]);
                        } catch(Exception ex) {
                            zex.getSubParams().put("COMPTEUR_TYPE", "");
                        }
                        
                        if(/*!compteurType[1].isEmpty() &&*/ !ville[1].isEmpty() && !proprietaire[1].isEmpty() && !contact[1].isEmpty() && !mail[1].isEmpty()){
                            //zex.getSubParams().put("COMPTEUR_TYPE", compteurType[1]);
                            zex.getSubParams().put("PROPRIETAIRE", proprietaire[1]);
                            zex.getSubParams().put("VILLE", ville[1]);
                            zex.getSubParams().put("CONTACT", contact[1]);
                            zex.getSubParams().put("MAIL", mail[1]);
                        }
                        
                    } catch (NumberFormatException ex) {
                        final String message = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(message);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        final String msg = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(msg);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                    zex.setMenuLevel(10);
                    return processZexpressLevel10Menu(zex, request);
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }
        
        final int option;
        if(menu.equalsIgnoreCase("GAZ")){
            if(zex.getSubParams().get("LAST_ORDER").equals(false)){
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    zex.setQuantity(option);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            
            //Json to get Previous Adresses
            try{
                final String url = zexpress_url;
                final Gson gson = new Gson();
                JsonObject getMovieTicketTypeList = new JsonObject();
                    getMovieTicketTypeList.addProperty("menu", "address");
                    getMovieTicketTypeList.addProperty("type", request.getMsisdn());
                final String addressListString = new HTTPUtil().getList(getMovieTicketTypeList, url);
                final ArrayList<Address> user = gson.fromJson(addressListString, new TypeToken<List<Address>>(){}.getType());
                zex.setAddressList(user);
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel5Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            if(zex.getAddressList().isEmpty()){
                zex.getSubParams().put("DELIVERY_OPTION", "1");
                zex.setMenuLevel(7);
                return processZexpressLevel7Menu(zex, request);
            }else{
                final String respMessage = "Votre adresse de livraison: \n1. Ajouter une adresse \n2. Choisir une adresse";
                resp.setApplicationResponse(respMessage);
            }
            
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else if(menu.equalsIgnoreCase("REPAS")){
            if(zex.getSubParams().get("LAST_ORDER").equals(false)){
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    zex.setQuantity(option);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            
            //Json to get Previous Adresses
            try{
                final String url = zexpress_url;
                final Gson gson = new Gson();
                JsonObject getMovieTicketTypeList = new JsonObject();
                    getMovieTicketTypeList.addProperty("menu", "address");
                    getMovieTicketTypeList.addProperty("type", request.getMsisdn());
                final String addressListString = new HTTPUtil().getList(getMovieTicketTypeList, url);
                final ArrayList<Address> user = gson.fromJson(addressListString, new TypeToken<List<Address>>(){}.getType());
                zex.setAddressList(user);
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel5Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            if(zex.getAddressList().isEmpty()){
                zex.getSubParams().put("DELIVERY_OPTION", "1");
                zex.setMenuLevel(7);
                return processZexpressLevel7Menu(zex, request);
            }else{
                final String respMessage = "Votre adresse de livraison: \n1. Ajouter une adresse \n2. Choisir une adresse";
                resp.setApplicationResponse(respMessage);
            }
            
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else if(menu.equalsIgnoreCase("FLEUR")){
            if(zex.getSubParams().get("FIRST_ORDER").equals(false)){
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    if (option < 1 || option > 2) {
                        throw new NumberFormatException();
                    }
                    zex.getSubParams().put("DELIVERY_OPTION", option);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            final String respMessage;
            switch (Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString())) {
                case 1:
                    respMessage = "Entrer votre adresse, N° Rue, N° Maison:";
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(zex.getMsisdn(), zex);
                    return resp;
                //break;
                case 2:
                    if(!zex.getAddressList().isEmpty()){
                        final StringBuilder msgResp = new StringBuilder("Veuillez choisir l'une de vos adresses précédentes:");
                        int i=0;
                        for(final Address data: zex.getAddressList()){
                            msgResp.append("\n").append(++i).append(". ").append(data.nom);
                        }
                        resp.setApplicationResponse(msgResp.toString());
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(zex.getMsisdn(), zex);
                        return resp;
                    }else{
                        resp.setMsisdn(request.getMsisdn());
                        resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                        resp.setFreeflow(UssdConstants.BREAK);
                        activeSessions_Zex.remove(request.getMsisdn());
                        Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > zex.getMovieTicketTypeList().size()) {
                    throw new NumberFormatException();
                }
                zex.setSelectedMovieTicketType(option-1);
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            final String respMessage = "Nombre de ticket(s): ";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel7Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level7 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            /*try {
                zex.getSubParams().put("VILLE", request.getSubscriberInput());
            } catch (Exception ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }*/ 
            try {
                zex.getSubParams().put("NUMERO_POLICE", request.getSubscriberInput());
            } catch (Exception ex) {
                final String message = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(message);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            final String respMessage = "Nom du propriétaire du compteur:";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }
        
        final int option;
        if(menu.equalsIgnoreCase("GAZ")){
            if(zex.getSubParams().get("FIRST_ORDER").equals(false)){
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    if (option < 1 || option > 2) {
                        throw new NumberFormatException();
                    }
                    zex.getSubParams().put("DELIVERY_OPTION", option);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            final String respMessage;
            switch (Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString())) {
                case 1:
                    respMessage = "Entrer votre adresse, N° Rue, N° Maison:";
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(zex.getMsisdn(), zex);
                    return resp;
                //break;
                case 2:
                    if(!zex.getAddressList().isEmpty()){
                        final StringBuilder msgResp = new StringBuilder("Veuillez choisir l'une de vos adresses précédentes:");
                        int i=0;
                        for(final Address data: zex.getAddressList()){
                            msgResp.append("\n").append(++i).append(". ").append(data.nom);
                        }
                        resp.setApplicationResponse(msgResp.toString());
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(zex.getMsisdn(), zex);
                        return resp;
                    }else{
                        resp.setMsisdn(request.getMsisdn());
                        resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                        resp.setFreeflow(UssdConstants.BREAK);
                        activeSessions_Zex.remove(request.getMsisdn());
                        Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("REPAS")){
            if(zex.getSubParams().get("FIRST_ORDER").equals(false)){
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    if (option < 1 || option > 2) {
                        throw new NumberFormatException();
                    }
                    zex.getSubParams().put("DELIVERY_OPTION", option);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            final String respMessage;
            switch (Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString())) {
                case 1:
                    respMessage = "Entrer votre adresse, N° Rue, N° Maison:";
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(zex.getMsisdn(), zex);
                    return resp;
                //break;
                case 2:
                    if(!zex.getAddressList().isEmpty()){
                        final StringBuilder msgResp = new StringBuilder("Veuillez choisir l'une de vos adresses précédentes:");
                        int i=0;
                        for(final Address data: zex.getAddressList()){
                            msgResp.append("\n").append(++i).append(". ").append(data.nom);
                        }
                        resp.setApplicationResponse(msgResp.toString());
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(zex.getMsisdn(), zex);
                        return resp;
                    }else{
                        resp.setMsisdn(request.getMsisdn());
                        resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                        resp.setFreeflow(UssdConstants.BREAK);
                        activeSessions_Zex.remove(request.getMsisdn());
                        Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("FLEUR")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString());
            switch (choice) {
                case 1:
                    try {
                        //zex.getAddress().setAddress(request.getSubscriberInput());
                        zex.getSubParams().put("ADDRESS", request.getSubscriberInput());
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (Exception ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                case 2:
                    try {
                        final int data = Integer.parseInt(request.getSubscriberInput());
                        if (data < 1 || data > zex.getAddressList().size()) {
                            throw new NumberFormatException();
                        }
                        zex.setSelectedAddress(data-1);
                        String s="@"+zex.getAddressList().get(zex.getSelectedAddress()).getNom();
                        zex.getSubParams().put("ADDRESS", s);
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (NumberFormatException ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            if(zex.getSubParams().get("LAST_ORDER").equals(false)){
                try {
                    option = Integer.parseInt(request.getSubscriberInput());
                    zex.setQuantity(option);
                } catch (NumberFormatException ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            
            //Json to get Previous Adresses
            try{
                final String url = zexpress_url;
                final Gson gson = new Gson();
                JsonObject getMovieTicketTypeList = new JsonObject();
                    getMovieTicketTypeList.addProperty("menu", "address");
                    getMovieTicketTypeList.addProperty("type", request.getMsisdn());
                final String addressListString = new HTTPUtil().getList(getMovieTicketTypeList, url);
                final ArrayList<Address> user = gson.fromJson(addressListString, new TypeToken<List<Address>>(){}.getType());
                zex.setAddressList(user);
            }catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }catch (Exception ex) {
                Logger.getLogger("processZexpressLevel7Menu").info("Exception encountered. Reason: " + ex.getMessage());
                resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.ARAD_REQUEST_FAILED.toString()));
                resp.setFreeflow(UssdConstants.BREAK);
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            if(zex.getAddressList().isEmpty()){
                zex.getSubParams().put("DELIVERY_OPTION", "1");
                zex.setMenuLevel(8);
                return processZexpressLevel8Menu(zex, request);
            }else{
                final String respMessage = "Votre adresse de livraison: \n1. Ajouter une adresse \n2. Choisir une adresse";
                resp.setApplicationResponse(respMessage);
            }
            
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
            
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel8Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level8 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            try {
                zex.getSubParams().put("PROPRIETAIRE", request.getSubscriberInput());
            } catch (Exception ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            } 
            /*final String respMessage = "Veuillez entrer votre contact whatsapp:";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;*/
            zex.setMenuLevel(10);
            return processZexpressLevel10Menu(zex, request);
        }
        
        final String respMessage;
        if(menu.equalsIgnoreCase("GAZ")){
            final int option = Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString());
            switch (option) {
                case 1:
                    //Express
                    /*Calendar now = Calendar.getInstance();
                    now.set(Calendar.HOUR_OF_DAY, 1);
                    zex.getAddress().setDate(now.toString());*/
                    
                    try {
                        //zex.getAddress().setAddress(request.getSubscriberInput());
                        zex.getSubParams().put("ADDRESS", request.getSubscriberInput());
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (Exception ex) {
                        respMessage = "Texte Invalide '"+request.getSubscriberInput()+"'";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                //break;
                case 2:
                    try {
                        final int choice = Integer.parseInt(request.getSubscriberInput());
                        if (choice < 1 || choice > zex.getAddressList().size()) {
                            throw new NumberFormatException();
                        }
                        zex.setSelectedAddress(choice-1);
                        String s="@"+zex.getAddressList().get(zex.getSelectedAddress()).getNom();
                        zex.getSubParams().put("ADDRESS", s);
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("REPAS")){
            final int option = Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString());
            switch (option) {
                case 1:
                    //Express
                    /*Calendar now = Calendar.getInstance();
                    now.set(Calendar.HOUR_OF_DAY, 1);
                    zex.getAddress().setDate(now.toString());*/
                    
                    try {
                        //zex.getAddress().setAddress(request.getSubscriberInput());
                        zex.getSubParams().put("ADDRESS", request.getSubscriberInput());
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (Exception ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                //break;
                case 2:
                    try {
                        final int choice = Integer.parseInt(request.getSubscriberInput());
                        if (choice < 1 || choice > zex.getAddressList().size()) {
                            throw new NumberFormatException();
                        }
                        zex.setSelectedAddress(choice-1);
                        String s="@"+zex.getAddressList().get(zex.getSelectedAddress()).getNom();
                        zex.getSubParams().put("ADDRESS", s);
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("FLEUR")){
            final int choice;
            try {
                choice = Integer.parseInt(request.getSubscriberInput());
                if (choice < 1 || choice > 2) {
                    throw new NumberFormatException();
                }
                zex.getSubParams().put("DELIVERY_ADDRESS", choice);
            } catch (NumberFormatException ex) {
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            switch (choice) {
                case 1:
                    //Express
                    //final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.HOUR, 1);
                    //fmt.format(now.getTime());
                    //zex.getAddress().setDate(now.toString());
                    zex.setAddressDate(now.getTime());
                    
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                    
                //break;
                case 2:
                    resp.setApplicationResponse("Veuillez renseigner la date (JJMMAAAA): ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            if(zex.getSubParams().get("FIRST_ORDER").equals(false)){
                final int choice;
                try {
                    choice = Integer.parseInt(request.getSubscriberInput());
                    if (choice < 1 || choice > 2) {
                        throw new NumberFormatException();
                    }
                    zex.getSubParams().put("DELIVERY_OPTION", choice);
                } catch (NumberFormatException ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                } catch (Exception ex) {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            }
            switch (Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString())) {
                case 1:
                    respMessage = "Entrer votre adresse, N° Rue, N° Maison:";
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(zex.getMsisdn(), zex);
                    return resp;
                //break;
                case 2:
                    if(!zex.getAddressList().isEmpty()){
                        final StringBuilder msgResp = new StringBuilder("Veuillez choisir l'une de vos adresses précédentes:");
                        int i=0;
                        for(final Address data: zex.getAddressList()){
                            msgResp.append("\n").append(++i).append(". ").append(data.nom);
                        }
                        resp.setApplicationResponse(msgResp.toString());
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(zex.getMsisdn(), zex);
                        return resp;
                    }else{
                        resp.setMsisdn(request.getMsisdn());
                        resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                        resp.setFreeflow(UssdConstants.BREAK);
                        activeSessions_Zex.remove(request.getMsisdn());
                        Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel9Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level9 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            /*try {
                zex.getSubParams().put("CONTACT", request.getSubscriberInput());
            } catch (Exception ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            } 
            final String respMessage = "Entrer votre adresse mail:";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;*/
        }
        
        final int option;
        final String respMessage;
        if(menu.equalsIgnoreCase("GAZ")){
            try {
                option = Integer.parseInt(request.getSubscriberInput());
                if (option < 1 || option > 2) {
                    throw new NumberFormatException();
                }
                zex.getSubParams().put("DELIVERY_ADDRESS", option);
            } catch (NumberFormatException ex) {
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            switch (option) {
                case 1:
                    //Express
                    //final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.HOUR, 1);
                    //fmt.format(now.getTime());
                    //zex.getAddress().setDate(now.toString());
                    zex.setAddressDate(now.getTime());
                    
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                //break;
                case 2:
                    resp.setApplicationResponse("Veuillez renseigner la date (JJMMAAAA): ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("REPAS")){
            final int choice;
            try {
                choice = Integer.parseInt(request.getSubscriberInput());
                if (choice < 1 || choice > 2) {
                    throw new NumberFormatException();
                }
                zex.getSubParams().put("DELIVERY_ADDRESS", choice);
            } catch (NumberFormatException ex) {
                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            switch (choice) {
                case 1:
                    //Express
                    //final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.HOUR, 1);
                    //fmt.format(now.getTime());
                    //zex.getAddress().setDate(now.toString());
                    zex.setAddressDate(now.getTime());
                    
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                    
                //break;
                case 2:
                    resp.setApplicationResponse("Veuillez renseigner la date (JJMMAAAA): ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("FLEUR")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());   
            switch (choice) {
                case 1:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getFlower()), zex.getFlowerList().get(zex.getSelectedFlower()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                case 2:
                    if (datePattern.matcher(request.getSubscriberInput()).matches()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                        final Date deliveryDate;
                        try {
                            deliveryDate = sdf.parse(request.getSubscriberInput());
                            Calendar now = Calendar.getInstance();
                            //now.set(Calendar.HOUR_OF_DAY, 23);
                            if (deliveryDate.before(now.getTime())) {//compares date portions only
                                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("date entered{" + deliveryDate + "} is before now():" + now);
                                activeSessions_Zex.remove(request.getMsisdn());
                                return resp;
                            } else {
                                //zex.getAddress().setDate(deliveryDate.toString());
                                zex.setAddressDate(deliveryDate);
                            }
                        } catch (ParseException ex) {
                            Logger.getLogger("qos_ussd_processor").info(ex);
                            final String responseMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
                            resp.setApplicationResponse(responseMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } else {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            option = Integer.parseInt(zex.getSubParams().get("DELIVERY_OPTION").toString());
            switch (option) {
                case 1:
                    //Express
                    /*Calendar now = Calendar.getInstance();
                    now.set(Calendar.HOUR_OF_DAY, 1);
                    zex.getAddress().setDate(now.toString());*/
                    
                    try {
                        //zex.getAddress().setAddress(request.getSubscriberInput());
                        zex.getSubParams().put("ADDRESS", request.getSubscriberInput());
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (Exception ex) {
                        respMessage = "Texte Invalide '"+request.getSubscriberInput()+"'";//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                //break;
                case 2:
                    try {
                        final int choice = Integer.parseInt(request.getSubscriberInput());
                        if (choice < 1 || choice > zex.getAddressList().size()) {
                            throw new NumberFormatException();
                        }
                        zex.setSelectedAddress(choice-1);
                        String s="@"+zex.getAddressList().get(zex.getSelectedAddress()).getNom();
                        zex.getSubParams().put("ADDRESS", s);
                        resp.setApplicationResponse("Veuillez renseigner la date de livraison: \n1. Express =====> 1h  \n2. Renseigner la date");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel10Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level10 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            if(zex.getSubParams().get("COMPTEUR").toString().equals("1")){
                /*try {
                    zex.getSubParams().put("MAIL", request.getSubscriberInput());
                } catch (Exception ex) {
                    final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }*/
            }
            final String respMessage = "Montant (Recharge a partir de 2000):";
            resp.setApplicationResponse(respMessage);
            resp.setFreeflow(UssdConstants.CONTINUE);
            zex.incrementMenuLevel();
            activeSessions_Zex.put(zex.getMsisdn(), zex);
            return resp;
        }
        
        //final int option;
        //final String respMessage;
        if(menu.equalsIgnoreCase("GAZ")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());     
            switch (choice) {
                case 1:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getGas()), zex.getGasWeightList().get(zex.getSelectedGasWeight()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (NumberFormatException ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }

                //break;
                case 2:
                    if (datePattern.matcher(request.getSubscriberInput()).matches()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                        final Date deliveryDate;
                        try {
                            deliveryDate = sdf.parse(request.getSubscriberInput());
                            Calendar now = Calendar.getInstance();
                            //now.set(Calendar.HOUR_OF_DAY, 23);
                            if (deliveryDate.before(now.getTime())) {//compares date portions only
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("date entered{" + deliveryDate + "} is before now():" + now);
                                activeSessions_Zex.remove(request.getMsisdn());
                                return resp;
                            } else {
                                //zex.getAddress().setDate(deliveryDate.toString());
                                zex.setAddressDate(deliveryDate);
                            }
                        } catch (ParseException ex) {
                            Logger.getLogger("qos_ussd_processor").info(ex);
                            final String responseMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
                            resp.setApplicationResponse(responseMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } else {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("REPAS")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());   
            switch (choice) {
                case 1:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getMeal()), zex.getMealList().get(zex.getSelectedMeal()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        return resp;
                    } catch (NumberFormatException ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                case 2:
                    if (datePattern.matcher(request.getSubscriberInput()).matches()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                        final Date deliveryDate;
                        try {
                            deliveryDate = sdf.parse(request.getSubscriberInput());
                            Calendar now = Calendar.getInstance();
                            //now.set(Calendar.HOUR_OF_DAY, 23);
                            if (deliveryDate.before(now.getTime())) {//compares date portions only
                                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("date entered{" + deliveryDate + "} is before now():" + now);
                                activeSessions_Zex.remove(request.getMsisdn());
                                return resp;
                            } else {
                                //zex.getAddress().setDate(deliveryDate.toString());
                                zex.setAddressDate(deliveryDate);
                            }
                        } catch (ParseException ex) {
                            Logger.getLogger("qos_ussd_processor").info(ex);
                            final String responseMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
                            resp.setApplicationResponse(responseMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } else {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("FLEUR")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());
            switch (choice) {
                case 1:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref = "vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df2.format(zex.getAddressDate())).append("|")
                                    .append("flower=").append(ZexpressMenus.translate(zex.getFlowerList().get(zex.getSelectedFlower()).getName())).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                final String respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    final String respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    final String respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                //break;
                case 2:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getFlower()), zex.getFlowerList().get(zex.getSelectedFlower()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        return resp;
                    } catch (NumberFormatException ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            final int choice;
            try {
                choice = Integer.parseInt(request.getSubscriberInput());
                if (choice < 1 || choice > 2) {
                    throw new NumberFormatException();
                }
                zex.getSubParams().put("DELIVERY_ADDRESS", choice);
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
            
            switch (choice) {
                case 1:
                    //Express
                    //final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.HOUR, 1);
                    //fmt.format(now.getTime());
                    //zex.getAddress().setDate(now.toString());
                    zex.setAddressDate(now.getTime());
                    
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                    
                //break;
                case 2:
                    resp.setApplicationResponse("Veuillez renseigner la date (JJMMAAAA): ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(Integer.toString(choice));//UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel11Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level11 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        
        if(menu.equalsIgnoreCase("FACTURES")){
            
            try {
                zex.getSubParams().put("AMOUNT_ON_BILL", Integer.valueOf(request.getSubscriberInput()));
                zex.getSubParams().put("MONTANT", (int) ceil(Integer.valueOf(request.getSubscriberInput()) / 5.0) * 5);

                //Json to get List of Meter fees
                try{
                    final String url = zexpress_url;
                    final Gson gson = new Gson();
                    JsonObject getMeterFeesList = new JsonObject();
                        getMeterFeesList.addProperty("menu", "meterFee");
                    final String addressListString = new HTTPUtil().getList(getMeterFeesList, url);
                    final ArrayList<MeterFees> user = gson.fromJson(addressListString, new TypeToken<List<MeterFees>>(){}.getType());
                    int i = Integer.valueOf(zex.getSubParams().get("MONTANT").toString());
                    for(MeterFees meter : user){
                        if (i >= meter.getDebut() && i <= meter.getFin()) zex.getSubParams().put("FRAIS_COURSE", meter.getFrais());
                    }
                    if(user.isEmpty()) zex.getSubParams().put("FRAIS_COURSE", 0);
                }catch (JsonSyntaxException ex) {
                    Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                    final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                    resp.setApplicationResponse(resMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
                double response = Integer.sum(Integer.valueOf(zex.getSubParams().get("MONTANT").toString()), Integer.valueOf(zex.getSubParams().get("FRAIS_COURSE").toString()));
                double frais = ceil((int) response * 0.017);
                response = Double.sum(response, frais);
                zex.getSubParams().put("FRAIS", frais);
                zex.setAmount(new BigDecimal(Math.round((float) response)));
                zex.setFrais(new BigDecimal(Math.round((float) frais)));
                final String message = "Montant: {AMOUNT}FCFA (dont Frais de Course: {COURSE}FCFA et Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                msg = msg.replace("{COURSE}", df.format(Double.parseDouble(zex.getSubParams().get("FRAIS_COURSE").toString())));
                resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                resp.setFreeflow(UssdConstants.CONTINUE);
                zex.incrementMenuLevel();
                activeSessions_Zex.put(request.getMsisdn(), zex);
                return resp;
            } catch (NumberFormatException ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            } catch (Exception ex) {
                final String respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(respMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        
        }
        
        //final int option;
        final String respMessage;
        if(menu.equalsIgnoreCase("GAZ")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());
            switch (choice) {
                case 1:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref = "vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df2.format(zex.getAddressDate())).append("|")
                                    .append("gasWeight=").append(ZexpressMenus.translate(zex.getGasWeightList().get(zex.getSelectedGasWeight()).getName())).append("|")
                                    .append("gasBrand=").append(ZexpressMenus.translate(zex.getGasBrandList().get(zex.getSelectedGasBrand()).getName())).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                case 2:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getGas()), zex.getGasWeightList().get(zex.getSelectedGasWeight()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("REPAS")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());
            switch (choice) {
                case 1:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref = "vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df2.format(zex.getAddressDate())).append("|")
                                    .append("mealType=").append(ZexpressMenus.translate(zex.getMealList().get(zex.getSelectedMeal()).getMealType())).append("|")
                                    .append("mealDish=").append(ZexpressMenus.translate(zex.getMealList().get(zex.getSelectedMeal()).getDish())).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                case 2:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getMeal()), zex.getMealList().get(zex.getSelectedMeal()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("FLEUR")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());
            switch (choice) {
                case 2:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref = "vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df1.format(zex.getAddressDate())).append("|")
                                    .append("flower=").append(ZexpressMenus.translate(zex.getFlowerList().get(zex.getSelectedFlower()).getName())).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());   
            switch (choice) {
                case 1:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getMovie()), zex.getMovieTicketTypeList().get(zex.getSelectedMovieTicketType()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                case 2:
                    if (datePattern.matcher(request.getSubscriberInput()).matches()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                        final Date deliveryDate;
                        try {
                            deliveryDate = sdf.parse(request.getSubscriberInput());
                            Calendar now = Calendar.getInstance();
                            //now.set(Calendar.HOUR_OF_DAY, 23);
                            if (deliveryDate.before(now.getTime())) {//compares date portions only
                                respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.DEPARTURE_DATE_IS_BEFORE_NOW.toString());
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                                Logger.getLogger("qos_ussd_processor").info("date entered{" + deliveryDate + "} is before now():" + now);
                                activeSessions_Zex.remove(request.getMsisdn());
                                return resp;
                            } else {
                                //zex.getAddress().setDate(deliveryDate.toString());
                                zex.setAddressDate(deliveryDate);
                            }
                        } catch (ParseException ex) {
                            Logger.getLogger("qos_ussd_processor").info(ex);
                            final String responseMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_DATE.toString());
                            resp.setApplicationResponse(responseMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("invalid date entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } else {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    resp.setApplicationResponse("Note de commande: ");
                    resp.setFreeflow(UssdConstants.CONTINUE);
                    zex.incrementMenuLevel();
                    activeSessions_Zex.put(request.getMsisdn(), zex);
                    return resp;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel12Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level12 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String respMessage;
        final String menu = zex.getSubParams().get("MENU").toString();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 30);
        
        if(menu.equalsIgnoreCase("FACTURES")){
            try {
                final int option = Integer.parseInt(request.getSubscriberInput());
                if (option == 1) {
                    //make reservation

                    Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                    JsonObject requestPayment = new JsonObject();
                    requestPayment.addProperty("msisdn", zex.getMsisdn());
                    requestPayment.addProperty("amount", zex.getAmount());

                    //final String transref = "vide";
                    final StringBuilder transref = new StringBuilder();
                    transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                            .append("address=").append("@").append("|")
                            .append("delivery=").append("").append("|")
                            .append("notes=").append("").append("|")
                            .append("quantity=").append("1").append("|")
                            .append("price=").append(zex.getAmount()).append("|")
                            .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                            .append("date=").append(df2.format(now.getTime())).append("|")
                            .append("nocompteur=").append(zex.getSubParams().get("NUMERO_COMPTEUR").toString()).append("|")
                            .append("compteur=").append(zex.getSubParams().get("FACTURES").toString()).append("|");
                            if(zex.getSubParams().get("FACTURES").equals("Electricite") && zex.getSubParams().get("COMPTEUR_TYPE") != null){
                                transref.append("compteurType=").append(zex.getSubParams().get("COMPTEUR_TYPE").toString()).append("|");
                            }else{
                                transref.append("compteurType=").append("").append("|");
                            }
                            transref.append("ville=").append(".").append("|")
                            .append("proprietaire=").append(ZexpressMenus.translate(zex.getSubParams().get("PROPRIETAIRE").toString())).append("|")
                            .append("contact=").append(".").append("|")
                            .append("mail=").append(".").append("|")
                            .append("montant=").append(zex.getSubParams().get("AMOUNT_ON_BILL").toString()).append("|")
                            .append("nopolice=").append(zex.getSubParams().get("NUMERO_POLICE").toString()).append("|")
                            .append("sessionid=").append(request.getSessionId())
                    ;
                    requestPayment.addProperty("transref", request.getSessionId());
                    requestPayment.addProperty("specialfield1", transref.toString());
                    requestPayment.addProperty("clientid", zex.getMerchantCode());

                    final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                    if (response.equals("")) {
                        Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                        respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                    } else {
                        JsonParser parseResponse = new JsonParser();
                        JsonObject jo = (JsonObject) parseResponse.parse(response);
                        if (jo.get("responsecode").getAsString().equals("01")) {
                            respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        } else {
                            respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                        }
                    }
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                } else {
                    respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                    resp.setApplicationResponse(respMessage);
                    resp.setFreeflow(UssdConstants.BREAK);
                    Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                    activeSessions_Zex.remove(request.getMsisdn());
                    return resp;
                }
            } catch (NumberFormatException ex) {
                final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            } catch (JsonSyntaxException ex) {
                Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                resp.setApplicationResponse(resMessage);
                resp.setFreeflow(UssdConstants.BREAK);
                Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                activeSessions_Zex.remove(request.getMsisdn());
                return resp;
            }
        }
        
        //final int option;
        if(menu.equalsIgnoreCase("GAZ")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());
            switch (choice) {
                case 2:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref = "vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df1.format(zex.getAddressDate())).append("|")
                                    .append("gasWeight=").append(ZexpressMenus.translate(zex.getGasWeightList().get(zex.getSelectedGasWeight()).getName())).append("|")
                                    .append("gasBrand=").append(ZexpressMenus.translate(zex.getGasBrandList().get(zex.getSelectedGasBrand()).getName())).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        } else if(menu.equalsIgnoreCase("REPAS")){
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());
            switch (choice) {
                case 2:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref ="vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df1.format(zex.getAddressDate())).append("|")
                                    .append("mealType=").append(ZexpressMenus.translate(zex.getMealList().get(zex.getSelectedMeal()).getMealType())).append("|")
                                    .append("mealDish=").append(ZexpressMenus.translate(zex.getMealList().get(zex.getSelectedMeal()).getDish())).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }else if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());
            switch (choice) {
                case 1:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref = "vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df2.format(zex.getAddressDate())).append("|")
                                    .append("movieTime=").append(ZexpressMenus.translate(zex.getMovieTicketHoursList().get(zex.getSelectedMovieTicketHours()).getName())).append("|")
                                    .append("cinemaTime=").append(df1.format(zex.getMovieDay())).append("|")
                                    .append("ticketType=").append(zex.getMovieTicketTypeList().get(zex.getSelectedMovieTicketType()).getName()).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(resMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    final String resMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(resMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(resMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(resMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                    
                //break;
                case 2:
                    try{
                        zex.getSubParams().put("NOTE", request.getSubscriberInput());
                        double response = zex.getQuantity() * Integer.sum(Integer.valueOf(zex.getDeliveryList().get(zex.getSelectedDelivery()).getMovie()), zex.getMovieTicketTypeList().get(zex.getSelectedMovieTicketType()).getPrice());
                        double frais = (int) response * 0.017;
                        response = Double.sum(response, frais);
                        zex.getSubParams().put("FRAIS", frais);
                        zex.setAmount(new BigDecimal(Math.round((float) response)));
                        zex.setFrais(new BigDecimal(Math.round((float) frais)));
                        final String message = "Montant de votre commande: {AMOUNT}FCFA (dont Frais Envoi: {FRAIS}FCFA). Voulez vous procéder au paiement? \n1. Oui \n2. Non";
                        String msg = message.replace("{FRAIS}", df.format(zex.getFrais().doubleValue()));
                        resp.setApplicationResponse(msg.replace("{AMOUNT}", zex.getAmount().toString()));
                        //resp.setApplicationResponse("AMOUNT 36000 XOF. Voulez vous procéder au paiement? \n1. Oui \n2. Non");
                        resp.setFreeflow(UssdConstants.CONTINUE);
                        zex.incrementMenuLevel();
                        activeSessions_Zex.put(request.getMsisdn(), zex);
                        return resp;
                    } catch (NumberFormatException ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (Exception ex) {
                        respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(respMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }
        else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
            return resp;
        }
    }
    
    private UssdResponse processZexpressLevel13Menu(ZexpressInfo zex, UssdRequest request) {
        Logger.getLogger("qos_ussd_processor").info("Zexpress menu level13 for " + request.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(request.getMsisdn());
        final String menu = zex.getSubParams().get("MENU").toString();
        //final int option;
        final String respMessage;
        if(menu.equalsIgnoreCase("TICKET_CINEMA")){
            
            final int choice = Integer.parseInt(zex.getSubParams().get("DELIVERY_ADDRESS").toString());   
            switch (choice) {
                case 2:
                    try {
                        final int option = Integer.parseInt(request.getSubscriberInput());
                        if (option == 1) {
                            //make reservation

                            Logger.getLogger("qos_ussd_processor").info("processing zexpress transaction for{" + zex.toString() + "} by" + request.getMsisdn());
                            JsonObject requestPayment = new JsonObject();
                            requestPayment.addProperty("msisdn", zex.getMsisdn());
                            requestPayment.addProperty("amount", zex.getAmount());

                            //final String transref = "vide";
                            final StringBuilder transref = new StringBuilder();
                            transref.append("msisdn=").append(zex.getMsisdn()).append("|")
                                    .append("address=").append(ZexpressMenus.translate(zex.getSubParams().get("ADDRESS").toString())).append("|")
                                    .append("delivery=").append(ZexpressMenus.translate(zex.getDeliveryList().get(zex.getSelectedDelivery()).getName())).append("|")
                                    .append("notes=").append(ZexpressMenus.translate(zex.getSubParams().get("NOTE").toString())).append("|")
                                    .append("quantity=").append(zex.getQuantity()).append("|")
                                    .append("price=").append(zex.getAmount()).append("|")
                                    .append("fee=").append(df.format(zex.getFrais().doubleValue())).append("|")
                                    .append("date=").append(df1.format(zex.getAddressDate())).append("|")
                                    .append("movieTime=").append(ZexpressMenus.translate(zex.getMovieTicketHoursList().get(zex.getSelectedMovieTicketHours()).getName())).append("|")
                                    .append("cinemaTime=").append(df1.format(zex.getMovieDay())).append("|")
                                    .append("ticketType=").append(ZexpressMenus.translate(zex.getMovieTicketTypeList().get(zex.getSelectedMovieTicketType()).getName())).append("|")
                                    .append("sessionid=").append(request.getSessionId())
                            ;
                            
                            requestPayment.addProperty("transref", request.getSessionId());
                            requestPayment.addProperty("specialfield1", transref.toString());
                            requestPayment.addProperty("clientid", zex.getMerchantCode());

                            final String response = new HTTPUtil().sendZexRequestPayment(requestPayment);
                            if (response.equals("")) {
                                Logger.getLogger("qos_ussd_processor").info("sendZexRequestPayment returned empty response");
                                respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                resp.setApplicationResponse(respMessage);
                                resp.setFreeflow(UssdConstants.BREAK);
                            } else {
                                JsonParser parseResponse = new JsonParser();
                                JsonObject jo = (JsonObject) parseResponse.parse(response);
                                if (jo.get("responsecode").getAsString().equals("01")) {
                                    respMessage = "Votre demande est en cours de traitement. Vous recevrez une demande de paiement dans un instant si votre solde est suffisant.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                } else {
                                    respMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                                    resp.setApplicationResponse(respMessage);
                                    resp.setFreeflow(UssdConstants.BREAK);
                                }
                            }
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        } else {
                            respMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.CANCEL_TRANSACTION.toString());
                            resp.setApplicationResponse(respMessage);
                            resp.setFreeflow(UssdConstants.BREAK);
                            Logger.getLogger("qos_ussd_processor").info("user opted to cancel transaction for Zexpress. user:" + request.getMsisdn());
                            activeSessions_Zex.remove(request.getMsisdn());
                            return resp;
                        }
                    } catch (NumberFormatException ex) {
                        final String resMessage = UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INVALID_OPTION.toString());
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    } catch (JsonSyntaxException ex) {
                        Logger.getLogger("qos_ussd_processor").info("JsonSyntaxException. Reason: " + ex.getMessage());
                        final String resMessage = "Votre demande a echouee. Veuillez reessayer plus tard.";
                        resp.setApplicationResponse(resMessage);
                        resp.setFreeflow(UssdConstants.BREAK);
                        Logger.getLogger("qos_ussd_processor").info("invalid option entered{" + request.getSubscriberInput() + "} by" + request.getMsisdn());
                        activeSessions_Zex.remove(request.getMsisdn());
                        return resp;
                    }
                default:
                    resp.setMsisdn(request.getMsisdn());
                    resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
                    resp.setFreeflow(UssdConstants.BREAK);
                    activeSessions_Zex.remove(request.getMsisdn());
                    Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
                    return resp;
            }
        }
        else{
            resp.setMsisdn(request.getMsisdn());
            resp.setApplicationResponse(UssdConstants.MESSAGES.getProperty(USSDSessionHandler.MessageKey.INTERNAL_ERROR.toString()));
            resp.setFreeflow(UssdConstants.BREAK);
            activeSessions_Zex.remove(request.getMsisdn());
            Logger.getLogger("qos_ussd_processor").info(String.format("removed {%s} from active sessions", request.getMsisdn()));
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
    
    UssdResponse showMainMenu(ZexpressInfo zex) {
        Logger.getLogger("qos_ussd_processor").info("showing zexpress main menu to: " + zex.getMsisdn());
        final UssdResponse resp = new UssdResponse();
        resp.setMsisdn(zex.getMsisdn());
        final String respMessage = "Menu Principal \n1. Gaz \n2. Repas \n3. Fleur \n4. Ticket Cinema \n5. Factures";
        resp.setApplicationResponse(respMessage);
        resp.setFreeflow(UssdConstants.CONTINUE);
        zex.setMenuLevel(0);
        zex.incrementMenuLevel();
        //zex.setMerchantName();
        activeSessions_Zex.put(zex.getMsisdn(), zex);
        return resp;
    }

    private boolean isset(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
