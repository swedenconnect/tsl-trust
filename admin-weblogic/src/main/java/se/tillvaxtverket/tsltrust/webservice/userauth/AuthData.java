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
