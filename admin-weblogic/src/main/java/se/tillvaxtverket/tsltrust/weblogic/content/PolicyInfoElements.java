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

import com.aaasec.lib.aaacert.AaaCertificate;
import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.CheckboxElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.SelectElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextInputElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import se.tillvaxtverket.tsltrust.common.utils.core.CorePEM;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.weblogic.data.ExternalCert;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.TslPolicy;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDb;
import se.tillvaxtverket.tsltrust.weblogic.db.ValPoliciesDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElement;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.ASN1Util;
import se.tillvaxtverket.tsltrust.weblogic.utils.ExtractorUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.HtmlUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.PolicyUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides methods for building UI component in the policy configuration menu of TSL Trust
 */
public class PolicyInfoElements implements HtmlConstants, TTConstants {

    private PolicyUtils policyUtils;
    private ValPoliciesDbUtil policyDb;
    private TslCertDb tslCertDb;

    /**
     * Constructor
     * @param model The TslTrustModel object holding essential application parameters and objects
     */
    public PolicyInfoElements(TslTrustModel model) {
        policyDb = model.getPolicyDb();
        tslCertDb = model.getTslCertDb();
        policyUtils = new PolicyUtils(model);
    }

    /**
     * Getter for the policy utility object. This object provides various policy utility 
     * functions such as testing trust services compliance to defined policies.
     * @return The policy utility object
     */
    public PolicyUtils getPolicyUtils() {
        return policyUtils;
    }

    /**
     * Builds a HtmlElement holding the UI for signature validation policy configuration
     * @param valPolicy The validation policy being configured
     * @param req Http request model holding http request information
     * @param authorized true if the user making the http request is authorized to edit validation policies
     * @return The HtmlElement providing validation policy configuration UI
     */
    public HtmlElement getValPolicyInfoTable(ValidationPolicy valPolicy, RequestModel req, boolean authorized) {

        //Basic Strtucture
        InfoTableModel tm = new InfoTableModel("vp" + FnvHash.getFNV1aToHex(valPolicy.getPolicyName()));
        tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
        InfoTableElements valPolicyElements = tm.getElements();

        CertificateInformation certInfo = new CertificateInformation(tm, req.getSession());

        //Policy configuration node
        InfoTableSection policyConfigNode = valPolicyElements.addNewSection(tm, true);
        policyConfigNode.setFoldedElement("Validation Policy Configuration");
        policyConfigNode.setKeepFoldableElement(true);
        InfoTableElements policyConfigElements = policyConfigNode.getElements();
        buildValidationPolicyConfigurationNode(tm, policyConfigElements, valPolicy, certInfo, authorized);

        //Compliant trust services node
        buildCompliantServicesNode(valPolicyElements, tm, valPolicy, req, certInfo, authorized);

        InfoTableFactory fact = new InfoTableFactory(tm, req.getSession());
        return fact.getTable();
    }

