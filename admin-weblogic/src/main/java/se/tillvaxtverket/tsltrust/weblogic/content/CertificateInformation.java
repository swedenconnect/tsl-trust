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
import com.aaasec.lib.aaacert.algo.PublicKeyData;
import com.aaasec.lib.aaacert.data.SubjectAttributeInfo;
import com.aaasec.lib.aaacert.display.DisplayCert;
import com.aaasec.lib.aaacert.enums.OidName;
import com.aaasec.lib.aaacert.enums.SupportedExtension;
import com.aaasec.lib.aaacert.extension.ExtensionInfo;
import com.aaasec.lib.aaacert.extension.QCStatementsExt;
import com.aaasec.lib.aaacert.utils.CertUtils;
import java.io.IOException;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElement;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.ASN1Util;
import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.core.PEM;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;

/**
 * Providing UI elements for displaying certificate information
 */
public class CertificateInformation implements HtmlConstants, TTConstants {

    private InfoTableModel tm;
    private SessionModel session;
    private InfoTableElements certElements;
    private ExtensionAttr extFact;
    private static final String[] ATTR = new String[]{ATTRIBUTE_NAME, ATTRIBUTE_VALUE};
    private static final String[] PROP = new String[]{PROPERTY_NAME, PROPERTY_VALUE};
    private static final String[] CERT_INFO = new String[]{INVERTED_HEAD, TABLE_SECTION_HEAD};
    private static final String[] EXT_ATTR = new String[]{EXTENSION_NAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE};
    private static final String[] EXT_PROP = new String[]{EXTENSION_NAME, PROPERTY_NAME, PROPERTY_VALUE};
    private static final Logger LOG = Logger.getLogger(CertificateInformation.class.getName());

    public CertificateInformation(InfoTableModel tableModel, SessionModel session) {
        this.tm = tableModel;
        this.session = session;
        extFact = new ExtensionAttr(EXT_PROP);
    }

