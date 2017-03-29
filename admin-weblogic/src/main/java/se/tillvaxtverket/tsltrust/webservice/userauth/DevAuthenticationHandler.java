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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;

/**
 * Authentication handler for develeopment mode operation of the service
 * This authentication handler is invoked by setting the the init parameter "mode"
 * to the value "devmode" in the web.xml deployment descriptor.
 * 
 * Devmode authentication provides user authentication data in a cookie that is set
 * using the user.jsp web page.
 */
public class DevAuthenticationHandler extends AuthenticationHandler {

    public DevAuthenticationHandler() {
    }

    @Override
    public AuthData getAuthenticationData(HttpServletRequest request, HttpServletResponse response) {
        AuthData authData = new AuthData();
        Map<String, String> usrAttr = new HashMap<String, String>();
        Map<String, String> authAttr = new HashMap<String, String>();

        // Get ID data from cookie
        Cookie cookie = getCookie("testID", request, response);
        if (cookie == null) {
            return authData;
        }
        Map<String, String> idData = new HashMap<String, String>();
        getCookieValues(Base64Coder.decodeString(cookie.getValue()), idData);
        if (idData.containsKey("id") && idData.containsKey("attr")) {
            usrAttr.put(idData.get("attr"), idData.get("id"));
            authData.setUserAttributes(usrAttr);
            authData.setIdAttribute(idData.get("attr"));
            authData.setUserId(idData.get("id"));
        }
        if (idData.containsKey("name")) {
            authData.setUserName(idData.get("name"));
        }
        if (idData.containsKey("idp")) {
            authAttr.put("Shib-Identity-Provider", idData.get("idp"));
            authData.setAuthAttributes(authAttr);
            authData.setIdentitySourceId(idData.get("idp"));
            authData.setAuthType("shibboleth");
        }
        // If not devmode, get authentication data through request


        return authData;
    }

    private static Cookie getCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = null;
        boolean found = false;
        for (Cookie ck : request.getCookies()) {
            if (ck.getName().equals(name)) {
                if (found) {
                    ck.setMaxAge(0);
                    response.addCookie(cookie);
                } else {
                    cookie = ck;
                    found = true;
                }
            }
        }
        return cookie;
    }

    private static void getCookieValues(String cookieValue, Map<String, String> cookieData) {
        String[] values = cookieValue.split(";");
        for (String value : values) {
            String[] idValueSplit = value.split(":");
            if (idValueSplit.length == 2) {
                cookieData.put(idValueSplit[0], Base64Coder.decodeString(idValueSplit[1]));
            }
        }
    }
}