    private void buildValidationPolicyConfigurationNode(InfoTableModel tm, InfoTableElements policyConfigElements,
            ValidationPolicy valPolicy, CertificateInformation certInfo, boolean authorized) {
        //Info sections within the policy configuration node

        //None of these sections are foldable. Only elements inside these sections are allowed to be foldable
        InfoTableElements infoElm = policyConfigElements.addNewSection(tm, "Policy Info", true).getElements();
        InfoTableElements tslPoliciesElm = policyConfigElements.addNewSection(tm, "TSL Policies", true).getElements();
        InfoTableElements addedCertElm = policyConfigElements.addNewSection(tm, "Non TSL Certificates", true).getElements();
        InfoTableElements blockedCertElm = policyConfigElements.addNewSection(tm, "Blocked Services", true).getElements();

        //Policy Info
        InfoTableSection policyNameSect = infoElm.addNewSection(tm, "Name");
        policyNameSect.addNewElement(valPolicy.getPolicyName());
        InfoTableSection policyDescSect = infoElm.addNewSection(tm, "Description");

        HtmlElement policyInfoText = new GenericHtmlElement("textarea");
        policyInfoText.addAttribute("id", "policyDescriptionInp");
        policyInfoText.addAttribute("cols", "65");
        policyInfoText.addAttribute("rows", "10");
        policyInfoText.addHtmlElement(new TextObject(valPolicy.getDescription()));

        //Button
        ButtonElement infoTextButton = new ButtonElement("Save change", ONCLICK, SEND_INPUT_FUNCTION, new String[]{
                    IFRAME_MAIN_DIV,
                    "setValPolicyDescription",
                    "policyDescriptionInp"
                });
        if (!authorized) {
            policyInfoText.addAttribute("readonly", "readonly");
            policyDescSect.addNewElement(valPolicy.getDescription());
        } else {
            policyDescSect.addNewElement(policyInfoText.toString());
            policyDescSect.addNewElement(infoTextButton.toString());
        }

        //Get TSL Policies
        List<String> tslPolicyNames = valPolicy.getTslPolicies();
        List<TslPolicy> tslPolicies = policyDb.getTslPolicies();
        for (TslPolicy tp : tslPolicies) {
            if (tslPolicyNames.contains(tp.getTslPolicyName())) {
                //Delete action
                String actionElement = "";
                if (authorized) {
                    //Action select box
                    String selectorName = "vpTpAction" + String.valueOf(tslPolicies.indexOf(tp));
                    SelectElement actions = new SelectElement(selectorName);
                    actions.addOption("-- Action --");
                    actions.addOption("Remove");
                    actions.addAction(ONCHANGE, EXECUTE_OPTION_FUNCTION, new String[]{
                                IFRAME_MAIN_DIV, //Load element for ajax response
                                selectorName, //Ajax request id
                                selectorName //Element holding the selected sort column
                            });
                    actionElement = SPACE + SPACE + actions.toString();
                }
                InfoTableSection tpSection = tslPoliciesElm.addNewSection(tm, true);
                tpSection.setFoldedElement(tp.getTslPolicyName() + actionElement);
                tpSection.setKeepFoldableElement(true);
                getTslPolicyData(tp, tm, tpSection.getElements(), false);
                //get tsl policy content
            }
        }

        // bCert inputfield for adding more TSL Policies
        List<String> unselectedTslPolicies = policyUtils.getUnselectedTslPolicies(valPolicy);
        if (authorized && !unselectedTslPolicies.isEmpty()) {
            //Policy select box
            String selectorName = "useTslPolicySelector";
            SelectElement policySelector = new SelectElement(selectorName);
            for (String tpName : unselectedTslPolicies) {
                policySelector.addOption(tpName);
            }
            //Button
            ButtonElement addButton = new ButtonElement("Add", ONCLICK, EXECUTE_OPTION_FUNCTION, new String[]{
                        IFRAME_MAIN_DIV,
                        "useTslPolicy",
                        "useTslPolicySelector"
                    });
            InfoTableSection addPolicySect = tslPoliciesElm.addNewSection(tm);
            addPolicySect.addNewElement("Add TSL Policy " + policySelector.toString() + SPACE + SPACE + addButton.toString());
        }
        //External certs
        boolean selectedCert = false;
        List<ExternalCert> externalCerts = policyDb.getExternalCerts();
        for (ExternalCert extCert : externalCerts) {
            AaaCertificate cert = extCert.getCert();
            if (cert == null) {
                continue;
            }
            //Check if selected
            List<String> policyExtCertIds = valPolicy.getAddCertIds();
            boolean selected = policyExtCertIds.contains(extCert.getCertificateId());
            //Abort if not selected and not authorized policy admin
            if (!authorized && !selected) {
                continue;
            }
            //get display components
            selectedCert = true;
            String certName = ASN1Util.getShortCertName(cert);
            String ecChbox = "";
            if (authorized) {
                CheckboxElement cbe = new CheckboxElement("extCertSelectChb", "", ONCHANGE, LOAD_DATA_FUNCTION, new String[]{
                            IFRAME_MAIN_DIV,
                            "externalCertSelect",
                            extCert.getCertificateId()
                        },
                        selected);
                ecChbox = cbe.toString() + SPACE;
            }
            //Add data to table
            InfoTableSection certSect = addedCertElm.addNewSection(tm);
            certSect.setFoldedElement(ecChbox + certName);
            certSect.setKeepFoldableElement(true);
            certSect.setSectionHeadingClasses(new String[]{TABLE_UNFOLD_BOLD, TABLE_UNFOLD_BOLD});
            certSect.setElements(certInfo.getCertInfo(cert));
        }
        if (!selectedCert) {
            addedCertElm.add(new InfoTableElement("No approved non-TSL service certificates"));
        }


        //Block list
        List<String> blockCertIds = valPolicy.getBlockCertIds();
        List<String> consCertList = new ArrayList<String>();
        Map<String, AaaCertificate> certMap = new HashMap<String, AaaCertificate>();
        for (String bCertId : blockCertIds) {
            AaaCertificate iaikCert = policyUtils.getIAIKCert(bCertId);
            if (iaikCert != null) {
                certMap.put(bCertId, iaikCert);
                consCertList.add(bCertId);
            }
        }

        // Update consolitated list
        valPolicy.setBlockCertIds(consCertList);
        policyDb.addOrReplaceValidationPolicy(valPolicy, true);

        if (consCertList.isEmpty()) {
            blockedCertElm.add(new InfoTableElement("No blocked trust services in list"));
        } else {
            for (String bCertId : consCertList) {
                AaaCertificate bCert = certMap.get(bCertId);
                String shortCertName = ASN1Util.getShortCertName(bCert);
                InfoTableSection bCertSect = blockedCertElm.addNewSection(tm);

                //Remove block button
                String actionElement = "";
                if (authorized) {
                    ButtonElement unblockCertButton = new ButtonElement("Unblock", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                                IFRAME_MAIN_DIV,
                                "vpUnblockCert",
                                bCertId
                            });
                    actionElement = unblockCertButton.toString() + SPACE;
                }
                bCertSect.setFoldedElement(actionElement + shortCertName);
                bCertSect.setKeepFoldableElement(true);
                bCertSect.setSectionHeadingClasses(new String[]{TABLE_UNFOLD_BOLD, TABLE_UNFOLD_BOLD});
                bCertSect.setElements(certInfo.getCertInfo(bCert));
            }
        }
    }

    private void buildCompliantServicesNode(InfoTableElements infoElements, InfoTableModel tm,
            ValidationPolicy valPolicy, RequestModel req, CertificateInformation certInfo, boolean authorized) {

        List<TslCertificates> policyCompliantCerts = policyUtils.getPolicyCompliantCerts(valPolicy);
        InfoTableSection compServicesSect = infoElements.addNewSection(tm, true);
        compServicesSect.setFoldedElement("Compliant Trust Services (" + policyCompliantCerts.size() + ")");
        compServicesSect.setKeepFoldableElement(true);
        InfoTableElements compServicesElem = compServicesSect.getElements();

        // State nodes
        List<String> states = policyUtils.getStateList(policyCompliantCerts);
        for (String state : states) {
            List<TslCertificates> stateCertList = policyUtils.getStateCertList(state, policyCompliantCerts);
            InfoTableSection stateSect = compServicesElem.addNewSection(tm, true);
            stateSect.setFoldedElement(state + HtmlUtil.countServices(stateCertList.size()));
            stateSect.setKeepFoldableElement(true);
            stateSect.setSectionHeadingClasses(new String[]{TABLE_UNFOLD_BOLD, TABLE_UNFOLD_BOLD});

            //TSP nodes
            List<String> tspList = policyUtils.getTspList(stateCertList);
            for (String tsp : tspList) {
                List<TslCertificates> tspCertList = policyUtils.getTspCertList(tsp, stateCertList);
                InfoTableSection tspSect = stateSect.getElements().addNewSection(tm, false);

                String tspActionElement = "";
                if (authorized) {
                    ButtonElement blockCertButton = new ButtonElement("Block All", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                                IFRAME_MAIN_DIV,
                                "vpBlockTsp",
                                FnvHash.getFNV1aToHex(state + tsp)
                            });
                    tspActionElement = blockCertButton.toString() + SPACE;
                }
                tspSect.setFoldedElement(tspActionElement + stringCut(tsp, 100) + HtmlUtil.countServices(tspCertList.size()));
                tspSect.setKeepFoldableElement(true);
                tspSect.setSectionHeadingClasses(new String[]{TABLE_UNFOLD_BOLD, TABLE_UNFOLD_BOLD});

                // Trust service nodes
                for (TslCertificates tc : tspCertList) {
                    //Delete action
                    String actionElement = "";
                    if (authorized) {
                        ButtonElement blockCertButton = new ButtonElement("Block", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                                    IFRAME_MAIN_DIV,
                                    "vpBlockCert",
                                    tc.getTslCertHash()
                                });
                        actionElement = blockCertButton.toString() + SPACE;
                    }

                    String servInfo = actionElement + stringCut(tc.getTsName(), 100)
                            + "  <i><b>(" + ExtractorUtil.stripRefUrl(tc.getTrustServiceType()) + ")</b></i>";
                    InfoTableSection tsSect = tspSect.getElements().addNewSection(tm, false);
                    tsSect.setFoldedElement(servInfo);
                    tsSect.setKeepFoldableElement(true);
                    tsSect.setSectionHeadingClasses(new String[]{TABLE_UNFOLD_BOLD, TABLE_UNFOLD_BOLD});
                    //display certificate
                    tsSect.setAjaxContentId(tc.getTslCertHash());
//                    tsSect.setElements(
//                            certInfo.getCertInfo(
//                            Base64Coder.decodeLines(tc.getTslCertificate())));
                }
            }
        }

        InfoTableFactory fact = new InfoTableFactory(tm, req.getSession());
    }

    public HtmlElement getExternalCertConfigTable(RequestModel req, boolean authorized) {
        //Basic Strtucture
        InfoTableModel tm = new InfoTableModel("extCertConfig");
        tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
        InfoTableElements externalCertElements = tm.getElements();

        CertificateInformation certInfo = new CertificateInformation(tm, req.getSession());

        //Policy configuration node
        if (authorized) {
            InfoTableSection certEntryNode = externalCertElements.addNewSection(tm, true);
            certEntryNode.setFoldedElement("New certificate entry");
            certEntryNode.setKeepFoldableElement(true);
            InfoTableElements certEntryElements = certEntryNode.getElements();
            buildCertEntryNode(tm, certEntryElements, req, certInfo, authorized);
        }

        //Compliant trust services node
        buildExternalCertsListNode(externalCertElements, tm, req, certInfo, authorized);

        InfoTableFactory fact = new InfoTableFactory(tm, req.getSession());
        return fact.getTable();
    }

    private void buildCertEntryNode(InfoTableModel tm, InfoTableElements certEntryElements,
            RequestModel req, CertificateInformation certInfo, boolean authorized) {

        SessionModel session = req.getSession();
        //None of these sections are foldable. Only elements inside these sections are allowed to be foldable
        InfoTableSection entrySect = certEntryElements.addNewSection(tm, "New Certificate", true);

        entrySect.addNewElement("Paste certificate (PEM formatted or raw Base64 encoded)");
        //Entry componentes
        HtmlElement certEntryText = new GenericHtmlElement("textarea");
        certEntryText.addAttribute("id", "certEntryInp");
        certEntryText.addAttribute("cols", "90");
        certEntryText.addAttribute("rows", "25");
        certEntryText.setText(session.getExtCertEntry());
        certEntryText.addAction(ONKEYUP, SEND_INPUT_FUNCTION, new String[]{
                    IFRAME_MAIN_DIV,
                    "externalCertEnter",
                    "certEntryInp"
                });
        certEntryText.addAction(ONCHANGE, SEND_INPUT_FUNCTION, new String[]{
                    IFRAME_MAIN_DIV,
                    "externalCertEnter",
                    "certEntryInp"
                });

        //Add Button
        ButtonElement addCertButton = new ButtonElement("Add Certificate", ONCLICK, SEND_INPUT_FUNCTION, new String[]{
                    IFRAME_MAIN_DIV,
                    "externalCertAdd",
                    "certEntryInp"
                });

        //Clear Button
        ButtonElement clearCertButton = new ButtonElement("Clear", ONCLICK, SEND_INPUT_FUNCTION, new String[]{
                    IFRAME_MAIN_DIV,
                    "externalCertClear",
                    "certEntryInp"
                });

        AaaCertificate cert;
        String pemCert;
        try {
            cert = CertificateUtils.getCertificate(session.getExtCertEntry());
            pemCert = CorePEM.getPemCert(cert.getEncoded());
            session.setExtCertEntry(pemCert);
            certEntryText.setText(pemCert);
        } catch (Exception ex) {
            cert = null;
        }
        String buttons = (cert != null)
                ? clearCertButton.toString() + SPACE + SPACE + addCertButton.toString()
                : clearCertButton.toString();

        entrySect.addNewElement(certEntryText.toString());
        entrySect.addNewElement(buttons);

        if (cert != null) {
            InfoTableSection certViewSect = certEntryElements.addNewSection(tm, "Certificate info", true);
            certViewSect.setElements(certInfo.getCertInfo(cert));
        }

    }

    private void buildExternalCertsListNode(InfoTableElements externalCertElements, InfoTableModel tm,
            RequestModel req, CertificateInformation certInfo, boolean authorized) {

        //Policy configuration node
        InfoTableSection certListNode = externalCertElements.addNewSection(tm, true);
        certListNode.setFoldedElement("External Certificate List");
        certListNode.setKeepFoldableElement(true);
        InfoTableElements certListElm = certListNode.getElements();
        //
        List<ExternalCert> externalCerts = policyDb.getExternalCerts();
        for (ExternalCert extCert : externalCerts) {
            AaaCertificate cert;
            try {
                cert = CertificateUtils.getCertificate(extCert.getB64Cert());
            } catch (Exception ex) {
                cert = null;
                externalCerts.remove(extCert);
                continue;
            }
            String certName = ASN1Util.getShortCertName(cert);
            String action = "";
            if (authorized) {
                //Action select box
                String selectorName = "externalCertDelete" + extCert.getCertificateId();
                SelectElement actions = new SelectElement(selectorName);
                actions.addOption("-Action-");
                actions.addOption("Remove");
                actions.addAction(ONCHANGE, EXECUTE_OPTION_FUNCTION, new String[]{
                            IFRAME_MAIN_DIV, //Load element for ajax response
                            selectorName, //Ajax request id
                            selectorName //Element holding the selected sort column
                        });
                action = SPACE + SPACE + actions.toString();
            }

            InfoTableSection certSect = certListElm.addNewSection(tm);
            certSect.setFoldedElement(action + certName);
            certSect.setKeepFoldableElement(true);
            certSect.setSectionHeadingClasses(new String[]{TABLE_UNFOLD_BOLD, TABLE_UNFOLD_BOLD});

            certSect.setElements(certInfo.getCertInfo(cert));
        }
    }

    /**
     * Creates a HtmlElement providing the UI for TSL Policy configuration
     * @param tslPolicy The TSL Policy being configured in the UI
     * @param req The HTTP request information
     * @param authorized true if the user is authorized to edit TSL policy configuration
     * @return HtmlElement holding the TSL policy config UI
     */
    public HtmlElement getTslPolicyInfoTable(TslPolicy tslPolicy, RequestModel req, boolean authorized) {
        InfoTableModel tm = new InfoTableModel("tp" + FnvHash.getFNV1aToHex(tslPolicy.getTslPolicyName()));
        tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
        InfoTableElements tslPolicyElements = tm.getElements();
        InfoTableSection infoSection = tslPolicyElements.addNewSection(tm, "Policy Info", true);

        getTslPolicyData(tslPolicy, tm, tslPolicyElements, authorized);

        InfoTableFactory fact = new InfoTableFactory(tm, req.getSession());
        return fact.getTable();
    }

    /*
     * Common methods
     */
    private void getTslPolicyData(TslPolicy tslPolicy, InfoTableModel tm, InfoTableElements tslElements, boolean authorized) {
        // update policy parameters if they have changed.
        policyUtils.recachePolicyParameters();
        InfoTableSection stateSect = tslElements.addNewSection(tm, "States", true);
        InfoTableSection typeSect = tslElements.addNewSection(tm, "Service Types", true);
        InfoTableSection statusSect = tslElements.addNewSection(tm, "Service Status", true);
        InfoTableSection signSect = tslElements.addNewSection(tm, "Sign Status", true);
        InfoTableSection expireSect = tslElements.addNewSection(tm, "Expired TSL", true);

        //States element
        List<String> tpSstates = tslPolicy.getStates();
        addElementList(stateSect, getPolicyElementList(tpSstates, policyUtils.getTslStateVals(), authorized, "policySelectState", true));
        //Service type element
        List<String> tpTypes = tslPolicy.getServiceTypes();
        addElementList(typeSect, getPolicyElementList(tpTypes, policyUtils.getTslTypeVals(), authorized, "policySelectType", false));
        //Service status element
        List<String> tpStatus = tslPolicy.getStatusTypes();
        addElementList(statusSect, getPolicyElementList(tpStatus, policyUtils.getTslStatusVals(), authorized, "policySelectStatus", false));
        //Signstatus element
        List<String> tpSign = tslPolicy.getSignStatus();
        addElementList(signSect, getPolicyElementList(tpSign, policyUtils.getTslSignatureVals(), authorized, "policySelectSign", false));
        //Expired TSL grace period
        int expiredTslGrace = tslPolicy.getExpiredTslGrace();
        // Create Input element
        TextInputElement expTslGraceInp = new TextInputElement("expTslGraceInp");
        expTslGraceInp.addAttribute("size", "4");
        expTslGraceInp.addAttribute("value", String.valueOf(expiredTslGrace));

        // AddButton
        ButtonElement updateButton = new ButtonElement("Update", ONCLICK, SEND_INPUT_FUNCTION, new String[]{
                    IFRAME_MAIN_DIV,
                    "policySelectExpTslInp",
                    "expTslGraceInp"
                });

        // Checkbox
        CheckboxElement cbe = new CheckboxElement("expTslGraceChb", "",
                ONCHANGE, LOAD_DATA_FUNCTION, new String[]{
                    IFRAME_MAIN_DIV,
                    "policySelectExpTslChb",
                    "null"
                },
                expiredTslGrace == -1);

        if (authorized) {
            expireSect.addNewElement(cbe.toString() + SPACE + "Ignore TSL Expiry date");
        } else {
            if (expiredTslGrace == -1) {
                expireSect.addNewElement("Ignore TSL Expiry date");
            }
        }
        if (expiredTslGrace > -1) {
            String expString = (authorized) ? expTslGraceInp.toString() : String.valueOf(expiredTslGrace);
            String button = (authorized) ? SPACE + SPACE + updateButton.toString() : "";
            expireSect.addNewElement("Accept TSL data " + expString + " days after TSL expity" + button);
        }



    }

    private List<String> getPolicyElementList(List<String> tpSelected, List<String> vals, boolean authorized, String selectorPrefix, boolean allSelect) {
        List<String> elementList = new ArrayList<String>();

        //Add selected values that are not within the list of available values
        //Important in order to not destroy policies in case TSLs hodlong a special value is unavailable
        for (String selVal : tpSelected) {
            if (!vals.contains(selVal)) {
                vals.add(selVal);
            }
        }
        boolean justFirst = allSelect && tpSelected.contains(vals.get(0));

        // If user is an authorized Policy Admin, then provide checkboxes to select or deselect policy elements
        if (authorized) {
            for (int i = 0; i < vals.size(); i++) {
                String val = vals.get(i);
                CheckboxElement cbe = new CheckboxElement(selectorPrefix + String.valueOf(i), "",
                        ONCHANGE, LOAD_DATA_FUNCTION, new String[]{
                            IFRAME_MAIN_DIV,
                            selectorPrefix,
                            String.valueOf(i)
                        },
                        tpSelected.contains(val));

                if (i == 0 || !justFirst) {
                    elementList.add(cbe.toString() + SPACE + val);
                }
            }
        } else {
            //If not Policy admin, then just show selected values
            if (justFirst) {
                elementList.add(vals.get(0));
            } else {
                for (String elm : tpSelected) {
                    elementList.add(elm);
                }
            }
        }
        return elementList;
    }

    private void addElementList(InfoTableSection itSection, List<String> elementList) {
        for (String elm : elementList) {
            itSection.addNewElement(elm);
        }
    }

    private String stringCut(String str, int maxLen) {
        String cutString;
        if (str.length() > maxLen) {
            return str.substring(0, maxLen - 3) + "...";
        } else {
            return str;
        }
    }
}
