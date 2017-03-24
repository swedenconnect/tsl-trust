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
    private ConfigData conf;
    private CRLChecker crlCache;
    private TrustStore trustStore;
    private long trustLoadTime;
    private long trustReloadInterval = 1000*60*5;

    public SigValidationBaseModel(ConfigData conf) {
        this.conf = conf;
        crlCache = new CRLChecker(conf);
        trustStore = new TrustStore(conf);
        trustLoadTime = System.currentTimeMillis();
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

    
}
