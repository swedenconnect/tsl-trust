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
package se.tillvaxtverket.tsltrust.common.xmldsig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.general.XmlUtils;

/**
 * Functions for XML signature processing
 */
public class OldXMLSign {

    public static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    private static final Logger LOG = Logger.getLogger(XMLSign.class.getName());
    private static DocumentBuilder xmlDocBuilder = null;

    static {
        org.apache.xml.security.Init.init();
        DocumentBuilderFactory docBuilderFactory;
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        try {
            xmlDocBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static byte[] getSignedXML(byte[] xmlData, PrivateKey key, X509Certificate cert) {
        try {
            Document doc = signXML(xmlData, key, cert, 1);
            byte[] signedDoc = XmlUtils.getCanonicalDocText(doc);

            return signedDoc;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        return xmlData;
    }

    public static SignatureInfo getTBSInfo(byte[] xmlData, PrivateKey key, X509Certificate cert) {
        try {
            Document doc = signXML(xmlData, key, cert, 1);

            SignatureInfo sigInfo = getDigestInfo(doc, cert.getPublicKey());

            // The following code is for testing recreation of original signature - to be deleted
            byte[] tbsDigestInfo = sigInfo.getTbsDigestInfo();
            byte[] rsaSign = rsaSign(tbsDigestInfo, key);
            sigInfo.setResignValue(String.valueOf(Base64Coder.encode(rsaSign)));

            return sigInfo;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void reSign(SignatureInfo signInfo, PrivateKey key) {
        try {
            PrivateKey pk = key;
            byte[] tbsData = signInfo.getTbsDigestInfo();
            byte[] newSig = rsaSign(tbsData, pk);
            signInfo.setResignValue(String.valueOf(Base64Coder.encode(newSig)));
        } catch (Exception ex) {
        }
    }

    private static Document signXML(byte[] xmlData, PrivateKey key, X509Certificate cert, int certsInKeyInfo) {
        return signXML(xmlData, key, cert, SHA256, RSA_SHA256, certsInKeyInfo);
    }

    private static Document signXML(byte[] xmlData, PrivateKey key, X509Certificate cert,
            String digestMethod, String signatureAlgo, int certsInKeyInfo) {
        try {

            InputStream is = new ByteArrayInputStream(xmlData);
            Document doc = xmlDocBuilder.parse(is);

            DOMSignContext dsc = new DOMSignContext(key, doc.getDocumentElement());
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            TransformParameterSpec transformSpec = null;
            Reference ref = fac.newReference("", fac.newDigestMethod(digestMethod, null),
                    Collections.singletonList(fac.newTransform(Transform.ENVELOPED, transformSpec)), null, null);

            C14NMethodParameterSpec spec = null;
            SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, spec),
                    fac.newSignatureMethod(signatureAlgo, null),
                    Collections.singletonList(ref));

            KeyInfoFactory kif = fac.getKeyInfoFactory();
            List x509Content = new ArrayList();
//            x509Content.add(cert.getSubjectX500Principal().getName());
            for (int i = 0; i < certsInKeyInfo; i++) {
                x509Content.add(cert);
            }
            X509Data xd = kif.newX509Data(x509Content);
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

            return doc;

        } catch (MarshalException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (XMLSignatureException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static SignatureInfo getDigestInfo(Document doc, PublicKey pubKey) throws ParserConfigurationException, SAXException, IOException {
        SignatureInfo tbsSigInfo = new SignatureInfo();
        Document cloneDoc = getDoc(XmlUtils.getCanonicalDocText(doc));
        XmlSigData xmlSigData = getSignatureData(cloneDoc);

        if (xmlSigData != null) {
            byte[] sigValue = xmlSigData.sigType.getSignatureValue().getByteArrayValue();

            /*
             * targetTBS contains ASN.1 providing 2 parameters (E.g for sha 1)
             * SEQUENCE(2 elem)
             *   SEQUENCE(2 elem)
             *     OBJECT IDENTIFIER1.3.14.3.2.26                                 //Sha1 OID
             *     NULL                                                           //Null paramenters
             *   OCTET STRING(20 byte) 4ADD3EDF460E9BFF6EE6E8C1BC06F91BC685D4E7   //Sha1 Hash
             * 
             */
            byte[] targetTBS = rsaVerify(sigValue, pubKey);

            tbsSigInfo.setSigDoc(xmlSigData.signature);
            tbsSigInfo.setSignatureType(xmlSigData.sigType);
            tbsSigInfo.setSignatureXml(xmlSigData.signatureXml);
            tbsSigInfo.setTbsDigestInfo(targetTBS);
            tbsSigInfo.setSignedDoc(doc);

            return tbsSigInfo;
        }
        return null;
    }

    public static SigVerifyResult verifySignature(byte[] xmlData) throws ParserConfigurationException, SAXException, IOException {
        return verifySignature(getDoc(xmlData));
    }

    public static SigVerifyResult verifySignature(byte[] xmlData, int sigIndex) throws ParserConfigurationException, SAXException, IOException {
        return verifySignature(getDoc(xmlData), sigIndex);
    }

    public static SigVerifyResult verifySignature(Document doc) {
        return verifySignature(doc, -1);
    }

    public static SigVerifyResult verifySignature(Document doc, int sigIndex) {
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            return new SigVerifyResult("No Signature");
        }
        int lastIndex = nl.getLength() - 1;
        if (sigIndex > lastIndex) {
            return new SigVerifyResult("Signature index out of range");
        }
        Node signatureNode = null;
        if (sigIndex < 0) {
            //No signature index provided, select last signature
            signatureNode = nl.item(lastIndex);
        } else {
            //Select indexed signature
            signatureNode = nl.item(sigIndex);
        }

        if (null == signatureNode) {
            return new SigVerifyResult("No Signature");
        }

        KeyInfoKeySelector keyInfoKeySelector = new KeyInfoKeySelector();
        DOMValidateContext valContext = new DOMValidateContext(
                keyInfoKeySelector, signatureNode);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature;
        try {
            signature = xmlSignatureFactory.unmarshalXMLSignature(valContext);
        } catch (MarshalException ex) {
            return new SigVerifyResult("XML signature parse error: " + ex.getMessage());
        }
        boolean coreValidity;
        try {
            coreValidity = signature.validate(valContext);
        } catch (XMLSignatureException ex) {
            return new SigVerifyResult("XML signature error: " + ex.getMessage());
        }

        // TODO: check what has been signed

        if (coreValidity) {
            return new SigVerifyResult(keyInfoKeySelector);
        }

        // Check core validation status if core signature validation failed.
        StringBuilder b = new StringBuilder();

        try {
            boolean sv = signature.getSignatureValue().validate(valContext);
            b.append("Signature validation status: ").append(sv).append("<br />");
            if (sv == false) {
                // Check the validation status of each Reference.
                Iterator i = signature.getSignedInfo().getReferences().iterator();
                for (int j = 0; i.hasNext(); j++) {
                    boolean refValid = ((Reference) i.next()).validate(valContext);
                    b.append("ref[").append(j).append("] validity status: ").append(refValid).append("<br />");
                }
            }
        } catch (XMLSignatureException ex) {
            b = new StringBuilder();
            b.append("Core Signature validation failure");
        }

        return new SigVerifyResult(keyInfoKeySelector, b.toString(), coreValidity);
    }

    public static Document getDoc(String xml) throws ParserConfigurationException, SAXException, IOException {
        return getDoc(xml.getBytes("UTF-8"));
    }

    public static Document getDoc(byte[] xmlData) throws ParserConfigurationException, SAXException, IOException {
        InputStream is = new ByteArrayInputStream(xmlData);
        Document doc = xmlDocBuilder.parse(is);
        return doc;
    }

    public static XmlSigData getSignatureData(Document doc) {
        NodeList sel = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        if (sel.getLength() > 0) {
            Node item = sel.item(sel.getLength() - 1);
            return getSigDataFromSigNode(item);
        }
        return null;
    }

    public static List<XmlSigData> getAllSignatures(Document doc) {
        List<XmlSigData> sigDataList = new ArrayList<XmlSigData>();
        List<Node> nodeList = new ArrayList<Node>();
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        for (int i = 0; i < nl.getLength(); i++) {
            Node sigNode = nl.item(i);
            nodeList.add(sigNode);
        }
        int nodeIndex = 0;
        for (Node sigNode : nodeList) {
            XmlSigData sigData = getSigDataFromSigNode(sigNode);
            if (sigData != null) {
                sigData.sigIntex = nodeIndex;
                sigDataList.add(sigData);
            }
            nodeIndex++;
        }
        return sigDataList;
    }

    private static XmlSigData getSigDataFromSigNode(Node sigNode) {

        try {
            Document sDoc = xmlDocBuilder.newDocument();
            sDoc.adoptNode(sigNode);
            sDoc.appendChild(sigNode);
            byte[] sigTxt = XmlUtils.getCanonicalDocText(sDoc);

            SignatureType sigType = SignatureDocument.Factory.parse(new ByteArrayInputStream(sigTxt)).getSignature();
            return new XmlSigData(sigType, sDoc, sigTxt);

        } catch (Exception ex) {
//            LOG.info("Error parsing singature element");
        }
        return null;
    }

    public static X509Certificate getCertificate(byte[] encoded) {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is = new ByteArrayInputStream(encoded);
            X509Certificate generateCertificate = (X509Certificate) fact.generateCertificate(is);
            is.close();
            return generateCertificate;
        } catch (IOException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String str(byte[] bytes) {
        return new String(bytes, Charset.forName("UTF-8"));
    }

    public static byte[] rsaVerify(byte[] data, PublicKey pubKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            byte[] cipherData = cipher.doFinal(data);
            return cipherData;
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static byte[] rsaSign(byte[] data, PrivateKey privKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privKey);
            byte[] cipherData = cipher.doFinal(data);
            return cipherData;
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static class XmlSigData {

        /**
         * The signature elemant java object (as defined by XML Dsig schema
         */
        public SignatureType sigType;
        /**
         * The signature XML document object
         */
        public Document signature;
        /**
         * The XML bytes of the signature element
         */
        public byte[] signatureXml;
        /**
         * The index of the signature element among all signature elements of
         * the document
         */
        public int sigIntex = -1;

        public XmlSigData() {
        }

        public XmlSigData(SignatureType sigType, Document signature, byte[] signatureXml) {
            this.sigType = sigType;
            this.signature = signature;
            this.signatureXml = signatureXml;
        }
    }

    public static class SigVerifyResult {

        /**
         * The X509 signature certificate
         */
        public X509Certificate cert = null;
        /**
         * Status of signature verification. "ok", "No Signature", "XML
         * signature parse error: [ex-message]", "XML signature error:
         * [ex-message]", "Signature validation status: [info] "Core Signature
         * validation failure"
         */
        public String status = "";
        /**
         * The key info selector holding the certificate and certificate chain
         */
        public KeyInfoKeySelector keyInfo;
        /**
         * true if the signature could be verified with the provided signature
         * certificate
         */
        public boolean valid = false;

        public SigVerifyResult() {
        }

        /**
         * Sets the certificate and the status to valid
         *
         * @param keyInfo The KeyInfoKeySelector object holding certificate data
         */
        public SigVerifyResult(KeyInfoKeySelector keyInfo) {
            this.keyInfo = keyInfo;
            this.cert = keyInfo.getCertificate();
            this.status = "ok";
            this.valid = true;
        }

        /**
         * Sets the values of the signature verification result
         *
         * @param keyInfo KeyInfoKeySelector object holding Certificate data
         * @param status Status
         * @param valid true if the signature is valid.
         */
        public SigVerifyResult(KeyInfoKeySelector keyInfo, String status, boolean valid) {
            this.keyInfo = keyInfo;
            this.cert = keyInfo.getCertificate();
            this.status = status;
            this.valid = valid;
        }

        /**
         * Sets the status comment for failed validation
         *
         * @param status Status
         */
        public SigVerifyResult(String status) {
            this.status = status;
            this.keyInfo = null;
            this.cert = null;
            this.valid = false;
        }
    }
}
