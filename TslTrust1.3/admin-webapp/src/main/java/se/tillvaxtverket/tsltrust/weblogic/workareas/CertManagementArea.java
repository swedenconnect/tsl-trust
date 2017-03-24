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

import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.ImageElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import se.tillvaxtverket.tsltrust.weblogic.content.CertManagementInfoElements;
import se.tillvaxtverket.tsltrust.weblogic.content.CertificateInformation;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.weblogic.content.InfoTableFactory;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import se.tillvaxtverket.tsltrust.weblogic.db.ValPoliciesDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.InputValidator;
import se.tillvaxtverket.tsltrust.weblogic.utils.PolicyUtils;
import iaik.x509.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Content provider for the Certificate Management menu
 */
public class CertManagementArea extends WorkArea implements HtmlConstants, TTConstants {

    private TslTrustModel model;
    private PolicyUtils policyUtils;
    private ValPoliciesDbUtil policyDb;
    private String activeImg = "img/Ok-icon.png", inactiveImg = "img/Nok-icon.png",
            pendingReconstructImg = "img/pendingRecycle.png", pendingDeleteImg = "img/pendingDelete.png",
            pendingImg = "img/pendingIcn.png";
    private Map<String, HtmlElement> vpActionIcns = new HashMap<String, HtmlElement>();
    private String icnHeight = "17";
    private CertManagementInfoElements certInfoElm;

    /**
     * Constructor
     *
     * @param model Application data
     */
    public CertManagementArea(TslTrustModel model) {
        this.model = model;
        policyDb = model.getPolicyDb();
        policyUtils = new PolicyUtils(model);
        policyUtils.recachePolicyParameters();
        certInfoElm = new CertManagementInfoElements(model);
        initIcons();
    }

    /**
     * Generates the html code for functions under the Certificate Management
     * menu
     *
     * @param req Data related to the Http request from the client.
     * @return html response data
     */
    @Override
    public String getHtmlData(RequestModel req) {
        SessionModel session = req.getSession();
        String html = "";
        if (authorized(req.getAuthzLvl(), AUTH_CONTEXT_CERT_MGMNT)) {
            if (req.getAction().equals("loadMain")) {
                return getCertMgmntAreaHtml(req);
            }

            if (req.getAction().equals("loadElement")) {
                return loadElementData(req);
            }

            //Called by an iFrame in page load to retrieve the iFrame content
            if (req.getAction().equals("loadFrameInfo")) {
                return getCertMgmntAreaHtml(req);
            }


            if (req.getAction().equals("frameLoad")) {
                String parameter = req.getParameter();
                if (req.getId().equals(VIEW_BUTTON)) {
                    setViewButtonParameter(parameter, req);
                }
            }
        } else {
            html = "You are not authorized to access this information";
        }

        return html;
    }

    /**
     * Sets the iFrame state of the policy management menu This function is
     * invoked either when a certificate view is requested or when certificate
     * view is terminated
     *
     *
     * @param parameter the http request parameter value from the http query
     * string
     *
     * @param session The session model
     */
    private void setViewButtonParameter(String parameter, RequestModel req) {
        SessionModel session = req.getSession();
        parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
        if (parameter.startsWith("cert")) {
            session.setCertButtonState("cert");
            session.setCertSelectedPemCert(parameter.substring(4));
            //session.setPemCert(policyUtils.getBase64Cert(parameter.substring(4), session.getExtCertEntry()));
        } else {
            session.setCertButtonState("data");
        }
    }

    /**
     * Handler of the loadData JavaScript function
     *
     * @param req the client http request model
     * @return html code in response to the request
     */
    private String loadElementData(RequestModel req) {
        SessionModel session = req.getSession();
        String id = req.getId();
        String parameter = req.getParameter();

        if (id.equals("selectValPolicy")) {
            session.setCertButtonState("data");
            session.setSelectedCertPolicy(getInt(parameter));
            return getCertMgmntAreaHtml(req);
        }


        if (id.equals(VIEW_BUTTON)) {
            setViewButtonParameter(parameter, req);
            return getCertMgmntAreaHtml(req);
        }

        if (id.equals(INFO_TABLE_AJAX_LOAD)) {
            InfoTableModel tm = new InfoTableModel("itAjax" + parameter);
            tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
            CertificateInformation certInfo = new CertificateInformation(tm, session);
            X509Certificate iAIKCert = certInfoElm.getCert(parameter);
            if (iAIKCert != null) {
                tm.setElements(certInfo.getCertInfo(iAIKCert));
                return new InfoTableFactory(tm, session).getTable().toString();
            } else {
                return "Certificate Load Error";
            }
        }
        if (id.equals("refresh")) {
            return getCertMgmntAreaHtml(req);
        }

        return "Not implemented response";
    }

