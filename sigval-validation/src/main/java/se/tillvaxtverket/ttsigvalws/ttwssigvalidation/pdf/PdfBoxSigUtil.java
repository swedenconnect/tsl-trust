/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.operator.OperatorCreationException;

/**
 *
 * @author stefan
 */
public class PdfBoxSigUtil {

    /**
     * This method extracts data from a dummy signature into the PdfBoxModel
     * used in a later stage to update the signature with an externally created
     * signature.
     *
     * @param model A PdfBoxModel for this signature task.
     * @throws IOException
     */
    public static void parseSignedData(PdfSignModel model) throws IOException {
        CMSSignedData signedData = model.getSignedData();
        SignerInformationStore signerInfos = signedData.getSignerInfos();
        Iterator iterator = signerInfos.getSigners().iterator();
        List<SignerInformation> siList = new ArrayList<SignerInformation>();
        while (iterator.hasNext()) {
            siList.add((SignerInformation) iterator.next());
        }
        if (!siList.isEmpty()) {
            SignerInformation si = siList.get(0);
            model.setCmsSigAttrBytes(si.getEncodedSignedAttributes());
        }
    }

    /**
     * A method that updates the PDF PKCS7 object from the model object with a
     * signature, certificates and SignedAttributes obtains from an external
     * source. The model contains
     *
     * <p>
     * The PKCS7 Signed data found in the model can be created using a different
     * private key and certificate chain. This method effectively replace the
     * signature value and certificate with the replacement data obtained from
     * the model.
     *
     * @param model A model for this signature replacement operation containing
     * necessary data for the process.
     * @return The bytes of an updated ODF signature PKCS7.
     */
    public static byte[] updatePdfPKCS7(PdfSignModel model) {

        //New variables
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DEROutputStream dout = new DEROutputStream(bout);
        ASN1EncodableVector npkcs7 = new ASN1EncodableVector();
        ASN1EncodableVector nsd = new ASN1EncodableVector();
        ASN1EncodableVector nsi = new ASN1EncodableVector();

        try {
            ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(model.getSignedData().getEncoded()));

            //
            // Basic checks to make sure it's a PKCS#7 SignedData Object
            //
            ASN1Primitive pkcs7;

            try {
                pkcs7 = din.readObject();
            } catch (IOException e) {
                throw new IllegalArgumentException("Illegal PKCS7");
            }
            if (!(pkcs7 instanceof ASN1Sequence)) {
                throw new IllegalArgumentException("Illegal PKCS7");
            }
            ASN1Sequence signedData = (ASN1Sequence) pkcs7;
            ASN1ObjectIdentifier objId = (ASN1ObjectIdentifier) signedData.getObjectAt(0);
            if (!objId.getId().equals(PdfObjectIds.ID_PKCS7_SIGNED_DATA)) {
                throw new IllegalArgumentException("No SignedData");
            }

            //Add Signed data content type to new PKCS7
            npkcs7.add(objId);

            /**
             * SignedData ::= SEQUENCE { version CMSVersion, digestAlgorithms
             * DigestAlgorithmIdentifiers, encapContentInfo
             * EncapsulatedContentInfo, certificates [0] IMPLICIT CertificateSet
             * OPTIONAL, crls [1] IMPLICIT RevocationInfoChoices OPTIONAL,
             * signerInfos SignerInfos }
             */
            //Get the SignedData sequence
            ASN1Sequence signedDataSeq = (ASN1Sequence) ((ASN1TaggedObject) signedData.getObjectAt(1)).getObject();
            int sdObjCount = 0;

            // the version
            nsd.add(signedDataSeq.getObjectAt(sdObjCount++));

            // the digestAlgorithms
            nsd.add(signedDataSeq.getObjectAt(sdObjCount++));

            // the possible ecapsulated content info
            nsd.add(signedDataSeq.getObjectAt(sdObjCount++));
            // the certificates. The certs are taken from the input parameters to the method            
            //ASN1EncodableVector newCerts = new ASN1EncodableVector();
            Certificate[] chain = model.getChain();
            ASN1Encodable[] newCerts = new ASN1Encodable[chain.length];
            //for (Certificate nCert : model.getCertChain()) {
            for (int i = 0; i < chain.length; i++) {
                ASN1InputStream cin = new ASN1InputStream(new ByteArrayInputStream(chain[i].getEncoded()));
                newCerts[i] = cin.readObject();

            }
            nsd.add(new DERTaggedObject(false, 0, new DERSet(newCerts)));

            //Step counter past tagged objects
            while (signedDataSeq.getObjectAt(sdObjCount) instanceof ASN1TaggedObject) {
                ++sdObjCount;
            }

            //SignerInfos is the next object in the sequence of Signed Data (first untagged after certs)
            ASN1Set signerInfos = (ASN1Set) signedDataSeq.getObjectAt(sdObjCount);
            if (signerInfos.size() != 1) {
                throw new IllegalArgumentException("Unsupported multiple signer infos");
            }
            ASN1Sequence signerInfo = (ASN1Sequence) signerInfos.getObjectAt(0);
            int siCounter = 0;

            // SignerInfo sequence
            //
            // 0 - CMSVersion 
            // 1 - SignerIdentifier (CHOICE IssuerAndSerialNumber SEQUENCE) 
            // 2 - DigestAglorithmIdentifier
            // 3 - [0] IMPLICIT SignedAttributes SET 
            // 3 - Signature AlgorithmIdentifier 
            // 4 - Signature Value OCTET STRING 
            // 5 - [1] IMPLICIT UnsignedAttributes
            //
            //version
            nsi.add(signerInfo.getObjectAt(siCounter++));

            // signing certificate issuer and serial number
            Certificate sigCert = chain[0];
            ASN1EncodableVector issuerAndSerial = getIssuerAndSerial(sigCert);
            nsi.add(new DERSequence(issuerAndSerial));
            siCounter++;

            //Digest AlgorithmIdentifier
            nsi.add(signerInfo.getObjectAt(siCounter++));

            //Add signed attributes from signature service
            ASN1InputStream sigAttrIs = new ASN1InputStream(model.getCmsSigAttrBytes());
            nsi.add(new DERTaggedObject(false, 0, sigAttrIs.readObject()));

            //Step counter past tagged objects (because signedAttrs i optional in the input data)
            while (signerInfo.getObjectAt(siCounter) instanceof ASN1TaggedObject) {
                siCounter++;
            }

            //Signature Alg identifier
            nsi.add(signerInfo.getObjectAt(siCounter++));

            //Add new signature value from signing service
            nsi.add(new DEROctetString(model.getSignatureBytes()));
            siCounter++;

            //Add unsigned Attributes if present
            if (signerInfo.size() > siCounter && signerInfo.getObjectAt(siCounter) instanceof ASN1TaggedObject) {
                nsi.add(signerInfo.getObjectAt(siCounter));
            }

            /*
             * Final Assembly
             */
            // Add the SignerInfo sequence to the SignerInfos set and add this to the SignedData sequence
            nsd.add(new DERSet(new DERSequence(nsi)));
            // Add the SignedData sequence as a eplicitly tagged object to the pkcs7 object
            npkcs7.add(new DERTaggedObject(true, 0, new DERSequence(nsd)));

            dout.writeObject((new DERSequence(npkcs7)));
            byte[] pkcs7Bytes = bout.toByteArray();
            dout.close();
            bout.close();

            return pkcs7Bytes;

        } catch (Exception e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * Internal helper method that constructs an IssuerAndSerial object for
     * SignerInfo based on a signer certificate.
     *
     * @param sigCert
     * @return An ASN1EncodableVector holding the IssuerAndSerial ASN.1
     * sequence.
     * @throws CertificateEncodingException
     * @throws IOException
     */
    private static ASN1EncodableVector getIssuerAndSerial(Certificate sigCert) throws CertificateEncodingException, IOException {
        ASN1EncodableVector issuerAndSerial = new ASN1EncodableVector();
        ASN1InputStream ain = new ASN1InputStream(sigCert.getEncoded());
        ASN1Sequence certSeq = (ASN1Sequence) ain.readObject();
        ASN1Sequence tbsSeq = (ASN1Sequence) certSeq.getObjectAt(0);

        int counter = 0;
        while (tbsSeq.getObjectAt(counter) instanceof ASN1TaggedObject) {
            counter++;
        }
        //Get serial
        ASN1Integer serial = (ASN1Integer) tbsSeq.getObjectAt(counter);
        counter += 2;

        ASN1Sequence issuerDn = (ASN1Sequence) tbsSeq.getObjectAt(counter);
        //Return the issuer field
        issuerAndSerial.add(issuerDn);
        issuerAndSerial.add(serial);

        return issuerAndSerial;
    }

    /**
     * Sets the signer name and location from the signer certificate subject DN
     *
     * @param signature The signature object to be updated
     * @param sigCert The certificate being source of data
     * @throws CertificateEncodingException
     * @throws IOException
     */
    public static void setSubjectNameAndLocality(PDSignature signature, Certificate sigCert) throws CertificateEncodingException, IOException {
        Map<SubjectDnAttribute, String> subjectDnAttributeMap = getSubjectAttributes(sigCert);
        signature.setName(getName(subjectDnAttributeMap));
        signature.setLocation(getLocation(subjectDnAttributeMap));
    }

    /**
     * Gets a map of recognized subject DN attributes
     *
     * @param cert X.509 certificate
     * @return Subject DN attribute map
     * @throws java.security.cert.CertificateEncodingException
     * @throws java.io.IOException
     */
    public static Map<SubjectDnAttribute, String> getSubjectAttributes(Certificate cert) throws CertificateEncodingException, IOException {
            ASN1InputStream ain = new ASN1InputStream(cert.getEncoded());
            ASN1Sequence certSeq = (ASN1Sequence) ain.readObject();
            ASN1Sequence tbsSeq = (ASN1Sequence) certSeq.getObjectAt(0);

            int counter = 0;
            while (tbsSeq.getObjectAt(counter) instanceof ASN1TaggedObject) {
                counter++;
            }
            //Get subject
            ASN1Sequence subjectDn = (ASN1Sequence) tbsSeq.getObjectAt(counter + 4);
        Map<SubjectDnAttribute, String> subjectDnAttributeMap = getSubjectAttributes(subjectDn);

        return subjectDnAttributeMap;
    }

    /**
     * Gets a map of recognized subject DN attributes
     *
     * @param subjectDn subhect Dn
     * @return Subject DN attribute map
     */
    public static Map<SubjectDnAttribute, String> getSubjectAttributes(ASN1Sequence subjectDn) {
        Map<SubjectDnAttribute, String> subjectDnAttributeMap = new EnumMap<SubjectDnAttribute, String>(SubjectDnAttribute.class);
        try {
            Iterator<ASN1Encodable> subjDnIt = subjectDn.iterator();
            while (subjDnIt.hasNext()) {
                ASN1Set rdnSet = (ASN1Set) subjDnIt.next();
                Iterator<ASN1Encodable> rdnSetIt = rdnSet.iterator();
                while (rdnSetIt.hasNext()) {
                    ASN1Sequence rdnSeq = (ASN1Sequence) rdnSetIt.next();
                    ASN1ObjectIdentifier rdnOid = (ASN1ObjectIdentifier) rdnSeq.getObjectAt(0);
                    String oidStr = rdnOid.getId();
                    ASN1Encodable rdnVal = rdnSeq.getObjectAt(1);
                    String rdnValStr = getStringValue(rdnVal);
                    SubjectDnAttribute subjectDnAttr = SubjectDnAttribute.getSubjectDnFromOid(oidStr);
                    if (!subjectDnAttr.equals(SubjectDnAttribute.unknown)) {
                        subjectDnAttributeMap.put(subjectDnAttr, rdnValStr);
                    }
                }
            }

        } catch (Exception e) {
        }

        return subjectDnAttributeMap;
    }

    public static byte[] getRSAPkcs1DigestInfo(DigestAlgorithm digestAlgo, byte[] hashValue) throws IOException {
        ASN1EncodableVector digestInfoSeq = new ASN1EncodableVector();
        AlgorithmIdentifier algoId = digestAlgo.getAlgorithmIdentifier();
        digestInfoSeq.add(algoId);
        digestInfoSeq.add(new DEROctetString(hashValue));

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DEROutputStream dout = new DEROutputStream(bout);
        dout.writeObject((new DERSequence(digestInfoSeq)));
        byte[] digestInfoBytes = bout.toByteArray();
        dout.close();
        bout.close();

        return digestInfoBytes;
    }

    private static String getStringValue(ASN1Encodable rdnVal) {
        if (rdnVal instanceof DERUTF8String) {
            DERUTF8String utf8Str = (DERUTF8String) rdnVal;
            return utf8Str.getString();
        }
        if (rdnVal instanceof DERPrintableString) {
            DERPrintableString str = (DERPrintableString) rdnVal;
            return str.getString();
        }
        return rdnVal.toString();
    }

    private static String getName(Map<SubjectDnAttribute, String> subjectDnAttributeMap) {
        String commonName = subjectDnAttributeMap.containsKey(SubjectDnAttribute.cn) ? subjectDnAttributeMap.get(SubjectDnAttribute.cn) : null;
        String surname = subjectDnAttributeMap.containsKey(SubjectDnAttribute.surname) ? subjectDnAttributeMap.get(SubjectDnAttribute.surname) : null;
        String givenName = subjectDnAttributeMap.containsKey(SubjectDnAttribute.givenName) ? subjectDnAttributeMap.get(SubjectDnAttribute.givenName) : null;

        if (commonName != null) {
            return commonName;
        }

        if (surname != null && givenName != null) {
            return givenName + " " + surname;
        }

        if (givenName != null) {
            return givenName;
        }

        if (surname != null) {
            return surname;
        }

        return "unknown";
    }

    private static String getLocation(Map<SubjectDnAttribute, String> subjectDnAttributeMap) {
        String country = subjectDnAttributeMap.containsKey(SubjectDnAttribute.country) ? subjectDnAttributeMap.get(SubjectDnAttribute.country) : null;
        String locality = subjectDnAttributeMap.containsKey(SubjectDnAttribute.locality) ? subjectDnAttributeMap.get(SubjectDnAttribute.locality) : null;

        if (country != null && locality != null) {
            return locality + ", " + country;
        }
        if (country != null) {
            return country;
        }

        if (locality != null) {
            return locality;
        }

        return "unknown";
    }

    public static DefaultSignedAttributeTableGenerator getPadesSignerInfoGenerator(Certificate signerCert, DigestAlgorithm digestAlgo, boolean includeIssuerSerial) throws IOException, CertificateEncodingException, OperatorCreationException, NoSuchAlgorithmException, CertificateException {

        ASN1EncodableVector signedCertAttr = PdfBoxSigUtil.getSignedCertAttr(digestAlgo, getCert(signerCert), includeIssuerSerial);
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DERSequence(signedCertAttr));

        DefaultSignedAttributeTableGenerator signedSignerCertAttrGenerator = new DefaultSignedAttributeTableGenerator(new AttributeTable(v));
        return signedSignerCertAttrGenerator;

    }

