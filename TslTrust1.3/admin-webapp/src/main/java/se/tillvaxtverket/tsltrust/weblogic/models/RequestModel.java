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
package se.tillvaxtverket.tsltrust.weblogic.models;

import java.util.HashMap;
import java.util.Map;

/*
 * This model class stores all data that are unique for each HTML request.
 * Typical data carried in an object of this class are http query string parameters
 * and information about the current user such as authenticated user identiy
 * and user's browser (user agent).
 */
public class RequestModel {

    private String action = "", id = "", parameter = "", userAgent = "", userName = "", authType = "", mode = "",container="";
    private String idpEntityID = "", userIdAttribute = "", userId = "", idpDisplayName = "";
    int authzLvl, windowHeight;
    private SessionModel session;
    boolean initialized = false, optionJs = true, validIdentity = false;
    Map<String, String> authContext = new HashMap<String, String>(), userAttributes = new HashMap<String, String>();

    public RequestModel(SessionModel session, int authzLvl, String mode) {
        this.session = session;
        this.authzLvl = authzLvl;
        this.mode = mode;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
        if (action.length() > 0) {
            initialized = true;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String containter) {
        this.id = containter;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String selected) {
        this.parameter = selected;
    }

    public SessionModel getSession() {
        return session;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isOptionJs() {
        return optionJs;
    }

    public void setOptionJs(boolean optionJs) {
        this.optionJs = optionJs;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Map<String, String> getAuthContext() {
        return authContext;
    }

    public void setAuthContext(Map<String, String> authContext) {
        this.authContext = authContext;
    }

    public Map<String, String> getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(Map<String, String> userAttributes) {
        this.userAttributes = userAttributes;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public int getAuthzLvl() {
        return authzLvl;
    }

    public String getMode() {
        return mode;
    }

    public String getIdpEntityID() {
        return idpEntityID;
    }

    public void setIdpEntityID(String idpEntityID) {
        this.idpEntityID = idpEntityID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserIdAttribute() {
        return userIdAttribute;
    }

    public void setUserIdAttribute(String userIdAttribute) {
        this.userIdAttribute = userIdAttribute;
    }

    public boolean isValidIdentity() {
        return validIdentity;
    }

    public void setValidIdentity(boolean validIdentity) {
        this.validIdentity = validIdentity;
    }

    public String getIdpDisplayName() {
        return idpDisplayName;
    }

    public void setIdpDisplayName(String idpDisplayName) {
        this.idpDisplayName = idpDisplayName;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }
    
}
