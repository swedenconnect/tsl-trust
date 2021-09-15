/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.tillvaxtverket.ttsigvalws.ttwebservice;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import se.tillvaxtverket.tsltrust.common.utils.general.FilenameFilterImpl;
import se.tillvaxtverket.ttsigvalws.daemon.ServletListener;
import se.tillvaxtverket.ttsigvalws.resultpage.SigFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for provision of signature validation services based on TSL Trust
 * trust management
 */
public class TTSigValServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TTSigValServlet.class.getName());
    private SigValHandler sigValHandler;

    @Override public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        sigValHandler = new SigValHandler();
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        Locale respLocale = Locale.getDefault();
        ServletListener.baseModel.refreshTrustStore();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            processFileUpload(request, response);
            return;
        }

        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.getWriter().write("");
            return;
        }

        if (action.equals("logout")) {
            response.setContentType("text/html;charset=UTF-8");
            String authType = (request.getAuthType() == null) ? "" : request.getAuthType();
            response.getWriter().write("<logout>" + authType.toLowerCase() + "</logout>");
        }

        if (action.equals("policylist")) {
            boolean contentAdded = true;
            if (contentAdded) {
                response.setContentType("text/xml;charset=UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write("<policies>" + getPolicyData() + "</policies>");
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            return;
        }

        if (action.equals("serverdoclist")) {
            boolean contentAdded = true;
            if (contentAdded) {
                response.setContentType("text/xml;charset=UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write("<sigFiles>" + getServerdocs() + "</sigFiles>");
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            return;
        }

        if (action.equals("verify")) {
            String verifyResult = sigValHandler.verifyServerDocSignature(request);
            if (verifyResult != null) {
                response.setContentType("text/xml;charset=UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write(verifyResult);
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            return;
        }
        if (action.equals("postverify")) {
            sigValHandler.processValidationPost(request, response);
            return;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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

    private String getPolicyData() {
        StringBuilder sb = new StringBuilder();

        List<String> rootNames = ServletListener.baseModel.getTrustStore().getRootNames();
        if (rootNames != null) {
            for (String pn : rootNames) {
                sb.append("<policy>");
                sb.append("<policyName>").append(pn).append("</policyName>");
                sb.append("<policyInfo>" + "Some info about ").append(pn).append("</policyInfo>");
                sb.append("</policy>");
            }
        } else {
            sb.append("<policy>");
            sb.append("<policyName>No Policy</policyName>");
            sb.append("<policyInfo>No Information</policyInfo>");
            sb.append("</policy>");
        }
        return sb.toString();
    }

    private String getServerdocs() {
        StringBuilder sb = new StringBuilder();

        List<String> sigfFiles = getAvailableSignedDocuments();
        if (sigfFiles != null) {
            for (String pn : sigfFiles) {
                sb.append("<fileName>").append(pn).append("</fileName>");
            }
        } else {
            sb.append("<fileName>No signed documents</fileName>");
        }
        return sb.toString();
    }

    private List<String> getAvailableSignedDocuments() {
        List<String> sigFiles = new ArrayList<String>();
        File serverDocDir = new File(ServletListener.baseModel.getConf().getDataDirectory(), ServletListener.baseModel.getDocumentFolderName());
        if (serverDocDir.canRead()) {
            File[] fileList = serverDocDir.listFiles(new FilenameFilterImpl("."));
            if (fileList.length > 0) {
                for (File listedFile : fileList) {
                    String lfName = listedFile.getName().toLowerCase();
                    if (lfName.endsWith(".pdf")) {
                        sigFiles.add(listedFile.getName());
                    }
                    if (lfName.endsWith(".xml")) {
                        sigFiles.add(listedFile.getName());
                    }
                    if (lfName.endsWith(".xsig")) {
                        sigFiles.add(listedFile.getName());
                    }
                    if (lfName.endsWith(".xades")) {
                        sigFiles.add(listedFile.getName());
                    }
                }
            }
        }
        return sigFiles;
    }

    private void processFileUpload(HttpServletRequest request, HttpServletResponse response) {
        // Create a factory for disk-based file items
        Map<String, String> paraMap = new HashMap<String, String>();
        SigFile sigFile = null;
        boolean uploaded = false;
        DiskFileItemFactory factory = new DiskFileItemFactory();
        File storageDir = new File(ServletListener.baseModel.getConf().getDataDirectory() + "/uploads");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        factory.setRepository(storageDir);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    String value = item.getString();
                    paraMap.put(name, value);
                } else {
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();
                    String contentType = item.getContentType();
                    boolean isInMemory = item.isInMemory();
                    long sizeInBytes = item.getSize();
                    sigFile = new SigFile(fileName);
                    File uploadedFile = sigFile.getStorageFile();
                    try {
                        item.write(uploadedFile);
                        uploaded = true;
                        uploadedFile.deleteOnExit();
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                }

            }
            if (uploaded && paraMap.containsKey("policy")) {
                String verifyResult = sigValHandler.verifySignature(paraMap.get("policy"), sigFile.getFileName(), sigFile.getStorageFile()).generateReport();
                sigFile.getStorageFile().delete();
                sigValHandler.sendValidationReport(verifyResult, response);
                return;
            }
            if (paraMap.containsKey("policy") && paraMap.containsKey("fileName")) {
                File serverSigFile = new File(sigValHandler.getFullSigFileName(paraMap.get("fileName")));
                String verifyResult = sigValHandler.verifySignature(paraMap.get("policy"), serverSigFile.getName(), serverSigFile).generateReport();
                sigValHandler.sendValidationReport(verifyResult, response);
                return;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        SigValHandler.nullResponse(response);
    }

}
