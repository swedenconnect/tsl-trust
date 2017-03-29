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
package se.tillvaxtverket.tsltrust.weblogic;

import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provider of the main menu in the TSL Trust administration service
 */
public class MainMenuProvider implements HtmlConstants, TTConstants {

    TslTrustModel model;
    private static String[] menuItems = new String[]{"Policy Manager", "TSL viewer", "TSP Records", "Certificate Management", "Authorization", "Logs"};
    private static boolean[] repeatMenu = new boolean[]{false, false, false, false, false, true};
    private static String repeatTimer = "5000";
    private static List<String> logoutAuthTypes = new ArrayList<String>();
    
    static{
        logoutAuthTypes.add("shibboleth");
    }

    /**
     * Creates an object of the menu provider class.
     * @param model Model holding static data for the web service.
     */
    public MainMenuProvider(TslTrustModel model) {
        this.model = model;
    }

    /**
     * Generates the main menu for the TSL Trust admin service.
     * @param req http request data
     * @return String representation of the html data of the main menu.
     */
    public String getMenu(RequestModel req) {
        SessionModel session = req.getSession();
        int selected = session.getSelectedMenu();
        // Create enclosing div
        HtmlElement menuDiv = new DivElement();
        menuDiv.addAttribute("class", "menu");

        //Build User info div
        HtmlElement authDiv = new DivElement("authContext");
        authDiv.addStyle("float", "right");
        ButtonElement logout;
        if (req.getMode().equals("devmode")) {
            logout = new ButtonElement("Logout", ONCLICK, DEV_LOGOUT_FUNCTION);
        } else {
            logout = new ButtonElement("Logout", ONCLICK, LOGOUT_FUNCTION);
        }
        String authz;
        if (req.isValidIdentity()) {
            authz = USER_TYPES[req.getAuthzLvl()];
        } else {
            authz = "Anonymous Guest";
        }

        HtmlElement name = new GenericHtmlElement("span");
        name.addAttribute("class", "userIdText");
        name.setText("<b>" + req.getUserName() + "<b> (" + authz + ")");
        authDiv.addHtmlElement(new TextObject("User: "));
        authDiv.addHtmlElement(name);
        if (logoutAuthTypes.contains(req.getAuthType().toLowerCase())) {
            authDiv.addHtmlElement(logout);
        }
        if (req.isValidIdentity()) {
            menuDiv.addHtmlElement(authDiv);
        }
        menuDiv.addHtmlElement(serverUpdateLabel(req));



        // Build menu table
        HtmlElement menuTable = new TableElement(MENU_CLASS);
        menuDiv.addHtmlElement(menuTable);
        HtmlElement tr = new TableRowElement();
        menuTable.addHtmlElement(tr);
        for (int i = 0; i < menuItems.length; i++) {
            HtmlElement td = new TableCellElement(menuItems[i]);
            addMenuFunction(td, i, req);
            if (i == selected) {
                td.addAttribute("class", SELECTED_CLASS);
            } else {
                td.addAttribute("class", MENU_CLASS);
            }
            tr.addHtmlElement(td);
        }

        return menuDiv.toString();
    }

    private void addMenuFunction(HtmlElement menuItem, int indx, RequestModel req) {
        SessionModel session = req.getSession();
        boolean policyConfMode = session.isPolicyConfMode();
        if ((indx == 0 || indx == 3) && policyConfMode) {
            menuItem.addAction(ONCLICK, TWO_STEP_MENU_FUNCTION, new String[]{
                        String.valueOf(indx),
                        POLICY_INFOTABLE_DIV,
                        "refresh",
                        "null"
                    });
        } else {
            if (repeatMenu[indx]) {
                menuItem.addAction(ONCLICK, REPEAT_MENU_FUNCTION, new String[]{String.valueOf(indx), repeatTimer});
            } else {
                menuItem.addAction(ONCLICK, SELECT_MENU_FUNCTION, String.valueOf(indx));
            }
        }

        //Determine hide or show
        switch (indx) {
            case 3:
                if (req.getAuthzLvl() > 3) {
                    menuItem.addStyle("display", "none");
                }
                break;
            case 4:
                if (req.getAuthzLvl() == 7 || req.getAuthzLvl() == 2 || req.getAuthzLvl() == 3 || !req.isValidIdentity()) {
                    menuItem.addStyle("display", "none");
                }
                if (req.getAuthzLvl() == 8) {
                    menuItem.setText("Admin Request Status");
                }
                if (req.getAuthzLvl() == 9) {
                    menuItem.setText("Request Authorization");
                }
                break;
            case 5:
                if (req.getAuthzLvl() > 1 && req.getAuthzLvl() != 3) {
                    menuItem.addStyle("display", "none");
                }
        }


    }

    private HtmlElement serverUpdateLabel(RequestModel req) {
        HtmlElement labelDiv = new DivElement();
        labelDiv.addStyle("float", "right");
        labelDiv.addStyle("margin-right", "100px");
        HtmlElement labelSpan = new GenericHtmlElement("span");
        labelDiv.addHtmlElement(labelSpan);
        if (req.getAuthzLvl() > 3) {
            return labelDiv;
        }
        File recacheFile = new File(model.getDataLocation() + "cfg/recacheTime");
        if (!recacheFile.canRead()) {
            labelSpan.addAttribute("class", ERROR);
            labelSpan.setText("  Error: No server deamon");
            return labelDiv;
        }
        long nextServerUpdate = 0;
        long currentTime = System.currentTimeMillis();
        try {
            long lastServerUpdate = Long.valueOf(FileOps.readTextFile(recacheFile).trim());
            nextServerUpdate = lastServerUpdate + model.getTslRefreshDelay();
        } catch (Exception ex) {
        }
        int maxUpdateTime = 1000 * 60 * 5;
        if (currentTime > nextServerUpdate + maxUpdateTime) {
            labelSpan.addAttribute("class", ERROR);
            labelSpan.setText("  Error: Server deamon is not running");
            return labelDiv;
        }
        if (currentTime > nextServerUpdate) {
            labelSpan.addAttribute("class", LABEL_TEXT);
            labelSpan.setText("  Server update in progress...");
            return labelDiv;
        }
        String timeString = TIME_FORMAT.format(new Date(nextServerUpdate));
        labelSpan.addAttribute("class", LABEL_TEXT);
        labelSpan.setText("  Next server update: " + timeString);

        return labelDiv;
    }
}
