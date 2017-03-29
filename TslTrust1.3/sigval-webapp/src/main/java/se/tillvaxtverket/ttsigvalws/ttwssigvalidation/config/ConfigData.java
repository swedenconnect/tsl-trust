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
