/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author stefan
 */
public class TimeStampData {
    byte[] timeStampToken;
    int version;
    String policy;
    DigestAlgorithm imprintHashAlgo;
    byte[] imprintDigest;
    BigInteger serialNumber;
    Date time;
    Map<SubjectDnAttribute, String> issuerDnMap;

    public TimeStampData() {
    }

    public byte[] getTimeStampToken() {
        return timeStampToken;
    }

    public void setTimeStampToken(byte[] timeStampToken) {
        this.timeStampToken = timeStampToken;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public DigestAlgorithm getImprintHashAlgo() {
        return imprintHashAlgo;
    }

    public void setImprintHashAlgo(DigestAlgorithm imprintHashAlgo) {
        this.imprintHashAlgo = imprintHashAlgo;
    }

    public byte[] getImprintDigest() {
        return imprintDigest;
    }

    public void setImprintDigest(byte[] imprintDigest) {
        this.imprintDigest = imprintDigest;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Map<SubjectDnAttribute, String> getIssuerDnMap() {
        return issuerDnMap;
    }

    public void setIssuerDnMap(Map<SubjectDnAttribute, String> issuerDnMap) {
        this.issuerDnMap = issuerDnMap;
    }
    
    
}
