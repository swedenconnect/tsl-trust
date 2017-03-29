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
