package se.tillvaxtverket.tsltrust.common.xmldsig;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.etsi.uri.x01903.v13.CertIDListType;
import org.etsi.uri.x01903.v13.CertIDType;
import org.etsi.uri.x01903.v13.DigestAlgAndValueType;
import org.etsi.uri.x01903.v13.QualifyingPropertiesDocument;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.etsi.uri.x01903.v13.SignedPropertiesType;
import org.etsi.uri.x01903.v13.SignedSignaturePropertiesType;
import org.w3.x2000.x09.xmldsig.DigestMethodType;
import org.w3.x2000.x09.xmldsig.X509IssuerSerialType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class holds a XAdES object based on data obtained from a sign response. 
 * This class also provides the basic operations needed to construct a signed document
 * This class can both be used by the signature server to construct the XAdES object form the request
 * as well as being used by the SP to construct the final signed document form the sign response.
 * based on these data. * 
 */
public class XadesObjectProvider {

    private QualifyingPropertiesDocument qpDoc;
    private static SecureRandom rnd;

    static {
        rnd = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Constructor
     * @param qpDoc an optional set of Qualifying properties that should be included in the XAdES signature.
     * This parameter can be set to null to only include the basic BES parameters.
     */
    public XadesObjectProvider(QualifyingPropertiesDocument qpDoc) {
        this.qpDoc = qpDoc;
        QualifyingPropertiesType qp = qpDoc.getQualifyingProperties();
        if (qp == null) {
            qp = qpDoc.addNewQualifyingProperties();
        }
        SignedPropertiesType sp = qp.getSignedProperties();
        if (sp == null) {
            qp.addNewSignedProperties();
        }
    }

    /**
     * Constructor fora basic provider that generates just basic BES content
     */
    public XadesObjectProvider() {
        qpDoc = QualifyingPropertiesDocument.Factory.newInstance();
        qpDoc.addNewQualifyingProperties().addNewSignedProperties();
    }


    /**
     * Generates the XAdES object and includes a reference to the signed part of the XAdES object into the signedInfo element of the signature.
     * @param doc The document being signed
     * @param signature The signature element being created
     * @param cert The certificate that is referenced in the XAdES Object.
     * @param digestMethod The hash method used to hash the signing certificate
     * @param addTimeMark set to true to include signing time as current time.
     * @throws IOException
     * @throws SAXException
     * @throws XMLSignatureException
     * @throws TransformationException 
     */
    public void addXadesToSignature(Document doc, XMLSignature signature, X509Certificate cert, String digestMethod, boolean addTimeMark) throws IOException, SAXException, XMLSignatureException, TransformationException, ParserConfigurationException {
        Element sigElm = signature.getElement();
        ObjectContainer objectContainer = new ObjectContainer(doc);

        // add id values
        String sigId = signature.getId() == null ? "id-" + (new BigInteger(128, rnd)).toString(16) : signature.getId();
        String sigDataId = "xades-id-" + (new BigInteger(128, rnd)).toString(16);
        sigElm.setAttribute("Id", sigId);
        QualifyingPropertiesType qualifyingProperties = qpDoc.getQualifyingProperties();
        qualifyingProperties.setTarget("#" + sigId);
        SignedPropertiesType signedProperties = qualifyingProperties.getSignedProperties();
        signedProperties.setId(sigDataId);
        // set cert hash
        setCertRef(cert, signedProperties, digestMethod, addTimeMark);
        //import the objectNode
        Document qpXmlDoc = XMLSign.getDoc(XmlBeansUtil.getCanonicalBytes(qpDoc));
        Node importNode = doc.importNode(qpXmlDoc.getDocumentElement(), true);
        objectContainer.appendChild(importNode);
        signature.appendObject(objectContainer);
        registerXadesIdNodes(doc);


        //Add reference
        Transforms transforms = new Transforms(doc);
        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

        signature.addDocument("#" + sigDataId, transforms, digestMethod, null, "http://uri.etsi.org/01903#SignedProperties");


    }

    private byte[] getHashValue(byte[] data, String digestAlgo) throws Exception {
        try {
            MessageDigest md = null;
            if (digestAlgo.equalsIgnoreCase(XMLSign.SHA1)) {
                md = MessageDigest.getInstance("SHA1");
            }
            if (digestAlgo.equalsIgnoreCase(XMLSign.SHA256)) {
                md = MessageDigest.getInstance("SHA-256");
            }
            md.update(data);
            return md.digest();

        } catch (Exception ex) {
            throw new Exception("No such hash algorithm or bad cert data");
        }
    }

    private void setCertRef(X509Certificate cert, SignedPropertiesType signedProperties, String digestMethod, boolean setTime) {
        try {
            SignedSignaturePropertiesType signedSignatureProperties = signedProperties.getSignedSignatureProperties();
            if (signedSignatureProperties == null) {
                signedSignatureProperties = signedProperties.addNewSignedSignatureProperties();
            }
            if (setTime) {
                signedSignatureProperties.setSigningTime(Calendar.getInstance());
            } else {
                if (signedSignatureProperties.getSigningTime() != null) {
                    signedSignatureProperties.unsetSigningTime();
                }
            }
            CertIDListType signingCertificate = signedSignatureProperties.getSigningCertificate();
            if (signingCertificate == null) {
                signingCertificate = signedSignatureProperties.addNewSigningCertificate();
            }
            CertIDType[] certArray = signingCertificate.getCertArray();
            CertIDType certIdType;
            if (certArray == null || certArray.length == 0) {
                certIdType = signingCertificate.addNewCert();
            } else {
                certIdType = signingCertificate.insertNewCert(0);
            }
            DigestAlgAndValueType certDigest = certIdType.getCertDigest();
            if (certDigest == null) {
                certDigest = certIdType.addNewCertDigest();
            }
            DigestMethodType digestType = DigestMethodType.Factory.newInstance();
            digestType.setAlgorithm(digestMethod);
            certDigest.setDigestMethod(digestType);
            certDigest.setDigestValue(getHashValue(cert.getEncoded(), digestMethod));

            X509IssuerSerialType issuerSerial = certIdType.getIssuerSerial();
            if (issuerSerial == null) {
                issuerSerial = certIdType.addNewIssuerSerial();
            }
            issuerSerial.setX509IssuerName(cert.getIssuerX500Principal().toString());
            issuerSerial.setX509SerialNumber(cert.getSerialNumber());

        } catch (Exception ex) {
            Logger.getLogger(XadesObjectProvider.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    private void registerXadesIdNodes(Document doc) {
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
    }
}
