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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context;

import iaik.x509.X509Certificate;
import java.util.Calendar;
import java.util.List;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import se.tillvaxtverket.tsltrust.common.iaik.KsCertFactory;

/**
 * Data class holding signature validation context parameters
 */
public class SignatureValidationContext {

    private String signatureName = "";
    private boolean coversDoc = false;
    private int revision;
    private int revisions;
    private Calendar signDate;
    private boolean timestamped;
//    private PdfPKCS7 pkcs7;
    private X509Certificate signCert;
    private boolean digestValid = false;
    private boolean sigValid = false;
    private Object[] sigValidationError = null;
    private boolean sigChainVerified = false;
    private CertVerifyContext signCertValidation = new CertVerifyContext();
    private TimeStampContext tstContext = new TimeStampContext();
    private List<X509Certificate> providedChain;
    private ASN1ObjectIdentifier signaturePkAlgOID;
    private ASN1ObjectIdentifier signatureHashAlgOID;
    private ASN1ObjectIdentifier signatureAlgOID;
    private boolean qualifiedCertificate;
    private boolean sscd;

    public SignatureValidationContext() {
    }

    public boolean isCoversDoc() {
        return coversDoc;
    }

    public void setCoversDoc(boolean coversDoc) {
        this.coversDoc = coversDoc;
    }

    public boolean isDigestValid() {
        return digestValid;
    }

    public void setDigestValid(boolean digestValid) {
        this.digestValid = digestValid;
    }

//    public PdfPKCS7 getPkcs7() {
//        return pkcs7;
//    }
//
//    public void setPkcs7(PdfPKCS7 pkcs7) {
//        this.pkcs7 = pkcs7;
//    }
//
    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public int getRevisions() {
        return revisions;
    }

    public void setRevisions(int revisions) {
        this.revisions = revisions;
    }

    public boolean isSigValid() {
        return sigValid;
    }

    public void setSigValid(boolean sigValid) {
        this.sigValid = sigValid;
    }

    public CertVerifyContext getSignCertValidation() {
        return signCertValidation;
    }

    public void setSignCertValidation(CertVerifyContext signCertValidation) {
        this.signCertValidation = signCertValidation;
    }

    public Calendar getSignDate() {
        return signDate;
    }

    public void setSignDate(Calendar signDate) {
        this.signDate = signDate;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public boolean isTimestamped() {
        return timestamped;
    }

    public void setTimestamped(boolean timestamped) {
        this.timestamped = timestamped;
    }

    public TimeStampContext getTstContext() {
        return tstContext;
    }

    public void setTstContext(TimeStampContext tstContext) {
        this.tstContext = tstContext;
    }

    public List<X509Certificate> getProvidedChain() {
        return providedChain;
    }

    public void setProvidedChain(List<X509Certificate> providedChain) {
        this.providedChain = providedChain;
    }

    public boolean isSigChainVerified() {
        return sigChainVerified;
    }

    public void setSigChainVerified(boolean sigChainVerified) {
        this.sigChainVerified = sigChainVerified;
    }

    public ASN1ObjectIdentifier getSignaturePkAlgOID() {
        return signaturePkAlgOID;
    }

    public void setSignaturePkAlgOID(ASN1ObjectIdentifier signatureAlgOID) {
        this.signaturePkAlgOID = signatureAlgOID;
    }

    public ASN1ObjectIdentifier getSignatureHashAlgOID() {
        return signatureHashAlgOID;
    }

    public void setSignatureHashAlgOID(ASN1ObjectIdentifier signatureHashAlgOID) {
        this.signatureHashAlgOID = signatureHashAlgOID;
    }

    public Object[] getSigValidationError() {
        return sigValidationError;
    }

    public void setSigValidationError(Object[] sigValidationError) {
        if (sigValidationError != null) {
            if (sigValidationError[0] != null && sigValidationError[0] instanceof java.security.cert.X509Certificate) {
                sigValidationError[0] = (X509Certificate) KsCertFactory.getIaikCert((java.security.cert.X509Certificate) sigValidationError[0]);
            }
        }
        this.sigValidationError = sigValidationError;
    }

    public boolean isQualifiedCertificate() {
        return qualifiedCertificate;
    }

    public void setQualifiedCertificate(boolean qualifiedCertificate) {
        this.qualifiedCertificate = qualifiedCertificate;
    }

    public boolean isSscd() {
        return sscd;
    }

    public void setSscd(boolean sscd) {
        this.sscd = sscd;
    }

    public ASN1ObjectIdentifier getSignatureAlgOID() {
        return signatureAlgOID;
    }

    public void setSignatureAlgOID(ASN1ObjectIdentifier signatureAlgOID) {
        this.signatureAlgOID = signatureAlgOID;
    }

    public X509Certificate getSignCert() {
        return signCert;
    }

    public void setSignCert(X509Certificate signCert) {
        this.signCert = signCert;
    }
    
}
