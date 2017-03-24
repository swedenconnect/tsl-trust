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
package se.tillvaxtverket.tsltrust.weblogic.workareas;

import se.tillvaxtverket.tsltrust.weblogic.db.LogDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.content.AdminElements;
import se.tillvaxtverket.tsltrust.weblogic.data.AdminUser;
import se.tillvaxtverket.tsltrust.weblogic.data.LogInfo;
import se.tillvaxtverket.tsltrust.weblogic.db.AuthDbUtil;
import java.io.UnsupportedEncodingException;
import java.util.List;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.InputValidator;
import java.net.URLDecoder;
import se.tillvaxtverket.tsltrust.weblogic.data.AdminLogRecord;

/**
 * Content provider for the Authorization menu
 */
public class AuthArea extends WorkArea {

    TslTrustModel model;
    AdminElements elements;

    /**
     * Constructs an content handler object for the authorization menu
     * @param model Application data model
     */
    public AuthArea(TslTrustModel model) {
        this.model = model;
        elements = new AdminElements(model);
    }

    /**
     * Main handler of a user's http request.
     * @param req The http request data
     * @return html response data
     */
    @Override
    public String getHtmlData(RequestModel req) {
        String html = "";

        if (req.getAction().equals("loadMain")) {
            return getMainViewArea(req);
        }

        if (req.getAction().equals("loadElement")) {
            return loadElementData(req);
        }

        return html;

    }

    private String getMainViewArea(RequestModel req) {
        return elements.getAuthorizationPage(req, "");
    }

    private String loadElementData(RequestModel req) {
        String id = req.getId();
        String parameter = req.getParameter();
        SessionModel session = req.getSession();

        if (id.equals("adminSubMenu")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_AUTHORIZATION)) {
                session.setAuthSubMenu(getInt(parameter));
            }
            return getMainViewArea(req);
        }

        if (id.equals("cancelSelection")) {
            return getMainViewArea(req);
        }

        if (id.equals("requestAccess")) {
            parameter = InputValidator.filter(parameter, InputValidator.Rule.PRINTABLE_ASCII);
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_REQUEST_AUTHZ)) {
                String status = checkRequestPw(req, parameter, model);
                return elements.getAuthorizationPage(req, status);
            }
        }
        if (id.equals("executeAdminActions")) {
            parameter = InputValidator.filter(parameter, InputValidator.Rule.PRINTABLE_ASCII);
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_AUTHORIZATION)) {
                updateAdminStatuses(req, parameter);
            }
            return getMainViewArea(req);
        }

        return "Not implemented response";

    }

    private void updateAdminStatuses(RequestModel req, String parameter) {
        LogDbUtil logDb = model.getLogDb();
        String[] split = parameter.split(":");
        AuthDbUtil adminDb = model.getAuthDb();
        List<AdminUser> adminUsers = req.getSession().getAdminActionList();
        if (split.length != adminUsers.size()) {
            return;
        }
        for (int i = 0; i < split.length; i++) {
            AdminUser admin = adminUsers.get(i);
            int action;
            try {
                action = Integer.valueOf(split[i]);
            } catch (Exception ex) {
                action = 0;
            }
            if (action == 1) {
                adminDb.deleteAdminUser(admin);
                logDb.addAdminEvent(new AdminLogRecord(req, admin, LogInfo.EventType.AdminDeny, "Deleted User"));
                continue;
            }
            switch (action) {
                case 5:
                    admin.setAuthLevel(1);
                    logDb.addAdminEvent(new AdminLogRecord(req, admin, LogInfo.EventType.AdminGrant, "Administrator"));
                    break;
                case 4:
                    admin.setAuthLevel(2);
                    logDb.addAdminEvent(new AdminLogRecord(req, admin, LogInfo.EventType.AdminGrant, "Policy Admin"));
                    break;
                case 3:
                    admin.setAuthLevel(3);
                    logDb.addAdminEvent(new AdminLogRecord(req, admin, LogInfo.EventType.AdminGrant, "Log Admin"));
                    break;
                case 2:
                    admin.setAuthLevel(7);
                    logDb.addAdminEvent(new AdminLogRecord(req, admin, LogInfo.EventType.AdminDeny, "Blocked User"));
                    break;
                default:
                    continue;
            }
            adminDb.addORreplaceAdminUser(admin);
        }
        int i = 0;
    }

    private String checkRequestPw(RequestModel req, String parameter, TslTrustModel model) {
        try {
            String pw = URLDecoder.decode(parameter, "UTF-8");
            if (!model.checkRequestPw(pw)) {
                return "badRequestPW";
            }
        } catch (UnsupportedEncodingException ex) {
            return "badRequestPW";
        }
        return "goodRequestPW";
    }
}
