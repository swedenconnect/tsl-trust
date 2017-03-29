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
package se.tillvaxtverket.tsltrust.common.xmldsig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.KeyValue;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.XPathContainer;
import org.apache.xml.security.utils.Constants;
import org.apache.xmlbeans.XmlObject;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.w3.x2000.x09.xmldsig.ReferenceType;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.tillvaxtverket.tsltrust.common.utils.general.XmlUtils;
import se.tillvaxtverket.tsltrust.common.xmldsig.SigVerifyResult.IndivdualSignatureResult;

/**
 *
 * @author stefan
 */
public class XMLSign {

    public static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static final String ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
    // From SignatureMethod
    public static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    // From DigestMethod
    public static final String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    private static final Logger LOG = Logger.getLogger(XMLSign.class.getName());
    private static DocumentBuilderFactory docBuilderFactory;
    private static OperationMode mode = OperationMode.DEFAULT;
    private static boolean includeSignatureId;
    private static boolean addXadesSigningTime;
    private static SecureRandom rnd;

    static {
        org.apache.xml.security.Init.init();
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        rnd = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes(Charset.forName("UTF-8")));
        includeSignatureId = false;
        addXadesSigningTime = true;
        }

    public static OperationMode getMode() {
        return mode;
    }

    public static void setIncludeSignatureId(boolean includeSignatureId) {
        XMLSign.includeSignatureId = includeSignatureId;
    }

    public static void setMode(OperationMode mode) {
        XMLSign.mode = mode;
    }

    public static void setAddXadesSigningTime(boolean addXadesSigningTime) {
        XMLSign.addXadesSigningTime = addXadesSigningTime;
    }    

    public static SignedXmlDoc getSignedXML(byte[] xmlData, PrivateKey key, X509Certificate cert, Node sigParent, boolean append, boolean addXpath) {
        return getSignedXML(xmlData, key, cert, SHA256, RSA_SHA256, sigParent, append, addXpath, null);
    }

    public static SignedXmlDoc getSignedXML(byte[] xmlData, PrivateKey key, X509Certificate cert, Node sigParent, boolean append, boolean addXpath, String idRef) {
        return getSignedXML(xmlData, key, cert, SHA256, RSA_SHA256, sigParent, append, addXpath, idRef);
    }

    public static SignedXmlDoc getSignedXML(byte[] xmlData, PrivateKey key, X509Certificate cert, String digestAlgo, String sigAlgo, Node sigParent, boolean append, boolean addXpath, String idRef) {
        return getSignedXML(xmlData, key, cert, digestAlgo, sigAlgo, sigParent, append, addXpath, idRef, null);
    }

    public static SignedXmlDoc getSignedXML(byte[] xmlData, PrivateKey key, X509Certificate cert, String digestAlgo, String sigAlgo, Node sigParent, boolean append, boolean addXpath, String idRef, XadesObjectProvider xadesObjectProvider) {
        SignedXmlDoc signedDoc = null;
        try {
            signedDoc = signXML(new ByteArrayInputStream(xmlData), key, cert, cert.getPublicKey(), digestAlgo, sigAlgo, sigParent, append, addXpath, idRef, xadesObjectProvider);
            signedDoc.sigDocBytes = XmlBeansUtil.getBytes(XmlObject.Factory.parse(signedDoc.doc));

            return signedDoc;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        return signedDoc;
    }

    public static XmlSignatureInfo getTBSInfo(byte[] xmlData, PrivateKey key, X509Certificate cert, String hashAlgo, String sigAlgo, Node sigParent, boolean append, boolean addXpath) {
        return getTBSInfo(xmlData, key, cert, hashAlgo, sigAlgo, sigParent, append, addXpath, null, null);
    }

    public static XmlSignatureInfo getTBSInfo(byte[] xmlData, PrivateKey key, X509Certificate cert, String hashAlgo, String sigAlgo, Node sigParent, boolean append, boolean addXpath, String idRef, XadesObjectProvider xadesObjectProvider) {
        try {
            SignedXmlDoc signedXML = signXML(new ByteArrayInputStream(xmlData), key, cert, cert.getPublicKey(), hashAlgo, sigAlgo, sigParent, append, addXpath, idRef, xadesObjectProvider);
            XmlSignatureInfo sigInfo = getDigestInfo(signedXML, cert.getPublicKey());
            return sigInfo;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static XmlSignatureInfo getDigestInfo(SignedXmlDoc signedXML, PublicKey pubKey) throws ParserConfigurationException, SAXException, IOException {
        XmlSignatureInfo tbsSigInfo = new XmlSignatureInfo();
        Document cloneDoc = getDoc(XmlUtils.getCanonicalDocText(signedXML.doc));
        XmlSigData xmlSigData = getSignatureData(cloneDoc);

        if (xmlSigData != null) {
            /*
             * targetTBS contains ASN.1 providing 2 parameters (E.g for sha 1)
             * SEQUENCE(2 elem)
             *   SEQUENCE(2 elem)
             *     OBJECT IDENTIFIER1.3.14.3.2.26                                 //Sha1 OID
             *     NULL                                                           //Null paramenters
             *   OCTET STRING(20 byte) 4ADD3EDF460E9BFF6EE6E8C1BC06F91BC685D4E7   //Sha1 Hash
             * 
             */

            tbsSigInfo.setSigDoc(xmlSigData.signature);
            tbsSigInfo.setSignatureType(xmlSigData.sigType);
            tbsSigInfo.setSignatureXml(xmlSigData.signatureXml);
            tbsSigInfo.setTbsDigestInfo(signedXML.getPkcs1Sha256TbsDigest());
            tbsSigInfo.setDigest(signedXML.getSha256Hash());
            tbsSigInfo.setCanonicalSignedInfo(signedXML.signedInfoOctets);
            tbsSigInfo.setSignedDoc(signedXML.doc);

            return tbsSigInfo;
        }
        return null;
    }

    private static SignedXmlDoc signXML(byte[] xmlData, PrivateKey key, X509Certificate cert, Node sigParent, boolean append, boolean addXpath) {
        return signXML(xmlData, key, cert, SHA256, RSA_SHA256, sigParent, append, addXpath);
    }

    private static SignedXmlDoc signXML(byte[] xmlData, PrivateKey key, X509Certificate cert,
            String digestMethod, String signatureAlgo, Node sigParent, boolean append, boolean addXpath) {
        return signXML(new ByteArrayInputStream(xmlData), key, cert, digestMethod, signatureAlgo, sigParent, append, addXpath, null);
    }

    public static SignedXmlDoc signXML(InputStream docIs, PrivateKey privateKey, PublicKey pk, String digestMethod, String signatureAlgo, Node sigParent, boolean append, boolean addXpath) {
        return signXML(docIs, privateKey, null, pk, digestMethod, signatureAlgo, sigParent, append, addXpath, null);
    }

    public static SignedXmlDoc signXML(InputStream docIs, PrivateKey privateKey, X509Certificate cert,
            String digestMethod, String signatureAlgo, Node sigParent, boolean append, boolean addXpath, String idReference) {
        return signXML(docIs, privateKey, cert, null, digestMethod, signatureAlgo, sigParent, append, addXpath, idReference);

    }

    public static SignedXmlDoc signXML(InputStream docIs, PrivateKey privateKey, X509Certificate cert, PublicKey pk,
            String digestMethod, String signatureAlgo, Node sigParent, boolean append, boolean addXpath, String idReference) {
        return signXML(docIs, privateKey, cert, pk, digestMethod, signatureAlgo, sigParent, append, addXpath, idReference, null);
    }

    public static SignedXmlDoc signXML(InputStream docIs, PrivateKey privateKey, X509Certificate cert, PublicKey pk,
            String digestMethod, String signatureAlgo, Node sigParent, boolean append, boolean addXpath, String idReference, XadesObjectProvider xadesObjectProvider) {
        byte[] signedHash;
        try {
            // Instantiate the document to be signed
            DocumentBuilder xmlDocBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = xmlDocBuilder.parse(docIs);

            //For adding id attribute
            //String id = String.valueOf(System.currentTimeMillis());

            // sign the whole contract and no signature and exclude condition1
            String xpathStr = "not(ancestor-or-self::ds:Signature)";

            {
                org.apache.xml.security.signature.XMLSignature signature =
                        new org.apache.xml.security.signature.XMLSignature(doc, "", signatureAlgo, Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

                if (sigParent != null) {
                    String namespaceURI = sigParent.getNamespaceURI();
                    String nodeName = sigParent.getLocalName();
                    NodeList nl = doc.getElementsByTagNameNS(namespaceURI, nodeName);
                    if (nl.getLength() > 0) {
                        Element sigObjNode = (Element) nl.item(0);
                        if (append) {
                            sigObjNode.appendChild(signature.getElement());
                        } else {
                            Node firstChildNode = sigObjNode.getFirstChild();
                            if (firstChildNode == null) {
                                sigObjNode.appendChild(signature.getElement());
                            } else {
                                sigObjNode.insertBefore(signature.getElement(), firstChildNode);
                            }
                        }
                    } else {
                        doc.getFirstChild().appendChild(signature.getElement());
                    }
                } else {
                    doc.getFirstChild().appendChild(signature.getElement());
                }

                //For adding ID attribute
                if (includeSignatureId) {
                    signature.setId("id-" + (new BigInteger(128, rnd)).toString(16));
                }

                String rootnamespace = doc.getNamespaceURI();
                boolean rootprefixed = (rootnamespace != null) && (rootnamespace.length() > 0);
                String rootlocalname = doc.getNodeName();
                Transforms transforms = new Transforms(doc);
                XPathContainer xpath = new XPathContainer(doc);

                xpath.setXPathNamespaceContext("ds", Constants.SignatureSpecNS);
                xpath.setXPath(xpathStr);
                transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
                transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                if (addXpath) {
                    transforms.addTransform(Transforms.TRANSFORM_XPATH,
                            xpath.getElementPlusReturns());
                }
                String signatureUriReference = getSignatureUriReference(idReference);
                signature.addDocument(signatureUriReference, transforms, digestMethod);

                if (xadesObjectProvider != null) {
                    xadesObjectProvider.addXadesToSignature(doc, signature, cert, digestMethod, addXadesSigningTime);
                }


                {
                    if (cert == null) {
                        signature.getKeyInfo().add(new KeyValue(doc, pk));
                    } else {
                        X509Data x509Data = new X509Data(doc);
                        x509Data.addCertificate(cert);
                        signature.getKeyInfo().add(x509Data);
                    }
                    signature.sign(privateKey);
                    signedHash = signature.getSignedInfo().getCanonicalizedOctetStream();
                }

                //Set Id attribute value on signature
//                try {
//                    Node sigValueNode = signature.getElement()
//                            .getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "SignatureValue").item(0);
//                    Attr idAttr = doc.createAttribute("Id");
//                    idAttr.setValue(id);
//                    sigValueNode.getAttributes().setNamedItem(idAttr);
//                } catch (Exception ex) {
//                }
            }
            return new SignedXmlDoc(signedHash, doc);
        } catch (Exception ex) {
            return null;
        }
    }

    public static SigVerifyResult verifySignature(byte[] signedXml) {
        try {
            Document doc = getDoc(signedXml);
            return verifySignature(doc);
        } catch (Exception ex) {
            Logger.getLogger(XMLSign.class.getName()).warning(ex.getMessage());
            return new SigVerifyResult("Unable to parse document");
        }


    }

    public static SigVerifyResult verifySignature(Document doc) {

        //Register XAdES SignedProperties Id attrbiutes
        NodeList xadesSigPropElms = doc.getElementsByTagNameNS("http://uri.etsi.org/01903/v1.3.2#", "SignedProperties");
        for (int i = 0; i < xadesSigPropElms.getLength(); i++) {
            Node xadesSigProp = xadesSigPropElms.item(i);
            try {
                Element xadesSigPropElm = (Element) xadesSigProp;
                xadesSigPropElm.setIdAttribute("Id", true);
            } catch (Exception ex) {
            }
        }

        SigVerifyResult result = new SigVerifyResult();
        // Get signature nodes;
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            return new SigVerifyResult("No Signature");
        }
        //Get document ID attribute
        Element rootElm = getFirstElementAttributes(doc);
        Node idAttrNode = null;
        try {
            NamedNodeMap rootAttr = rootElm.getAttributes();
            idAttrNode = rootAttr.getNamedItem("ID");
            if (idAttrNode != null) {
                rootElm.setIdAttribute("ID", true);
            }
        } catch (Exception ex) {
        }
        if (idAttrNode == null) {
            try {
                NamedNodeMap rootAttr = rootElm.getAttributes();
                idAttrNode = rootAttr.getNamedItem("Id");
                if (idAttrNode != null) {
                    rootElm.setIdAttribute("Id", true);
                }
            } catch (Exception ex) {
            }
        }


        boolean hasId = false;
        String docID = null;
        if (idAttrNode != null) {
            try {
                docID = idAttrNode.getTextContent();
                hasId = docID != null;
            } catch (Exception ex) {
            }
        }

        //Verify all signatures
        for (int i = 0; i < nl.getLength(); i++) {
            //Check if this signature covers the document
            boolean coversDoc = false;
            try {
                Node sigNode = nl.item(i);
                SignatureType sigType = SignatureDocument.Factory.parse(sigNode).getSignature();
                ReferenceType[] referenceArray = sigType.getSignedInfo().getReferenceArray();
                for (ReferenceType ref : referenceArray) {
                    if (ref.getURI().equals("")) {
                        coversDoc = true;
                    }
                    if (hasId) {
                        if (ref.getURI().equals("#" + docID)) {
                            coversDoc = true;
                        }
                    }
                }
                //Verify the signature if it covers the doc
                if (coversDoc || mode.equals(OperationMode.NO_SCOPE)) {
                    IndivdualSignatureResult newResult = result.addNewIndividualSignatureResult();
                    newResult.thisSignatureNode = sigNode;
                    verifySignatureElement(doc, (Element) sigNode, newResult);
                }

            } catch (Exception ex) {
            }
        }
        result.consolidateResults();

        return result;
    }

    public static void verifySignatureElement(Document doc, Element sigElement, SigVerifyResult.IndivdualSignatureResult result) {
        try {
            org.apache.xml.security.signature.XMLSignature signature = new org.apache.xml.security.signature.XMLSignature(sigElement, "");
//            signature.addResourceResolver(new OfflineResolver());
            KeyInfo ki = signature.getKeyInfo();

            if (ki == null) {
                result.thisStatus = "No Key Info";
                return;
            }
            X509Certificate cert = signature.getKeyInfo().getX509Certificate();

            if (cert == null) {
                result.thisStatus = "No Certificate in signature";
                return;
            }
            result.thisValid = signature.checkSignatureValue(cert);
            result.thisStatus = result.thisValid ? "Signature valid" : "Signature validation failed";
            result.thisSignatureNode = sigElement;
            result.thisCert = cert;
            return;
        } catch (Exception ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        }
        result.thisStatus = "Signature parsing error";
        return;
    }

    public static Document getDoc(String xml) throws ParserConfigurationException, SAXException, IOException {
        return getDoc(xml.getBytes("UTF-8"));
    }

    public static Document getDoc(byte[] xmlData) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = new ByteArrayInputStream(xmlData);
        DocumentBuilder xmlDocBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = xmlDocBuilder.parse(is);
        return doc;
    }

    public static XmlSigData getSignatureData(Document doc) {
        NodeList sel = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        if (sel.getLength() > 0) {
            try {
                Node item = sel.item(sel.getLength() - 1);
                DocumentBuilder xmlDocBuilder = docBuilderFactory.newDocumentBuilder();
                Document sDoc = xmlDocBuilder.newDocument();
                sDoc.adoptNode(item);
                sDoc.appendChild(item);
                byte[] sigTxt = XmlBeansUtil.getBytes(XmlObject.Factory.parse(sDoc));
                SignatureType sigType = SignatureDocument.Factory.parse(sDoc).getSignature();

                return new XmlSigData(sigType, sDoc, sigTxt);

            } catch (Exception ex) {
//                Logger.getLogger(XMLSign.class.getName()).log(Level.WARNING, null, ex);
            }
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

    public static byte[] rsaVerify(byte[] signature, PublicKey pubKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            byte[] cipherData = cipher.doFinal(signature);
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

    public static byte[] rsaSign(byte[] pkcs1PaddedHash, PrivateKey privKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privKey);
            byte[] cipherData = cipher.doFinal(pkcs1PaddedHash);
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

    public static EcdsaSigValue ecdsaSignDigest(byte[] digest, PrivateKey privKey) {
        try {
            ECDSASigner ecdsa = new ECDSASigner();
            CipherParameters param = ECUtil.generatePrivateKeyParameter(privKey);


            ecdsa.init(true, param);
            BigInteger[] signature = ecdsa.generateSignature(digest);
            EcdsaSigValue sigVal = new EcdsaSigValue(signature[0], signature[1]);
            return sigVal;
        } catch (InvalidKeyException ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean ecdsaVerifyDigest(byte[] digest, byte[] signature, PublicKey pubKey) {
        return ecdsaVerifyDigest(digest, EcdsaSigValue.getInstance(signature), pubKey);
    }

    public static boolean ecdsaVerifyDigest(byte[] digest, EcdsaSigValue signature, PublicKey pubKey) {
        try {
            ECDSASigner ecdsa = new ECDSASigner();
            CipherParameters param = ECUtil.generatePublicKeyParameter(pubKey);
            ecdsa.init(false, param);
            EcdsaSigValue sigVal = EcdsaSigValue.getInstance(signature);
            return ecdsa.verifySignature(digest, sigVal.getR(), sigVal.getS());
        } catch (Exception ex) {
            Logger.getLogger(XMLSign.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static EcdsaSigValue ecdsaSignDataWithSha256(byte[] data, PrivateKey privKey) {
        try {
            Signature ecdsaSigner = Signature.getInstance("SHA256/ECDSA", "BC");
            ecdsaSigner.initSign(privKey, new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes()));
            ecdsaSigner.update(data);
            byte[] asn1Signature = ecdsaSigner.sign();

            ASN1InputStream a1i = new ASN1InputStream(asn1Signature);
            ASN1Sequence a1s = ASN1Sequence.getInstance(a1i.readObject());
            EcdsaSigValue sigVal = new EcdsaSigValue(a1s);

            return sigVal;
        } catch (Exception ex) {
        }
        return null;
    }

    public static boolean ecdsaVerifySignedDataWithSHA256(byte[] data, byte[] signature, PublicKey pubKey) {
        return ecdsaVerifySignedDataWithSHA256(data, EcdsaSigValue.getInstance(signature), pubKey);

    }

    public static boolean ecdsaVerifySignedDataWithSHA256(byte[] data, EcdsaSigValue signature, PublicKey pubKey) {
        try {
            EcdsaSigValue sigVal = EcdsaSigValue.getInstance(signature);
            byte[] asn1Signature = sigVal.toASN1Object().getEncoded();

            Signature ecdsaSigner = Signature.getInstance("SHA256/ECDSA", "BC");
            ecdsaSigner.initVerify(pubKey);
            ecdsaSigner.update(data);
            return ecdsaSigner.verify(asn1Signature);
        } catch (Exception ex) {
        }
        return false;
    }

    private static Element getFirstElementAttributes(Document doc) {
        NodeList childNodes = doc.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }
        return null;
    }

    private static String getSignatureUriReference(String idReference) {
        if (idReference == null) {
            return "";
        }
        if (idReference.startsWith("#")) {
            return idReference;
        }
        return "#" + idReference;
    }

    public static class XmlSigData {

        public SignatureType sigType;
        public Document signature;
        public byte[] signatureXml;

        public XmlSigData() {
        }

        public XmlSigData(SignatureType sigType, Document signature, byte[] signatureXml) {
            this.sigType = sigType;
            this.signature = signature;
            this.signatureXml = signatureXml;
        }
    }

    public static enum OperationMode {

        DEFAULT, NO_SCOPE;
    }
}
