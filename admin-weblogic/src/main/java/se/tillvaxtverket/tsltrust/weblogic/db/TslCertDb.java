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
package se.tillvaxtverket.tsltrust.weblogic.db;

import com.aaasec.lib.aaacert.AaaCertificate;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.TslMetaData;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.tsl.TrustService;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceProvider;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;

/**
 * Abstract class providing access to the TSL trust service database. THis class
 * provides basic functionality for database updates caused by TSL recaching.
 */
public abstract class TslCertDb {

    protected static final Logger LOG = Logger.getLogger(TslCertDb.class.getName());

    public abstract List<TslCertificates> getAllTslCertificate(boolean defaultNull);

    public abstract List<TslCertificates> getAllTslCertificate(String sortID);

    public List<TslCertificates> getNewCertificates(List<TslCertificates> dbList, List<TslMetaData> tslMdList, LogDbUtil log) {
        List resultList = new LinkedList<TslCertificates>();

        List<BigInteger> dbCertIdList = new LinkedList<BigInteger>();
        for (TslCertificates dbCert : dbList) {
            try {
                //BigInteger dbCertID = dbCert.getTslCertHash() + dbCert.getTrustServiceType().trim();
                BigInteger dbCertID = FnvHash.getFNV1a(dbCert.getTslCertHash() + dbCert.getTrustServiceType().trim() + dbCert.getTsName().trim());
                dbCertIdList.add(dbCertID);
            } catch (Exception ex) {
            }
        }

        for (TslMetaData tslMD : tslMdList) {
            for (TrustServiceProvider tsp : tslMD.getTsl().getTrustServiceProviders()) {
                for (TrustService ts : tsp.getTrustServices()) {
                    try {
                        byte[] cert = ts.getServiceDigitalIdentityData();
                        AaaCertificate x509Cert = new AaaCertificate(cert);
                        String certHash = FnvHash.getFNV1aToHex(cert);
                        //String tslCertID = certHash + ts.getType().trim();
                        BigInteger tslCertID = FnvHash.getFNV1a(certHash + ts.getType().trim() + ts.getName().trim());

                        if (!dbCertIdList.contains(tslCertID)) {
                            TslCertificates tc = new TslCertificates();
                            tc.setTspName(tsp.getName().trim());
                            tc.setTsName(ts.getName().trim());
                            tc.setServiceStatus(ts.getStatus().trim());
                            tc.setTerritory(tslMD.getTsl().getSchemeTerritory());
                            tc.setTrustServiceType(ts.getType().trim());
                            tc.setTslCertHash(certHash);
                            tc.setTslCertificate(Base64Coder.encodeLines(cert));
                            if (tslMD.getTsl().getIssueDate() != null) {
                                tc.setTslDate(tslMD.getTsl().getIssueDate().getTime());
                            }
                            if (tslMD.getTsl().getNextUpdate() != null) {
                                tc.setTslExpDate(tslMD.getTsl().getNextUpdate().getTime());
                            }
                            tc.setSdiType(CertificateUtils.getSdiType(x509Cert));
                            tc.setCertExpiry(x509Cert.getNotAfter().getTime());
                            tc.setTslSeqNo(tslMD.getTsl().getSequenceNumber().toString());
                            tc.setTslSha1(tslMD.getTsl().getSha1Fingerprint());
                            tc.setExtractorStatus("present");
                            tc.setSignStatus(tslMD.getSignStatus());

                            resultList.add(tc);
                        }
                    } catch (Exception ex) {
                    }

                }
            }
        }
        log.addConsoleEvent(new ConsoleLogRecord("TSL Recache", resultList.size() + " new Trust Services", "TSL Extractor"));
        return resultList;
    }

