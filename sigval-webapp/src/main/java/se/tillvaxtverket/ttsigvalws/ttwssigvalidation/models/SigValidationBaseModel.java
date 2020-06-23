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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models;

import se.tillvaxtverket.tsltrust.common.utils.general.ContextLogger;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.ConfigData;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.TrustStore;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck.CRLChecker;

/**
 * This base model loads and stores trust information for the signature validation process
 * as well as other basic common data elements.
 * 
 * The model continuously checks if the cache deamon has updated any data.
 */
public class SigValidationBaseModel {
    
    public final ContextLogger LOG = new ContextLogger("BaseLogger", true);
    private final String documentFolderName;
    private ConfigData conf;
    private CRLChecker crlCache;
    private TrustStore trustStore;
    private long trustLoadTime;
    private long trustReloadInterval = 1000*60*5;

    public SigValidationBaseModel(ConfigData conf) {
        this.conf = conf;
        this.crlCache = new CRLChecker(conf);
        this.trustStore = new TrustStore(conf);
        this.trustLoadTime = System.currentTimeMillis();
        this.documentFolderName = "serverdocs";
    }
    
    /**
     * Perform a check if the current trust data built from the local
     * cache is more than 5 minutes old. If it is, the trust data
     * is reloaded from the local cache.
     */
    public void refreshTrustStore(){
        if (System.currentTimeMillis()>(trustLoadTime+trustReloadInterval)){
            trustStore = new TrustStore(conf);
            trustLoadTime=System.currentTimeMillis();
        }
    }

    /**
     * @return Logger
     */
    public ContextLogger getLOG() {
        return LOG;
    }

    /**
     * @return Configuration data
     */
    public ConfigData getConf() {
        return conf;
    }

    /**
     * @return CRL cache database handler
     */
    public CRLChecker getCrlCache() {
        return crlCache;
    }

    /**
     * @return Trust data built from the local trust cache.
     */
    public TrustStore getTrustStore() {
        return trustStore;
    }

    /** Document folder */
    public String getDocumentFolderName() {
        return documentFolderName;
    }
}
