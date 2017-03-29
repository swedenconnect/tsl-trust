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

import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.ImageElement;
import se.tillvaxtverket.tsltrust.common.html.elements.SelectElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextInputElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.core.PEM;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.tsltrust.weblogic.content.CertManagementInfoElements;
import se.tillvaxtverket.tsltrust.weblogic.content.CertificateInformation;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.weblogic.content.InfoTableFactory;
import se.tillvaxtverket.tsltrust.weblogic.content.PolicyInfoElements;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.data.ExternalCert;
import se.tillvaxtverket.tsltrust.weblogic.data.LogInfo;
import se.tillvaxtverket.tsltrust.weblogic.data.TslPolicy;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import se.tillvaxtverket.tsltrust.weblogic.db.ValPoliciesDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.InputValidator;
import se.tillvaxtverket.tsltrust.weblogic.utils.PolicyUtils;
import iaik.x509.X509Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tillvaxtverket.tsltrust.weblogic.data.MajorLogRecord;

/**
 * Content provider for the Policy Management menu
 */
public class PolicyManagementArea extends WorkArea implements HtmlConstants, TTConstants {

    private TslTrustModel model;
    private PolicyUtils policyUtils;
    private ValPoliciesDbUtil policyDb;
    private String activeImg = "img/Ok-icon.png", inactiveImg = "img/Nok-icon.png",
            pendingReconstructImg = "img/pendingRecycle.png", pendingDeleteImg = "img/pendingDelete.png",
            pendingImg = "img/pendingIcn.png";
    private Map<String, HtmlElement> vpActionIcns = new HashMap<String, HtmlElement>();
    private String icnHeight = "17";
    private PolicyInfoElements policyInfo;
    private final CertManagementInfoElements certInfoElm;

    /**
     * Constructor
     * @param model Application data model
     */
    public PolicyManagementArea(TslTrustModel model) {
        this.model = model;
        policyDb = model.getPolicyDb();
        policyInfo = new PolicyInfoElements(model);
        policyUtils = policyInfo.getPolicyUtils();
        policyUtils.recachePolicyParameters();
        certInfoElm = new CertManagementInfoElements(model);
        initIcons();

    }

    /**
     * Generates the html code for functions under the Policy Management menu
     * @param req Data related to the Http request from the client.
     * @return A string of html code
     */
    @Override
    public String getHtmlData(RequestModel req) {
        SessionModel session = req.getSession();
        String html = "";

        if (req.getAction().equals("loadMain")) {
            return getPolicyViewAreaHtml(req);
        }

        if (req.getAction().equals("loadElement")) {
            return loadElementData(req);
        }

        //Called by an iFrame in page load to retrieve the iFrame content
        if (req.getAction().equals("loadFrameInfo")) {
            return getPolicyViewAreaHtml(req);
        }


        if (req.getAction().equals("frameLoad")) {
            String parameter = req.getParameter();
            if (req.getId().equals(VIEW_BUTTON)) {
                setViewButtonParameter(parameter, req);
            }
        }

        return html;
    }

    /**
     * Sets the iFrame state of the policy management menu This function is invoked
     * either when a certificate view is requested or when certificate view is terminated     * 
     * @param parameter the http request parameter value from the http query string     * 
     * @param session The session model
     */
    private void setViewButtonParameter(String parameter, RequestModel req) {
        SessionModel session = req.getSession();
        parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
        if (parameter.startsWith("cert")) {
            session.setPolicyButtonState("cert");
            session.setPolicySelectedPemCert(parameter.substring(4));
        } else {
            if (parameter.equals("edit")) {
                session.setPolicyConfMode(true);
                resetAllValPolicyFoldings(session);
            }
            if (parameter.equals("stopEdit")) {
                session.setPolicyConfMode(false);
                resetAllValPolicyFoldings(session);
            }
            session.setPolicyButtonState("data");
        }
    }

