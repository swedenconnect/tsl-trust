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
