/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

/**
 *
 * @author stefan
 */
public enum SupportedSigAlgoritm {

    ECDSAWITHSHA1(X9ObjectIdentifiers.ecdsa_with_SHA1, DigestAlgorithm.SHA1, PublicKeyType.EC, XMLIdentifiers.ECDSA_SHA1),
    SHA256WITHECDSA(X9ObjectIdentifiers.ecdsa_with_SHA256, DigestAlgorithm.SHA256, PublicKeyType.EC, XMLIdentifiers.ECDSA_SHA256),
    SHA512WITHECDSA(X9ObjectIdentifiers.ecdsa_with_SHA512, DigestAlgorithm.SHA512, PublicKeyType.EC, XMLIdentifiers.ECDSA_SHA512),
    SHA1WITHRSA(PKCSObjectIdentifiers.sha1WithRSAEncryption, DigestAlgorithm.SHA1, PublicKeyType.RSA, XMLIdentifiers.RSA_SHA1),
    SHA224WITHRSA(PKCSObjectIdentifiers.sha224WithRSAEncryption, DigestAlgorithm.SHA224, PublicKeyType.RSA, XMLIdentifiers.RSA_SHA224),
    SHA256WITHRSA(PKCSObjectIdentifiers.sha256WithRSAEncryption, DigestAlgorithm.SHA256, PublicKeyType.RSA, XMLIdentifiers.RSA_SHA256),
    SHA512WITHRSA(PKCSObjectIdentifiers.sha512WithRSAEncryption, DigestAlgorithm.SHA512, PublicKeyType.RSA, XMLIdentifiers.RSA_SHA512);


    ASN1ObjectIdentifier sigAlgoOid;
    DigestAlgorithm digestAlgo;
    PublicKeyType pkAlgoType;
    String xmlName;

    private SupportedSigAlgoritm(ASN1ObjectIdentifier sigAlgoOid, DigestAlgorithm digestAlgo, PublicKeyType pkAlgoType, String xmlName) {
        this.sigAlgoOid = sigAlgoOid;
        this.digestAlgo = digestAlgo;
        this.pkAlgoType = pkAlgoType;
        this.xmlName = xmlName;
    }

    public DigestAlgorithm getDigestAlgo() {
        return digestAlgo;
    }

    public PublicKeyType getPkAlgoType() {
        return pkAlgoType;
    }

    public ASN1ObjectIdentifier getSigAlgoOid() {
        return sigAlgoOid;
    }

    public String getXmlName() {
        return xmlName;
    }

    public static SupportedSigAlgoritm getAlgoFromTypeAndHash(DigestAlgorithm digestAlgo, PublicKeyType pkType) {
        for (SupportedSigAlgoritm sigAlgo : values()) {
            if (sigAlgo.getDigestAlgo().equals(digestAlgo) && sigAlgo.getPkAlgoType().equals(pkType)) {
                return sigAlgo;
            }
        }
        return null;
    }

    public static SupportedSigAlgoritm getAlgoFromOid(String oid) {
        for (SupportedSigAlgoritm sigAlgo : values()) {
            if (sigAlgo.getSigAlgoOid().getId().equalsIgnoreCase(oid)) {
                return sigAlgo;
            }
        }
        return null;
    }

    public static SupportedSigAlgoritm getAlgoFromOid(ASN1ObjectIdentifier oid) {
        for (SupportedSigAlgoritm sigAlgo : values()) {
            if (sigAlgo.getSigAlgoOid().equals(oid)) {
                return sigAlgo;
            }
        }
        return null;
    }

    public static SupportedSigAlgoritm getAlgoFromXmlName(String reqSigAlgorithm) {
        for (SupportedSigAlgoritm sigAlgo : values()) {
            if (sigAlgo.getXmlName().equalsIgnoreCase(reqSigAlgorithm)) {
                return sigAlgo;
            }
        }
        return null;
    }
}
