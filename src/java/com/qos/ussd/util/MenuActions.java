/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util;

import com.qos.ussd.dto.UssdRequest;
import com.qos.ussd.main.SubscriberInfo;

/**
 *
 * @author ptrack
 */
public enum MenuActions {
    exit {
        @Override
        public void execute(final SubscriberInfo sub, UssdRequest req) {
//            LoggingUtil.logDebugInfo("executing exit action");
//            session.getUserParamsMap().put(MessageKey.CONTINUE, USSDConstants.NO);
//            session.getUserParamsMap().put(MessageKey.RESPONSE_MESSAGE,
//                    StringsCollector.get(session, MessageKey.EXIT_MESSAGE));
//            // USSDConstants.activeSessions.remove(session.)
//            session.setAction(Action.end);
        }
    };

    public abstract void execute(final SubscriberInfo subs, UssdRequest req);
//        if(null== subs){
//            return null;
//        }
//        for (final ConsumerMenuActions menuoperation: ConsumerMenuActions.values()) {
//            if (menuoperation.toString().equals(operation)) {
//                return menuoperation;
//            }
//        }
//        return null;
}
