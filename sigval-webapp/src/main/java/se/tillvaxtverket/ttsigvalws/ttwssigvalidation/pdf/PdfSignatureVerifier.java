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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.CollectionStore;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.SigDocument;

/**
 * Verifies signatures on PDF documents
 *
 * @author stefan
 */
public class PdfSignatureVerifier {

    private static final Logger LOG = Logger.getLogger(PdfSignatureVerifier.class.getName());

    /**
     * Verifies the signature on a PDF document
     *
     * @param pdfDoc The bytes of a PDF document
     * @param verifyPades If this is set to false, signature validation will
     * ignore any errors in any present PAdES Signed signature certificate data.
     * This should normally always be true. If this is set to true, signature
     * validation will still succeed if PAdES data is absent.
     * @return Signature verification result data.
     * @throws IOException
     */
    public static PdfSigVerifyResult verifyPdfSignatures(SigDocument pdfDoc, boolean verifyPades) throws IOException {
        PDDocument doc = PDDocument.load(pdfDoc.getDocInputStream());
        PdfSigVerifyResult result = new PdfSigVerifyResult();
        List<PDSignature> signatureDicts = doc.getSignatureDictionaries();
        for (PDSignature sig : signatureDicts) {
            byte[] signedContent = sig.getSignedContent(pdfDoc.getDocInputStream());
            byte[] sigBytes = sig.getContents(pdfDoc.getDocInputStream());

            CMSSigVerifyResult sigResult = result.addNewIndividualSignatureResult();
            try {
                verifySign(sigBytes, signedContent, sigResult, verifyPades);
            } catch (Exception ex) {
                sigResult.setStatus("Failed with exception: " + ex.getMessage());
            }
        }

        result.consolidateResults();
        return result;
    }

    /**
     * Verifies the signature on a PDF document
     *
     * @param pdfFile The PDF file to verify
     * @param verifyPades If this is set to false, signature validation will
     * ignore any errors in any present PAdES Signed signature certificate data.
     * This should normally always be true. If this is set to true, signature
     * validation will still succeed if PAdES data is absent.
     * @return Signature verification result data.
     * @throws IOException
     */
    public static PdfSigVerifyResult verifyPdfSignatures(File pdfFile, boolean verifyPades) throws IOException {
        PDDocument doc = PDDocument.load(pdfFile);
        PdfSigVerifyResult result = new PdfSigVerifyResult();
        List<PDSignature> signatureDicts = doc.getSignatureDictionaries();
        for (PDSignature sig : signatureDicts) {
            byte[] signedContent = sig.getSignedContent(new FileInputStream(pdfFile));
            byte[] sigBytes = sig.getContents(new FileInputStream(pdfFile));

            CMSSigVerifyResult sigResult = result.addNewIndividualSignatureResult();
            try {
                verifySign(sigBytes, signedContent, sigResult, verifyPades);
            } catch (Exception ex) {
                sigResult.setStatus("Failed with exception: " + ex.getMessage());
            }
        }

        result.consolidateResults();
        return result;
    }

    /**
     * Verifies one individual signature element of a signed PDF document
     *
     * @param signedData The SignedData of this signature
     * @param signedContentBytes The data being signed by this signature
     * @param sigResult The signature verification result object used to express
     * signature result data.
     * @param verifyPades The value true causes verification to check for the
     * signed signature certificate signed attributes. If present, this
     * attribute is validated against the provided signature certificate in
     * signed data.
     * @throws Exception
     */
    public static void verifySign(byte[] signedData, byte[] signedContentBytes, CMSSigVerifyResult sigResult, boolean verifyPades) throws Exception {
        InputStream is = new ByteArrayInputStream(signedContentBytes);
        CMSSignedDataParser sp = new CMSSignedDataParser(new BcDigestCalculatorProvider(), new CMSTypedStream(is), signedData);
        CMSTypedStream signedContent = sp.getSignedContent();
        signedContent.drain();
        sigResult.setSignedData(signedData);

        verifyCMSSignature(sp, sigResult);
        checkTimestamps(sp, sigResult);
    }

