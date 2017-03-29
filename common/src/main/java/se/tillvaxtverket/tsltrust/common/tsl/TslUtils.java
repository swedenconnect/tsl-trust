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
package se.tillvaxtverket.tsltrust.common.tsl;

import iaik.x509.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.etsi.uri.trstSvc.svcInfoExt.eSigDir199993ECTrustedList.QualificationsDocument;
import org.etsi.uri.x02231.v2.AdditionalServiceInformationDocument;
import org.etsi.uri.x02231.v2.DigitalIdentityListType;
import org.etsi.uri.x02231.v2.DigitalIdentityType;
import org.etsi.uri.x02231.v2.ExpiredCertsRevocationInfoDocument;
import org.etsi.uri.x02231.v2.ExtensionType;
import org.etsi.uri.x02231.v2.MultiLangNormStringType;
import org.etsi.uri.x02231.v2.MultiLangStringType;
import org.etsi.uri.x02231.v2.additionaltypes.TakenOverByDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tillvaxtverket.tsltrust.common.tsl.sie.AdditionalServiceInformationSie;
import se.tillvaxtverket.tsltrust.common.tsl.sie.ExpiredCertsRevocationInfoSie;
import se.tillvaxtverket.tsltrust.common.tsl.sie.QualificatioinsSie;
import se.tillvaxtverket.tsltrust.common.tsl.sie.ServiceInfoExtension;
import se.tillvaxtverket.tsltrust.common.tsl.sie.SieType;
import se.tillvaxtverket.tsltrust.common.tsl.sie.TakenOverBySie;
import se.tillvaxtverket.tsltrust.common.tsl.sie.UnknownSie;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;

/**
 * TSL parsing utilities
 */
public final class TslUtils {

    /**
     * Get the target value matching the selected language
     * @param mlStringList list of strings in different languages
     * @param locale the target language locale
     * @return target value in matching language if exists, else the English language value
     */
    public static String getLocalisedNormString(MultiLangNormStringType[] mlStringList, Locale locale) {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        String val = "", defaultVal = "";
        for (MultiLangNormStringType mls : mlStringList) {
            if (mls.getLang().equalsIgnoreCase(locale.getLanguage())) {
                val = mls.getStringValue();
            }
            if (mls.getLang().toLowerCase(locale).startsWith(Locale.ENGLISH.getLanguage())) {
                defaultVal = mls.getStringValue();
            }
        }
        val = (val.length() == 0) ? defaultVal : val;
        return val;
    }

    /**
     * Get the target value matching the selected language
     * @param mlStringList list of strings in different languages
     * @param locale the target language locale
     * @return target value in matching language if exists, else the English language value
     */
    public static String getLocalisedString(MultiLangStringType[] mlStringList, Locale locale) {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        String val = "", defaultVal = "";
        for (MultiLangStringType mls : mlStringList) {
            if (mls.getLang().equalsIgnoreCase(locale.getLanguage())) {
                val = mls.getStringValue();
            }
            if (mls.getLang().toLowerCase(locale).startsWith(Locale.ENGLISH.getLanguage())) {
                defaultVal = mls.getStringValue();
            }
        }
        val = (val.length() == 0) ? defaultVal : val;
        return val;
    }

    public static List<byte[]> getDigitalIdentityties(DigitalIdentityListType sdi) {
        List<byte[]> certDataList = new ArrayList<byte[]>();
        DigitalIdentityType[] sdiList = null;
        try {
            sdiList = sdi.getDigitalIdArray();
        } catch (Exception ex) {
        }
        for (DigitalIdentityType di : sdiList) {
            try {
                byte[] certBytes = di.getX509Certificate();
                if (certBytes != null) {
                    certDataList.add(certBytes);
                }
            } catch (Exception ex) {
            }
        }
        return certDataList;
    }

    public static X509Certificate getServiceDigitalIdentityCert(byte[] certData) {
        if (certData == null) {
            return null;
        }
        return CertificateUtils.getCertificate(certData);
    }

    static List<ServiceInfoExtension> getExtensions(ExtensionType[] extensionArray) {
        List<ServiceInfoExtension> extensionList = new ArrayList<ServiceInfoExtension>();
        for (ExtensionType sie : extensionArray) {
            boolean critical = sie.getCritical();
            Node domNode = sie.getDomNode();
            NodeList childNodes = domNode.getChildNodes();
            int length = childNodes.getLength();
            for (int i = 0; i < length; i++) {
                Node node = childNodes.item(i);
                String nodeName = node.getLocalName();
                if (nodeName != null) {
                    SieType sieType = null;
                    try {
                        sieType = SieType.valueOf(nodeName);
                    } catch (Exception ex) {
                        sieType = SieType.UNKNOWN;
                    }
                    extensionList.add(getSie(sieType, node, critical));
                }
            }
        }
        return extensionList;
    }

    private static ServiceInfoExtension getSie(SieType sieType, Node node, boolean critical) {
        try {
            switch (sieType) {
                case AdditionalServiceInformation:
                    AdditionalServiceInformationDocument asi = AdditionalServiceInformationDocument.Factory.parse(node);
                    return new AdditionalServiceInformationSie(asi.getAdditionalServiceInformation(), node, critical);
                case Qualifications:
                    QualificationsDocument qf = QualificationsDocument.Factory.parse(node);
                    return new QualificatioinsSie(qf.getQualifications(), node, critical);
                case TakenOverBy:
                    TakenOverByDocument to = TakenOverByDocument.Factory.parse(node);
                    return new TakenOverBySie(to.getTakenOverBy(), node, critical);
                case ExpiredCertsRevocationInfo:
                    ExpiredCertsRevocationInfoDocument exp = ExpiredCertsRevocationInfoDocument.Factory.parse(node);
                    return new ExpiredCertsRevocationInfoSie(exp, node, critical);
            }
        } catch (Exception ex) {
        }
        return new UnknownSie(node);
    }
}
