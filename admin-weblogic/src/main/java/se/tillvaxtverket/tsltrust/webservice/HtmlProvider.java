/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
 */
package se.tillvaxtverket.tsltrust.webservice;

import se.tillvaxtverket.tsltrust.webservice.utility.RequestModelFactory;
import com.google.gson.Gson;
import iaik.x509.extensions.qualified.structures.QCStatement;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import se.tillvaxtverket.tsltrust.common.iaik.AuthContextQCStatement;
import se.tillvaxtverket.tsltrust.common.iaik.PdsQCStatement;
import se.tillvaxtverket.tsltrust.weblogic.MainMenuProvider;
import se.tillvaxtverket.tsltrust.weblogic.MainTslTrust;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;
import se.tillvaxtverket.tsltrust.webservice.daemon.ContextParameters;

/**
 * This class is the main provider of the TSL Trust administration service servlet.
 * This servlet handles all request from the browser accessing the service.
 */
public class HtmlProvider extends HttpServlet {
//    private ServletContext context;

    Map<BigInteger, SessionModel> sessionMap;
    long day;
    private String superAdminID, superAdminAttribute, superAdminIdP;
    private MainTslTrust tslTrust;
    private TslTrustModel model;
    private MainMenuProvider menuProvider;
    private TslTrustConfig conf;
    private Gson gson = new Gson();

    /**
     * Initialization of the servlet
     * @param config ServletConfig passed from the HttpServlet main class
     * @throws ServletException 
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        //        this.context = config.getServletContext();
        Provider[] providers = Security.getProviders();
        Security.insertProviderAt(new iaik.security.provider.IAIK(), providers.length);

        sessionMap = new HashMap<BigInteger, SessionModel>();
        day = 1000 * 60 * 60 * 24;
                

        model = ContextParameters.getModel();
        conf = (TslTrustConfig) model.getConf();
        
        this.superAdminID = conf.getSuperAdminID();
        this.superAdminAttribute = conf.getSuperAdminAttribute();
        this.superAdminIdP = conf.getSuperAdminIdP();
        
        tslTrust = new MainTslTrust(model);
        menuProvider = new MainMenuProvider(model);
        //Register private QCStatements
        QCStatement.register(PdsQCStatement.statementID, PdsQCStatement.class);
        QCStatement.register(AuthContextQCStatement.statementID, AuthContextQCStatement.class);

    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (model == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.getWriter().write("");
            return;
        }
        SessionModel session = getSession(request, response);
        RequestModel req = RequestModelFactory.getRequestModel(
                request, response, session, superAdminID, superAdminAttribute, superAdminIdP, model);

        if (!req.isInitialized()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.getWriter().write("");
            return;
        }

        if (req.getAction().equals("loadMenu")) {
            try {
                session.setSelectedMenu(Integer.parseInt(req.getParameter()));
            } catch (Exception ex) {
            }
            response.setContentType("text/html;charset=UTF-8");
            String htmlResponse = menuProvider.getMenu(req);
            response.getWriter().write(htmlResponse);
            return;
        }

        if (req.getAction().equals("loadxml")) {
            response.setContentType("text/xml;charset=UTF-8");
            String htmlResponse = tslTrust.loadData(req);
            response.getWriter().write(htmlResponse);
            return;
        }

        if (req.getAction().equals("foldchange")) {
            response.setContentType("application/json;charset=UTF-8");
            String table = req.getId();
            String nodeId = req.getParameter();
            session.setTableFold(table, nodeId);
            response.getWriter().write("[]");
            return;
        }

        if (req.getAction().equals("loadCertInfo")) {
            response.setContentType("application/json;charset=UTF-8");
            if (req.getParameter().length() > 0) {
                String pem = session.getPemCert(req.getParameter());
                response.getWriter().write(gson.toJson(new certResponse(pem)));
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                response.getWriter().write("");
            }
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        String htmlResponse = tslTrust.loadData(req);
        response.getWriter().write(htmlResponse);


//        response.setContentType("text/html;charset=UTF-8");
//        PrintWriter out = response.getWriter();
//        try {
//            /* TODO output your page here
//            out.println("<html>");
//            out.println("<head>");
//            out.println("<title>Servlet HtmlProvider</title>");  
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet HtmlProvider at " + request.getContextPath () + "</h1>");
//            out.println("</body>");
//            out.println("</html>");
//             */
//        } finally {
//            out.close();
//        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private SessionModel getSession(HttpServletRequest request, HttpServletResponse response) {
        BigInteger sessionID = new BigInteger(32, new Random(System.currentTimeMillis()));
        Cookie[] cookies = request.getCookies();
        try {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("ttSession")) {
                    sessionID = new BigInteger(cookie.getValue());
                }
            }
        } catch (Exception ex) {
        }
        response.addCookie(new Cookie("ttSession", sessionID.toString()));
        return getSessionFromID(sessionID);
    }

    private SessionModel getSessionFromID(BigInteger sessionID) {
        SessionModel session = null;
        if (sessionMap == null) {
            sessionMap = new HashMap<BigInteger, SessionModel>();
        }
        List<BigInteger> removeList = new ArrayList<BigInteger>();
        if (!sessionMap.isEmpty()) {
            Set<BigInteger> idSet = sessionMap.keySet();
            for (BigInteger id : idSet) {
                SessionModel storedSession = sessionMap.get(id);
                long lastUse = storedSession.getLastUsed();
                if ((lastUse + day) < System.currentTimeMillis()) {
                    removeList.add(id);
                    continue;
                }
                if (id.equals(sessionID)) {
                    storedSession.setLastUsed(System.currentTimeMillis());
                    session = storedSession;
                }
            }
        }
        for (BigInteger id : removeList) {
            sessionMap.remove(id);
        }
        if (session == null) {
            session = new SessionModel(sessionID);
            session.setLastUsed(System.currentTimeMillis());
            sessionMap.put(sessionID, session);
        }
        return session;
    }

    class certResponse {

        String pem;

        public certResponse(String pem) {
            this.pem = pem;
        }
    }
}
