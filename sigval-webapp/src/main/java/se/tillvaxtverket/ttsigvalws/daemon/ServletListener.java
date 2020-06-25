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
package se.tillvaxtverket.ttsigvalws.daemon;

import iaik.x509.ocsp.net.OCSPContentHandlerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.ConfigData;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;

import java.net.HttpURLConnection;
import java.security.Security;
import java.util.Locale;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener, INvoking the servlet daemon.
 */
public class ServletListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(ServletListener.class.getName());
    private static final String SERVLET_PATH_ENV = "SERVLET_PATH";
    private static final String DATA_LOCATION_ENV = "SIGVAL_DATALOCATION";
    private static final String defaultServletPath = "/sigval";
    public static SigValidationBaseModel baseModel;
    private ServletDaemon daemonTask = null;

    static {
        // Remove any occurance of the BC provider
        Security.removeProvider("BC");
        // Insert the BC provider in a preferred position
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        Security.insertProviderAt(new iaik.security.provider.IAIK(), 2);
        try {
            SecurityManager secMan = new SecurityManager();
            secMan.checkSetFactory();
            HttpURLConnection.setContentHandlerFactory(new OCSPContentHandlerFactory());
            LOG.info("Setting URL Content handler factory to OCSPContentHandlerFactory");
        } catch (Exception ex) {
            LOG.warning("Error when setting URL content handler factory");
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext servletContext = sce.getServletContext();
        String contextPath = servletContext.getContextPath();
        contextPath = (contextPath == null) ? "null" : contextPath;
        String envServletPath = System.getenv(SERVLET_PATH_ENV);
        String servletPath = envServletPath == null ? defaultServletPath: envServletPath;

        if (contextPath.equals(servletPath)) {
            LOG.info("Sigval Servlet - found context path: " + contextPath);
            // Init models

            String envDataLocation = System.getenv(DATA_LOCATION_ENV);
            String dataDir = envDataLocation == null ? servletContext.getInitParameter("DataDirectory") : envDataLocation;

            ConfigData conf = new ConfigData(dataDir);
            baseModel = new SigValidationBaseModel(conf);
            Locale.setDefault(new Locale(baseModel.getConf().getLanguageCode()));

            //Init Daemon
            LOG.info("Sigval Servlet - initializing context parameters");
            ContextParameters contextParams = new ContextParameters(servletContext);
            LOG.info("Sigval Servlet - context parameters initlized");
            if (daemonTask == null && contextParams.isEnableCaching()) {
                LOG.info("Valid context parameters - starting daemon");
                daemonTask = new DaemonTask(contextParams);
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
