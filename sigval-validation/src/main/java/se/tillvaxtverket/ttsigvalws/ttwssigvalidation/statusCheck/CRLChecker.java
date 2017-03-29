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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck;

import se.tillvaxtverket.tsltrust.common.utils.general.ConfigConstants;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.CertVerifyContext;
import java.net.MalformedURLException;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import iaik.asn1.structures.DistributionPoint;
import iaik.x509.X509CRL;
import iaik.x509.X509Certificate;
import iaik.x509.X509ExtensionInitException;
import iaik.x509.extensions.CRLDistributionPoints;
import java.io.File;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CertificateEncodingException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.core.DerefUrl;
import se.tillvaxtverket.tsltrust.common.utils.general.GeneralStaticUtils;
import se.tillvaxtverket.tsltrust.common.utils.core.DbCrlCache;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.ConfigData;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.db.CrlCacheTable;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.ValidationLogger;

/**
 * Implements CRL checking for TSL Trust signature validation. Test if the
 * referenced CRL is in the local cache. It it isn't, then that CRL is added to
 * the CRL cache database and the CRL is cached.
 */
public class CRLChecker extends ValidationLogger implements ConfigConstants {
    
    private String homeDir;
    private String crlDirName;
    private File crlDir;
    private File crlDbFile;
    private String crlDbUrl;
    private String userid = "iaik", password = "iaik";
    private Statement stmt;
    private Connection con;
    private final ConfigData conf;
    private CrlCacheTable dbCrlCache;
    
    public CRLChecker(ConfigData conf) {
        super(15);
        this.conf = conf;
        this.homeDir = conf.getDataDirectory();
        crlDirName = FileOps.getfileNameString(homeDir, "CrlCache");
        crlDir = new File(crlDirName);
        if (!crlDir.canRead()) {
            crlDir.mkdirs();
        }
        crlDbFile = new File(crlDirName, "crlDb");
        dbCrlCache = new CrlCacheTable(crlDbFile.getAbsolutePath());
    }

    /**
     * Extracts CRL distributeion points from certificates, obtains referenced
     * revocation data and updates the CRL cache database.
     *
     * @param cvCont The certificate verification context data holding the
     * necessary metadata for the operation
     */
    public void derefCRL(CertVerifyContext cvCont) {
        X509Certificate cert = cvCont.getChain().get(0);

        //Checking validity
        Calendar present = Calendar.getInstance();
        
        if (cvCont.isNotValidYet() || cvCont.isExpired()) {
            return;
        }
        
        Calendar nextUpd = Calendar.getInstance();
        CRLDistributionPoints cdp;
        X509CRL crl = null;
        List<String> crlKeys = new ArrayList<String>();
        boolean cached = false;
        logString("Checking CRL revocation for certificate issued to:");
        logString(cert.getSubjectDN().getName());
        logString("");
        logString("Dereferencing CRL Dristribution points");
        
        try {
            cdp = (CRLDistributionPoints) cert.getExtension(CRLDistributionPoints.oid);
            if (cdp != null) {
                
                Enumeration dPoints = cdp.getDistributionPoints();

                //For every distribution point
                while (dPoints.hasMoreElements()) {
                    DistributionPoint dp = (DistributionPoint) dPoints.nextElement();
                    String[] uris = dp.getDistributionPointNameURIs();

                    //For every URI
                    for (String uri : uris) {
                        logString("Distribution point URI: " + uri);
                        if (uri.toLowerCase().startsWith("http")) {
                            cached = false;
                            String hash = FnvHash.getFNV1a(uri).toString(16); //Gen FNV 64 bit hash of URI
                            logString("CRL cache key " + hash);
                            File crlFile = new File(crlDir, hash + ".crl");

                            //Cecking in Cache db if this CRL is cached
                            DbCrlCache dbCrl = dbCrlCache.getDbRecord(hash);
                            if (dbCrl != null) {
                                if (GeneralStaticUtils.getTime(dbCrl.getNextUpdate()).after(Calendar.getInstance())) {
                                    cached = true;
                                    crlKeys.add(hash);
                                    cvCont.addCdpUrl(uri);
                                    
                                }
                            }

                            //Log
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            logString((cached) ? "Found cached CRL with Next Update: "
                                    + dateFormat.format(GeneralStaticUtils.getTime(dbCrl.getNextUpdate()).getTime()) : "No recent CRL in cache");

                            //If not cached
                            if (!cached) {
                                URL url;
                                try {
                                    url = new URL(uri);
                                    DerefUrl.downloadFile(url, crlFile);
                                    if (crlFile.canRead()) {
                                        crl = KsCertFactory.getCRL(FileOps.readBinaryFile(crlFile));
                                        if (crl != null) {
                                            logString("Sucessfully downloaded CRL");
                                            DbCrlCache dbc = new DbCrlCache();
                                            dbc.setHash(hash);
                                            dbc.setUrl(uri);
                                            nextUpd.setTime(crl.getNextUpdate());
                                            dbc.setNextUpdate(nextUpd.getTimeInMillis());
                                            cvCont.addCdpUrl(uri);
                                            //Checking if CRL is fresh
                                            if (present.after(nextUpd)) {
                                                logString("CRL with next update " + dateFormat.format(nextUpd.getTime()) + " is not up to date - CRL not cached");
                                            } else {
                                                logString("Cached CRL with Next Update:" + dateFormat.format(nextUpd.getTime()));
                                                crlKeys.add(hash);
                                                dbCrlCache.addOrReplaceRecord(dbc);
//                                                storeCrlCache(dbc);
                                            }
                                            
                                        } else {
                                            logString("Failed downloading CRL");
                                        }
                                    }
                                } catch (MalformedURLException ex) {
                                    logString("Malformed URL");
                                }
                            }
                        } else {
                            logString("Skipping... no \"http:\" URL");
                        }
                        
                    }
                }
            }
        } catch (X509ExtensionInitException ex) {
            logException(ex);
        }
        logString("");
        cvCont.setCrlKeys(crlKeys);
    }
    
