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
