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
package se.tillvaxtverket.tsltrust.weblogic.db;

import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.TslMetaData;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.tsl.TrustService;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceProvider;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;

/**
 * Abstract class providing access to the TSL trust service database.
 * THis class provides basic functionality for database updates caused by 
 * TSL recaching.
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
                        X509Certificate x509Cert = KsCertFactory.getIaikCert(cert);
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
                        String tslCertHash = FnvHash.getFNV1aToHex(tslCert); // Get cert FNV1a hash
                        String tslCertID = tslCertHash + ts.getType().trim()+tslMD.getCountry().getIsoCode().toUpperCase();
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
            String dbCertID = dbCert.getTslCertHash() + dbCert.getTrustServiceType().trim()+dbCert.getTerritory().toUpperCase();
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
