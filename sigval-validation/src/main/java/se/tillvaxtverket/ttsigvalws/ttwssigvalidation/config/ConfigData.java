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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.config.ConfigFactory;

/**
 * Configuration data class
 */
public final class ConfigData {

    private final static Logger LOG = Logger.getLogger(ConfigData.class.getName());
    private String dataDirectory;
    private String languageCode;
    private long validationTimeout = 20000;
    private boolean valid = true;
    private TTvalConfig jsonConf;

    public ConfigData(String dataDir) {
        String langCode, timeoutSeconds;
        validate(dataDir);
        if (valid) {
            ConfigFactory<TTvalConfig> confFact = new ConfigFactory<TTvalConfig>(dataDir, new TTvalConfig());
            jsonConf = confFact.getConfData();
        }
        setParams(jsonConf.getLanguage(), jsonConf.getSignatureValidationTimeoutSeconds());
    }

    public void validate(String dataDir) {

        // set data directory
        if (dataDir != null && dataDir.length() > 0) {
            File dataDirFile = new File(dataDir);
            try {
                if (!dataDirFile.exists()) {
                    boolean mkdirs = dataDirFile.mkdirs();
                    if (!mkdirs) {
                        LOG.warning("Unable to create specified data directory. Disabling trust store");
                        valid = false;
                    }
                }
            } catch (Exception ex) {
                LOG.warning("Error while accessing or creating data directory. Disabling trust store");
                valid = false;
            }

        } else {
            LOG.warning("Unspecified data location. Disabling trust store.");
            valid = false;
        }
        dataDirectory = (valid) ? dataDir : "";


    }

    public void setParams(String langCode, String timeoutSeconds) {
        //Set langCode
        try {
            Locale locale = new Locale(langCode);
            languageCode = langCode;
        } catch (Exception ex) {
            languageCode = "en";
        }

        // Set validation timeout
        long timeout = longVal(timeoutSeconds, validationTimeout / 1000);
        validationTimeout = timeout * 1000;
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

    public String getDataDirectory() {
        return dataDirectory;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public boolean isValid() {
        return valid;
    }

    public long getValidationTimeout() {
        return validationTimeout;
    }

    public TTvalConfig getJsonConf() {
        return jsonConf;
    }
}
