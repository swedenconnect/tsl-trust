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
package se.tillvaxtverket.tsltrust.weblogic.content;

import se.tillvaxtverket.tsltrust.weblogic.data.AdminUser;
import se.tillvaxtverket.tsltrust.weblogic.data.LogInfo;
import se.tillvaxtverket.tsltrust.weblogic.db.AuthDbUtil;
import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.ParagraphElement;
import se.tillvaxtverket.tsltrust.common.html.elements.SelectElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.common.html.elements.PasswordInputElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import se.tillvaxtverket.tsltrust.weblogic.data.MajorLogRecord;

/**
 * Content provider supporting the authorization menu
 */
public class AdminElements implements HtmlConstants, TTConstants {

    private static final int GUEST = 9;
    private static final int REQUEST = 8;
    private static final int BLOCKED = 7;
    private TslTrustModel model;

    /**
     * Constructor
     * @param model Application model data
     */
    public AdminElements(TslTrustModel model) {
        this.model = model;
    }

    /**
     * Provides the HTML for the Authorization menu
     * @param req The request data
     * @param option Option for building the menu
     * @return The HTML of the Authorization menu
     */
    public String getAuthorizationPage(RequestModel req, String option) {
        AuthDbUtil adminDb = model.getAuthDb();
        StringBuilder b = new StringBuilder();
        DivElement messageBoxDiv = new DivElement();

        if (req.getAuthzLvl() > 1) {
            DivElement reqDiv = new DivElement();
            reqDiv.addHtmlElement(getRequestMenu(req, messageBoxDiv, option));
            b.append(reqDiv.toString());
        } else {
            DivElement adminDiv = new DivElement();
            adminDiv.addHtmlElement(getAdminSubMenu(req));
            switch (req.getSession().getAuthSubMenu()) {
                case 1:
                    adminDiv.addHtmlElement(getManageAdminTable(req, messageBoxDiv, "pending"));
                    break;
                case 2:
                    adminDiv.addHtmlElement(getManageAdminTable(req, messageBoxDiv, "block"));
                    break;
                case 3:
                    adminDiv.addHtmlElement(getRequestPassword(messageBoxDiv));
                    break;
                default:
                    adminDiv.addHtmlElement(getManageAdminTable(req, messageBoxDiv, "admin"));
            }
            b.append(adminDiv.toString());
        }
        b.append(messageBoxDiv.toString());
        // Render informatioin
        return b.toString();
    }

