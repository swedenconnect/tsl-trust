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

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

import java.util.Arrays;
import java.util.Optional;

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
    SHA512WITHRSA(PKCSObjectIdentifiers.sha512WithRSAEncryption, DigestAlgorithm.SHA512, PublicKeyType.RSA, XMLIdentifiers.RSA_SHA512),
    SHA224WITHRSAANDMGF1(PKCSObjectIdentifiers.id_RSASSA_PSS, DigestAlgorithm.SHA224, PublicKeyType.RSA, XMLIdentifiers.RSASSA_PSS_SHA224),
    SHA256WITHRSAANDMGF1(PKCSObjectIdentifiers.id_RSASSA_PSS, DigestAlgorithm.SHA256, PublicKeyType.RSA, XMLIdentifiers.RSASSA_PSS_SHA256),
    SHA512WITHRSAANDMGF1(PKCSObjectIdentifiers.id_RSASSA_PSS, DigestAlgorithm.SHA512, PublicKeyType.RSA, XMLIdentifiers.RSASSA_PSS_SHA512);


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

    public static SupportedSigAlgoritm getAlgoFromOidAndHash(ASN1ObjectIdentifier sigAlgoOid, DigestAlgorithm digestAlgo) {
        Optional<SupportedSigAlgoritm> sigAlgoritmOptional = Arrays.stream(values())
          .filter(sigAlgo -> sigAlgo.getSigAlgoOid().equals(sigAlgoOid) && sigAlgo.getDigestAlgo().equals(digestAlgo))
          .findFirst();
        return sigAlgoritmOptional.isPresent() ? sigAlgoritmOptional.get() : null;
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
