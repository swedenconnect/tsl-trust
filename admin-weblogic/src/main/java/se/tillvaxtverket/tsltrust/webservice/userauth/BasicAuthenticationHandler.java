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
