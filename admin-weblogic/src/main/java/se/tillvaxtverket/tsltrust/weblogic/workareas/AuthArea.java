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