    public static List<TslCertificates> getChangedCertificates(List<TslCertificates> dbList, List<TslMetaData> tslMdList, LogDbUtil log) {
        List changedList = new LinkedList<TslCertificates>();
        Map<String, TslMetaData> tslMdMap = new HashMap<String, TslMetaData>();
        Map<String, TrustServiceProvider> tspMap = new HashMap<String, TrustServiceProvider>();
        Map<String, TrustService> tsMap = new HashMap<String, TrustService>();

        //For each TSL Metadata record
        for (TslMetaData tslMD : tslMdList) {
            // For each Trust Service Provider in each TSL
            for (TrustServiceProvider tsp : tslMD.getTsl().getTrustServiceProviders()) {
                //For each Trust Service
                for (TrustService ts : tsp.getTrustServices()) {
                    byte[] tslCert = ts.getServiceDigitalIdentityData();
                    if (tslCert != null) { // If trust service has certificate
                        String tslCertID = getCertId(tslCert, ts, tsp, tslMD);
                        //String tslCertHash = FnvHash.getFNV1aToHex(tslCert); // Get cert FNV1a hash
                        //String tslCertID = tslCertHash + ts.getType().trim()+tslMD.getCountry().getIsoCode().toUpperCase();

                        //debug
                        if (tslMdMap.containsKey(tslCertID)) {
                            TrustService duplServ = tsMap.get(tslCertID);
                            String dupName = duplServ.getName();
                            TrustServiceProvider duplTsp = tspMap.get(tslCertID);
                            String dupTspName = duplTsp.getName();

                            String tspName = tsp.getName();
                            String srvName = ts.getName();

                            int asdf = 0;
                        }

                        tslMdMap.put(tslCertID, tslMD);
                        tspMap.put(tslCertID, tsp);
                        tsMap.put(tslCertID, ts);
                    }
                }
            }
        }

        // Fore each database record
        for (TslCertificates dbCert : dbList) {
            // Get compare string for each cert = cert hash + service type
            //String dbCertID = dbCert.getTslCertHash() + dbCert.getTrustServiceType().trim()+dbCert.getTerritory().toUpperCase();

            String dbCertID = getCertId(dbCert);

            if (tslMdMap.containsKey(dbCertID)) {
                TslMetaData tslMD = tslMdMap.get(dbCertID);
                TrustServiceProvider tsp = tspMap.get(dbCertID);
                TrustService ts = tsMap.get(dbCertID);
                // If TSL hash has changed = updated record
                if (!tslMD.getTsl().getSha1Fingerprint().equalsIgnoreCase(dbCert.getTslSha1())
                        || !tslMD.getSignStatus().equals(dbCert.getSignStatus())) {

                    dbCert.setTspName(tsp.getName().trim());
                    dbCert.setTsName(ts.getName().trim());
                    dbCert.setServiceStatus(ts.getStatus().trim());
                    dbCert.setTerritory(tslMD.getTsl().getSchemeTerritory());
                    dbCert.setTrustServiceType(ts.getType().trim());
                    if (tslMD.getTsl().getIssueDate() != null) {
                        dbCert.setTslDate(tslMD.getTsl().getIssueDate().getTime());
                    }
                    if (tslMD.getTsl().getNextUpdate() != null) {
                        dbCert.setTslExpDate(tslMD.getTsl().getNextUpdate().getTime());
                    }
                    dbCert.setTslSeqNo(tslMD.getTsl().getSequenceNumber().toString());
                    dbCert.setTslSha1(tslMD.getTsl().getSha1Fingerprint());
                    dbCert.setExtractorStatus("present");
                    dbCert.setSignStatus(tslMD.getSignStatus());

                    changedList.add(dbCert);
                }
            }

        }

        log.addConsoleEvent(new ConsoleLogRecord("TSL Recache", changedList.size() + " Trust Services with updated TSL info", "TSL Extractor"));
        return changedList;
    }

    private static String getCertId(TslCertificates dbCert) {
        byte[] certBytes = Base64Coder.decodeLines(dbCert.getTslCertificate());
        return getCertId(certBytes, dbCert.getTrustServiceType(), dbCert.getTsName(), dbCert.getTspName(), dbCert.getTerritory());
    }

    private static String getCertId(byte[] tslCert, TrustService ts, TrustServiceProvider tsp, TslMetaData tslMD) {
        return getCertId(tslCert, ts.getType(), ts.getName(), tsp.getName(), tslMD.getCountry().getIsoCode());
    }
    
    private static String getCertId (byte[] certBytes, String serviceType, String serviceName, String tspName, String territory){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(certBytes);
            md.update(serviceType.trim().toLowerCase().getBytes("UTF-8"));
            md.update(serviceName.trim().toLowerCase().getBytes("UTF-8"));
            md.update(tspName.trim().toLowerCase().getBytes("UTF-8"));
            md.update(territory.trim().toLowerCase().getBytes("UTF-8"));
            String certId = String.valueOf(Base64Coder.encode(md.digest()));
            return certId;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<TslCertificates> getAbsentCertificates(List<TslCertificates> dbList, List<TslMetaData> tslMdList, LogDbUtil log) {

        List<BigInteger> tslCertIdList = new LinkedList<BigInteger>();

        //For each TSL Metadata record
        for (TslMetaData tslMD : tslMdList) {
            // For each Trust Service Provider in each TSL
            for (TrustServiceProvider tsp : tslMD.getTsl().getTrustServiceProviders()) {
                //For each Trust Service
                for (TrustService ts : tsp.getTrustServices()) {
                    byte[] tslCert = ts.getServiceDigitalIdentityData();
                    if (tslCert != null) { // If trust service has certificate
                        String tslCertHash = FnvHash.getFNV1aToHex(tslCert); // Get cert FNV1a hash
                        // Fore each database record
                        BigInteger tslCertID = FnvHash.getFNV1a(tslCertHash + ts.getType().trim() + ts.getName().trim());
                        tslCertIdList.add(tslCertID);
                    }
                }
            }
        }
        for (TslCertificates dbCert : dbList) {
            // Get compare string for each cert = cert hash + service type
            BigInteger dbCertID = FnvHash.getFNV1a(dbCert.getTslCertHash() + dbCert.getTrustServiceType().trim() + dbCert.getTsName().trim());
            // Record exist if IDs match
            if (tslCertIdList.contains(dbCertID)) {
                dbCert.setExtractorStatus("present");
            } else {
                dbCert.setExtractorStatus("absent");
            }
        }
        return dbList;
    }

    public abstract void addCertificates(List<TslCertificates> newCertList, LogDbUtil log);

    public abstract void updateCertificates(List<TslCertificates> changedCerts, LogDbUtil log);

    public abstract void updateAbsentStatus(List<TslMetaData> tslMdList, LogDbUtil log);

    public abstract int deleteAbsentStatusRecords(List<TslMetaData> tslMdList, LogDbUtil log);
}
