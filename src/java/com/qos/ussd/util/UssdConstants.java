/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util;

import java.util.Properties;

/**
 *
 * @author ptrack
 */
public class UssdConstants {

    public enum Action {
        CONTINUE("FC"),
        BREAK("FB");

        private final String action;

        Action(String s) {
            action = s;
        }

        public String getAction() {
            return action;
        }

        @Override
        public String toString() {
            return "Freeflow Action{" + "action=" + action + '}';
        }
    }
    
    public static final String CONTINUE = "FC";
    public static final String BREAK = "FB";
    public static final String NEW_REQUEST = "1";
    public static Properties MESSAGES = new Properties();
//    public static final String merchantDetails_Username="USR00";
//    public static final String merchantDetails_Password="YG739G5XFVPYYV4ADJVW";
    
    //live settings
//    public static final String merchantDetails_Username="QSUSR06";
//    public static final String merchantDetails_Password="Y0739G5XFVPYY11AIJK";
    
    //test settings
    public static final String merchantDetails_Username="USR00";
    public static final String merchantDetails_Password="YG739G5XFVPYYV4ADJVW";
    
    public static final String[] ARAD_COMPANIES={"","La Poste","Confort","ATT"};
    public static final String[] ARAD_DEPARTURE_LOCATIONS={"","Cotonou","Calavi / Arcon Ville","Calavi / IITA","Bohicon","Parakou","Malanville","Dassa-Zoume"};
    public static final String[] ARAD_DESTINATION_LOCATIONS={"","Cotonou","Calavi / Arcon Ville","Calavi / IITA","Bohicon","Parakou","Malanville","Dassa-Zoume"};
    public static final String[] ARAD_DEPARTURE_TIMES={"","07:00","11:00","15:00","19:00","21:00"};
    public static final String[] TVM_TAX_TYPE={"","TVMTP","TVMS","TVMTPP","TVMTPM","TVMTC"};
    public static final String[] TVM_PAYER_TYPE={"","P","E"};
}
