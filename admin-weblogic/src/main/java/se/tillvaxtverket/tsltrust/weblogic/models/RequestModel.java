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