    private HtmlElement getIcon(String source) {
        HtmlElement icn = new ImageElement(source);
        icn.addAttribute("height", icnHeight);
        return icn;
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

    private String getCertMgmntAreaHtml(RequestModel req) {
        try {
            SessionModel session = req.getSession();

            //On iFrame data load request
            if (req.getAction().equals("loadFrameInfo")) {
                return getInfoBody(req);
            }
            //On data reload request from iframe component
            if (req.getContainer().equals(IFRAME_MAIN_DIV)) {
                return getInfoBody(req);
            }

//        HtmlElement subMenuDiv = new DivElement();
            HtmlElement policyDiv = new DivElement(POLICY_INFOTABLE_DIV);
            HtmlElement infoHeadDiv = new DivElement(INFO_HEAD_DIV);
            addViewButton(req, infoHeadDiv);

            //Get validation policy table data
            ValidationPolicy vp = getSelectedCertValidationPolicy(req);
            HtmlElement policyTable = getValidationPoliciesContent(req, (req.getContainer().equals(POLICY_INFOTABLE_DIV)));
            policyDiv.addHtmlElement(policyTable);

            // Setup iFrame
            HtmlElement dataIframe = setupIframe(req.getWindowHeight());
            if (session.getCertButtonState().equals("data")) {
                dataIframe.addAttribute("src", "tslDataframe.jsp?parameter=null&nocache=false");
            }
            if (session.getCertButtonState().equals("cert")) {
                dataIframe.addAttribute("src", "certDataframe.jsp?parameter=" + session.getCertSelectedPemCert());
            }

            //Organizing structure
            HtmlElement mainCertMgmntDiv = new DivElement(MAIN_TSL_DIV);
            HtmlElement infoDiv = new DivElement(PRESENTATION_INFO_DIV);
            infoDiv.addHtmlElement(infoHeadDiv);
            infoDiv.addHtmlElement(dataIframe);
//        mainCertMgmntDiv.addHtmlElement(subMenuDiv);
            mainCertMgmntDiv.addHtmlElement(policyDiv);
            mainCertMgmntDiv.addHtmlElement(infoDiv);


            //Set div widtha
            policyDiv.addStyle("width", "18%");
            infoDiv.addStyle("width", "80%");

            // IF only refresh of the policy table div is requested then just return that table.
            if (req.getContainer().equals(POLICY_INFOTABLE_DIV)) {
                return policyTable.toString();
            }

            return mainCertMgmntDiv.toString();
        } catch (Exception ex) {
            Logger.getLogger(CertManagementArea.class.getName()).log(Level.WARNING, "Failed to provide Cert Management Table data", ex);
            return ex.getMessage();
        }
    }

    private String getInfoBody(RequestModel req) {
        HtmlElement infoBodyDiv = new DivElement(INFO_BODY_DIV);
        SessionModel session = req.getSession();
        //Get data
        ValidationPolicy vp = getSelectedCertValidationPolicy(req);
        if (vp != null) {
            infoBodyDiv.addHtmlElement(certInfoElm.getValPolicyInfoTable(vp, req));
        } else {
            infoBodyDiv.addHtmlElement(new TextObject("No validation policies are available"));
        }
        return infoBodyDiv.toString();
    }

    private void addViewButton(RequestModel req, HtmlElement containerDiv) {
        SessionModel session = req.getSession();
        HtmlElement certButton = new ButtonElement("close inspector", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
            MAIN_DATA_AREA,
            VIEW_BUTTON,
            "data"});

        certButton.addStyle("float", "right");

        if (session.getCertButtonState().equals("cert")) {
            containerDiv.addHtmlElement(certButton);
            containerDiv.setText("Certificate data");
        } else {
            containerDiv.setText("Certificate Management Information");
        }
        containerDiv.addAttribute("width", "100%");
    }

    private ValidationPolicy getSelectedCertValidationPolicy(RequestModel req) {
        SessionModel session = req.getSession();
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();
        try {
            ValidationPolicy vp = validationPolicies.get(session.getSelectedCertPolicy());
            return vp;
        } catch (Exception ex) {
            return null;
        }
    }

    private HtmlElement getValidationPoliciesContent(RequestModel req, boolean pendingStatus) {
        SessionModel session = req.getSession();
        List<ValidationPolicy> validationPolicies = policyDb.getValidationPolicies();

        TableElement policyTable = new TableElement("policyTable");
        String[] tableHeading = new String[]{"Validation Policy", "Status"};
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

            // If pending status info is to be displayed (normally supressed on initial load for performance reasons)
            if (pendingStatus) {
                certInfoElm.isPendingAction(vp, session);
            }
            if (vp.getStatus().equals(ValidationPolicy.ENABLE_STATE)) {
                if (session.isPolicyPending(vp.getPolicyName())) {
                    vpStatus = vpActionIcns.get(ValidationPolicy.POLICY_STATE[4]).toString();
                }
            }

            //Get default row class
            String className = (i % 2 == 0) ? TABLE_EVEN_CLASS : TABLE_ODD_CLASS;

            // Create tableRow cells
            TableCellElement nameCell = new TableCellElement(vp.getPolicyName());
            TableCellElement statusCell = new TableCellElement(vpStatus);

            //Add select actions
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

            //Set selected class
            if (session.getSelectedCertPolicy() == i) {
                nameCell.addAttribute("class", TABLE_ROW_SELECTED);
                statusCell.addAttribute("class", TABLE_ROW_SELECTED);
            }

            //Assemble row and add to table
            TableRowElement tr = new TableRowElement(className);
            tr.addHtmlElement(nameCell);
            tr.addHtmlElement(statusCell);
            policyTable.addHtmlElement(tr);
        }

        return policyTable;
    }

    private HtmlElement setupIframe(int winHeight) {
        HtmlElement iFrame = new GenericHtmlElement("iframe");

        int margin = 100;

        int frmHeight = winHeight - margin;
        frmHeight = (frmHeight < 500) ? 500 : frmHeight;

        iFrame.addAttribute("id", "tslIframe");
        iFrame.addAttribute("frameborder", "0");
        iFrame.addAttribute("height", String.valueOf(frmHeight));
        iFrame.addAttribute("width", "100%");

        return iFrame;
    }
}
