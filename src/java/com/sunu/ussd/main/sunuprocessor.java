/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.main;

import com.qos.ussd.dto.UssdRequest;
import com.qos.ussd.dto.UssdResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;


/**
 *
 * @author ptrack
 */
public class ussdprocessor extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            UssdRequest req;
            try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(UssdRequest.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    req = (UssdRequest) unmarshaller.unmarshal(request.getInputStream());
                } catch (JAXBException | IOException ex) {
                    Logger.getLogger("qos_ussd_processor").error("error occured: "+ex.getMessage());
                    Logger.getLogger(this.getClass()).error("JAXBException|IOException " + ex + " by " + Arrays.toString(ex.getStackTrace()).replaceAll(", ", "\n"));
                    return ;
                }
            Logger.getLogger("ussdcdrs").info(req);
            Logger.getLogger("qos_ussd_processor").info("req received= "+req);
            if(null != req.getType() && req.getType().equals("cleanup")){
                Logger.getLogger("qos_ussd_processor").info("cleanup req received= "+req);
                return;
            }
            
            UssdResponse resp= new USSDSessionHandler(req).processRequest();
//            if(USSDSessionHandler.activeSessions.containsKey(req.getMsisdn())){
//                resp = USSDSessionHandler.activeSessions.get(req.getMsisdn()).processRequest();
//            }else{
//                resp = new USSDSessionHandler(req).processRequest();
//            }
            
            try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(UssdResponse.class);
                    Marshaller m = jaxbContext.createMarshaller();
                    m.marshal(resp, out);
                    Logger.getLogger("ussdcdrs").info(resp);
                    Logger.getLogger("qos_ussd_processor").info(resp);
                } catch (JAXBException ex) {
                    Logger.getLogger("qos_ussd_processor").error("error occured while marshalling response: "+ex.getMessage());
                    Logger.getLogger(this.getClass()).error("JAXBException " + ex + " by " + Arrays.toString(ex.getStackTrace()).replaceAll(", ", "\n"));
                    //return ;
                }
            //out.println("Message received successful.");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
