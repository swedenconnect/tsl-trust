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

import se.tillvaxtverket.tsltrust.weblogic.data.LogInfo;
import se.tillvaxtverket.tsltrust.weblogic.db.LogDbUtil;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.common.html.elements.ParagraphElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import java.util.List;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;

/**
 * Content provider for the Log menu
 */
public class LogsArea extends WorkArea implements HtmlConstants, TTConstants {

    TslTrustModel model;

    /**
     * Constructor
     * @param model Application data model
     */
    public LogsArea(TslTrustModel model) {
        this.model = model;
    }

    /**
     * Handler of http requests related to the Log menu
     * @param req http request data
     * @return html response data
     */
    @Override
    public String getHtmlData(RequestModel req) {
        String html = "";
        if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_LOGGING)) {
            if (req.getAction().equals("loadMain")) {
                return getMainViewArea(req, true);
            }

            if (req.getAction().equals("loadElement")) {
                return loadElementData(req);
            }
        } else {
            html = "unauthorized request";
        }
        return html;

    }

    private String getMainViewArea(RequestModel req, boolean refresh) {
        return getLogsPage(req, refresh);
    }

    private String loadElementData(RequestModel req) {
        String id = req.getId();
        String parameter = req.getParameter();
        SessionModel session = req.getSession();

        if (id.equals("logsSubMenu")) {
            session.setLogsSubMenu(getInt(parameter));
            return getMainViewArea(req, true);
        }
        if (id.equals("refreshDisable")) {
            return getMainViewArea(req, false);
        }

        return "Not implemented response";

    }

    public String getLogsPage(RequestModel req, boolean refresh) {
        SessionModel session = req.getSession();
        StringBuilder b = new StringBuilder();
        DivElement messageBoxDiv = new DivElement();
        model.getLogDb().deleteExcessEventRecords();
        model.getLogDb().deleteOldAccessRecords();

        if (req.getAuthzLvl() > 1 && req.getAuthzLvl() != 3) {
            //If not Super Admin, Administrator or Log Admin
            DivElement logsDiv = new DivElement();
            logsDiv.addHtmlElement(getUnauthorizedMenu());
            b.append(logsDiv.toString());
        } else {
            DivElement logsDiv = new DivElement();
            HtmlElement refreshFunc;
            if (refresh) {
                refreshFunc = new ButtonElement(
                        "Stop auto refresh",
                        ONCLICK, LOAD_DATA_FUNCTION,
                        new String[]{MAIN_DATA_AREA, "refreshDisable", "null"});
            } else {
                refreshFunc = new DivElement();
                refreshFunc.addHtmlElement(new TextObject("Auto Refresh <b>Disabled</b>"));
            }
            refreshFunc.addStyle("float", "right");
            logsDiv.addHtmlElement(refreshFunc);
            logsDiv.addHtmlElement(getLogsSubMenu(req));
            switch (req.getSession().getLogsSubMenu()) {
                case 1:
                    logsDiv.addHtmlElement(getLogsTable(req, MAJOR_LOG_TABLE));
                    break;
                case 2:
                    logsDiv.addHtmlElement(getLogsTable(req, CONSOLE_LOG_TABLE));
                    break;
                default:
                    logsDiv.addHtmlElement(getLogsTable(req, ADMIN_LOG_TABLE));
            }
            b.append(logsDiv.toString());
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
    private HtmlElement getLogsSubMenu(RequestModel req) {
        TableElement amt = new TableElement(SUBMENU_CLASS, "adminSubMenu");

        //Get user count
        int admins = 0, requests = 0, blocked = 0;
        String[] menuItems = new String[]{"Administration Log", "Major Events", "Console Log"};


        TableRowElement tr = new TableRowElement();
        amt.addHtmlElement(tr);
        for (int i = 0; i < menuItems.length; i++) {
            TableCellElement td = new TableCellElement(menuItems[i]);
            td.addAction(ONCLICK, REPEAT_LOAD_FUNCTION, new String[]{MAIN_DATA_AREA, "logsSubMenu", String.valueOf(i), String.valueOf(5000)});
            if (req.getSession().getLogsSubMenu() == i) {
                td.addAttribute("class", SELECTED_CLASS);
            } else {
                td.addAttribute("class", SUBMENU_CLASS);
            }
            tr.addHtmlElement(td);
        }
        return amt;


    }

    private HtmlElement getUnauthorizedMenu() {
        ParagraphElement unauth = new ParagraphElement("Log access requires Administrator privileges");
        return unauth;
    }

    private HtmlElement getLogsTable(RequestModel req, String dbTable) {
        LogDbUtil logDb = model.getLogDb();
        List<LogInfo> logList = logDb.getRecords(dbTable);
        String[] tableHeading = new String[]{"Log time", "Event", "Target/Description", "Caused by"};
        String className;
        String[] tableRow;
        TableElement table = new TableElement();
        table.addRow(tableHeading, TABLE_HEAD_CLASS);

        int i = 0;
        for (LogInfo log : logList) {
            if (i++ % 2 == 0) {
                className = TABLE_EVEN_CLASS;
            } else {
                className = TABLE_ODD_CLASS;
            }
            tableRow = new String[]{
                log.getFormattedTime(),
                log.getEvent(),
                log.getDescription(),
                log.getOrigin()
            };
            table.addRow(tableRow, className);
        }
        return table;
    }    
}