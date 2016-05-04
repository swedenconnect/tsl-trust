/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.tsltrust.common.xmldsig;

import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3c.dom.Document;

/**
 *
 * @author stefan
 */
public class XmlSignatureInfo {

    private SignatureType signatureType = null;
    private byte[] signatureXml = null;
    private Document sigDoc = null;
    private Document signedDoc=null;
    private byte[] tbsDigestInfo;
    private byte[] digest;
    private byte[] canonicalSignedInfo;
    private String tbsHash = "";
    private String resignValue = "";
    private String resignCert = "";
    private String requestId = "";

    public XmlSignatureInfo() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResignCert() {
        return resignCert;
    }

    public void setResignCert(String resignCert) {
        this.resignCert = resignCert;
    }

    public String getResignValue() {
        return resignValue;
    }

    public void setResignValue(String resignValue) {
        this.resignValue = resignValue;
    }

    public Document getSigDoc() {
        return sigDoc;
    }

    public void setSigDoc(Document sigDoc) {
        this.sigDoc = sigDoc;
    }

    public SignatureType getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(SignatureType signatureType) {
        this.signatureType = signatureType;
    }

    public byte[] getSignatureXml() {
        return signatureXml;
    }

    public void setSignatureXml(byte[] signatureXml) {
        this.signatureXml = signatureXml;
    }

    public byte[] getTbsDigestInfo() {
        return tbsDigestInfo;
    }

    public void setTbsDigestInfo(byte[] tbsDigestInfo) {
        this.tbsDigestInfo = tbsDigestInfo;
    }

    public String getTbsHash() {
        return tbsHash;
    }

    public void setTbsHash(String tbsHash) {
        this.tbsHash = tbsHash;
    }

    public Document getSignedDoc() {
        return signedDoc;
    }

    public void setSignedDoc(Document signedDoc) {
        this.signedDoc = signedDoc;
    }

    public byte[] getDigest() {
        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public byte[] getCanonicalSignedInfo() {
        return canonicalSignedInfo;
    }

    public void setCanonicalSignedInfo(byte[] canonicalSignedInfo) {
        this.canonicalSignedInfo = canonicalSignedInfo;
    }
    
    
}
