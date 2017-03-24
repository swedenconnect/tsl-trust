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
package se.tillvaxtverket.tsltrust.weblogic.content.ts;

import java.util.Date;
import java.util.List;
import se.tillvaxtverket.tsltrust.common.tsl.sie.QualificationsCriteria;
import se.tillvaxtverket.tsltrust.common.tsl.sie.QualificationsCriteria.PolicyOid;
import se.tillvaxtverket.tsltrust.common.tsl.sie.QualificationsElement;
import se.tillvaxtverket.tsltrust.common.tsl.sie.ServiceInfoExtension;
import se.tillvaxtverket.tsltrust.common.tsl.sie.SieType;
import se.tillvaxtverket.tsltrust.weblogic.content.*;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.tsl.ServiceHistoryInstance;
import se.tillvaxtverket.tsltrust.common.tsl.sie.AdditionalServiceInformationSie;
import se.tillvaxtverket.tsltrust.common.tsl.sie.ExpiredCertsRevocationInfoSie;
import se.tillvaxtverket.tsltrust.common.tsl.sie.QualificatioinsSie;
import se.tillvaxtverket.tsltrust.common.tsl.sie.TakenOverBySie;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElement;
import se.tillvaxtverket.tsltrust.weblogic.utils.ExtractorUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.HtmlUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.InfoTableUtils;

/**
 * Providing UI elements for displaying historical information
 */
public class ServiceExtensioininfo implements HtmlConstants, TTConstants {

    private static final String[] ATTR = new String[]{ATTRIBUTE_NAME, ATTRIBUTE_VALUE};
    private static final String[] PROP = new String[]{PROPERTY_NAME, PROPERTY_VALUE};
    private static final String[] CERT_INFO = new String[]{INVERTED_HEAD, TABLE_SECTION_HEAD};
    private static final String[] EXT_ATTR = new String[]{EXTENSION_NAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE};
    private static final String[] EXT_PROP = new String[]{EXTENSION_NAME, PROPERTY_NAME, PROPERTY_VALUE};
    private static final Logger LOG = Logger.getLogger(ServiceExtensioininfo.class.getName());

    private ServiceExtensioininfo() {
    }

    public static void addExtensionInfo(ServiceInfoExtension sie, InfoTableSection extensionSect, InfoTableModel tm) {

        //get fold element text

        extensionSect.setFoldedElement(sie.getInfo());
        extensionSect.setKeepFoldableElement(true);
        extensionSect.setSectionHeadingClasses(new String[]{TABLE_SECTION_HEAD, TABLE_SECTION_HEAD});
        InfoTableElements elements = extensionSect.getElements();
        addSingleObject(elements, tm, "Critical", sie.isCritical() ? "true" : "false");
        //add Data
        SieType extType = sie.getType();
        switch (extType) {
            case AdditionalServiceInformation:
                AdditionalServiceInformationSie aSie = (AdditionalServiceInformationSie) sie;
                String uRI = aSie.getURI();
                if (uRI != null && uRI.length() > 0) {
                    extensionSect.setFoldedElement(new InfoTableElement(new String[]{
                                sie.getInfo(), ExtractorUtil.stripRefUrl(uRI)}, new String[]{
                                "", PROPERTY_NAME}));
                    extensionSect.setKeepFirstFoldableCell(true);
                }
                addAdditionalServices(elements, tm, aSie);
                break;
            case ExpiredCertsRevocationInfo:
                ExpiredCertsRevocationInfoSie exp = (ExpiredCertsRevocationInfoSie) sie;
                Date revDate = exp.getExpiredCertsRevDate();
                if (revDate != null) {
                    extensionSect.setFoldedElement(new InfoTableElement(new String[]{
                                sie.getInfo(), TIME_FORMAT.format(revDate)}, new String[]{
                                "", PROPERTY_NAME}));
                    extensionSect.setKeepFirstFoldableCell(true);
                }
                addSingleObject(elements, tm, "Earliest date", exp.getExpiredCertsRevDate());
                break;
            case Qualifications:
                QualificatioinsSie qf = (QualificatioinsSie) sie;
                addQualifications(elements, tm, qf);
                break;
            case TakenOverBy:
                TakenOverBySie to = (TakenOverBySie) sie;
                addTakenOverBy(elements, tm, to);
                break;
            case UNKNOWN:
                addSingleObject(elements, tm, "Type", "This extension type is unknown. Consult XML data for further information");
                break;
        }
    }

