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
