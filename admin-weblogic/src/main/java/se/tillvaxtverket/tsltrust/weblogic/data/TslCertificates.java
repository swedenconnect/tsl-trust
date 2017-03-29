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
package se.tillvaxtverket.tsltrust.weblogic.data;
// Generated Jun 2, 2010 7:43:41 AM by Hibernate Tools 3.2.1.GA

/**
 * TSL trust service database record data class
 */
public class TslCertificates implements java.io.Serializable {

    private Integer id;
    private String tspName;
    private String tsName;
    private String territory;
    private String trustServiceType;
    private String serviceStatus;
    private long tslDate;
    private long tslExpDate;
    private String tslCertHash;
    private String tslCertificate;
    private short sdiType;
    private long certExpiry;
    private String tslSeqNo;
    private String tslSha1;
    private String extractorStatus;
    private String signStatus;
    public static final String[] SDI_TYPE = new String[]{"EE","CA", "Root", "QcEE","QcCA", "QcRoot"};

    public TslCertificates() {
    }

    public TslCertificates(String tspName, String tsName, String territory, String trustServiceType, 
            String serviceStatus, long tslDate, String tslCertHash, String tslCertificate, short sdiType, 
            String tslSeqNo, String tslSha1, String extractorStatus) {
        this.tspName = tspName;
        this.tsName = tsName;
        this.territory = territory;
        this.trustServiceType = trustServiceType;
        this.serviceStatus = serviceStatus;
        this.tslDate = tslDate;
        this.tslCertHash = tslCertHash;
        this.tslCertificate = tslCertificate;
        this.sdiType = sdiType;
        this.tslSeqNo = tslSeqNo;
        this.tslSha1 = tslSha1;
        this.extractorStatus = extractorStatus;
    }

    public String getSdiTypeString() {
        try {
            return SDI_TYPE[sdiType];
        } catch (Exception ex) {
            return "Unknown";
        }
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTspName() {
        return this.tspName;
    }

    public void setTspName(String tspName) {
        this.tspName = tspName;
    }

    public String getTsName() {
        return this.tsName;
    }

    public void setTsName(String tsName) {
        this.tsName = tsName;
    }

    public String getTerritory() {
        return this.territory;
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public String getTrustServiceType() {
        return this.trustServiceType;
    }

    public void setTrustServiceType(String trustServiceType) {
        this.trustServiceType = trustServiceType;
    }

    public String getServiceStatus() {
        return this.serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public long getTslDate() {
        return this.tslDate;
    }

    public void setTslDate(long tslDate) {
        this.tslDate = tslDate;
    }

    public String getTslCertHash() {
        return this.tslCertHash;
    }

    public void setTslCertHash(String tslCertHash) {
        this.tslCertHash = tslCertHash;
    }

    public String getTslCertificate() {
        return this.tslCertificate;
    }

    public void setTslCertificate(String tslCertificate) {
        this.tslCertificate = tslCertificate;
    }

    public short getSdiType() {
        return this.sdiType;
    }

    public void setSdiType(short sdiType) {
        this.sdiType = sdiType;
    }

    public String getTslSeqNo() {
        return this.tslSeqNo;
    }

    public void setTslSeqNo(String tslSeqNo) {
        this.tslSeqNo = tslSeqNo;
    }

    public String getTslSha1() {
        return this.tslSha1;
    }

    public void setTslSha1(String tslSha1) {
        this.tslSha1 = tslSha1;
    }

    public String getExtractorStatus() {
        return this.extractorStatus;
    }

    public void setExtractorStatus(String extractorStatus) {
        this.extractorStatus = extractorStatus;
    }

    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    public long getTslExpDate() {
        return tslExpDate;
    }

    public void setTslExpDate(long tslExpDate) {
        this.tslExpDate = tslExpDate;
    }

    public long getCertExpiry() {
        return certExpiry;
    }

    public void setCertExpiry(long certExpiry) {
        this.certExpiry = certExpiry;
    }    
}
