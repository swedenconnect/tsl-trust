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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import se.tillvaxtverket.tsltrust.common.config.ConfigFactory;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HibernateConfigFactory;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HibernateUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.LotlSigCert;

/**
 * Class holding the parameter values for the servlet daemon as specified in
 * context-param elements of the web.xml deployment descriptor.
 */
public class ContextParameters {

    private final static Logger LOG = Logger.getLogger(ContextParameters.class.getName());
    private static boolean initialized = false;
    private static final List<String> parameterExceptions = new LinkedList<String>();
    private static boolean verboseLogging = false;
    private static TslTrustModel model;

    static {
        parameterExceptions.add("DiscoFeedUrl");
        parameterExceptions.add("LogDbUserName");
        parameterExceptions.add("LogDbPassword");
        parameterExceptions.add("PolicyDbUserName");
        parameterExceptions.add("PolicyDbPassword");
        parameterExceptions.add("DbAutoCreateTables");
        parameterExceptions.add("DbVerboseLogging");
    }

    /**
     * Dummy constructor to prevent instantiation of this class;
     */
    private ContextParameters() {
    }

    /**
     * Extracting context parameters
     *
     * @param sc servlet context information
     */
    public static void extractContextParameters(ServletContext sc) {

        initialized = true;

        String dataLocation = getParam("DataLocation", sc);
        ConfigFactory<TslTrustConfig> confFact = new ConfigFactory<TslTrustConfig>(dataLocation, new TslTrustConfig());
        TslTrustConfig conf = confFact.getConfData();

        model = new TslTrustModel(conf, dataLocation, conf.getMode(), conf.getTSLrecacheTime(), conf.getMaxConsoleLogSize(), conf.getMaxMajorLogAge(), conf.getLotlURL(), conf.getDiscoFeedUrl());

        HibernateConfigFactory.setLogConnectionUrl(conf.getLogDbConnectionUrl());
        HibernateConfigFactory.setLogUserName(conf.getLogDbUserName());
        HibernateConfigFactory.setLogUserPassword(conf.getLogDbPassword());
        HibernateConfigFactory.setPolicyConnectionUrl(conf.getPolicyDbConnectionUrl());
        HibernateConfigFactory.setPolicyUserName(conf.getPolicyDbUserName());
        HibernateConfigFactory.setPolicyUserPassword(conf.getPolicyDbPassword());
        HibernateConfigFactory.setAutoCreate(conf.getDbAutoCreateTables());
        HibernateConfigFactory.setVerboseLogging(conf.getDbVerboseLogging());
        if (initialized) {
            HibernateUtil.initSessionFactories();
            model.setLotlSigCerts(LotlSigCert.getCertificates(model.getDataLocation()));
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean isVerboseLogging() {
        return verboseLogging;
    }

    public static TslTrustModel getModel() {
        return model;
    }

    private static String getParam(String paramName, ServletContext sc) {
        String value = sc.getInitParameter(paramName);
        if (parameterExceptions.contains(paramName)) {
            return value == null ? "" : value;
        }
        if (value != null && value.length() > 0) {
            return value;
        }
        initialized = false;
        return "";
    }
}
