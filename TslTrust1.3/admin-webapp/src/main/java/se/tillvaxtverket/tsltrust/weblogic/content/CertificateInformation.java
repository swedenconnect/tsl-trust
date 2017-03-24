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
package se.tillvaxtverket.tsltrust.weblogic.content;

import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElement;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.ASN1Util;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.core.PEM;
import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.GeneralName;
import iaik.asn1.structures.GeneralNames;
import iaik.asn1.structures.PolicyInformation;
import iaik.x509.V3Extension;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.BasicConstraints;
import iaik.x509.extensions.CertificatePolicies;
import iaik.x509.extensions.ExtendedKeyUsage;
import iaik.x509.extensions.KeyUsage;
import iaik.x509.extensions.SubjectAltName;
import iaik.x509.extensions.qualified.QCStatements;
import iaik.x509.extensions.qualified.structures.QCStatement;
import iaik.x509.extensions.qualified.structures.QCStatementInfo;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;

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
        X509Certificate iaikCert = KsCertFactory.getIaikCert(certBytes);
        return getCertInfo(iaikCert);
    }

    public InfoTableElements getCertInfo(java.security.cert.X509Certificate cert) {
        X509Certificate iaikCert = KsCertFactory.getIaikCert(cert);
        return getCertInfo(iaikCert);
    }

    public InfoTableElements getCertInfo(X509Certificate cert) {
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
        addTechnicalInfoTextFrame(cert,false);
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

        Iterator<Entry<ObjectID, String>> rdns = ASN1Util.getCertNameAttributeSet(dn).iterator();
        while (rdns.hasNext()) {
            Entry<ObjectID, String> entry = rdns.next();
            String type = entry.getKey().getName();
            String value = entry.getValue();
            section.addNewElement(aStr(type, value), ATTR);
        }
    }

    private void addPublicKeyInfo(X509Certificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "Public Key", unfold);
        section.setSectionHeadingClasses(CERT_INFO);

        PublicKey publicKey = cert.getPublicKey();
        String algorithm = publicKey.getAlgorithm();
        int keyBits = 0;
        String oidStr = "";
        String oidName = "unknown";

        try {
            ASN1 pkasn1 = new ASN1(publicKey.getEncoded());
            ASN1Object algID = pkasn1.getComponentAt(0);
            ASN1Object pk = pkasn1.getComponentAt(1);
            ASN1Object algOID = algID.getComponentAt(0);
            ObjectID oid = new ObjectID((String) algOID.getValue());
            int keydatalen = ((byte[]) pk.getValue()).length;
            keyBits = (keydatalen - 12) * 8;
            if (keydatalen > 127 && keydatalen < 150) {
                keyBits = 1024;
            }
            if (keydatalen > 255 && keydatalen < 280) {
                keyBits = 2048;
            }
            if (keydatalen > 511 && keydatalen < 540) {
                keyBits = 4096;
            }
            oidStr = oid.getID();
            oidName = oid.getName();
        } catch (CodingException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        String fold = oidName;
        section.addNewElement(aStr("Algorithm", oidName), ATTR);
        if (keyBits > 0) {
            fold += " (" + String.valueOf(keyBits) + ")";
            section.addNewElement(aStr("Key size", String.valueOf(keyBits)), ATTR);
        }
        section.setFoldedElement(fold);
    }

    private void addCertificateExtensionInfo(X509Certificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "Extensions", unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        InfoTableElements extElements = section.getElements();
        extFact.clear();

        Enumeration<V3Extension> e = cert.listExtensions();
        if (e == null) {
            return;
        }

        List<V3Extension> extList = new ArrayList<V3Extension>();
        while (e.hasMoreElements()) {
            extList.add(e.nextElement());
        }
        section.setFoldedElement("Extension summary (out of " + String.valueOf(extList.size()) + " total Extensions)");
        section.setKeepFoldableElement(true);

        for (V3Extension rawExt : extList) {
            //Basic Constraints
            if (rawExt.getObjectID().equals(BasicConstraints.oid)) {
                BasicConstraints bc = (BasicConstraints) rawExt;
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                extFact.add("cA", String.valueOf(bc.ca()));
                extFact.addExtension(extElements);
            }
            //Key Usage
            if (rawExt.getObjectID().equals(KeyUsage.oid)) {
                KeyUsage ku = (KeyUsage) rawExt;
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                int i = 0;
                String[] label = new String[]{"digitalSignature", "nonRepudiation", "keyEncipherment", "dataEncipherment", "keyAgreement", "keyCertSign", "cRLSign", "encipherOnly", "decipherOnly"};
                for (boolean usage : ku.getBooleanArray()) {
                    if (usage) {
                        extFact.add(label[i], "");
                    }
                    i++;
                }
                extFact.addExtension(extElements);
            }

            //QcStatements
            if (rawExt.getObjectID().equals(QCStatements.oid)) {
                QCStatements qc = (QCStatements) rawExt;
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                QCStatement[] qCStatements = qc.getQCStatements();
                for (QCStatement statement : qCStatements) {
                    QCStatementInfo statementInfo = statement.getStatementInfo();
                    extFact.add(statement.getStatementID().getName(),
                            (statementInfo != null) ? normalized(statementInfo.toString()) : "true");
                }
                extFact.addExtension(extElements);
            }

//            //EKU
            if (rawExt.getObjectID().equals(ExtendedKeyUsage.oid)) {
                ExtendedKeyUsage eku = (ExtendedKeyUsage) rawExt;
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                ObjectID[] keyPurposeIDs = eku.getKeyPurposeIDs();
                for (ObjectID oid : keyPurposeIDs) {
                    extFact.add((oid.getName() != null && oid.getName().length() > 1) ? oid.getName() : oid.getID(), oid.getID());
                }
                extFact.addExtension(extElements);
            }

//            //CertificatePolicies
            if (rawExt.getObjectID().equals(CertificatePolicies.oid)) {
                CertificatePolicies cp = (CertificatePolicies) rawExt;
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                PolicyInformation[] policyInformation = cp.getPolicyInformation();
                for (PolicyInformation pi : policyInformation) {
                    ObjectID oid = pi.getPolicyIdentifier();
                    extFact.add("Policy", oid.getNameAndID());
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
            if (rawExt.getObjectID().equals(SubjectAltName.oid)) {
                SubjectAltName san = (SubjectAltName) rawExt;
                extFact.add(getExtNameAndOID(rawExt), EXT_ATTR);
                // set property
                String[] nameType = new String[]{"otherName", "rfc822Name", "dNSName", "x400Address", "directoryName", "ediPartyName", "uniformResourceIdentifier", "iPAddress", "registeredID"};
                GeneralNames generalNames = san.getGeneralNames();
                Enumeration<GeneralName> names = generalNames.getNames();
                while (names.hasMoreElements()) {
                    GeneralName name = names.nextElement();
                    int type = name.getType();
                    if (type == 1 || type == 2 || type == 6 || type == 7) {
                        extFact.add(nameType[type], name.getName().toString());
                    }
                }
                extFact.addExtension(extElements);
            }
        }
    }

    private void addTechnicalInfoTextFrame(X509Certificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "Technical Info", unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        section.setFoldedElement("Certificate details information");
        HtmlElement certificateDetails = new GenericHtmlElement("textarea");
        certificateDetails.addAttribute("readonly", "readonly");
        certificateDetails.addAttribute("cols", "70");
        certificateDetails.addAttribute("rows", "40");
        certificateDetails.addHtmlElement(new TextObject(cert.toString(true)));
        section.addNewElement(certificateDetails.toString());
    }

    private void addPemInfo(X509Certificate cert, boolean unfold) {
        InfoTableSection section = certElements.addNewSection(tm, "PEM encoded", unfold);
        section.setSectionHeadingClasses(CERT_INFO);
        section.setFoldedElement("Base64 encoded certificate");

        String pemCert = "Encode error";
        try {
            pemCert = PEM.getPemCert(cert.getEncoded(), "<br />");
            section.addNewElement(aStr(pemCert), aStr(CODE_TEXT));
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(CertificateInformation.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    private void addASN1Inspector(X509Certificate cert) {

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

    public String[] getExtNameAndOID(V3Extension rawExt) {
        String iD = rawExt.getObjectID().getID();
        String name = rawExt.getObjectID().getName();
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