    public InfoTableElements getCertInfo(byte[] certBytes) {
        AaaCertificate cert;
        try {
            cert = new AaaCertificate(certBytes);
            return getCertInfo(cert);
        } catch (CertificateException ex) {
            Logger.getLogger(CertificateInformation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CertificateInformation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public InfoTableElements getCertInfo(AaaCertificate cert) {
        certElements = new InfoTableElements();
        //Make sure there is a certificate to parse
        if (cert == null) {
            certElements.add(new InfoTableElement("No valid certificate information"));
            return certElements;
        }

        //Add Subject Name
        addDistinguishedNameAttributes(cert.getSubjectX500Principal(), "Subject Name", true);

        //Add Validity period
        Date notBefore = cert.getNotBefore();
        Date notAfter = cert.getNotAfter();
        addValidityPeriod(notBefore, notAfter, false);

        //Add Issuer Name
        addDistinguishedNameAttributes(cert.getIssuerX500Principal(), "Issuer Name", false);

        //Add PublicKeyInfo
        addPublicKeyInfo(cert, false);

        //Add ExtensionInfo
        addCertificateExtensionInfo(cert, true);
        addTechnicalInfoTextFrame(cert, false);
        addPemInfo(cert, false);
        addASN1Inspector(cert);

        return certElements;
    }

    public void addValidityPeriod(Date notBefore, Date notAfter, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "Validity", unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        section.setFoldedElement(TIME_FORMAT.format(notAfter));
        section.addNewElement(aStr("Not Before", TIME_FORMAT.format(notBefore)), ATTR);
        section.addNewElement(aStr("Not After", TIME_FORMAT.format(notAfter)), ATTR);
    }

    public void addDistinguishedNameAttributes(X500Principal dn, String nameType, boolean unfold) {
        //Signer DN Attributes
        InfoTableSection section = certElements.addNewSection(tm, nameType, unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        section.setFoldedElement(ASN1Util.getShortName(dn));

        List<SubjectAttributeInfo> attributeInfoList = CertUtils.getAttributeInfoList(dn);
        for (SubjectAttributeInfo attrInfo:attributeInfoList){
            String type = attrInfo.getDispName();
            String value = attrInfo.getValue();
            section.addNewElement(aStr(type, value), ATTR);
            
        }        
    }

    private void addPublicKeyInfo(AaaCertificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "Public Key", unfold);
        section.setSectionHeadingClasses(CERT_INFO);

        PublicKey publicKey = cert.getPublicKey();
        PublicKeyData pkData = new PublicKeyData(publicKey);
        
        
        String algorithm = pkData.getAlgorithm();
        int keyBits = pkData.getKeySize();
        String oidStr = pkData.getAlgorithmOid().getId();
        String oidName = pkData.getPkType().getName();

        String fold = oidName;
        section.addNewElement(aStr("Algorithm", oidName), ATTR);
        if (keyBits > 0) {
            fold += " (" + String.valueOf(keyBits) + ")";
            section.addNewElement(aStr("Key size", String.valueOf(keyBits)), ATTR);
        }
        section.setFoldedElement(fold);
    }

    private void addCertificateExtensionInfo(AaaCertificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "Extensions", unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        InfoTableElements extElements = section.getElements();
        extFact.clear();
        
        List<ExtensionInfo> extList = cert.getExtensionInfoList();
        if (extList==null){
            return;
        }


        section.setFoldedElement("Extension summary (out of " + String.valueOf(extList.size()) + " total Extensions)");
        section.setKeepFoldableElement(true);

        for (ExtensionInfo rawExt : extList) {
            //Basic Constraints
            if (rawExt.getExtensionType().equals(SupportedExtension.basicConstraints)) {
                BasicConstraints bc = BasicConstraints.getInstance(rawExt.getExtDataASN1());
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                extFact.add("cA", String.valueOf(bc.isCA()));
                extFact.addExtension(extElements);
            }
            //Key Usage
            if (rawExt.getExtensionType().equals(SupportedExtension.keyUsage)) {
                KeyUsage ku = KeyUsage.getInstance(rawExt.getExtDataASN1());
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                extFact.add("Usage", DisplayCert.getKeyUsageText(ku));
                extFact.addExtension(extElements);
            }

            //QcStatements
            if (rawExt.getExtensionType().equals(SupportedExtension.qCStatements)) {
                QCStatementsExt qc = QCStatementsExt.getInstance(rawExt.getExtDataASN1());
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                if (qc.isQcCompliance()){
                    extFact.add("Qualified", "true");
                }
                if (qc.isQcSscd()){
                    extFact.add("QSSCD", "true");
                }
                extFact.addExtension(extElements);
            }

//            //EKU
            if (rawExt.getExtensionType().equals(SupportedExtension.extendedKeyUsage)) {
                ExtendedKeyUsage eku = ExtendedKeyUsage.getInstance(rawExt.getExtDataASN1());
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                KeyPurposeId[] keyPurposeIDs = eku.getUsages();
                for (KeyPurposeId oid : keyPurposeIDs) {
                    extFact.add(OidName.getName(oid.getId()), oid.getId());
                }
                extFact.addExtension(extElements);
            }

//            //CertificatePolicies
            if (rawExt.getExtensionType().equals(SupportedExtension.certificatePolicies)) {
                CertificatePolicies cp = CertificatePolicies.getInstance(rawExt.getExtDataASN1());
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                PolicyInformation[] policyInformation = cp.getPolicyInformation();
                for (PolicyInformation pi : policyInformation) {
                    ASN1ObjectIdentifier oid = pi.getPolicyIdentifier();
                    extFact.add("Policy", OidName.getName(oid.getId()));
                }
                extFact.addExtension(extElements);
            }

//            //SubjectAlterantive Name
//            /**
//             *    GeneralName ::= CHOICE {
//             *    otherName                       [0]     OtherName,
//             *    rfc822Name                      [1]     IA5String,
//             *    dNSName                         [2]     IA5String,
//             *    x400Address                     [3]     ORAddress,
//             *    directoryName                   [4]     Name,
//             *    ediPartyName                    [5]     EDIPartyName,
//             *    uniformResourceIdentifier       [6]     IA5String,
//             *    iPAddress                       [7]     OCTET STRING,
//             *    registeredID                    [8]     OBJECT IDENTIFIER }
//             */
            if (rawExt.getExtensionType().equals(SupportedExtension.subjectAlternativeName)) {
                GeneralNames san = GeneralNames.getInstance(rawExt.getExtDataASN1());
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                String[] nameType = new String[]{"otherName", "rfc822Name", "dNSName", "x400Address", "directoryName", "ediPartyName", "uniformResourceIdentifier", "iPAddress", "registeredID"};
                GeneralName[] generalNames = san.getNames();
                for (GeneralName name:generalNames){
                    int type = name.getTagNo();
                    if (type == 1 || type == 2 || type == 6 || type == 7) {
                        extFact.add(nameType[type], name.getName().toString());
                    }                    
                }
                extFact.addExtension(extElements);
            }
        }
    }

    private void addTechnicalInfoTextFrame(AaaCertificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "Technical Info", unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        section.setFoldedElement("Certificate details information");
        
//        HtmlElement certificateDetails = new GenericHtmlElement("textarea");
//        certificateDetails.addAttribute("readonly", "readonly");
//        certificateDetails.addAttribute("cols", "70");
//        certificateDetails.addAttribute("rows", "40");
//        certificateDetails.addHtmlElement(new TextObject(cert.toString(true)));
        section.addNewElement(cert.toHtml(false));
    }

    private void addPemInfo(AaaCertificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "PEM encoded", unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        section.setFoldedElement("Base64 encoded certificate");

        String pemCert = "Encode error";
        pemCert = PEM.getPemCert(cert.getEncoded(), "<br />");
        section.addNewElement(aStr(pemCert), aStr(CODE_TEXT));
    }

    private void addASN1Inspector(AaaCertificate cert) {

        InfoTableSection section = certElements.addNewSection(tm, "ASN.1");
        section.setSectionHeadingClasses(CERT_INFO);

        try {
            String certHash = FnvHash.getFNV1aToHex(cert.getEncoded());
            session.addPemCert(cert);
            ButtonElement certButton = new ButtonElement("Inspect Certificate ASN.1", ONCLICK, FRAME_LOAD_FUNCTION, new String[]{
                "index.jsp",
                VIEW_BUTTON,
                "cert" + certHash});
            section.addNewElement(aStr(certButton.toString()), aStr(CODE_TEXT));
        } catch (Exception ex) {
        }
    }

    public String[] getExtNameAndOID(ExtensionInfo rawExt) {
        String iD = rawExt.getOid().getId();
        String name = rawExt.getExtensionType().getName();
        boolean critical = rawExt.isCritical();
        name = (name != null && name.length() > 0) ? name : iD;
        return new String[]{name, (critical) ? "Critical" : "Non-Critical"};
    }

    private String normalized(String toString) {
        char[] chars = toString.trim().toCharArray();
        StringBuilder b = new StringBuilder();
        boolean cc = false;
        for (char c : chars) {
            if ((int) c > 30) {
                cc = false;
                b.append(c);
            } else {
                //prevent multipple control characters from creating multipple commas
                if (!cc) {
                    b.append(", ");
                }
                cc = true;
            }
        }
        return b.toString();
    }

    private String[] aStr(String s1) {
        return new String[]{s1};
    }

    private String[] aStr(String s1, String s2) {
        return new String[]{s1, s2};
    }

    private String[] aStr(String s1, String s2, String s3) {
        return new String[]{s1, s2, s3};
    }

    private static class ExtensionAttr {

        String extName;
        String[] defaultClasses;
        List<String[]> attrList = new ArrayList<String[]>();
        List<String[]> classList = new ArrayList<String[]>();

        public ExtensionAttr(String[] defaultClasses) {
            this.extName = "";
            this.defaultClasses = defaultClasses;
        }

        public void setDefaultClasses(String[] defaultClasses) {
            this.defaultClasses = defaultClasses;
        }

        public void setExtName(String extName) {
            this.extName = extName;
        }

        public void add(String[] values, String[] classes) {
            if (values == null || classes == null) {
                return;
            }
            attrList.add(values);
            classList.add(classes);
        }

        public void add(String attr, String value) {
            add(attr, value, defaultClasses);
        }

        public void add(String attr, String value, String[] htmlClasses) {
            if (htmlClasses == null || value == null || value == null) {
                return;
            }
            classList.add(htmlClasses);
            if (attrList.isEmpty()) {
                attrList.add(new String[]{extName, attr, value});
            } else {
                attrList.add(new String[]{SPACE, attr, value});
            }
        }

        public String[] getValues(int i) {
            return attrList.get(i);
        }

        public String[] getClass(int i) {
            return classList.get(i);
        }

        public void addExtension(InfoTableElements elementList) {
            for (int i = 0; i < attrList.size(); i++) {
                if (i < (attrList.size() - 1)) {
                    elementList.add(new InfoTableElement(attrList.get(i), classList.get(i)));
                } else {
                    elementList.add(new InfoTableElement(attrList.get(i), lastRowClass(classList.get(i))));
                }
            }
            clear();
        }

        private String[] lastRowClass(String[] classes) {
            if (classes == null || classes.length < 2) {
                return new String[]{EXTESION_PADDING};
            }
            String[] result = new String[classes.length];
            result[0] = EXTESION_PADDING;
            for (int i = 1; i < classes.length; i++) {
                result[i] = classes[i];
            }
            return result;
        }

        private void clear() {
            attrList = new ArrayList<String[]>();
            classList = new ArrayList<String[]>();
            extName = "";
        }
    }
}
