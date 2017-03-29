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
package se.tillvaxtverket.tsltrust.common.utils.general;

/**
 * Configuration data constants
 */
public interface ConfigConstants {
    static final String HOMEDIR = "HomeDirectory";
    static final String CONFDIR = "ConfigDirectory";
    static final String CA_INFO_REFRESH_TIME = "CaInfoRefreshTime";
    static final String ROOT_URL = "RootUrl";
    static final String CRL_CACHE_MODE = "CrlCacheMode";
    static final String CRL_CACHE_CYCLE = "CrlCacheCycle";
    static final String CRL_CACHE_MODE_INSTANT = "instant";
    static final String CRL_CACHE_MODE_HALF_WAY = "halfway";
    static final String CRL_CACHE_MODE_EXPIRY = "expiry";
    static final String LOCALE = "Locale";
    static final String STATUS_CHECK_TIMEOUT = "statusCheckTimeout";
    
}
