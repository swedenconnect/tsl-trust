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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class for authentication handlers.
 * 
 * <p>
 * Authentication handlers extract user authentication data and user identity data
 * from the current http request. An Authentication handler extending this class
 * is selected using logic provided in the AuthenticationHandlerFactory class.
 */
public abstract class AuthenticationHandler {

    /**
     * Main method of the Authentication handler, creating an AuthData object for the
     * current http request. This method may also provide modifications to the http
     * response, e.g. by setting cookie values.
     * @param request the current http request
     * @param response the current http response being prepared
     * @return An AuthData object holding authentication and user identity information
     */
    public abstract AuthData getAuthenticationData(HttpServletRequest request, HttpServletResponse response);

    /**
     * This method process a UTF-8 string that has been encoded using ISO 8859-1 (Latin 1)
     * by decoding the string using 8859-1 encoding and reencoding the resulting bytes as utf-8.
     * This method is used to correct encoding issues with some authentication schemes where
     * utf-8 strings falsely appears as being ISO 8859 encoded.
     * @param isoStr The ISO 8859-1 encoded utf-8 string     * 
     * @return String with corrected utf-8 encoding
     */
    protected static String utf8(String isoStr) {
        if (isoStr == null) {
            return "";
        }
        byte[] bytes = isoStr.getBytes(Charset.forName("ISO-8859-1"));
        return new String(bytes, Charset.forName("UTF-8"));
    }

    /**
     * Method processing attributes provided in the http request and parse it 
     * against an array of expected attribute values.
     * @param request http request holding attributes
     * @param attributeNames array of attribute names
     * @return a map holding the present attributes matching the array of attribute names,
     * having attribute names as key.
     */
    protected static Map<String, String> getAttributes(HttpServletRequest request, String[] attributeNames) {
        Map<String, String> attributeMap = new HashMap<String, String>();

        for (String attributeName : attributeNames) {
            String attributeValue = (String) request.getAttribute(attributeName);
            if (attributeValue != null) {
                attributeValue = utf8(attributeValue);
                attributeMap.put(attributeName, attributeValue);
            }
        }
        return attributeMap;
    }

    /**
     * Method processing attributes provided in the http request and parse it 
     * against an array of expected attribute values.
     * <p>
     * attributes holding multiple values separated by semicolon are split returned as
     * separate attributes. 
     * @param request http request holding attributes
     * @param attributeNames array of attribute names
     * @return a map holding the present attributes matching the array of attribute names,
     * having attribute names as key.
     */
    protected static Map<String, String> getSplitAttributes(HttpServletRequest request, String[] attributeNames) {
        Map<String, String> attributeMap = new HashMap<String, String>();

        for (String attributeName : attributeNames) {
            String attribute = (String) request.getAttribute(attributeName);

            if (attribute != null) {

                String compositValue = utf8(attribute);
                String[] attributeValues = compositValue.split(";");
                for (String attributeValue : attributeValues) {
                    attributeMap.put(attributeName, attributeValue);
                }
            }
        }

        return attributeMap;
    }
}
