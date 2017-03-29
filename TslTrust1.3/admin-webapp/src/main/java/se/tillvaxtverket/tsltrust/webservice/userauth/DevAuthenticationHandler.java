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
