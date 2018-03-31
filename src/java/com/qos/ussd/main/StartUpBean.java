/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.main;

import com.qos.ussd.util.UssdConstants;
import java.io.FileInputStream;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.log4j.Logger;

/**
 *
 * @author epatabo
 */
@Singleton
@Startup
@LocalBean
public class StartUpBean {

    @PostConstruct
    public void init() {
        Logger logger = Logger.getLogger(this.getClass());
        String languagePath = "/applications/strings/ussdstrings.props";
        try {
            UssdConstants.MESSAGES.load(new FileInputStream(languagePath));
            //PropertyConfigurator.configure(p);
            logger.info("messages have been loaded from: "+languagePath);
        } catch (IOException e) {
            //DAMN! I'm not....
            logger.info("could not load messages from: "+languagePath+". Reason:s "+e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        /* Shutdown stuff here */
    }
}
