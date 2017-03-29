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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authentication handler for Shibboleth authentication
 */
public class ShibAuthenticationHandler extends AuthenticationHandler {

    /**
     * Shib-Application-ID          The applicationId property derived for the request.
     * Shib-Session-ID              The internal session key assigned to the session associated with the request.
     * Shib-Identity-Provider       The entityID of the IdP that authenticated the user associated with the request.
     * Shib-Authentication-Instant  The ISO timestamp provided by the IdP indicating the time of authentication.
     * Shib-Authentication-Method   The AuthenticationMethod or <AuthnContextClassRef> value supplied by the IdP, if any.
     * Shib-AuthnContext-Class      The AuthenticationMethod or <AuthnContextClassRef> value supplied by the IdP, if any.
     * Shib-AuthnContext-Decl       The <AuthnContextDeclRef> value supplied by the IdP, if any.
     */
    private static final String[] shibAttributeNames = new String[]{"Shib-Application-ID", "Shib-Session-ID", "Shib-Identity-Provider", "Shib-Authentication-Instant",
        "Shib-Authentication-Method", "Shib-AuthnContext-Class", "Shib-AuthnContext-Decl"};
    private static final String[] userAttributeNames = new String[]{"displayName", "cn", "initials", "sn", "givenName", "norEduPersonNIN", "personalIdentityNumber", "mail",
        "telephoneNumber", "mobileTelephoneNumber", "eppn", "persistent-id", "o", "ou", "departmentNumber", "employeeNumber", "employeeType", "title", "description",
        "affiliation", "entitlement", "street", "postOfficeBox", "postalCode", "st", "l", "preferredLanguage"};
    private static final String[] supportedIdAttributes = new String[]{"personalIdentityNumber", "norEduPersonNIN", "persistent-id"};

    public ShibAuthenticationHandler() {
    }

    @Override
    public AuthData getAuthenticationData(HttpServletRequest request, HttpServletResponse response) {
        AuthData authData = new AuthData();

        //Get attribute maps
        Map<String, String> authAttributes = getAttributes(request, shibAttributeNames);
        Map<String, String> userAttributes = getSplitAttributes(request, userAttributeNames);
        authData.setAuthAttributes(authAttributes);
        authData.setUserAttributes(userAttributes);

        //Set key user authentication values
        authData.setUserName((request.getRemoteUser() != null) ? utf8(request.getRemoteUser()) : "");
        authData.setAuthType((request.getAuthType() != null) ? utf8(request.getAuthType()) : "");
        if (authAttributes.containsKey("Shib-Identity-Provider")) {
            authData.setIdentitySourceId(authAttributes.get("Shib-Identity-Provider"));
        }

        String attr = "";
        String value = "";
        for (String type : supportedIdAttributes) {
            if (userAttributes.containsKey(type)) {
                attr = type;
                value = userAttributes.get(type);
                break;
            }
        }
        authData.setIdAttribute(attr);
        authData.setUserId(value);


        return authData;
    }
}
