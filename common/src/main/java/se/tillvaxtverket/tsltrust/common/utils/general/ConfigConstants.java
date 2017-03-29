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