    private static void verifyCMSSignature(CMSSignedDataParser sp, CMSSigVerifyResult sigResult) throws CMSException, IOException, CertificateException, OperatorCreationException {
        CollectionStore certStore = (CollectionStore) sp.getCertificates();
        Iterator ci = certStore.iterator();
        List<X509Certificate> certList = new ArrayList<>();
        while (ci.hasNext()) {
            X509CertificateHolder ch = (X509CertificateHolder) ci.next();
            certList.add(getCert(ch));
        }
        sigResult.setCertList(certList);

        SignerInformationStore signers = sp.getSignerInfos();
        Collection c = signers.getSigners();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            SignerInformation signer = (SignerInformation) it.next();
            Date claimedSigningTime = getClaimedSigningTime(signer);
            sigResult.setClaimedSigningTime(claimedSigningTime);
            Collection certCollection = certStore.getMatches(signer.getSID());
            X509CertificateHolder certHolder = (X509CertificateHolder) certCollection.iterator().next();
            sigResult.setCert(getCert(certHolder));

            //Check signature
            sigResult.setValid(signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(certHolder)));

            sigResult.setStatus(sigResult.isValid() ? "Valid" : "Signature verification failed");
            if (!sigResult.isValid()) {
                return;
            }

            // Collect sig algo data
            getPkParams(sigResult.getCert().getPublicKey(), sigResult);
            DigestAlgorithm signerInfoHashAlgo = DigestAlgorithm.getDigestAlgoFromOid(signer.getDigestAlgOID());
            sigResult.setDigestAlgo(signerInfoHashAlgo);
            String encryptionAlgOID = signer.getEncryptionAlgOID();
            SupportedSigAlgoritm sigAlgoFromSignerInfoAndCert = SupportedSigAlgoritm.getAlgoFromOidAndHash(new ASN1ObjectIdentifier(encryptionAlgOID), signerInfoHashAlgo);
            sigResult.setSigAlgo(sigAlgoFromSignerInfoAndCert);
            Attribute cmsAlgoProtAttr = signer.getSignedAttributes().get(new ASN1ObjectIdentifier(PdfObjectIds.ID_AA_CMS_ALGORITHM_PROTECTION));
            getCMSAlgoritmProtectionData(cmsAlgoProtAttr, sigResult);
            if (!checkAlgoritmConsistency(sigResult)) {
                sigResult.setValid(false);
                sigResult.setStatus("Signature was verified but with inconsistent Algoritm declarations or unsupported algoritms");
            }
            if (sigResult.isValid()) {
                verifyPadesProperties(signer, sigResult);
            }
        }
    }

    private static void checkTimestamps(CMSSignedDataParser sp, CMSSigVerifyResult sigResult) throws CMSException {
        List<TimeStampResult> timeStampResultList = sigResult.getTimStampResultList();
        sigResult.setTimStampResultList(timeStampResultList);
        SignerInformationStore signers = sp.getSignerInfos();
        Collection c = signers.getSigners();
        Iterator it = c.iterator();
        if (!it.hasNext()) {
            return;
        }
        SignerInformation signer = (SignerInformation) it.next();

        //Collect and check time stamps
        AttributeTable unsignedAttributes = signer.getUnsignedAttributes();
        if (unsignedAttributes == null) {
            return;
        }
        ASN1EncodableVector timeStamps = unsignedAttributes.getAll(new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.14"));
        if (timeStamps.size() == 0) {
            return;
        }
        for (int i = 0; i < timeStamps.size(); i++) {
            try {
                Attribute timestampAttr = Attribute.getInstance(timeStamps.get(i));
                byte[] timeStampBytes = timestampAttr.getAttrValues().getObjectAt(0).toASN1Primitive().getEncoded();
                TimeStampResult tsResult = new TimeStampResult();
                tsResult.setTimestamp(timeStampBytes);
                timeStampResultList.add(tsResult);

                InputStream tsis = new ByteArrayInputStream(timeStampBytes);
                CMSSignedDataParser tsSp = new CMSSignedDataParser(new BcDigestCalculatorProvider(), tsis);

                byte[] tsInfoBytes = IOUtils.toByteArray(tsSp.getSignedContent().getContentStream());
                TimeStampData timeStampData = PdfBoxSigUtil.getTimeStampData(tsInfoBytes);
                tsResult.setTsData(timeStampData);

                //Compare TimeStamp data hash with signature hash
                byte[] sigHash = getDigest(timeStampData.getImprintHashAlgo(), signer.getSignature());
                tsResult.setTimestampMatch(Arrays.equals(sigHash, timeStampData.getImprintDigest()));

                CMSSigVerifyResult tsSigResult = new CMSSigVerifyResult();
                tsSigResult.setSignedData(timeStampBytes);
                tsResult.setSignatureVerification(tsSigResult);

                verifyCMSSignature(tsSp, tsSigResult);
            } catch (Exception e) {
            }

        }
        sigResult.setTimStampResultList(timeStampResultList);
    }

    /**
     * converts an X509CertificateHolder object to an X509Certificate object.
     *
     * @param certHolder the cert holder object
     * @return X509Certificate object
     * @throws IOException
     * @throws CertificateException
     */
    public static X509Certificate getCert(X509CertificateHolder certHolder) throws IOException, CertificateException {
        X509Certificate cert = null;
        ByteArrayInputStream certIs = new ByteArrayInputStream(certHolder.getEncoded());

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(certIs);

        } finally {
            certIs.close();
        }

        return cert;
    }

    private static Date getClaimedSigningTime(SignerInformation signer) {
        try {
            AttributeTable signedAttributes = signer.getSignedAttributes();
            Attribute sigTimeAttr = signedAttributes.get(new ASN1ObjectIdentifier("1.2.840.113549.1.9.5"));
            ASN1Encodable[] attributeValues = sigTimeAttr.getAttributeValues();
            ASN1UTCTime utcTime = (ASN1UTCTime) attributeValues[0];
            return utcTime.getDate();
        } catch (Exception e) {
            return null;
        }
    }

    private static void verifyPadesProperties(SignerInformation signer, CMSSigVerifyResult sigResult) {
        try {
            AttributeTable signedAttributes = signer.getSignedAttributes();
            Attribute essSigningCertV2Attr = signedAttributes.get(new ASN1ObjectIdentifier(PdfObjectIds.ID_AA_SIGNING_CERTIFICATE_V2));
            Attribute signingCertAttr = signedAttributes.get(new ASN1ObjectIdentifier(PdfObjectIds.ID_AA_SIGNING_CERTIFICATE_V1));

            if (essSigningCertV2Attr == null && signingCertAttr == null) {
                sigResult.setPades(false);
                sigResult.setPadesVerified(false);
                return;
            }

            //Start assuming that PAdES validation is non-successful
            sigResult.setPades(true);
            sigResult.setPadesVerified(false);
            sigResult.setValid(false);

            DEROctetString certHashOctStr = null;
            DigestAlgorithm hashAlgo = null;

            if (essSigningCertV2Attr != null) {
                ASN1Encodable[] attributeValues = essSigningCertV2Attr.getAttributeValues();
                ASN1Sequence signingCertificateV2Seq = (ASN1Sequence) attributeValues[0]; //Holds sequence of certs and policy
                ASN1Sequence essCertV2Seq = (ASN1Sequence) signingCertificateV2Seq.getObjectAt(0); // holds sequence of cert
                ASN1Sequence certSeq = (ASN1Sequence) essCertV2Seq.getObjectAt(0); //Holds seq of algoId, cert hash and sigId

                ASN1Encodable algoIdOrHash = certSeq.getObjectAt(0);
                if (algoIdOrHash instanceof ASN1Sequence){
                    // Hash algorithm is specified
                    ASN1Sequence algoSeq = (ASN1Sequence) algoIdOrHash; //Holds sequence of OID and algo params
                    ASN1ObjectIdentifier algoOid = (ASN1ObjectIdentifier) algoSeq.getObjectAt(0);
                    hashAlgo = getDigestAlgo(algoOid);
                    certHashOctStr = (DEROctetString) certSeq.getObjectAt(1);
                } else {
                    // No hash algo ID is specified. Use default SHA 256
                    hashAlgo = DigestAlgorithm.SHA256; // SHA-256 is default
                    certHashOctStr = (DEROctetString) algoIdOrHash;
                }

            } else {
                if (signingCertAttr != null) {
                    ASN1Encodable[] attributeValues = signingCertAttr.getAttributeValues();
                    ASN1Sequence signingCertificateV2Seq = (ASN1Sequence) attributeValues[0]; //Holds sequence of certs and policy
                    ASN1Sequence essCertV2Seq = (ASN1Sequence) signingCertificateV2Seq.getObjectAt(0); // holds sequence of cert
                    ASN1Sequence certSeq = (ASN1Sequence) essCertV2Seq.getObjectAt(0); //holds sequence of cert hash and sigID                
                    certHashOctStr = (DEROctetString) certSeq.getObjectAt(0);
                    hashAlgo = DigestAlgorithm.SHA1;
                }
            }

            if (hashAlgo == null || certHashOctStr == null) {
                sigResult.setStatus("Unsupported hash algo for ESS-SigningCertAttributeV2");
                return;
            }

            MessageDigest md = MessageDigest.getInstance(hashAlgo.getName());
            md.update(sigResult.getCert().getEncoded());
            byte[] certHash = md.digest();

//            //Debug
//            String certHashStr = String.valueOf(Base64Coder.encode(certHash));
//            String expectedCertHashStr = String.valueOf(Base64Coder.encode(certHashOctStr.getOctets()));
            if (!Arrays.equals(certHash, certHashOctStr.getOctets())) {
                sigResult.setStatus("Cert Hash mismatch");
                return;
            }

            //PadES validation was successful
            sigResult.setPadesVerified(true);
            sigResult.setValid(true);

        } catch (Exception e) {
            sigResult.setStatus("Exception while examining Pades signed cert attr: " + e.getMessage());
        }
    }

    private static DigestAlgorithm getDigestAlgo(ASN1ObjectIdentifier algId) {
        String oid = algId.getId();
        for (DigestAlgorithm digestAlgo : DigestAlgorithm.values()) {
            if (oid.equalsIgnoreCase(digestAlgo.getOid())) {
                return digestAlgo;
            }
        }
        return null;
    }

    /**
     * Retrieves Public key parameters from a public key
     *
     * @param pubKey The public key
     * @param sigResult The data object where result data are stored
     * @throws IOException
     */
    public static void getPkParams(PublicKey pubKey, CMSSigVerifyResult sigResult) throws IOException {

        try {
            String pkStr = String.valueOf(Base64Coder.encode(pubKey.getEncoded()));

            ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(pubKey.getEncoded()));
            //ASN1Primitive pkObject = din.readObject();
            ASN1Sequence pkSeq = ASN1Sequence.getInstance(din.readObject());
            ASN1BitString keyBits = (ASN1BitString) pkSeq.getObjectAt(1);

            AlgorithmIdentifier algoId = AlgorithmIdentifier.getInstance(pkSeq.getObjectAt(0));
            PublicKeyType pkType = PublicKeyType.getTypeFromOid(algoId.getAlgorithm().getId());
            sigResult.setPkType(pkType);
            sigResult.setPkType(pkType);
            if (pkType.equals(PublicKeyType.EC)) {
                ASN1ObjectIdentifier curveOid = ASN1ObjectIdentifier.getInstance(algoId.getParameters());
                EcCurve curve = EcCurve.getEcCurveFromOid(curveOid.getId());
                sigResult.setEcCurve(curve);
                int totalKeyBits = getEcKeyLength(keyBits);
                sigResult.setKeyLength(totalKeyBits);
                return;
            }

            if (pkType.equals(PublicKeyType.RSA)) {
                ASN1InputStream keyIs = new ASN1InputStream(keyBits.getOctets());
                ASN1Sequence keyParamsSeq = ASN1Sequence.getInstance(keyIs.readObject());
                ASN1Integer modInt = ASN1Integer.getInstance(keyParamsSeq.getObjectAt(0));
                int modLen = getAsn1IntegerBitLength(modInt);
                sigResult.setKeyLength(modLen);
                return;
            }

        } catch (Exception e) {
            int asdf = 0;
        }

    }

    private static int getAsn1IntegerBitLength(ASN1Integer modInt) throws IOException {
        byte[] encoded = modInt.getEncoded();
        int lenOctets = 0;
        int intValOffset;

        int lenType = encoded[1] & 0x80;
        int lenInLenType = encoded[1] & 0x7f; //The lengthinformation in the first lenghth byte
        if (lenType == 0) {
            //Short length encoding (Bits 1-7)
            lenOctets = lenInLenType;
            intValOffset = 2;
        } else {
            //Long length encoding
            if (lenInLenType > 2 || lenInLenType + 2 > encoded.length) {
                //Checks that there are enough data to provide length bytes.
                //If more than 2 bytes is used to specify the byte length of an integer. Something is clearly wrong. Abort.
                return 0;
            }
            int multiplicator = 1;
            for (int i = 0; i < lenInLenType; i++) {
                lenOctets += (encoded[2 + i] & 0x0000ff) * multiplicator;
                multiplicator *= 256;
            }
            intValOffset = lenInLenType + 2;
        }
        //remove padding byte from bit length
        if (encoded[intValOffset] == 0) {
            lenOctets--;
        }
        //Return number of bits
        return lenOctets * 8;
    }

    private static int getEcKeyLength(ASN1BitString bitString) throws IOException {
        byte[] encoded = bitString.getEncoded();
        int lenOctets = 0;
        int valOffset;

        int lenType = encoded[1] & 0x80;
        int lenInLenType = encoded[1] & 0x7f; //The lengthinformation in the first lenghth byte
        if (lenType == 0) {
            //Short length encoding (Bits 1-7)
            lenOctets = lenInLenType;
            valOffset = 2;
        } else {
            //Long length encoding
            if (lenInLenType > 2 || lenInLenType + 2 > encoded.length) {
                //Checks that there are enough data to provide length bytes.
                //If more than 2 bytes is used to specify the byte length of an integer. Something is clearly wrong. Abort.
                return 0;
            }
            int multiplicator = 1;
            for (int i = 0; i < lenInLenType; i++) {
                lenOctets += (encoded[2 + i] & 0x0000ff) * multiplicator;
                multiplicator *= 256;
            }
            valOffset = lenInLenType + 2;
        }

        byte[] keyBytes = Arrays.copyOfRange(encoded, valOffset, encoded.length);
        if (keyBytes.length % 2 != 0) {
            //ERROR. Key bytes should be dividable by 2. We return a best estimate
            return lenOctets * 4;
        }

        int partLen = keyBytes.length / 2;
        //check for padding
        if (keyBytes[0] == 0 || keyBytes[partLen] == 0) {
            lenOctets -= 2;
        }

        //Return number of bits
        return lenOctets * 4;
    }

    private static void getCMSAlgoritmProtectionData(Attribute cmsAlgoProtAttr, CMSSigVerifyResult sigResult) {
        if (cmsAlgoProtAttr == null) {
            sigResult.setCmsAlgoProtection(false);
            return;
        }
        sigResult.setCmsAlgoProtection(true);

        try {
            ASN1Sequence cmsapSeq = ASN1Sequence.getInstance(cmsAlgoProtAttr.getAttrValues().getObjectAt(0));

            //Get Hash algo
            AlgorithmIdentifier hashAlgoId = AlgorithmIdentifier.getInstance(cmsapSeq.getObjectAt(0));
            DigestAlgorithm digestAlgo = DigestAlgorithm.getDigestAlgoFromOid(hashAlgoId.getAlgorithm().getId());
            sigResult.setCmsapDigestAlgo(digestAlgo);

            //GetSigAlgo
            for (int objIdx = 1; objIdx < cmsapSeq.size(); objIdx++){
                ASN1Encodable asn1Encodable = cmsapSeq.getObjectAt(objIdx);
                if (asn1Encodable instanceof ASN1TaggedObject) {
                    ASN1TaggedObject taggedObj = ASN1TaggedObject.getInstance(asn1Encodable);
                    if (taggedObj.getTagNo() == 1) {
                        AlgorithmIdentifier algoId = AlgorithmIdentifier.getInstance(taggedObj, false);
                        SupportedSigAlgoritm sigAlgo = SupportedSigAlgoritm.getAlgoFromOidAndHash(algoId.getAlgorithm(), digestAlgo);
                        sigResult.setCmsapSigAlgo(sigAlgo);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Failed to parse CMSAlgoritmProtection algoritms");
        }

    }

    private static boolean checkAlgoritmConsistency(CMSSigVerifyResult sigResult) {
        if (sigResult.getSigAlgo() == null) {
            return false;
        }
        if (sigResult.getDigestAlgo() == null) {
            return false;
        }
        if (!sigResult.getSigAlgo().getDigestAlgo().equals(sigResult.getDigestAlgo())) {
            return false;
        }

        //Ceheck if CML Algoprotection is present.
        if (!sigResult.isCmsAlgoProtection()) {
            return true;
        }
        if (!sigResult.getSigAlgo().equals(sigResult.getCmsapSigAlgo())) {
            return false;
        }
        if (!sigResult.getDigestAlgo().equals(sigResult.getCmsapDigestAlgo())) {
            return false;
        }

        return true;
    }

    public static byte[] getDigest(DigestAlgorithm digestAlgo, byte[] signature) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(digestAlgo.getName());
        md.update(signature);
        byte[] hashVal = md.digest();
        return hashVal;
    }

}