    public static X509Certificate getCert(Certificate inCert) throws IOException, CertificateException {
        X509Certificate cert = null;
        ByteArrayInputStream certIs = new ByteArrayInputStream(inCert.getEncoded());

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(certIs);

        } finally {
            certIs.close();
        }
        return cert;
    }

    public static ASN1EncodableVector getSignedCertAttr(DigestAlgorithm digestAlgo, X509Certificate certificate, boolean includeIssuerSerial) throws NoSuchAlgorithmException, CertificateEncodingException, IOException {
        final X500Name issuerX500Name = new X509CertificateHolder(certificate.getEncoded()).getIssuer();
        final GeneralName generalName = new GeneralName(issuerX500Name);
        final GeneralNames generalNames = new GeneralNames(generalName);
        final BigInteger serialNumber = certificate.getSerialNumber();
        final IssuerSerial issuerSerial = new IssuerSerial(generalNames, serialNumber);

        ASN1EncodableVector signedCert = new ASN1EncodableVector();

        boolean essSigCertV2;
        ASN1ObjectIdentifier signedCertOid;
        switch (digestAlgo) {
            case SHA1:
                signedCertOid = new ASN1ObjectIdentifier(PdfObjectIds.ID_AA_SIGNING_CERTIFICATE_V1);
                essSigCertV2 = false;
                break;
            default:
                signedCertOid = new ASN1ObjectIdentifier(PdfObjectIds.ID_AA_SIGNING_CERTIFICATE_V2);
                essSigCertV2 = true;
        }

        MessageDigest md = MessageDigest.getInstance(digestAlgo.getName());
        md.update(certificate.getEncoded());
        byte[] certHash = md.digest();
        DEROctetString certHashOctetStr = new DEROctetString(certHash);

        signedCert.add(signedCertOid);

        ASN1EncodableVector attrValSet = new ASN1EncodableVector();
        ASN1EncodableVector signingCertObjSeq = new ASN1EncodableVector();
        ASN1EncodableVector essCertV2Seq = new ASN1EncodableVector();
        ASN1EncodableVector certSeq = new ASN1EncodableVector();
        ASN1EncodableVector algoSeq = new ASN1EncodableVector();
        algoSeq.add(new ASN1ObjectIdentifier(digestAlgo.getOid()));
        algoSeq.add(DERNull.INSTANCE);
        if (essSigCertV2) {
            certSeq.add(new DERSequence(algoSeq));
        }
        //Add cert hash
        certSeq.add(new DEROctetString(certHash));
        if (includeIssuerSerial) {
            certSeq.add(issuerSerial);
        }

        //Finalize assembly
        essCertV2Seq.add(new DERSequence(certSeq));
        signingCertObjSeq.add(new DERSequence(essCertV2Seq));
        attrValSet.add(new DERSequence(signingCertObjSeq));
        signedCert.add(new DERSet(attrValSet));

        return signedCert;
    }

    public static byte[] removeSignedAttr(byte[] signedAttrBytes, ASN1ObjectIdentifier[] attrOid) throws IOException, NoSuchAlgorithmException, CertificateException {
        ASN1Set inAttrSet = ASN1Set.getInstance(new ASN1InputStream(signedAttrBytes).readObject());
        ASN1EncodableVector newSigAttrSet = new ASN1EncodableVector();
        List<ASN1ObjectIdentifier> attrOidList = Arrays.asList(attrOid);

        for (int i = 0; i < inAttrSet.size(); i++) {
            Attribute attr = Attribute.getInstance(inAttrSet.getObjectAt(i));

            if (!attrOidList.contains(attr.getAttrType())) {
                newSigAttrSet.add(attr);
            }
        }

        //Der encode the new signed attributes set
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DEROutputStream dout = new DEROutputStream(bout);
        dout.writeObject(new DERSet(newSigAttrSet));
        byte[] newSigAttr = bout.toByteArray();
        dout.close();
        bout.close();
        return newSigAttr;
    }

    /**
     * Parse a Time-Stamp TsInfo byte array
     *
     * @param tsToken The bytes of a tsInfo object
     * @return A data object holding essential time stamp information
     */
    public static TimeStampData getTimeStampData(byte[] tsToken) {
        TimeStampData tsData = new TimeStampData();
        tsData.setTimeStampToken(tsToken);
        try {

            ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(tsToken));
            ASN1Sequence tsTokenSeq = ASN1Sequence.getInstance(din.readObject());

            // Get version
            int seqIdx = 0;
            int version = ASN1Integer.getInstance(tsTokenSeq.getObjectAt(seqIdx++)).getPositiveValue().intValue();
            tsData.setVersion(version);

            //Get Policy
            String policy = ASN1ObjectIdentifier.getInstance(tsTokenSeq.getObjectAt(seqIdx++)).getId();
            tsData.setPolicy(policy);

            //Get Message Imprint data (hash algo and hash value
            ASN1Sequence messageImprintSeq = ASN1Sequence.getInstance(tsTokenSeq.getObjectAt(seqIdx++));
            AlgorithmIdentifier miAi = AlgorithmIdentifier.getInstance(messageImprintSeq.getObjectAt(0));
            byte[] miOctets = DEROctetString.getInstance(messageImprintSeq.getObjectAt(1)).getOctets();
            tsData.setImprintHashAlgo(DigestAlgorithm.getDigestAlgoFromOid(miAi.getAlgorithm().getId()));
            tsData.setImprintDigest(miOctets);

            //Serial number
            tsData.setSerialNumber(ASN1Integer.getInstance(tsTokenSeq.getObjectAt(seqIdx++)).getValue());

            // Time
            Date tsTime = ASN1GeneralizedTime.getInstance(tsTokenSeq.getObjectAt(seqIdx++)).getDate();
            tsData.setTime(tsTime);

            // Skip until next tagged token
            while (tsTokenSeq.size() > seqIdx && !(tsTokenSeq.getObjectAt(seqIdx) instanceof ASN1TaggedObject)) {
                seqIdx++;
}

            // Get TSA name
            GeneralName tsaName = GeneralName.getInstance(tsTokenSeq.getObjectAt(seqIdx));
            try {
                ASN1Sequence genNameSeq = ASN1Sequence.getInstance(tsaName.getName());
                ASN1TaggedObject taggedGenNameOjb = ASN1TaggedObject.getInstance(genNameSeq.getObjectAt(0));
                if (taggedGenNameOjb.getTagNo() == 4) {
                    ASN1Sequence nameSeq = ASN1Sequence.getInstance(taggedGenNameOjb.getObject());
                    Map<SubjectDnAttribute, String> subjectAttributes = getSubjectAttributes(nameSeq);
                    tsData.setIssuerDnMap(subjectAttributes);
                }

            } catch (Exception e) {
            }

        } catch (IOException | ParseException ex) {
            Logger.getLogger(PdfBoxSigUtil.class.getName()).warning(ex.getMessage());
        }

        return tsData;

    }
}
