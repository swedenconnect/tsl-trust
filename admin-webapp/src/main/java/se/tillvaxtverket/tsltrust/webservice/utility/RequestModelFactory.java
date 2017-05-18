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
package se.tillvaxtverket.tsltrust.webservice.utility;

import se.tillvaxtverket.tsltrust.weblogic.data.AdminUser;
import se.tillvaxtverket.tsltrust.weblogic.db.AuthDbUtil;
import java.util.List;
import java.util.Map;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.tillvaxtverket.tsltrust.common.utils.general.URIComponentCoder;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.InputValidator;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.webservice.userauth.AuthData;
import se.tillvaxtverket.tsltrust.webservice.userauth.AuthenticationHandler;
import se.tillvaxtverket.tsltrust.webservice.userauth.AuthenticationHandlerFactory;

/**
 * Factory creating the request object hodling data about the current http request for the TSL Trust
 * policy administration web application.
 */
public class RequestModelFactory implements TTConstants{

    /**
     * Factory for generating gathering all http request related data that is passed to various classes of TSL trust
     * @param request The incoming http request
     * @param response The outgoing http response object (not yet sent at this time)
     * @param session state variables for the user's session, identified through the user's session cookie.
     * @param superAdminID the identity attribute for the pre-configured super admin.
     * @param superAdminAttribute the SAML attribute type of the super admin.
     * @param superAdminIdP The entityID of the IdP through which the super admin is authenticated.
     * @param appModel general static data for the TSL Trust application (common to all users and sessions).
     * @return an object of the RequestModel class holding data necessary to handle the user's http request.
     */
    public static RequestModel getRequestModel(HttpServletRequest request, HttpServletResponse response,
            SessionModel session, String superAdminID, String superAdminAttribute,
            String superAdminIdP, TslTrustModel appModel) {

        // Get request parameters
        String action = request.getParameter("action");
        String id = request.getParameter("id");
        String parameter = request.getParameter("parameter");
        String container = request.getParameter("container");
        String windowHeight = request.getParameter("winheight");
        String userAgent = request.getHeader("user-agent");
        String remoteUser = request.getRemoteUser();
        String mode = appModel.getMode();
        String dataLocation = appModel.getDataLocation();
        
        //Get Authentication data and authorization level
        AuthenticationHandler authenticationHandler = AuthenticationHandlerFactory.getAuthenticationHandler(request, mode);
        AuthData authdata = authenticationHandler.getAuthenticationData(request, response);
        int authzLevel = getAuthorizationLevel(authdata, superAdminID, superAdminAttribute, superAdminIdP, mode, dataLocation);

        //Create request model
        RequestModel reqModel = new RequestModel(session, authzLevel, mode);
        reqModel.setAction((action != null) ? InputValidator.filter(action, InputValidator.Rule.TEXT_LABEL) : "");
        reqModel.setId((id != null) ? InputValidator.filter(id, InputValidator.Rule.TEXT_LABEL) : "");
        try {
            reqModel.setParameter(URIComponentCoder.decodeURIComponent(parameter));
        } catch (Exception ex) {
            reqModel.setParameter("");
        }
        reqModel.setContainer((container != null) ? InputValidator.filter(container, InputValidator.Rule.TEXT_LABEL) : "");
        reqModel.setUserAgent((userAgent != null ? userAgent : ""));

        //Set browser window height
        int height;
        try {
            height = Integer.parseInt(windowHeight);
        } catch (Exception ex) {
            height = -1;
        }
        reqModel.setWindowHeight(height);

        if (reqModel.getUserAgent().indexOf("Safari") > 0) {
            reqModel.setOptionJs(false);
        }

        reqModel.setUserName(authdata.getUserName());
        reqModel.setAuthType(authdata.getAuthType());
        reqModel.setAuthContext(authdata.getAuthAttributes());
        reqModel.setUserAttributes(authdata.getUserAttributes());
        reqModel.setIdpEntityID(authdata.getIdentitySourceId());
        reqModel.setUserIdAttribute(authdata.getIdAttribute());
        reqModel.setUserId(authdata.getUserId());
        reqModel.setValidIdentity(authdata.isValidUserId());
        Map<String, String> idpDisplayNames = appModel.getIdpDisplayNames();
        reqModel.setIdpDisplayName(idpDisplayNames.containsKey(authdata.getIdentitySourceId())
                ? idpDisplayNames.get(authdata.getIdentitySourceId()) : authdata.getIdentitySourceId());
        return reqModel;
    }

    private static int getAuthorizationLevel(AuthData authData, String superAdminID,
            String superAdminAttribute, String superAdminIdP, String mode, String dataLocation) {
        int authzLvl = ROLE_GUEST;

        //Determine authorizationlevel
        // Check for super admin
        if (authData.getIdentitySourceId().equals(superAdminIdP)
                && authData.getIdAttribute().equals(superAdminAttribute)
                && authData.getUserId().equals(superAdminID)) {
            return ADM_ROLE_SUPER_ADMIN;
        }

        // Check against user Db
        AuthDbUtil db = new AuthDbUtil(dataLocation);
        List<AdminUser> adminUsers = db.getAdminUsers();
        for (AdminUser admin : adminUsers) {
            if (authData.getIdentitySourceId().equals(admin.getIdpEntityId())
                    && authData.getIdAttribute().equals(admin.getAttributeId())
                    && authData.getUserId().equals(admin.getIdentifier())) {
                authzLvl = admin.getAuthLevel();
                //prevent elevation to superUser through db modification;
                if (authzLvl == ADM_ROLE_SUPER_ADMIN) {
                    // Illegal value, downgrade to guest
                    authzLvl = ROLE_GUEST;
                }
                return authzLvl;
            }
        }

        // If no match, return guest status
        return ROLE_GUEST;
    }
}
