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
