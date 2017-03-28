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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElement;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.content.CertificateInformation;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.etsi.uri.x02231.v2.InternationalNamesType;
import org.etsi.uri.x02231.v2.MultiLangNormStringType;
import org.etsi.uri.x02231.v2.NonEmptyURIListType;
import org.etsi.uri.x02231.v2.PostalAddressType;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceProvider;
import se.tillvaxtverket.tsltrust.common.tsl.TslUtils;

/**
 * Utility class for html tables created for TSL related data using the info table model
 */
public class InfoTableUtils implements HtmlConstants {

    private InfoTableModel tm;
    private SessionModel session;
    private String[] attrClass;
    private String[] addInfoFirst = new String[]{ATTRIBUTE_VALUE, PROPERTY_NAME};
    private String[] addInfoLast = new String[]{EXTESION_PADDING, PROPERTY_NAME};
    private CertificateInformation certInfo;

    public InfoTableUtils(InfoTableModel tm, SessionModel session) {
        this.tm = tm;
        this.session = session;
        certInfo = new CertificateInformation(tm, session);
    }

    public String[] getAttrClass() {
        return attrClass;
    }

    public void setAttrClass(String[] attrClass) {
        this.attrClass = attrClass;
    }

    public void addPostalAddress(InfoTableElements parentElements, PostalAddressType pa) {
        if (pa == null) {
            return;
        }
        InfoTableSection posta = parentElements.addNewSection(tm, "Postal Address");
        posta.addNewElement(new String[]{"Street Address", pa.getStreetAddress()}, attrClass);
        posta.addNewElement(new String[]{"Postal Code", pa.getPostalCode() + " " + pa.getLocality()}, attrClass);
        posta.addNewElement(new String[]{"Country", pa.getCountryName()}, attrClass);
        posta.setFoldedElement(getShortPostalAddress(pa));
    }

    private String getShortPostalAddress(PostalAddressType pa) {
        StringBuilder b = new StringBuilder();
        if (pa != null) {
            b.append(pa.getStreetAddress()).append(", ");
            b.append(pa.getLocality()).append(", ");
            b.append(pa.getCountryName());
        }
        return b.toString();
    }

    public String getShortElectronicAddress(List<String> eAddresses) {
        StringBuilder b = new StringBuilder();

        b = new StringBuilder();
        String httpLink = "";
        String mailLink = "";
        String link;
        for (String eadr : eAddresses) {
            eadr = eadr.trim();
            link = HtmlUtil.link(eadr);
            try {
                if (eadr.startsWith("http")) {
                    httpLink = HtmlUtil.link(eadr);
                }
                if (eadr.startsWith("mailto:")) {
                    link = HtmlUtil.link(eadr, eadr.substring(7));
                    mailLink = link;
                }
            } catch (Exception ex) {
            }
            b.append(link).append(", ");
        }
        b.deleteCharAt(b.lastIndexOf(","));
        if (mailLink.length() > 1) {
            return mailLink;
        }
        if (httpLink.length() > 1) {
            return httpLink;
        }
        return b.toString();
    }

    public void addElectronicAddress(InfoTableElements parentElements, List<String> eAddresses, boolean unfold) {
        StringBuilder b = new StringBuilder();

        InfoTableSection elAdr = parentElements.addNewSection(tm, "Electronic Address", unfold);
        b = new StringBuilder();
        String httpLink = "";
        String mailLink = "";
        String link;
        for (String eadr : eAddresses) {
            eadr = eadr.trim();
            link = HtmlUtil.link(eadr);
            try {
                if (eadr.startsWith("http")) {
                    httpLink = HtmlUtil.link(eadr);
                }
                if (eadr.startsWith("mailto:")) {
                    link = HtmlUtil.link(eadr, eadr.substring(7));
                    mailLink = link;
                }
            } catch (Exception ex) {
            }
            elAdr.addNewElement(link);
            b.append(link).append(", ");
        }
        b.deleteCharAt(b.lastIndexOf(","));
        if (eAddresses.size() > 1) {
            if (mailLink.length() > 1) {
                elAdr.setFoldedElement(mailLink);
                return;
            }
            if (httpLink.length() > 1) {
                elAdr.setFoldedElement(httpLink);
                return;
            }
            elAdr.setFoldedElement((b.toString()).trim());
        }
    }

    public void addInformationUri(InfoTableElements parentElements, List<String> infoUris) {
        InfoTableSection uris = parentElements.addNewSection(tm, "Information URI");
        for (String siURI : infoUris) {
            uris.addNewElement(HtmlUtil.link(siURI.trim()));
        }
        if (infoUris.size() > 1) {
            uris.setFoldedElement(HtmlUtil.link(infoUris.get(0)));
        }
    }

    public String getFirsListItem(List<String> stringList) {
        try {
            return stringList.get(0);
        } catch (Exception ex) {
            return "";
        }
    }

    public InfoTableElement getTSPInfoHeading(TrustServiceProvider tsp) {
        String tn = tsp.getTradeName();
        String name;
        if (tn == null || tn.length() == 0) {
            name = HtmlUtil.link(getFirsListItem(tsp.getInformationUris()));
        } else {
            name = tn;
        }
        return new InfoTableElement(name);
    }

