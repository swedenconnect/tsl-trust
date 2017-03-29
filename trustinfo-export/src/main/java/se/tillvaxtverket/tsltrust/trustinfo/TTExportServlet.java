/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.tsltrust.trustinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author stefan
 */
public class TTExportServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TTExportServlet.class.getName());
    private static String tslTrustPublishLocation, sigServicePublishLocation;

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        tslTrustPublishLocation = sc.getInitParameter("TslTrustPublishLocation");
        sigServicePublishLocation = sc.getInitParameter("SigServicePublishLocation");
    }

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

        if (request.getServletPath().startsWith("/trustinfo")) {
            returnFile(request, response, tslTrustPublishLocation);
            return;
        }
        if (request.getServletPath().startsWith("/ca-publish")) {
            returnFile(request, response, sigServicePublishLocation);
            return;
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

    private void returnFile(HttpServletRequest request, HttpServletResponse response, String dataLocation) throws FileNotFoundException, IOException {

        String reqUrl = request.getRequestURL().toString();
        String reqFilePath = reqUrl.substring(reqUrl.lastIndexOf(request.getServletPath()) + request.getServletPath().length());
        String filePath = dataLocation + reqFilePath;
        File downloadFile = new File(filePath);
        if (!downloadFile.canRead()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        FileInputStream inStream = new FileInputStream(downloadFile);

        //LOG.info("Filepath: " + filePath);
        // obtains ServletContext
        ServletContext context = getServletContext();

        // gets MIME type of the file
        String mimeType = context.getMimeType(filePath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        //LOG.info("MIME type: " + mimeType);

        // modifies response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // obtains response's output stream
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inStream.close();
        outStream.close();
    }
}
