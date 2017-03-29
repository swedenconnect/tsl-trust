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
