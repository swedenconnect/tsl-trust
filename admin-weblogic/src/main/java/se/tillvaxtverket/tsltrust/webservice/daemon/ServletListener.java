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
package se.tillvaxtverket.tsltrust.webservice.daemon;

import java.security.Security;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 */
public class ServletListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(ServletListener.class.getName());
    private static final String SERVLET_PATH = "/TTAdminService";
    private ServletDaemon daemonTask = null;
    
    static{
            Security.insertProviderAt(new iaik.security.provider.IAIK(),2);        
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext servletContext = sce.getServletContext();
        String contextPath = servletContext.getContextPath();
        contextPath = (contextPath == null) ? "null" : contextPath;
        if (contextPath.equals(SERVLET_PATH)) {
            //Init Daemon
            ContextParameters.extractContextParameters(servletContext);
            if (daemonTask == null && ContextParameters.isInitialized()) {
                daemonTask = new DaemonTask();
                daemonTask.invokeDaemon();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (daemonTask != null) {
            daemonTask.stopDaemon();
            daemonTask = null;
        }
    }
}