    public X509CRL getIaikCRLfromKey(String key) {
        DbCrlCache dbCrl = dbCrlCache.getDbRecord(key);
        if (GeneralStaticUtils.getTime(dbCrl.getNextUpdate()).after(Calendar.getInstance())) {
            File crlFile = new File(crlDir, key + ".crl");
            if (crlFile.canRead()) {
                X509CRL crl = KsCertFactory.getCRL(FileOps.readBinaryFile(crlFile));
                return crl;
            }
        }
        return null;
    }
    
    public byte[] getCRLBytesfromKey(String key) {
        DbCrlCache dbCrl = dbCrlCache.getDbRecord(key);
        if (GeneralStaticUtils.getTime(dbCrl.getNextUpdate()).after(Calendar.getInstance())) {
            File crlFile = new File(crlDir, key + ".crl");
            if (crlFile.canRead()) {
                byte[] crlBytes = FileOps.readBinaryFile(crlFile);
                return crlBytes;
            }
        }
        return null;
    }
    
    public CRL getCRLfromKey(String key) {
        return KsCertFactory.convertCRL(getIaikCRLfromKey(key));
    }
    
    public void checkRevocation(CertVerifyContext cvCont) {
        logString("Checking certificate against CRL");
        boolean good = true;
        cvCont.setCrlStatusDetermined(false);
        cvCont.setRevoked(false);
        
        if (!isParentStatusValid(cvCont)) {
            return;
        }
        
        for (String key : cvCont.getCrlKeys()) {
            X509CRL crl = getIaikCRLfromKey(key);
            List<X509Certificate> chain = cvCont.getChain();
            if (chain == null || chain.size() < 2 || crl == null) {
                return;
            }
            X509Certificate cert = chain.get(0);
            //Debug
//            String certSubjName = chain.get(1).getSubjectX500Principal().getName();
//            if (certSubjName.contains("Test underskrift")){
//                int asdfg=0;
//            }
            
            PublicKey pk = chain.get(1).getPublicKey();
            
            Calendar present = Calendar.getInstance();
            Calendar nextUpdate = Calendar.getInstance();
            nextUpdate.setTime(crl.getNextUpdate());
            if (present.after(nextUpdate)) {
                logString("CRL is not up to date - aborting CRL check");
                return;
            }
            
//            try {
//                boolean bcCheck = BcCRLChecker.validateCertificateByCrl(cert.getEncoded(), getCRLBytesfromKey(key), pk);
//            } catch (Exception ex) {
//                Logger.getLogger(CRLChecker.class.getName()).log(Level.SEVERE, null, ex);
//            }
            
            if (pk != null) {
                try {
                    crl.verify(pk);
                    logString("CRL signed by certificate issuer");
                    logString("Signature on CRL verified");
                } catch (Exception ex) {
                    logString("CRL Signature error: " + ex.getMessage());
                    break;
                }
            } else {
                logString("No CRL Signer public key - CRL could not be checked");
                break;
            }
            if (crlIsInScope(crl, cert)) {
                if (crl.isRevoked(cert)) {
                    cvCont.setRevoked(true);
                    cvCont.setCrlStatusDetermined(true);
                    logString("Certificate is revoked");
                } else {
                    logString("Certificate is NOT revoked");
                    cvCont.setCrlStatusDetermined(true);
                }
            }
        }
        logString("");
        return;
    }
    
    private boolean crlIsInScope(java.security.cert.X509CRL crl, X509Certificate cert) {
        boolean inScope = true;
        return inScope;
    }

    /**
     * Checks if parent certificate is revoked
     *
     * @param cvCont
     * @return
     */
    private boolean isParentStatusValid(CertVerifyContext cvCont) {
        boolean parentValidStatus = true;
        if (cvCont.getIssuingCertContext() == null) {
            return true;
        }
        boolean parentOCSP = false;
        boolean parentCRL = false;
        if (cvCont.getIssuingCertContext().getOcspVerifyContext() != null) {
            if (cvCont.getIssuingCertContext().getOcspVerifyContext().isOcspCheckOK()) {
                parentOCSP = true;
            }
        }
        if (!cvCont.getIssuingCertContext().isRevoked()
                && cvCont.getIssuingCertContext().isCrlStatusDetermined()) {
            parentCRL = true;
        }
        if (!(parentOCSP || parentCRL)) {
            parentValidStatus = false;
            logString("Parent revoked or not checked for validity - aborting revocation check");
        }
        return parentValidStatus;
    }
    
}
