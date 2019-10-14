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

import iaik.x509.ocsp.net.OCSPContentHandlerFactory;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.general.FilenameFilterImpl;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.ConfigData;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.SigDocument;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller.SignatureValidationReport;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifierFactory;

/**
 * Servlet for provision of signature validation services based on TSL Trust
 * trust management
 */
public class TTSigValServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TTSigValServlet.class.getName());
    private static final String SERVER_DOC_FOLDER = "serverdocs";
    private ServletContext context;
    private SigValidationBaseModel baseModel;
    private String currentDir = System.getProperty("user.dir");
    private ResourceBundle infoText;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.context = config.getServletContext();
        // Remove any occurance of the BC provider
        Security.removeProvider("BC");
        // Insert the BC provider in a preferred position
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        try {
            SecurityManager secMan = new SecurityManager();
            secMan.checkSetFactory();
            HttpURLConnection.setContentHandlerFactory(new OCSPContentHandlerFactory());
            LOG.info("Setting URL Content handler factory to OCSPContentHandlerFactory");
        } catch (Exception ex) {
            LOG.warning("Error when setting URL content handler factory");
        }
        infoText = ResourceBundle.getBundle("infoText");
        LOG.info(currentDir);
        String dataDir = context.getInitParameter("DataDirectory");
        ConfigData conf = new ConfigData(dataDir);
        baseModel = new SigValidationBaseModel(conf);
        Locale.setDefault(new Locale(baseModel.getConf().getLanguageCode()));
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
        baseModel.refreshTrustStore();
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

        if (action.equals("authdata")) {
            response.setContentType("text/xml;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            StringBuilder b = new StringBuilder();
            String authType = request.getAuthType();
            String remoteUser = utf8(request.getRemoteUser());
            if (authType != null && remoteUser != null) {
                b.append("<remoteUser type=\"").append(authType).append("\">");
                b.append(remoteUser).append("</remoteUser>");
                b.append("<authContext>");
                b.append(getAuthContext(request));
                b.append("</authContext>");
                b.append("<userAttributes>");
                b.append(getAuthAttributes(request));
                b.append("</userAttributes>");
            }
            String authResponse = "<authData>" + b.toString() + "</authData>";
            response.getWriter().write(authResponse);
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
            String verifyResult = verifyServerDocSignature(request);
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
            processValidationPost(request, response);
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

        List<String> rootNames = baseModel.getTrustStore().getRootNames();
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
        File caDir = new File(baseModel.getConf().getDataDirectory(), SERVER_DOC_FOLDER);
        if (caDir.canRead()) {
            File[] fileList = caDir.listFiles(new FilenameFilterImpl("."));
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

    private String verifyServerDocSignature(HttpServletRequest request) {
        try {
            String sigFileName = getRequestFileName(request);
            String docName = request.getParameter("id");
            String policyName = request.getParameter("policy");
            policyName = policyName == null ? "" : policyName;

            return verifySignature(request, policyName, docName, sigFileName, null);
        } catch (Exception ex) {
        }
        return "";

    }

    private String verifySignature(HttpServletRequest request, String policyName, String docName, String sigFileName, byte[] sigBytes) {
        SigValidationModel model;
        Thread verifierTask;

        model = new SigValidationModel();
        model.setBaseModel(baseModel);
        SigDocument sigDoc = null;
        if (sigBytes == null) {
            sigDoc = new SigDocument(new File(sigFileName));
            model.setSigDocument(sigDoc);
        } else {
            sigDoc = new SigDocument(sigBytes);
            model.setSigDocument(sigDoc);
        }
        String pName = (policyName == null) ? "" : policyName;
        model.setPolicyName(pName);
        sigDoc.setDocName(docName == null ? "" : docName);
        String policyDesc;
        try {
            policyDesc = baseModel.getTrustStore().getPolicyDescMap().get(pName);
            model.setPolicyDescription(policyDesc);
        } catch (Exception ex) {
        }
        model.setCheckOcspAndCrl(false);
        model.setPrefSpeed(true);

        KeyStore keyStore = baseModel.getTrustStore().getKeyStore(pName);

        if (keyStore != null) {
            SigVerifier verifier = SigVerifierFactory.getSigVerifier(model);
            if (verifier != null) {
                try {
                    //Verify Signature
                    verifierTask = new Thread(verifier);
                    verifierTask.start();
                    verifierTask.join();
                    SignatureValidationReport report = new SignatureValidationReport(model);
                    return report.generateReport();
                } catch (InterruptedException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
        }
        return null;
    }

    private String getRequestFileName(HttpServletRequest request) {
        return getFullSigFileName(request.getParameter("id"));
    }

    private String getFullSigFileName(String formFileName) {
        String fileName = "";
        File serverDocsDir = new File(baseModel.getConf().getDataDirectory(), SERVER_DOC_FOLDER);
        if (serverDocsDir.canRead()) {
            File[] fileList = serverDocsDir.listFiles(new FilenameFilterImpl("."));
            for (File listedFile : fileList) {
                if (listedFile.getName().equalsIgnoreCase(formFileName)) {
                    fileName = listedFile.getAbsolutePath();
                }
            }
        }
        return fileName;
    }

    private String getAuthContext(HttpServletRequest request) {
        StringBuilder b = new StringBuilder();

        /**
         * Shib-Application-ID The applicationId property derived for the
         * request. Shib-Session-ID The internal session key assigned to the
         * session associated with the request. Shib-Identity-Provider The
         * entityID of the IdP that authenticated the user associated with the
         * request. Shib-Authentication-Instant The ISO timestamp provided by
         * the IdP indicating the time of authentication.
         * Shib-Authentication-Method The AuthenticationMethod or
         * <AuthnContextClassRef> value supplied by the IdP, if any.
         * Shib-AuthnContext-Class The AuthenticationMethod or
         * <AuthnContextClassRef> value supplied by the IdP, if any.
         * Shib-AuthnContext-Decl The <AuthnContextDeclRef> value supplied by
         * the IdP, if any.
         */
        String[] shibAttrIds = new String[]{"Shib-Application-ID", "Shib-Session-ID", "Shib-Identity-Provider", "Shib-Authentication-Instant",
            "Shib-Authentication-Method", "Shib-AuthnContext-Class", "Shib-AuthnContext-Decl"};

        for (String id : shibAttrIds) {
            String attribute = (String) request.getAttribute(id);
            if (attribute != null) {
                attribute = utf8(attribute);
                b.append("<context type=\"").append(id).append("\">");
                b.append(attribute).append("</context>");
            }
        }

        return b.toString();
    }

    private String getAuthAttributes(HttpServletRequest request) {
        StringBuilder b = new StringBuilder();

        //Get user attributes
        String[] attrIds = new String[]{"displayName", "cn", "initials", "sn", "givenName", "norEduPersonNIN", "personalIdentityNumber", "mail",
            "telephoneNumber", "mobileTelephoneNumber", "eppn", "persistent-id", "o", "ou", "departmentNumber", "employeeNumber", "employeeType", "title", "description",
            "affiliation", "entitlement", "street", "postOfficeBox", "postalCode", "st", "l", "preferredLanguage"};

        for (String id : attrIds) {
            String attribute = (String) request.getAttribute(id);

            if (attribute != null) {

                String full = utf8(attribute);
                String[] values = full.split(";");
                for (String attr : values) {
                    String attrLabel = (infoText.containsKey(id)) ? infoText.getString(id) : id;
                    b.append("<attribute type=\"").append(attrLabel).append("\">");
                    b.append(attr).append("</attribute>");
                }
            }
        }

        return b.toString();
    }

    private String utf8(String isoStr) {
        if (isoStr == null) {
            return "";
        }
        byte[] bytes = isoStr.getBytes(Charset.forName("ISO-8859-1"));
        return new String(bytes, Charset.forName("UTF-8"));
    }

    private void checkEncoding(byte[] attrBytes) {
        SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
        Set<String> keySet = availableCharsets.keySet();
        List<String> charsets = new ArrayList<String>();
        List<String> decoded = new ArrayList<String>();
        for (String key : keySet) {
            Charset cs = availableCharsets.get(key);
            charsets.add(key);
            decoded.add(new String(attrBytes, cs));
        }
        int i = 0;
    }

    private void processFileUpload(HttpServletRequest request, HttpServletResponse response) {
        // Create a factory for disk-based file items
        Map<String, String> paraMap = new HashMap<String, String>();
        File uploadedFile = null;
        boolean uploaded = false;
        DiskFileItemFactory factory = new DiskFileItemFactory();
        File storageDir = new File(baseModel.getConf().getDataDirectory() + "/uploads");
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
                    uploadedFile = new File(storageDir, fileName);
                    try {
                        if (uploadedFile.exists()){
                            uploadedFile.delete();
                        }
                        item.write(uploadedFile);
                        uploaded = true;
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                }

            }
            if (uploaded && paraMap.containsKey("policy")) {
                String verifyResult = verifySignature(request, paraMap.get("policy"), uploadedFile.getName(), uploadedFile.getAbsolutePath(), null);
                sendValidationReport(verifyResult, response);
                return;
            }
            if (paraMap.containsKey("policy") && paraMap.containsKey("fileName")) {
                File sigFile = new File(getFullSigFileName(paraMap.get("fileName")));
                String verifyResult = verifySignature(request, paraMap.get("policy"), sigFile.getName(), sigFile.getAbsolutePath(), null);
                sendValidationReport(verifyResult, response);
                return;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        nullResponse(response);
    }

    private static void nullResponse(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.getWriter().write("");
            response.getWriter().close();
        } catch (Exception ex) {
        }
    }

    private void processValidationPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            String dataStr = request.getParameter("data");
            String docName = request.getParameter("id");
            String policy = request.getParameter("policy");
            byte[] docBytes = Base64Coder.decode(dataStr);
            String verifyResult = verifySignature(request, policy, docName, null, docBytes);
            sendValidationReport(verifyResult, response);
        } catch (Exception ex) {
            nullResponse(response);
        }
    }

    private void sendValidationReport(String verifyResult, HttpServletResponse response) {
        if (verifyResult != null) {
            response.setContentType("text/xml;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            try {
                response.getWriter().write(verifyResult);
                return;
            } catch (IOException ex) {
                nullResponse(response);
            }
        } else {
            nullResponse(response);
        }

    }
}
