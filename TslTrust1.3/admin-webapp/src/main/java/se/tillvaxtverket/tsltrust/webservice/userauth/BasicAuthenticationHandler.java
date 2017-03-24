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
import javax.servlet.http.HttpServletResponse;

/**
 * Authentication handler for basic authentication
 */
public class BasicAuthenticationHandler extends AuthenticationHandler {

    String type;

    public BasicAuthenticationHandler(String type) {
        this.type = type;
    }

    @Override
    public AuthData getAuthenticationData(HttpServletRequest request, HttpServletResponse response) {
        AuthData authData = new AuthData();

        //Set key user authentication values
        authData.setUserName((request.getRemoteUser() != null) ? request.getRemoteUser() : "");
        authData.setAuthType((request.getAuthType() != null) ? request.getAuthType() : "");
        authData.setIdentitySourceId(type);
        authData.setIdAttribute("User ID");
        authData.setUserId(authData.getUserName());

        return authData;
    }
}
