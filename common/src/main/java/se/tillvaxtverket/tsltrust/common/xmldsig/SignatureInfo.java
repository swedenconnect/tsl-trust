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

import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3c.dom.Document;

/**
 * Data class for storing information related to an XML signature
 */
public class SignatureInfo {

    private SignatureType signatureType = null;
    private byte[] signatureXml = null;
    private Document sigDoc = null;
    private Document signedDoc=null;
    private byte[] tbsDigestInfo;
    private String tbsHash = "";
    private String resignValue = "";
    private String resignCert = "";
    private String relayState = "";

    public SignatureInfo() {
    }

    public String getRelayState() {
        return relayState;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
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
    
    
}
