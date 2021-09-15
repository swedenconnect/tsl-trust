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

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import se.tillvaxtverket.tsltrust.common.config.ConfigFactory;
import se.tillvaxtverket.tsltrust.common.utils.core.DerefUrl;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.TTvalConfig;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.db.CrlCacheTable;

/**
 * Class holding the parameter values for the servlet daemon as specified in
 * context-param elements of the web.xml deployment descriptor.
 */
public class ContextParameters {

    private final static Logger LOG = Logger.getLogger(ContextParameters.class.getName());
    private long daemonIdleTime = 3600;
    private String crlCacheMode = "expiry", dataDirectory = "";
    private boolean enableCaching = false, verboseLogging = false;
    private URL trustInfoUrl = null;
    private CrlCacheTable dbCrlCache = null;
    private String crlDirName = "", trustDirName = "";
    private boolean noCacheRootList = false;

    /**
     * Constructor
     *
     * @param servletContext servlet context information from the web.xml
     * deployment descriptor.
     */
    public ContextParameters(ServletContext servletContext, String dataDir) {
        extractContextParameters(servletContext, dataDir);
    }

    /**
     * Extracting context parameters
     *
     * @param servletContext servlet context information
     */
    private void extractContextParameters(ServletContext servletContext, String dataDir) {
        // set data directory
        enableCaching=true;
        if (dataDir != null && dataDir.length() > 0) {
            File dataDirFile = new File(dataDir);
            try {
                if (!dataDirFile.exists()) {
                    boolean mkdirs = dataDirFile.mkdirs();
                    if (!mkdirs) {
                        LOG.warning("Unable to create specified data directory. Switching off caching");
                        enableCaching = false;
                    }
                    LOG.info("Created new data directory at: " + dataDir);
                }
            } catch (Exception ex) {
                LOG.warning("Error while accessing or creating data directory. Switching off caching");
                enableCaching = false;
            }

        } else {
            LOG.warning("Unspecified data location. Switching off caching.");
            enableCaching = false;
        }
        dataDirectory = (enableCaching) ? dataDir : "";

        //Get config data
        ConfigFactory<TTvalConfig> confFact = new ConfigFactory<TTvalConfig>(dataDir, new TTvalConfig());
        TTvalConfig jsonConf = confFact.getConfData();

        String timerSec = jsonConf.getTimerSeconds();
        String crlcm = jsonConf.getCrlCacheMode();
        String enable = jsonConf.getEnableCaching();
        String verbose = jsonConf.getVerboseLogging();
        String trustUrl = jsonConf.getTrustinfoRUrl();
        noCacheRootList = "true".equalsIgnoreCase(jsonConf.getNoCacheRootList());

        LOG.info("Loaded Sigval config from: " + dataDir);

        // Set daemon idle time
        daemonIdleTime = longVal(timerSec, daemonIdleTime);
        LOG.info("Setting daemon idle time to: " + String.valueOf(daemonIdleTime) + " sec");
        daemonIdleTime *= 1000;

        // set cache mode
        if (crlcm != null) {
            crlCacheMode = (crlcm.equalsIgnoreCase("instant")) ? "instant" : crlCacheMode;
            crlCacheMode = (crlcm.equalsIgnoreCase("halftime")) ? "halftime" : crlCacheMode;
        }

        // set enable caching
        if (enable != null) {
            enableCaching = (enable.equalsIgnoreCase("true"));
        }

        // set verbose logging
        if (verbose != null) {
            verboseLogging = (verbose.equalsIgnoreCase("true"));
        }

        // set trust information url
        LOG.info("Testing trust info URL");
        try {
            trustInfoUrl = new URL(trustUrl);

            // NOTE: This test is disabled. Those setting up the service must be responsible for applying a good URL
            // In som instances it is relevant to setup connection to a host on the local vlan, which is incompatible with this test.
/*
            String urlHost = trustInfoUrl.getHost();
            String urlScheme = trustInfoUrl.getProtocol();
            if (!urlHost.equalsIgnoreCase("localhost") && !urlScheme.equalsIgnoreCase("https")) {
                LOG.warning("Trust information URL MUST be either localhost or use https scheme. Switching off caching");
                enableCaching = false;
            } else {
//                byte[] trustData = DerefUrl.getBytes(trustInfoUrl, DerefUrl.SslSecurityPolicy.ACCEPT_ALL);
//                if (trustData == null) {
//                    LOG.warning("Error dereferencing trust info URL. Switching off caching");
//                    enableCaching = false;
//                }
//                LOG.info("Received valid rootlist xml file from: " + trustInfoUrl);
            }
*/
        } catch (Exception ex) {
            LOG.warning("Illegal trust information URL. Switching off rootlist caching");
            noCacheRootList = true;
        }

        //Setup database connection
        if (enableCaching) {
            LOG.info("Caching enabled - Attempting to intialize CRL caching");
            crlDirName = FileOps.getfileNameString(dataDirectory, "CrlCache");
            File crlDir = new File(crlDirName);
            if (!crlDir.canRead()) {
                boolean mkdirs = crlDir.mkdirs();
                if (!mkdirs) {
                    LOG.warning("Unable to create specified crl cache data directory. Switching off caching");
                    enableCaching = false;
                }
            }
            File crlDbFile = new File(crlDirName, "crlDb");
            dbCrlCache = new CrlCacheTable(crlDbFile.getAbsolutePath());
            LOG.info("CRL caching initialized");
        }

        //Get Trust cache dir
        trustDirName = FileOps.getfileNameString(dataDirectory, "trustCache");
        File tcDir = new File(trustDirName);
        if (!tcDir.exists()) {
            boolean mkdirs = tcDir.mkdirs();
            if (!mkdirs) {
                LOG.warning("Unable to create specified trust cache directory. Switching off caching");
                enableCaching = false;
            }
            LOG.info("Created new CRL cache directory");
        }
        LOG.info("Context parameter initialization complete");
    }

    /**
     * Get the long value of a string
     *
     * @param longString String representation of the long value
     * @param defaultVal default long value
     * @return long value parsed from the string
     */
    private long longVal(String longString, long defaultVal) {
        long val = defaultVal;
        try {
            val = Long.parseLong(longString);
        } catch (Exception ex) {
        }
        return val;
    }

    public String getCrlCacheMode() {
        return crlCacheMode;
    }

    public long getDaemonIdleTime() {
        return daemonIdleTime;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public boolean isEnableCaching() {
        return enableCaching;
    }

    public URL getTrustInfoUrl() {
        return trustInfoUrl;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public String getCrlDirName() {
        return crlDirName;
    }

    public CrlCacheTable getDbCrlCache() {
        return dbCrlCache;
    }

    public String getTrustDirName() {
        return trustDirName;
    }

    public boolean isNoCacheRootList() {
        return noCacheRootList;
    }
}