    /**
     * Handler of the loadData JavaScript function
     * @param req the client http request model
     * @return html code in response to the request
     */
    private String loadElementData(RequestModel req) {
        SessionModel session = req.getSession();
        String id = req.getId();
        String parameter = req.getParameter();

        if (id.equals("policySubMenu")) {
            session.setPolicyButtonState("data");
            session.setPolicySubMenu(getInt(parameter));
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        if (id.equals("returnFromAccept")) {
            if (authorized(req.getAuthzLvl(), 0)) {
                if (parameter.equals("VPOK")) {
                    updateValPolicyState(req);
                    return getPolicyViewAreaHtml(req);
                }
                if (parameter.equals("TPOK")) {
                    updateTslPolicyState(req);
                    return getPolicyViewAreaHtml(req);
                }
            }
            session.setAddNewPolicy(false);
            session.setPolicyConfRequired(false);
            return getPolicyViewAreaHtml(req);
        }

        if (id.equals(VIEW_BUTTON)) {
            setViewButtonParameter(parameter, req);
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }

        if (id.equals(INFO_TABLE_AJAX_LOAD)) {
            InfoTableModel tm = new InfoTableModel("itAjax" + parameter);
            tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
            InfoTableElements valPolicyElements = tm.getElements();
            CertificateInformation certInfo = new CertificateInformation(tm, session);
            X509Certificate iAIKCert = policyUtils.getIAIKCert(parameter);
            if (iAIKCert != null) {
                tm.setElements(certInfo.getCertInfo(iAIKCert));
                return new InfoTableFactory(tm, session).getTable().toString();
            } else {
                return "Certificate Load Error";
            }
        }
        if (id.equals("refresh")) {
            return getPolicyViewAreaHtml(req);
        }


        //-------------------------
        //Validation Policy Actions
        //-------------------------
        if (id.equals("selectValPolicy")) {
            session.setPolicyButtonState("data");
            session.setSelectedValPolicy(getInt(parameter));
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        /*
         * Executed when an action is selected for a policy, changing its current state
         * (Active, disabled, rebuild, delete)
         */
        if (id.startsWith("vPolicyAction")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                int policyIndex = getInt(id.substring("vPolicyAction".length()));
                int action = getInt(parameter);
                session.setSelectedValPolicy(policyIndex);
                session.setRequestedPolicyAction(action);
                if (action > 2) {
                    session.setPolicyConfRequired(true);
                } else {
                    updateValPolicyState(req);
                }
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        if (id.equals("newValPolicy")) {
            session.setAddNewPolicy(true);
            return getPolicyViewAreaHtml(req);
        }
        if (id.equals("addNewValPolicy")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
                addNewValidationPolicy(req, parameter);
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        /*
         * Function executed if a TSL Policy is added to a validation policy
         */
        if (id.equals("useTslPolicy")) {
            //Add Selected TSL policy to the validation policy
            ValidationPolicy vp = getSelectedValidationPolicy(req);
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT) && vp != null) {
                List<String> unselectedTslPolicies = policyUtils.getUnselectedTslPolicies(vp);
                int index = getInt(parameter);
                if (index < unselectedTslPolicies.size()) {
                    String tpName = unselectedTslPolicies.get(getInt(parameter));
                    vp.getTslPolicies().add(tpName);
                    policyDb.addOrReplaceValidationPolicy(vp, true);
                    policyUtils.resetPolicyTableFolding(vp, session);
                }
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        /*
         * Function executed if an action is selected for a particular TSL policy in a a Validation policy
         * The available actions are null (0) and delete TSL Policy (1)
         */
        if (id.startsWith("vpTpAction")) {
            ValidationPolicy vp = getSelectedValidationPolicy(req);
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT) && vp != null) {
                int policyIndex = getInt(id.substring("vpTpAction".length()));
                int action = getInt(parameter);
                // Get the selected TslPolicy
                List<TslPolicy> tslPolicies = policyDb.getTslPolicies();
                if (policyIndex < tslPolicies.size()) {
                    TslPolicy tp = tslPolicies.get(policyIndex);
                    //Get list of Tsl Policies in the validation policy
                    List<String> tslPolicyNames = vp.getTslPolicies();
                    // Delete if selected policy is in list
                    if (tslPolicyNames.contains(tp.getTslPolicyName())) {
                        tslPolicyNames.remove(tp.getTslPolicyName());
                    }
                    //Store updated Validation policy
                    policyDb.addOrReplaceValidationPolicy(vp, true);
                    policyUtils.resetPolicyTableFolding(vp, session);
                }
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        if (id.equals("setValPolicyDescription")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                ValidationPolicy vp = getSelectedValidationPolicy(req);
                if (vp != null) {
                    // XSS filtering, Do not allow textarea tags, frames or scripts in input data
                    parameter = InputValidator.filter(parameter, new InputValidator.Rule[] {
                        InputValidator.Rule.TEXTAREA, 
                        InputValidator.Rule.HTML_SCRUB
                    });
                    vp.setDescription(parameter);
                    policyDb.addOrReplaceValidationPolicy(vp, true);
                }
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        /*
         * Function executed if one of the compliant trust services are blocked
         */
        if (id.equals("vpBlockCert")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                ValidationPolicy vp = getSelectedValidationPolicy(req);
                if (vp != null) {
                    parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
                    vp.getBlockCertIds().add(parameter);
                    policyDb.addOrReplaceValidationPolicy(vp, true);
                    policyUtils.resetPolicyTableFolding(vp, session);
                }
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        if (id.equals("vpBlockTsp")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                ValidationPolicy vp = getSelectedValidationPolicy(req);
                if (vp != null) {
                    parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
                    policyUtils.blockallTSPServices(vp, parameter);
                    policyUtils.resetPolicyTableFolding(vp, session);
                }
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }

        /*
         * Function executed if a previously blocked trust service is unblocked
         */
        if (id.equals("vpUnblockCert")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                ValidationPolicy vp = getSelectedValidationPolicy(req);
                if (vp != null) {
                    parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
                    vp.getBlockCertIds().remove(parameter);
                    policyDb.addOrReplaceValidationPolicy(vp, true);
                    policyUtils.resetPolicyTableFolding(vp, session);
                }
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        if (id.equals("externalCertSelect")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                ValidationPolicy vp = getSelectedValidationPolicy(req);
                List<String> vpExtCertIds = vp.getAddCertIds();
                // toggle selection
                parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
                if (vpExtCertIds.contains(parameter)) {
                    vpExtCertIds.remove(parameter);
                } else {
                    vpExtCertIds.add(parameter);
                }
                policyDb.addOrReplaceValidationPolicy(vp, true);
            }
            session.setAddNewPolicy(false);
            return getPolicyViewAreaHtml(req);
        }
        //-------------------------
        //TSL Policy Actions
        //-------------------------
        if (id.equals("selectTslPolicy")) {
            session.setSelectedTslPolicy(getInt(parameter));
            return getPolicyViewAreaHtml(req);
        }
        if (id.startsWith("tPolicyAction")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                int policyIndex = getInt(id.substring("tPolicyAction".length()));
                int action = getInt(parameter);
                session.setSelectedTslPolicy(policyIndex);
                session.setRequestedPolicyAction(action);
                if (action > 0) {
                    session.setPolicyConfRequired(true);
                } else {
                    updateTslPolicyState(req);
                }
            }
            return getPolicyViewAreaHtml(req);
        }
        if (id.equals("addNewTslPolicy")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
                addNewTslPolicy(req, parameter);
            }
            return getPolicyViewAreaHtml(req);
        }
        if (id.startsWith("policySelect")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                updateTslPolicySettings(req);
            }
            return getPolicyViewAreaHtml(req);
        }
        //-------------------------
        //ExternalCert Actions
        //-------------------------
        if (id.startsWith("externalCert")) {
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                parameter = InputValidator.filter(parameter, new InputValidator.Rule[]{InputValidator.Rule.PRINTABLE_ASCII, InputValidator.Rule.HTML_TAGS});
                externalCertAction(req, parameter);
            }
            return getPolicyViewAreaHtml(req);
        }




        return "Not implemented response";
    }

    /**
     * Main method for generating html response to any request.
     * @param req
     * @return 
     */
    private String getPolicyViewAreaHtml(RequestModel req) {
        SessionModel session = req.getSession();
        boolean authorized = authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT) && session.isPolicyConfMode();

        //On iFrame data load request
        if (req.getAction().equals("loadFrameInfo")) {
            return getInfoBody(req);
        }
        //On data reload request from iframe component
        if (req.getContainer().equals(IFRAME_MAIN_DIV)) {
            return getInfoBody(req);
        }

        HtmlElement subMenuDiv = new DivElement();
        HtmlElement policyDiv = new DivElement(POLICY_INFOTABLE_DIV);
        HtmlElement infoHeadDiv = new DivElement(INFO_HEAD_DIV);
        addViewButton(req, infoHeadDiv);

        //Get data
        HtmlElement policyTable = new TableElement();
        if (authorized) {
            subMenuDiv.addHtmlElement(getPolicySubMenu(session));
        } else {
            session.setPolicySubMenu(0);
        }
        switch (session.getPolicySubMenu()) {
            case 0:
                if (session.isPolicyConfRequired()) {
                    switch (session.getRequestedPolicyAction()) {
                        case 3:
                            getActionConfirmation(0, MAIN_DATA_AREA, policyDiv);
                            break;
                        default:
                            getActionConfirmation(1, MAIN_DATA_AREA, policyDiv);
                    }
                } else {
                    ValidationPolicy vp = getSelectedValidationPolicy(req);
                    policyTable = getValidationPoliciesContent(req, (req.getContainer().equals(POLICY_INFOTABLE_DIV)));
                    policyDiv.addHtmlElement(policyTable);
                }
                break;
            case 1:
                if (session.isPolicyConfRequired()) {
                    switch (session.getRequestedPolicyAction()) {
                        case 1:
                            getActionConfirmation(2, MAIN_DATA_AREA, policyDiv);
                    }
                } else {
                    TslPolicy tp = getSelectedTslPolicy(req);
                    getTslPoliciesContent(req, policyDiv);
                }
                break;
            case 2:
                infoHeadDiv.addHtmlElement(new TextObject(" - Manual entry of additional (Non-TSL) Certificates"));
                break;
            default:
                policyDiv.setText("Not implemented menu choice");
        }

        // Setup iFrame
        HtmlElement dataIframe = setupIframe(req.getWindowHeight(), authorized);
        if (session.getPolicyButtonState().equals("data")) {
            dataIframe.addAttribute("src", "tslDataframe.jsp?selected=null&nocache=false");
        }
        if (session.getPolicyButtonState().equals("cert")) {
            dataIframe.addAttribute("src", "certDataframe.jsp?parameter=" + session.getPolicySelectedPemCert());
        }

        //Organizing structure
        HtmlElement mainPolicyDiv = new DivElement(MAIN_TSL_DIV);
        HtmlElement infoDiv = new DivElement(PRESENTATION_INFO_DIV);
        infoDiv.addHtmlElement(infoHeadDiv);
        infoDiv.addHtmlElement(dataIframe);
        mainPolicyDiv.addHtmlElement(subMenuDiv);
        mainPolicyDiv.addHtmlElement(policyDiv);
        mainPolicyDiv.addHtmlElement(infoDiv);


        //Set div widtha
        if (session.getPolicySubMenu() == 2) {
            infoDiv.addStyle("width", "99%");
        } else {
            if (authorized) {
                policyDiv.addStyle("width", "20%");
                infoDiv.addStyle("width", "78%");
            } else {
                policyDiv.addStyle("width", "18%");
                infoDiv.addStyle("width", "80%");
            }
        }

        // If only refresh of the policy table div is requested, then just return that table.
        // This feature is currently unused
        if (req.getContainer().equals(POLICY_INFOTABLE_DIV)) {
            return policyTable.toString();
        }

        return mainPolicyDiv.toString();
    }

    /**
     * Provides a HTML element holding the Authorization sub menu;
     * @param req Request data
     * @return  HTML element
     */
    private HtmlElement getPolicySubMenu(SessionModel session) {
        TableElement policySubMenu = new TableElement(SUBMENU_CLASS, "policySubMenu");
        ValPoliciesDbUtil policyDb = model.getPolicyDb();

        //Get user count
        int admins = 0, requests = 0, blocked = 0;
        String[] menuItems = new String[]{"Validation Policies", "TSL Policies", "Non TSL Certificates"};
        TableRowElement tr = new TableRowElement();
        policySubMenu.addHtmlElement(tr);
        for (int i = 0; i < menuItems.length; i++) {
            TableCellElement td = new TableCellElement(menuItems[i]);
            if (i == 0) {
                td.addAction(ONCLICK, TWO_STEP_LOAD_FUNCTION, new String[]{
                            MAIN_DATA_AREA, "policySubMenu", String.valueOf(i),
                            POLICY_INFOTABLE_DIV, "refresh", "null"
                        });
            } else {
                td.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                            MAIN_DATA_AREA, "policySubMenu", String.valueOf(i)
                        });
            }
            if (session.getPolicySubMenu() == i) {
                td.addAttribute("class", SELECTED_CLASS);
            } else {
                td.addAttribute("class", SUBMENU_CLASS);
            }
            tr.addHtmlElement(td);
        }
        return policySubMenu;
    }

    private String getInfoBody(RequestModel req) {
        HtmlElement infoBodyDiv = new DivElement(INFO_BODY_DIV);
        SessionModel session = req.getSession();
        //Get data
        boolean authorized = authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT) && session.isPolicyConfMode();
        switch (session.getPolicySubMenu()) {
            case 0:
                ValidationPolicy vp = getSelectedValidationPolicy(req);
                if (vp != null) {
                    infoBodyDiv.addHtmlElement(policyInfo.getValPolicyInfoTable(vp, req, authorized));
                } else {
                    infoBodyDiv.addHtmlElement(new TextObject("Create a new Policy to edit"));
                }
                break;
            case 1:
                TslPolicy tp = getSelectedTslPolicy(req);
                if (tp != null) {
                    infoBodyDiv.addHtmlElement(policyInfo.getTslPolicyInfoTable(tp, req, authorized));
                } else {
                    infoBodyDiv.addHtmlElement(new TextObject("Create a new Tsl Policy to edit"));
                }
                break;
            case 2:
                infoBodyDiv.addHtmlElement(policyInfo.getExternalCertConfigTable(req, authorized));
                break;
            default:
                infoBodyDiv.setText("Not implemented menu choice");
        }
        return infoBodyDiv.toString();
    }

    private void addViewButton(RequestModel req, HtmlElement containerDiv) {
        SessionModel session = req.getSession();
        boolean policyAdmin = authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT);
        HtmlElement certButton = new ButtonElement("close inspector", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                    MAIN_DATA_AREA,
                    VIEW_BUTTON,
                    "data"});
        HtmlElement policyEditButton = new ButtonElement("Configuration Mode", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                    MAIN_DATA_AREA,
                    VIEW_BUTTON,
                    "edit"});
        HtmlElement policyStopEditButton = new ButtonElement("Exit Configuration Mode", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                    MAIN_DATA_AREA,
                    VIEW_BUTTON,
                    "stopEdit"});

        certButton.addStyle("float", "right");
        policyEditButton.addStyle("float", "right");
        policyStopEditButton.addStyle("float", "right");

        if (session.getPolicyButtonState().equals("cert")) {
            containerDiv.addHtmlElement(certButton);
            containerDiv.setText("Certificate data");
        } else {
            if (policyAdmin) {
                if (session.isPolicyConfMode()) {
                    containerDiv.addHtmlElement(policyStopEditButton);
                    containerDiv.setText("Policy Configuration");
                } else {
                    containerDiv.addHtmlElement(policyEditButton);
                    containerDiv.setText("Policy Information");
                }
            } else {
                containerDiv.setText("Policy Information");
            }
        }

        containerDiv.addAttribute("width", "100%");
    }

    private HtmlElement setupIframe(int winHeight, boolean authorized) {
        HtmlElement iFrame = new GenericHtmlElement("iframe");

        int margin = (authorized) ? 120 : 100;

        int frmHeight = winHeight - margin;
        frmHeight = (frmHeight < 500) ? 500 : frmHeight;

        iFrame.addAttribute("id", "tslIframe");
        iFrame.addAttribute("frameborder", "0");
        iFrame.addAttribute("height", String.valueOf(frmHeight));
        iFrame.addAttribute("width", "100%");

        return iFrame;
    }

    /**
     * Generates a HTML element holding a accept/cancel message and accept/cancel buttons
     * @param actionType The requested action that detemines the displaytext
     * @param target The target html element to be loaded with data upon accept/reject
     * @param acceptParam Action parameter for the accept button
     * @param cacelParam Action parameter for the cancel button
     * @param container The html element container of the response information
     * @return 
     */
    private void getActionConfirmation(int actionType, String target, HtmlElement container) {
        /*
         * Actions
         * 0 - Reconstruct Validation Policy
         * 1 - Remove Validation Policy
         * 2 - Remove Tsl Policy
         */

        StringBuilder b = new StringBuilder();
        String acceptResponse = "";

        switch (actionType) {
            case 0:
                b.append(p("Performing this action will remove the current policy CA and all it's "
                        + "issued certificates on next server update"));
                b.append(p("A new policy CA will be created under the same name and new certificates will be issued."
                        + " This operation can not be undone once executed by the server."));
                acceptResponse = "VPOK";
                break;
            case 1:
                b.append(p("Performing this action will permanently remove the selected policy CA and all it's"
                        + " issued certificates on next server update."
                        + " This operation can not be undone once executed by the server."));
                acceptResponse = "VPOK";
                break;
            case 2:
                b.append(p("Performing this action will permanently remove the selected TSL Policy."
                        + " This operation can not be undone."));
                acceptResponse = "TPOK";
                break;

        }
        b.append("<br />");
        HtmlElement message = new TextObject(b.toString());

        HtmlElement cancelButton = new ButtonElement("Cancel", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                    target,
                    "returnFromAccept",
                    "cancel"
                });
        HtmlElement acceptButton = new ButtonElement("OK", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                    target,
                    "returnFromAccept",
                    acceptResponse
                });

        HtmlElement buttonDiv = new DivElement();
        buttonDiv.addHtmlElement(acceptButton);
        buttonDiv.addHtmlElement(new TextObject(SPACE + SPACE + SPACE));
        buttonDiv.addHtmlElement(cancelButton);

        container.addHtmlElement(message);
        container.addHtmlElement(buttonDiv);
    }

    private void resetAllValPolicyFoldings(SessionModel session) {
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();
        for (ValidationPolicy vp : validationPolicies) {
            policyUtils.resetPolicyTableFolding(vp, session);
        }
    }

    private String p(String text) {
        return "<p>" + text + "</p>";
    }

    private void initIcons() {
        HtmlElement activeIcn = new ImageElement(activeImg);
        HtmlElement inactiveIcn = new ImageElement(inactiveImg);
        HtmlElement pendingReconstructIcn = new ImageElement(pendingReconstructImg);
        HtmlElement pendingDeleteIcn = new ImageElement(pendingDeleteImg);
        HtmlElement pendingIcn = new ImageElement(pendingImg);

        activeIcn.addAttribute("height", icnHeight);
        inactiveIcn.addAttribute("height", icnHeight);
        pendingReconstructIcn.addAttribute("height", "20");
        pendingDeleteIcn.addAttribute("height", "20");
        pendingIcn.addAttribute("height", "20");
        vpActionIcns.put(ValidationPolicy.POLICY_STATE[0], activeIcn);
        vpActionIcns.put(ValidationPolicy.POLICY_STATE[1], inactiveIcn);
        vpActionIcns.put(ValidationPolicy.POLICY_STATE[2], pendingReconstructIcn);
        vpActionIcns.put(ValidationPolicy.POLICY_STATE[3], pendingDeleteIcn);
        vpActionIcns.put(ValidationPolicy.POLICY_STATE[4], pendingIcn);
    }

    //-------------------------------------------------
    //Validation Policies data
    //-------------------------------------------------
    private HtmlElement getValidationPoliciesContent(RequestModel req, boolean pendingStatus) {
        SessionModel session = req.getSession();
        boolean authorized = authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT) && session.isPolicyConfMode();
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();

        TableElement policyTable = new TableElement("policyTable");
        String[] tableHeading = (authorized)
                ? new String[]{"Validation Policy", "Status", "Action"}
                : new String[]{"Validation Policy", "Status"};
        policyTable.addRow(tableHeading, TABLE_HEAD_CLASS);

        for (int i = 0; i < validationPolicies.size(); i++) {
            ValidationPolicy vp = validationPolicies.get(i);

            // Status icon
            String vpStatus;
            try {
                vpStatus = vpActionIcns.get(vp.getStatus()).toString();
            } catch (Exception ex) {
                vpStatus = vpActionIcns.get(ValidationPolicy.POLICY_STATE[1]).toString();
            }
            if (authorized) {
                // If pending status info is to be displayed (normally supressed on initial load for performance reasons)
                if (pendingStatus) {
                    certInfoElm.isPendingAction(vp, session);
                }
                boolean vpPending = session.isPolicyPending(vp.getPolicyName());
                if (vp.getStatus().equals(ValidationPolicy.ENABLE_STATE)) {
                    if (session.isPolicyPending(vp.getPolicyName())) {
                        vpStatus = vpActionIcns.get(ValidationPolicy.POLICY_STATE[4]).toString();
                    }
                }
            }

            //Action select box
            String selectorName = "vPolicyAction" + String.valueOf(i);
            SelectElement actions = new SelectElement(selectorName);
            actions.addOption("--Action--");
            actions.addOption("Enable");
            actions.addOption("Disable");
            actions.addOption("Reconstruct");
            actions.addOption("Remove");
            actions.addAction(ONCHANGE, EXECUTE_OPTION_FUNCTION, new String[]{
                        MAIN_DATA_AREA, //Load element for ajax response
                        selectorName, //Ajax request id
                        selectorName //Element holding the selected sort column
                    });

            //Get default row class
            String className = (i % 2 == 0) ? TABLE_EVEN_CLASS : TABLE_ODD_CLASS;

            // Create tableRow cells
            TableCellElement nameCell = new TableCellElement(vp.getPolicyName());
            TableCellElement statusCell = new TableCellElement(vpStatus);
            TableCellElement actionCell = new TableCellElement(actions.toString());

            //Add select actions for authorized users and update the status icon if policy is pending an update.
            if (authorized) {
                nameCell.addAction(ONCLICK, TWO_STEP_LOAD_FUNCTION, new String[]{
                            MAIN_DATA_AREA,
                            "selectValPolicy",
                            String.valueOf(i),
                            POLICY_INFOTABLE_DIV, "refresh", "null"
                        });
                statusCell.addAction(ONCLICK, TWO_STEP_LOAD_FUNCTION, new String[]{
                            MAIN_DATA_AREA,
                            "selectValPolicy",
                            String.valueOf(i),
                            POLICY_INFOTABLE_DIV, "refresh", "null"
                        });
            } else {
                //Add select actions for unauthorized users
                nameCell.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                            MAIN_DATA_AREA,
                            "selectValPolicy",
                            String.valueOf(i)
                        });
                statusCell.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                            MAIN_DATA_AREA,
                            "selectValPolicy",
                            String.valueOf(i)
                        });
            }
            //Set selected class
            if (session.getSelectedValPolicy() == i) {
                nameCell.addAttribute("class", TABLE_ROW_SELECTED);
                statusCell.addAttribute("class", TABLE_ROW_SELECTED);
            }

            //Assemble row and add to table
            TableRowElement tr = new TableRowElement(className);
            tr.addHtmlElement(nameCell);
            tr.addHtmlElement(statusCell);
            if (authorized) {
                tr.addHtmlElement(actionCell);
            }
            policyTable.addHtmlElement(tr);
        }

        // Add final row for adding new Validation Policy (if authorized)
        if (authorized) {
            String className = (validationPolicies.size() % 2 == 0) ? TABLE_EVEN_CLASS : TABLE_ODD_CLASS;
            TableRowElement tr = new TableRowElement(className);

            // Create Input element
            TextInputElement newPolicyInp = new TextInputElement("newValPolicy");
            newPolicyInp.addAttribute("size", "17");
            TableCellElement inpCell = new TableCellElement(newPolicyInp.toString());
            inpCell.addAttribute("colspan", "2");

            // AddButton
            ButtonElement addButton = new ButtonElement("Add", ONCLICK, SEND_INPUT_FUNCTION, new String[]{
                        MAIN_DATA_AREA,
                        "addNewValPolicy",
                        "newValPolicy"
                    });
            TableCellElement buttonCell = new TableCellElement(addButton.toString());

            // Create new policy button
            ButtonElement createButton = new ButtonElement("New policy", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                        POLICY_INFOTABLE_DIV,
                        "newValPolicy",
                        "stopTimer",});
            TableCellElement createNewCell = new TableCellElement(createButton.toString());

            // add table row data
            if (session.isAddNewPolicy()) {
                tr.addHtmlElement(inpCell);
                tr.addHtmlElement(buttonCell);
                policyTable.addHtmlElement(tr);

            } else {
                tr.addHtmlElement(createNewCell);
                policyTable.addHtmlElement(tr);
            }



        }

        return policyTable;
    }

    private void updateValPolicyState(RequestModel req) {
        SessionModel session = req.getSession();
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();
        int action = session.getRequestedPolicyAction();
        int policyIndex = session.getSelectedValPolicy();
        if (policyIndex < validationPolicies.size() && action < (ValidationPolicy.POLICY_STATE.length + 1) && action > 0) {
            ValidationPolicy vPolicy = validationPolicies.get(policyIndex);
            vPolicy.setStatus(ValidationPolicy.POLICY_STATE[action - 1]);
            policyDb.addOrReplaceValidationPolicy(vPolicy, true);

            //Log event
            model.getLogDb().addMajorEvent(new MajorLogRecord("Policy action",
                    ValidationPolicy.POLICY_STATE[action - 1] + " policy: " + vPolicy.getPolicyName() + " ", LogInfo.getAdminIdString(req)));
        }
        //Clear action state variables
        session.setPolicyConfRequired(false);
        session.setRequestedPolicyAction(0);
    }

    private void addNewValidationPolicy(RequestModel req, String parameter) {
        SessionModel session = req.getSession();
        if (parameter.length() == 0) {
            return;
        }
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();
        for (ValidationPolicy vp : validationPolicies) {
            if (vp.getPolicyName().equals(parameter)) {
                return;
            }
        }
        ValidationPolicy newVp = new ValidationPolicy(parameter);
        newVp.setStatus(ValidationPolicy.POLICY_STATE[1]);
        policyDb.addOrReplaceValidationPolicy(newVp, false);

        //Log event
        model.getLogDb().addMajorEvent(new MajorLogRecord("Policy action",
                "Added policy: " + newVp.getPolicyName() + " ", LogInfo.getAdminIdString(req)));

        // Find the new policy index
        validationPolicies = policyDb.getValidationPolicies();
        for (ValidationPolicy vp : validationPolicies) {
            if (vp.getPolicyName().equals(newVp.getPolicyName())) {
                session.setSelectedValPolicy(validationPolicies.indexOf(vp));
            }
        }
    }

    private ValidationPolicy getSelectedValidationPolicy(RequestModel req) {
        SessionModel session = req.getSession();
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();
        try {
            ValidationPolicy vp = validationPolicies.get(session.getSelectedValPolicy());
            return vp;
        } catch (Exception ex) {
            return null;
        }
    }

    //-------------------------------------------------
    //TSL Polivies data
    //-------------------------------------------------
    private void getTslPoliciesContent(RequestModel req, HtmlElement policyDiv) {
        SessionModel session = req.getSession();
        List<TslPolicy> tslPolicies = policyDb.getTslPolicies();
        
        if (policyUtils.getTslTypeVals()==null || policyUtils.getTslTypeVals().isEmpty()){
            policyDiv.addHtmlElement(new TextObject("No TSL Data is available. Start the cache daemon to load TSL Data"));
            return;
        }

        TableElement policyTable = new TableElement("policyTable");
        String[] tableHeading = (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT))
                ? new String[]{"Policy Name", "In use", "Action"}
                : new String[]{"Policy Name", "In use"};
        policyTable.addRow(tableHeading, TABLE_HEAD_CLASS);

        for (int i = 0; i < tslPolicies.size(); i++) {
            TslPolicy tp = tslPolicies.get(i);

            // Status icon
            Boolean inUse = isTslPolicyInUse(tp);
            String vpStatus = (inUse)
                    ? vpActionIcns.get(ValidationPolicy.POLICY_STATE[0]).toString()
                    : "Not in use";

            //Action select box
            String selectorName = "tPolicyAction" + String.valueOf(i);
            SelectElement actions = new SelectElement(selectorName);
            actions.addOption("--Action--");
            actions.addOption("Remove");
            actions.addAction(ONCHANGE, EXECUTE_OPTION_FUNCTION, new String[]{
                        MAIN_DATA_AREA, //Load element for ajax response
                        selectorName, //Ajax request id
                        selectorName //Element holding the selected sort column
                    });

            //Get default row class
            String className = (i % 2 == 0) ? TABLE_EVEN_CLASS : TABLE_ODD_CLASS;

            // Create tableRow cells
            TableCellElement nameCell = new TableCellElement(tp.getTslPolicyName());
            TableCellElement statusCell = new TableCellElement(vpStatus);
            TableCellElement actionCell = new TableCellElement((inUse) ? "-No actions-" : actions.toString());

            //Add select actions
            nameCell.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                        MAIN_DATA_AREA,
                        "selectTslPolicy",
                        String.valueOf(i)
                    });
            statusCell.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                        MAIN_DATA_AREA,
                        "selectTslPolicy",
                        String.valueOf(i)
                    });

            //Set selected class
            if (session.getSelectedTslPolicy() == i) {
                nameCell.addAttribute("class", TABLE_ROW_SELECTED);
                statusCell.addAttribute("class", TABLE_ROW_SELECTED);
            }

            //Assemble row and add to table
            TableRowElement tr = new TableRowElement(className);
            tr.addHtmlElement(nameCell);
            tr.addHtmlElement(statusCell);
            if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
                tr.addHtmlElement(actionCell);
            }
            policyTable.addHtmlElement(tr);
        }

        // Add final row for adding new Validation Policy (if authorized)
        if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_POLICY_MGMNT)) {
            String className = (tslPolicies.size() % 2 == 0) ? TABLE_EVEN_CLASS : TABLE_ODD_CLASS;
            TableRowElement tr = new TableRowElement(className);

            // Create Input element
            TextInputElement newPolicyInp = new TextInputElement("newTslPolicy");
            newPolicyInp.addAttribute("size", "17");
            TableCellElement inpCell = new TableCellElement(newPolicyInp.toString());
            inpCell.addAttribute("colspan", "2");
            tr.addHtmlElement(inpCell);

            // AddButton
            ButtonElement addButton = new ButtonElement("Add", ONCLICK, SEND_INPUT_FUNCTION, new String[]{
                        MAIN_DATA_AREA,
                        "addNewTslPolicy",
                        "newTslPolicy"
                    });
            TableCellElement buttonCell = new TableCellElement(addButton.toString());
            tr.addHtmlElement(buttonCell);
            policyTable.addHtmlElement(tr);
        }

        policyDiv.addHtmlElement(policyTable);
    }

    private boolean isTslPolicyInUse(TslPolicy tslPolicy) {
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();
        boolean inUse = false;
        for (ValidationPolicy vp : validationPolicies) {
            List<String> tslPolicies = vp.getTslPolicies();
            for (String tpName : tslPolicies) {
                if (tpName.equals(tslPolicy.getTslPolicyName())) {
                    inUse = true;
                }
            }
        }
        return inUse;
    }

    private void updateTslPolicyState(RequestModel req) {
        SessionModel session = req.getSession();
        List<TslPolicy> tslPolicies = policyDb.getTslPolicies();
        int action = session.getRequestedPolicyAction();
        int policyIndex = session.getSelectedTslPolicy();
        if (policyIndex < tslPolicies.size() && action == 1) {
            TslPolicy tPolicy = tslPolicies.get(policyIndex);
            policyDb.deteleRecord(tPolicy);

            //Log event
            model.getLogDb().addMajorEvent(new MajorLogRecord("Policy action",
                    "Deleted TSL policy: " + tPolicy.getTslPolicyName() + " ", LogInfo.getAdminIdString(req)));
        }
        //Clear action state variables
        session.setPolicyConfRequired(false);
        session.setRequestedPolicyAction(0);
        session.setSelectedTslPolicy(0);
    }

    private void addNewTslPolicy(RequestModel req, String policyName) {
        SessionModel session = req.getSession();
        if (policyName.length() == 0) {
            return;
        }
        List<TslPolicy> tslPolicies = policyDb.getTslPolicies();
        for (TslPolicy tp : tslPolicies) {
            if (tp.getTslPolicyName().equals(policyName)) {
                return;
            }
        }
        TslPolicy newTp = new TslPolicy();
        newTp.setTslPolicyName(policyName);
        policyDb.addOrReplaceTslPolicy(newTp, false);

        //Log event
        model.getLogDb().addMajorEvent(new MajorLogRecord("Policy action",
                "Added TSL policy: " + newTp.getTslPolicyName() + " ", LogInfo.getAdminIdString(req)));

        // Find the new policy index
        tslPolicies = policyDb.getTslPolicies();
        for (TslPolicy tp : tslPolicies) {
            if (tp.getTslPolicyName().equals(newTp.getTslPolicyName())) {
                session.setSelectedTslPolicy(tslPolicies.indexOf(tp));
            }
        }
    }

    private TslPolicy getSelectedTslPolicy(RequestModel req) {
        SessionModel session = req.getSession();
        List<TslPolicy> tslPolicies = policyDb.getTslPolicies();
        try {
            TslPolicy tp = tslPolicies.get(session.getSelectedTslPolicy());
            return tp;
        } catch (Exception ex) {
            return null;
        }
    }

    private void updateTslPolicySettings(RequestModel req) {
        String id = req.getId();
        int parm = getInt(req.getParameter());
        TslPolicy tp = getSelectedTslPolicy(req);

        if (id.indexOf("State") > 0) {
            List<String> vals = policyUtils.getTslStateVals();
            List<String> selVals = tp.getStates();
            updateExplicitPolicy(tp, selVals, vals, parm);
        }
        if (id.indexOf("Type") > 0) {
            List<String> vals = policyUtils.getTslTypeVals();
            List<String> selVals = tp.getServiceTypes();
            updateExplicitPolicy(tp, selVals, vals, parm);
        }
        if (id.indexOf("Status") > 0) {
            List<String> vals = policyUtils.getTslStatusVals();
            List<String> selVals = tp.getStatusTypes();
            updateExplicitPolicy(tp, selVals, vals, parm);
        }
        if (id.indexOf("Sign") > 0) {
            List<String> vals = policyUtils.getTslSignatureVals();
            List<String> selVals = tp.getSignStatus();
            updateExplicitPolicy(tp, selVals, vals, parm);
        }
        if (id.indexOf("ExpTslInp") > 0) {
            tp.setExpiredTslGrace(parm);
            policyDb.addOrReplaceTslPolicy(tp, true);
        }
        if (id.indexOf("ExpTslChb") > 0) {
            //Toggle values 0 and -1 (ignore expiry)
            if (tp.getExpiredTslGrace() == -1) {
                tp.setExpiredTslGrace(0);
            } else {
                tp.setExpiredTslGrace(-1);
            }
            policyDb.addOrReplaceTslPolicy(tp, true);
        }
    }

    /**
     * Updating an explicit policy setting of a TSL Policy
     * @param tp The TSL Policy to be updated
     * @param policyValues The current list of policy values to be updated
     * @param valueList The list of possible supported values
     * @param index The index of the policy value select box clicked in the web UI
     */
    private void updateExplicitPolicy(TslPolicy tp, List<String> policyValues, List<String> valueList, int index) {
        //If within supported values, then toggle
        if (index < valueList.size()) {
            String selValue = valueList.get(index);
            if (policyValues.contains(selValue)) {
                policyValues.remove(selValue);
            } else {
                policyValues.add(selValue);
            }            
        } else {
            //Not within supported values, so delete value from policy
            List<String> extVals = getExtendedVals(valueList, policyValues);
            if (index < extVals.size()) {
                String selValue = extVals.get(index);
                policyValues.remove(selValue);
            }
        }
        policyDb.addOrReplaceTslPolicy(tp, true);
    }

    private List<String> getExtendedVals(List<String> baseList, List<String> valList) {
        List<String> extendedList = new ArrayList<String>();

        for (String val : baseList) {
            extendedList.add(val);
        }
        for (String val : valList) {
            if (!extendedList.contains(val)) {
                extendedList.add(val);
            }
        }
        return extendedList;
    }

    private void externalCertAction(RequestModel req, String parameter) {
        String id = req.getId();
        SessionModel session = req.getSession();
        String certHash;
        X509Certificate cert;
        String pemCert;

        if (id.indexOf("Enter") > 0) {
            session.setExtCertEntry(parameter);
        }
        if (id.indexOf("Add") > 0) {
            try {
                cert = KsCertFactory.getIaikCert(CertificateUtils.getCertificate(session.getExtCertEntry()));
            } catch (Exception ex) {
                cert = null;
            }
            if (cert == null) {
                return;
            }
            try {
                byte[] certBytes = cert.getEncoded();
                certHash = FnvHash.getFNV1aToHex(certBytes);
                pemCert = PEM.getPemCert(certBytes);
            } catch (CertificateEncodingException ex) {
                session.setExtCertEntry("This certificate has syntax error");
                return;
            }
            // Check for duplicates
            List<ExternalCert> externalCerts = policyDb.getExternalCerts();
            for (ExternalCert extCert : externalCerts) {
                if (extCert.getCertificateId().equals(certHash)) {
                    session.setExtCertEntry("This certificate is already in the list");
                    return;
                }
            }
            String errorMsg = checkCertificate(cert);
            if (errorMsg.length() == 0) {
                ExternalCert newExtCert = new ExternalCert();
                newExtCert.setCertificateId(certHash);
                newExtCert.setB64Cert(PEM.trimPemCert(pemCert));
                policyDb.addOrReplaceCert(newExtCert, true);
            }
            session.setExtCertEntry(errorMsg);
        }
        if (id.indexOf("Clear") > 0) {
            session.setExtCertEntry("");
        }
        if (id.indexOf("Delete") > 0) {
            if (getInt(parameter) != 1) {
                return;
            }
            String certId = "";
            try {
                certId = id.substring("externalCertDelete".length());
            } catch (Exception ex) {
                return;
            }
            List<ExternalCert> externalCerts = policyDb.getExternalCerts();
            for (ExternalCert extCert : externalCerts) {
                if (extCert.getCertificateId().equals(certId)) {
                    externalCerts.remove(extCert);
                    policyDb.deteleRecord(extCert);
                    return;
                }
            }
        }
    }

    private String checkCertificate(X509Certificate cert) {
        StringBuilder b = new StringBuilder();
        long currentTime = System.currentTimeMillis();
        long notBefore = cert.getNotBefore().getTime();
        long notAfter = cert.getNotAfter().getTime();
        int basicConstraints = cert.getBasicConstraints();
        String errorMess = "The certificate was not added as a Trust Service certificate for the following reason(s):\n\n";

        //Check validity
        if (currentTime < notBefore) {
            b.append("-The certificate is not within its validity period (Not yet valid)\n");
        }
        if (currentTime > notAfter) {
            b.append("-The certificate is not within its validity period (Expired)\n");
        }
        if (basicConstraints < 0) {
            b.append("-The certificate is not a CA certificate\n");
        }

        errorMess = (b.length() > 0)
                ? errorMess + b.toString()
                : "";


        return errorMess;
    }
}
