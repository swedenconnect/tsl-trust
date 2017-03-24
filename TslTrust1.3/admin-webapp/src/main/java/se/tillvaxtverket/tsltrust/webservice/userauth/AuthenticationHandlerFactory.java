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
package se.tillvaxtverket.tsltrust.webservice.userauth;

import javax.servlet.http.HttpServletRequest;

/**
 * Factory for choosing an appropriate authentication handler
 * <p>
 * New authentication handlers may be added to handle various forms of user authentication.
 * By default two authentication handlers are provided. One for Shibboleth authentication
 * and one for the development mode authentication based on cookie settings only.
 */
public class AuthenticationHandlerFactory {

    /**
     * Selects an appropriate authentication handler.
     * Modify this function to add logic for invocation of custom authentication handlers
     * @param request The http request holding information about authentication method
     * @param mode A mode identifier to provide additional information (e.g. by configuration)
     * @return Authentication handler
     */
    public static AuthenticationHandler getAuthenticationHandler(HttpServletRequest request, String mode) {
        // Get authentication handler
        AuthenticationHandler authHandler;
        String authType = (request.getAuthType() != null) ? request.getAuthType() : "";

        // If devmode, return the dev authentication handler
        if (mode.equalsIgnoreCase("devmode")) {
            return new DevAuthenticationHandler();
        }
        // If authType is shibboleth, return the shibboleth authentication handler
        if (authType.equalsIgnoreCase("shibboleth")) {
            return new ShibAuthenticationHandler();
        }
        // If authType is basic authentication, return the basic authentication handler
        if (authType.equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
            return new BasicAuthenticationHandler("Basic Authentication");
        }
        // If authType is client cert authentication, return the basic authentication handler
        if (authType.equalsIgnoreCase(HttpServletRequest.CLIENT_CERT_AUTH)) {
            return new BasicAuthenticationHandler("Client Cert Authentication");
        }
        // If authType is digest authentication, return the basic authentication handler
        if (authType.equalsIgnoreCase(HttpServletRequest.DIGEST_AUTH)) {
            return new BasicAuthenticationHandler("Digest Authentication");
        }
        // By default return the basic authentication handler specifying unknown source.
        // Add logic here for selection of alternative authentication handlers.
        return new BasicAuthenticationHandler("Unknown");
    }
}
