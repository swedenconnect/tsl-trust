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

import se.tillvaxtverket.tsltrust.common.config.ConfigData;

/**
 *
 * @author stefan
 */
public class TTvalConfig implements ConfigData {

    /**
     * <param-name>TimerSeconds</param-name>
     * <param-value>3600</param-value>
     * <param-name>CrlCacheMode</param-name>
     * <param-value>expiry</param-value>
     * <param-name>EnableCaching</param-name>
     * <param-value>true</param-value>
     * <param-name>VerboseLogging</param-name>
     * <param-value>false</param-value>
     * <param-name>TrustinfoRUrl</param-name>
     * <param-value>http://localhost/~stefan/tsltrust/rootlist.xml</param-value>
     * <param-name>Language</param-name>
     * <param-value>en</param-value>
     * <param-name>SignatureValidationTimeoutSeconds</param-name>
     * <param-value>30</param-value>
     */
    private String TimerSeconds,
            CrlCacheMode,
            EnableCaching,
            VerboseLogging,
            TrustinfoRUrl,
            Language,
            SignatureValidationTimeoutSeconds;

    @Override
    public void setDefaults() {
        TimerSeconds = "3600";
        CrlCacheMode = "expiry";
        EnableCaching = "true";
        VerboseLogging = "false";
        TrustinfoRUrl = "http://localhost/~stefan/tsltrust/rootlist.xml";
        Language = "en";
        SignatureValidationTimeoutSeconds = "30";
    }

    @Override
    public String getName() {
        return "ttvalconf";
    }

    public String getTimerSeconds() {
        return TimerSeconds;
    }

    public String getCrlCacheMode() {
        return CrlCacheMode;
    }

    public String getEnableCaching() {
        return EnableCaching;
    }

    public String getVerboseLogging() {
        return VerboseLogging;
    }

    public String getTrustinfoRUrl() {
        return TrustinfoRUrl;
    }

    public String getLanguage() {
        return Language;
    }

    public String getSignatureValidationTimeoutSeconds() {
        return SignatureValidationTimeoutSeconds;
    }
    
}