    /**
     * Provides a HTML element holding the Authorization sub menu;
     * @param req Request data
     * @return  HTML element
     */
    private HtmlElement getAdminSubMenu(RequestModel req) {
        TableElement amt = new TableElement(SUBMENU_CLASS, "adminSubMenu");
        AuthDbUtil adminDb = model.getAuthDb();

        //Get user count
        int admins = 0, requests = 0, blocked = 0;
        List<AdminUser> adminUsers = adminDb.getAdminUsers();
        String[] menuItems = new String[]{"Manage Administrators", "Authorization Requests", "Block List", "Request Password"};
        for (AdminUser usr : adminUsers) {
            int al = usr.getAuthLevel();
            switch (al) {
                case 7:
                    blocked++;
                    menuItems[2] = "<b>Block List (" + String.valueOf(blocked) + ")</b>";
                    break;
                case 8:
                    requests++;
                    menuItems[1] = "<b>Authorization Requests (" + String.valueOf(requests) + ")</b>";
                    break;
                case 9:
                    break;
                default:
                    admins++;
                    menuItems[0] = "<b>Manage Administrators (" + String.valueOf(admins) + ")</b>";
            }
        }


        TableRowElement tr = new TableRowElement();
        amt.addHtmlElement(tr);
        for (int i = 0; i < menuItems.length; i++) {
            TableCellElement td = new TableCellElement(menuItems[i]);
            td.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{MAIN_DATA_AREA, "adminSubMenu", String.valueOf(i)});
            if (req.getSession().getAuthSubMenu() == i) {
                td.addAttribute("class", SELECTED_CLASS);
            } else {
                td.addAttribute("class", SUBMENU_CLASS);                
            }
            tr.addHtmlElement(td);
        }
        return amt;
    }

    private HtmlElement getManageAdminTable(RequestModel req,
            DivElement messageBoxDiv, String option) {

        SessionModel session = req.getSession();
        boolean adminOpt = option.equals("admin");
        boolean pendingOpt = option.equals("pending");
        boolean blockOpt = option.equals("block");

        AuthDbUtil adminDb = model.getAuthDb();
        Map<String, String> idpDisplayNames = model.getIdpDisplayNames();
        TableRowElement tr;
        GenericHtmlElement action;
        String[] cellData;
        String[] tableHeadings;
        if (adminOpt) {
            tableHeadings = new String[]{"Name", "Identifier", "ID type", "Identity Source", "Status", "Resources", "Action",};
        } else {
            tableHeadings = new String[]{"Name", "Identifier", "ID type", "Identity Source", "Action",};
        }

        // get admin list for table
        List<AdminUser> adminUsers = adminDb.getAdminUsers();
        List<AdminUser> selectedUsers = new ArrayList<AdminUser>();
        for (AdminUser usr : adminUsers) {
            if (pendingOpt && usr.getAuthLevel() == 8) {
                selectedUsers.add(usr);
            }
            if (blockOpt && usr.getAuthLevel() == 7) {
                selectedUsers.add(usr);
            }
            if (adminOpt && usr.getAuthLevel() < 4) {
                selectedUsers.add(usr);
            }
        }
        session.setAdminActionList(selectedUsers);

        // Generate html
        DivElement tDiv = new DivElement();
        TableElement table = new TableElement();
        tDiv.addHtmlElement(table);
        table.addRow(tableHeadings, TABLE_HEAD_CLASS);

        adminUsers = session.getAdminActionList();
        for (int i = 0; i < adminUsers.size(); i++) {
            AdminUser admin = adminUsers.get(i);
            String idpDisplayName = (idpDisplayNames.containsKey(admin.getIdpEntityId()))
                    ?idpDisplayNames.get(admin.getIdpEntityId())
                    :admin.getIdpEntityId();
            if (adminOpt) {
                cellData = new String[]{
                    admin.getDisplayName(),
                    admin.getIdentifier(),
                    admin.getAttributeId(),
                    idpDisplayName,
//                    idpDisplayNames.containsKey(admin.getIdpEntityId()) ? idpDisplayNames.get(admin.getIdpEntityId()) : admin.getIdpEntityId(),
                    getAuthzString(admin.getAuthLevel()),
                    //                  getDemoTargets().get(0)
                    admin.getTargetList().isEmpty() ? SPACE : admin.getTargetList().get(0)
                };
            } else {
                cellData = new String[]{
                    admin.getDisplayName(),
                    admin.getIdentifier(),
                    admin.getAttributeId(),
                    idpDisplayName
//                    idpDisplayNames.containsKey(admin.getIdpEntityId()) ? idpDisplayNames.get(admin.getIdpEntityId()) : admin.getIdpEntityId()
                };
            }
            String className;
            if (i % 2 == 0) {
                className = TABLE_EVEN_CLASS;
            } else {
                className = TABLE_ODD_CLASS;
            }
            table.addRow(cellData, className);
            tr = table.getLastTableRow();
            action = new GenericHtmlElement("td");
            SelectElement select = new SelectElement("adminAction" + String.valueOf(i));
            tr.addHtmlElement(action);
            action.addHtmlElement(select);
            select.addOption("-select action-");
            select.addOption("Remove");
            if (!blockOpt) {
                select.addOption("Block");
                select.addOption("Log Admin");
                select.addOption("Policy Admin");
                select.addOption("Administrator");
            }
            if (adminOpt && admin.getTargetList().size() > 1) {
                for (int j = 1; j < admin.getTargetList().size(); j++) {
                    String target = admin.getTargetList().get(j);
//            if (getDemoTargets().size() > 1) {
//                for (int j = 1; j < getDemoTargets().size(); j++) {
//                    String target = getDemoTargets().get(j);
                    table.addRow(getTargetRow(target), className);
                }
            }
        }

        DivElement executeDiv = new DivElement();
        executeDiv.addHtmlElement(new ButtonElement("Execute actions", ONCLICK, EXECUTE_SELECTED_FUNCTION, new String[]{
                    MAIN_DATA_AREA,
                    "executeAdminActions",
                    "adminAction",
                    String.valueOf(adminUsers.size())
                }));
        String parameter = adminOpt ? "admin" : "pending";
        executeDiv.addHtmlElement(new ButtonElement("Reset", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                    MAIN_DATA_AREA,
                    "cancelSelection",
                    parameter
                }));
        tDiv.addHtmlElement(executeDiv);

        return tDiv;
    }

    /**
     * Dialogue for requesting authorization
     * @param req Request data
     * @param messageBox A HTML element holding the message box data for outputting user messages.
     * @param option option from the html request.
     * @param adminDb The database handler object
     * @return Authorization request HTML element
     */
    private HtmlElement getRequestMenu(RequestModel req, HtmlElement messageBox, String option) {
        AuthDbUtil adminDb = model.getAuthDb();
        DivElement requestAccessDiv = new DivElement();

        //Check status
        if (req.getAuthzLvl() == REQUEST) {
            messageBox.addHtmlElement(new ParagraphElement("Your request for admin privileges is pending"));
            return requestAccessDiv;
        }

        if (req.getAuthzLvl() <= BLOCKED) {
            messageBox.addHtmlElement(new ParagraphElement("You are not authorized to access this function"));
            return requestAccessDiv;
        }

        //If access was requested
        if (option.equals("goodRequestPW")) {
            AdminUser user = getCurrentUser(adminDb, req);
            boolean requested;
            if (user == null) {
                user = new AdminUser();
                requested = addReqUser(user, req);
            } else {
                user.setAuthLevel(REQUEST);
                adminDb.addORreplaceAdminUser(user);
                requested = true;
            }
            if (requested) {
                messageBox.addHtmlElement(new ParagraphElement("Administrative privileges requested for: " + req.getUserName()));
                messageBox.addHtmlElement(new ParagraphElement("Your request is pending administrator approval"));
                model.getLogDb().addMajorEvent(new MajorLogRecord(req,LogInfo.EventType.AdminReq));
            }
            return requestAccessDiv;
        }

        if (option.equals("badRequestPW")) {
            requestAccessDiv.addHtmlElement(new ParagraphElement("Wrong or old authorization request password"));
            PasswordInputElement pwInput = new PasswordInputElement("authPassword");
            pwInput.addAttribute("width", "10");
            ButtonElement reqButton = new ButtonElement("Request", ONCLICK, SEND_INPUT_FUNCTION, new String[]{MAIN_DATA_AREA, "requestAccess", "authPassword"});
            requestAccessDiv.addHtmlElement(new TextObject("Enter request password:"));
            requestAccessDiv.addHtmlElement(pwInput);
            requestAccessDiv.addHtmlElement(reqButton);
            messageBox.addHtmlElement(new ParagraphElement("Check your password and try again."));
                model.getLogDb().addMajorEvent(new MajorLogRecord(req,LogInfo.EventType.AdminReqBadPW));
            return requestAccessDiv;
        }

        // None of the above. Then allow user to request admin privileges if logged in
        // Check if user is loged in
        if (req.isValidIdentity()) {
            requestAccessDiv.addHtmlElement(new ParagraphElement("Request administration rights for (" + req.getUserName() + ")"));
            PasswordInputElement pwInput = new PasswordInputElement("authPassword");
            pwInput.addAttribute("width", "10");
            ButtonElement reqButton = new ButtonElement("Request", ONCLICK, SEND_INPUT_FUNCTION, new String[]{MAIN_DATA_AREA, "requestAccess", "authPassword"});
            requestAccessDiv.addHtmlElement(new TextObject("Enter request password:"));
            requestAccessDiv.addHtmlElement(pwInput);
            requestAccessDiv.addHtmlElement(reqButton);
        } else {
            messageBox.addHtmlElement(new ParagraphElement("You need to be logged in to request authoeization"));
        }
        return requestAccessDiv;
    }

    private HtmlElement getRequestPassword(DivElement messageBoxDiv) {
        List<String> pw = model.getCurrentRequestPassword();
        if (pw == null || pw.size() < 2) {
            messageBoxDiv.addHtmlElement(new ParagraphElement("Password generation Error"));
            return new DivElement();
        }
        TableElement pwTable = new TableElement();
        pwTable.addRow(new String[]{"Password", "Password Expiry Date"}, TABLE_HEAD_CLASS);
        pwTable.addRow(new String[]{pw.get(0), pw.get(1)});
        messageBoxDiv.addHtmlElement(new ParagraphElement("This password currently allows a requester to request administration rights until the stated time of expiry."));
        messageBoxDiv.addHtmlElement(new ParagraphElement("The request needs approval from a registered administrator"));

        return pwTable;
    }

    private boolean addReqUser(AdminUser user, RequestModel req) {
        if (!req.isValidIdentity()){
            return false;
        }
        AuthDbUtil adminDb = model.getAuthDb();
//        String idAttr = null;
//        String identifier = null;
//        String idpEntityId = null;;
//        for (String attr : SUPPORTED_ID_ATTRIBUTES) {
//            if (req.getUserAttributes().containsKey(attr)) {
//                idAttr = attr;
//                identifier = req.getUserAttributes().get(attr);
//            }
//        }
//        if (req.getAuthContext().containsKey(IDP_ATTRIBUTE)) {
//            idpEntityId = req.getAuthContext().get(IDP_ATTRIBUTE);
//        }
//
//        if (idAttr == null || idpEntityId == null || identifier == null || req.getUserName().length() == 0) {
//            return false;
//        }
//        user.setAttributeId(idAttr);
//        user.setIdentifier(identifier);
//        user.setIdpEntityId(idpEntityId);
        user.setAttributeId(req.getUserIdAttribute());
        user.setIdentifier(req.getUserId());
        user.setIdpEntityId(req.getIdpEntityID());
        user.setIdpDisplayName(req.getIdpDisplayName());
        user.setDisplayName(req.getUserName());
        user.setAuthLevel(REQUEST);
        adminDb.addAdminUser(user);
        return true;
    }

    private static AdminUser getCurrentUser(AuthDbUtil adminDb, RequestModel req) {
        List<AdminUser> admins = adminDb.getAdminUsers();
        AdminUser user = null;

        for (AdminUser usr : admins) {
            Map<String, String> authAttr = req.getAuthContext();
            Map<String, String> usrAttr = req.getUserAttributes();
            if (authAttr.containsKey(IDP_ATTRIBUTE)) {
                if (authAttr.get(IDP_ATTRIBUTE).equals(usr.getIdpEntityId())) {
                    if (usrAttr.containsKey(usr.getAttributeId())) {
                        if (usrAttr.get(usr.getAttributeId()).equals(usr.getIdentifier())) {
                            user = usr;
                        }
                    }
                }
            }
        }
        return user;
    }

    private static String getAuthzString(int authzLvl) {
        switch (authzLvl) {
            case 0:
                return "Super Admin";
            case 1:
                return "Administrator";
            case 2:
                return "Policy Admin";
            case 3:
                return "Log Admin";
            case 7:
                return "Blocked";
            case 8:
                return "Pending Request";
            default:
                return "Guest";
        }
    }

    private static String[] getTargetRow(String target) {
        String[] row = new String[7];
        for (int i = 0; i < 7; i++) {
            row[i] = SPACE;
        }
        row[5] = target;
        return row;
    }

    private static List<String> getDemoTargets() {
        List<String> targets = new ArrayList<String>();
        targets.add("Allmost All Policy");
        targets.add("All QC");
        targets.add("All EU TSL Certificates");
        return targets;
    }
}