    public InfoTableElement getTSPFoldHeading(TrustServiceProvider tsp) {
        String name;
        try {
            name = tsp.getName();
        } catch (Exception ex) {
            name = "No name";
        }
        InfoTableElement foldElement;
        try {
            foldElement = new InfoTableElement(new String[]{
                        name, HtmlUtil.link(getFirsListItem(tsp.getInformationUris()))},
                    new String[]{
                        EXTENSION_NAME});
        } catch (Exception ex) {
            foldElement = new InfoTableElement(tsp.getName());
        }
        return foldElement;
    }

    public void addCertificate(InfoTableElements parentElements, iaik.x509.X509Certificate cert, String heading, boolean unfold) {
        try {
            addCertificate(parentElements, cert.getEncoded(), heading, unfold);
        } catch (CertificateEncodingException ex) {
            addCertificate(parentElements, new byte[]{}, heading, unfold);
        }
    }

    public void addCertificate(InfoTableElements parentElements, byte[] cert, String heading, boolean unfold) {
        InfoTableSection sdiSect = parentElements.addNewSection(tm, heading, unfold);
        sdiSect.setFoldedElement(ASN1Util.getShortCertName(cert));
        sdiSect.setKeepFoldableElement(true);
        sdiSect.setSectionHeadingClasses(new String[]{TABLE_SECTION_HEAD, TABLE_SECTION_HEAD});
        sdiSect.setElements(certInfo.getCertInfo(cert));
    }

    public void addOtherTslPointerInfo(InfoTableElements parentElements,
            String url, List<Object[]> addInfoList, List<iaik.x509.X509Certificate> certList) {

        if (url == null || url.length() == 0) {
            return;
        }

        InfoTableSection section = parentElements.addNewSection(tm);

        // Get heading
        Map<String, List<String>> addInfoMap = getAdditionalInfoMap(addInfoList);
        StringBuilder b = new StringBuilder();
        if (addInfoMap.containsKey("Scheme Territory")) {
            List<String> terrList = addInfoMap.get("Scheme Territory");
            if (!terrList.isEmpty()) {
                b.append("<b>").append(terrList.get(0)).append("</b>");
            }
        }
        if (addInfoMap.containsKey("Content Type")){
            String ct = addInfoMap.get("Content Type").get(0);
            String shortCt = (ct.startsWith("Machine"))?" (MR)":" (HR)";
            b.append(shortCt);
        }
        b.append(" - ");
        String sopn = "";
        if (addInfoMap.containsKey("Scheme Operator Name")) {
            List<String> scopList = addInfoMap.get("Scheme Operator Name");
            if (!scopList.isEmpty()) {
                sopn = scopList.get(0);
            }
        }
        b.append((sopn.length() == 0) ? url : sopn);

        section.setFoldedElement(new InfoTableElement(new String[]{b.toString(), HtmlUtil.link(url)}));
        section.setKeepFoldableElement(true);
        section.setKeepFirstFoldableCell(true);
        InfoTableElements tslPtrElm = section.getElements();
        tslPtrElm.addNewSection(tm, "Url").addNewElement(HtmlUtil.link(url));
        InfoTableSection addInfoSect = tslPtrElm.addNewSection(tm, "Information");
        Set<String> keySet = addInfoMap.keySet();
        for (String key : keySet) {
            List<String> valueList = addInfoMap.get(key);
            for (int i = 0; i < valueList.size(); i++) {
                if (i == 0 && valueList.size() > 1) {
                    addInfoSect.addNewElement(new String[]{key, valueList.get(i)}, addInfoFirst);
                }
                if (i == 0 && valueList.size() == 1) {
                    addInfoSect.addNewElement(new String[]{key, valueList.get(i)}, addInfoLast);
                }
                if (i > 0 && (i < valueList.size() - 1)) {
                    addInfoSect.addNewElement(new String[]{SPACE, valueList.get(i)}, addInfoFirst);
                }
                if (i > 0 && (i == valueList.size() - 1)) {
                    addInfoSect.addNewElement(new String[]{SPACE, valueList.get(i)}, addInfoLast);
                }
            }
        }
        for (iaik.x509.X509Certificate cert : certList) {
            addCertificate(tslPtrElm, cert, "Certificate", true);
        }

    }

    private Map<String, List<String>> getAdditionalInfoMap(List<Object[]> addInfoList) {
        Map<String, List<String>> addInfoMap = new HashMap<String, List<String>>();
        for (Object[] o : addInfoList) {
            String name = (String) o[0];
            Object value = o[1];
            List<String> strValList = new ArrayList<String>();
            try {
                if (value instanceof String) {
                    strValList.add((String) value);
                }
                if (value instanceof NonEmptyURIListType) {
                    NonEmptyURIListType uriList = (NonEmptyURIListType) value;
                    for (String uri : uriList.getURIArray()) {
                        strValList.add(uri);
                    }
                }

                if (value instanceof InternationalNamesType) {
                    InternationalNamesType in = (InternationalNamesType) value;
                    MultiLangNormStringType[] mlNameList = in.getNameArray();
                    String engName = TslUtils.getLocalisedNormString(mlNameList, Locale.ENGLISH);
                    strValList.add(engName);
                }
            } catch (Exception ex) {
            }
            addInfoMap.put(name, strValList);
        }
        return addInfoMap;
    }
}