    private static void addSingleObject(InfoTableElements elm, InfoTableModel tm, String label, Object o) {
        addSingleObject(elm, tm, label, o, false);
    }

    private static void addSingleObject(InfoTableElements elm, InfoTableModel tm, String label, Object o, boolean strip) {
        if (o == null) {
            return;
        }
        if (o instanceof String) {
            String str = (String) o;
            if (str.length() == 0) {
                return;
            }
        }
        if (strip) {
            elm.addNewSection(tm, label).addNewElement(ExtractorUtil.stripRefUrl((String) o));
        } else {
            elm.addNewSection(tm, label).addNewElement(o);
        }
    }

    private static void addAdditionalServices(InfoTableElements elm, InfoTableModel tm, AdditionalServiceInformationSie aSie) {
        addSingleObject(elm, tm, "Information", aSie.getInformationValue());
        addSingleObject(elm, tm, "Service", aSie.getURI(), true);
    }

    private static void addQualifications(InfoTableElements elm, InfoTableModel tm, QualificatioinsSie qf) {
        List<QualificationsElement> qualificationsList = qf.getQualificationsList();
        if (!qualificationsList.isEmpty()) {
            if (qualificationsList.size() == 1) {
                addQualification(elm, tm, qualificationsList.get(0));
                return;
            }
            for (QualificationsElement qfe : qualificationsList) {
                InfoTableSection qfSect = elm.addNewSection(tm, qf.getName(), true);
                qfSect.setFoldedElement(qf.getInfo());
                qfSect.setKeepFoldableElement(true);
                InfoTableElements qfElm = qfSect.getElements();
                addQualification(qfElm, tm, qfe);
            }
        }
    }

    private static void addQualification(InfoTableElements elm, InfoTableModel tm, QualificationsElement qfe) {
        List<String> qualifierUriList = qfe.getQualifierUriList();
        if (!qualifierUriList.isEmpty()) {
            InfoTableSection qualifierSect = elm.addNewSection(tm, "Qualifiers");
            for (String qualifier : qualifierUriList) {
                qualifierSect.addNewElement(ExtractorUtil.stripRefUrl(qualifier));
            }
            QualificationsCriteria criteria = qfe.getCriteria();
            addSingleObject(elm, tm, "Assert", criteria.getAssStr());
            addSingleObject(elm, tm, "Description", criteria.getDescription());

            List<List<String>> keyUsages = criteria.getKeyUsages();
            for (List<String> kuList : keyUsages) {
                if (!kuList.isEmpty()) {
                    InfoTableSection kuSect = elm.addNewSection(tm, "Key Usages");
                    for (String ku : kuList) {
                        kuSect.addNewElement(ku);
                    }
                }
            }

            List<List<PolicyOid>> policyList = criteria.getPolicyList();
            for (List<PolicyOid> oidList : policyList) {
                InfoTableSection policySect = elm.addNewSection(tm, "Policies");
                for (PolicyOid oid : oidList) {
                    String oidDescription = oid.getOidDescription();
                    if (oidDescription != null && oidDescription.length() > 0) {
                        policySect.addNewElement(new String[]{"Description", oidDescription}, PROP);
                    }
                    String identifierUri = oid.getIdentifierUri();
                    if (identifierUri != null && identifierUri.length() > 0) {
                        policySect.addNewElement(new String[]{"Object Identifier", identifierUri}, ATTR);
                    }
                    List<String> documentationRef = oid.getDocumentationRef();                    
                    for (String ref : documentationRef) {
                        policySect.addNewElement(new String[]{"Reference", ref}, ATTR);
                    }
                }
            }
        }
    }

    private static void addTakenOverBy(InfoTableElements elm, InfoTableModel tm, TakenOverBySie to) {
        addSingleObject(elm, tm, "Info URI", HtmlUtil.link(to.getURI()));
        addSingleObject(elm, tm, "Scheme Operator", to.getSchemeOperatorName());
        addSingleObject(elm, tm, "Territory", to.getSchemeTerritory());
        addSingleObject(elm, tm, "TSP Name", to.getTspName());
    }
}
