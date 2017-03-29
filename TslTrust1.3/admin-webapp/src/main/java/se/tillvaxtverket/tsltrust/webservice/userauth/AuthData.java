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
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;

/**
 * Data class for holding user authentication data
 */
public class AuthData implements TTConstants {

    private Map<String, String> authAttributes = new HashMap<String, String>();
    private Map<String, String> userAttributes = new HashMap<String, String>();
    private String userName = "", authType = "", identitySourceId = "", idAttribute = "", userId = "";

    public AuthData() {
    }

    /**
     * Setter for authentication attributes map. The authentication attributes
     * holds information about how the user was authenticated
     * @param authAttributes map with attribute names as key and attribute values as values
     */
    public void setAuthAttributes(Map<String, String> authAttributes) {
        this.authAttributes = authAttributes;
    }

    /**
     * Setter for user attribute map. The user authentication attributes holds
     * information about the user's identity
     * @param userAttributes map with attribute names as key and attribute values as values
     */
    public void setUserAttributes(Map<String, String> userAttributes) {
        this.userAttributes = userAttributes;
    }

    /**
     * Test if the object of this class holds a complete user identity
     * @return true if, and only if a complete user identity is set
     */
    public boolean isValidUserId() {
        return (identitySourceId.length() > 0 && idAttribute.length() > 0 && userId.length() > 0);
    }

    /**
     * Getter for authentication attributes
     * @return authAttributes map with attribute names as key and attribute values as values
     */
    public Map<String, String> getAuthAttributes() {
        return authAttributes;
    }

    /**
     * Getter for user identity attributes
     * @return authAttributes map with attribute names as key and attribute values as values
     */
    public Map<String, String> getUserAttributes() {
        return userAttributes;
    }

    /**
     * The authentication type value. E.g. "shibboleth" for shibboleth authentication
     * @return authentication type value
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * Sets the authentication type value
     * @param authType authentication type value, identifying the type of authentication method used
     */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * @return user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName User name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return The name of the id attribute holding the users identifier
     */
    public String getIdAttribute() {
        return idAttribute;
    }

    /**
     * @param idAttribute The name of the id attribute holding the users identifier
     */
    public void setIdAttribute(String idAttribute) {
        this.idAttribute = idAttribute;
    }

    /**
     * @return The name of the source of identity. For SAML based authentication
     * this is the entity ID of the Identity provider.
     */
    public String getIdentitySourceId() {
        return identitySourceId;
    }

    /**
     * @param idpEntityId The name of the source of identity. For SAML based authentication
     * this is the entity ID of the Identity provider.
     */
    public void setIdentitySourceId(String idpEntityId) {
        this.identitySourceId = idpEntityId;
    }

    /**
     * @return The identifier value of the current user.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The identifier value of the current user.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
