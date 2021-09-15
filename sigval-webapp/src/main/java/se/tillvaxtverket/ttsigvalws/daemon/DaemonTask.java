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
package se.tillvaxtverket.ttsigvalws.daemon;

import com.aaasec.lib.aaacert.AaaCertificate;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AccessDescription;
import iaik.x509.X509CRL;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.SubjectInfoAccess;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tillvaxtverket.tsltrust.common.iaik.KsCertFactory;
import se.tillvaxtverket.tsltrust.common.utils.core.DbCrlCache;
import se.tillvaxtverket.tsltrust.common.utils.core.DerefUrl;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.GeneralStaticUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.RootInfo;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.db.CrlCacheTable;

/**
 * This class provides the functions performed by the servlet daemon
 */
public class DaemonTask extends ServletDaemon {

    private final static String CRL_CACHE_MODE_INSTANT = "instant";
    private final static String CRL_CACHE_MODE_HALF_WAY = "halftime";
    private final static String CRL_CACHE_MODE_EXPIRY = "expiry";
    private static final SimpleDateFormat tFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private CrlCacheTable dbCrlCache;
    private String crlMode;
    private long idleTime;
    private List<String> rootNames;
    private List<AaaCertificate> rootCerts;
    private RootInfo rootInfo;
    private boolean initialized;
    private Map<String, AaaCertificate> rootMap;
    Map<String, KeyStore> keyStoreMap;
    private final File rootXmlFile;
    private final String trustCacheDirName;


    public DaemonTask(ContextParameters contextparams) {
        super(contextparams);
        dbCrlCache = contextparams.getDbCrlCache();
        crlMode = contextparams.getCrlCacheMode();
        idleTime = contextparams.getDaemonIdleTime();
        trustCacheDirName = contextParams.getTrustDirName();
        rootXmlFile = new File(trustCacheDirName, "rootlist.xml");
    }

    @Override
    void doDaemonTask() {
        long startTime = System.currentTimeMillis();
        refreshTrustCache();
        LOG.info("Trust information (Root and intermediary certificates) recached from trust store");
        crlCacheTask();
        
        long elapsed = System.currentTimeMillis()-startTime;
        String nextUpdate = "Next recache scheduled at "+tFormat.format(new Date(System.currentTimeMillis()+idleTime-elapsed));

        LOG.info(alive ? "CRL recache completed. " +nextUpdate: "CRL recache interrupted. "+nextUpdate);
        taskComplete = true;
    }

    private void log(String info) {
        log(info, true, false);
    }

    private void log(String info, boolean verbose, boolean warning) {
        if (verbose && !contextParams.isVerboseLogging()) {
            return;
        }

        if (verbose) {
            if (warning) {
                LOG.warning(info);
            } else {
                System.out.println(info);
            }
        } else {
            if (warning) {
                LOG.warning(info);
            } else {
                LOG.info(info);
            }
        }
    }

    public void refreshTrustCache() {
        cachePolicyRoots();
        getAvailablePolicyRoots();
        try {
            cacheCApkcs7Files();
            log("CA certificates re-cached");
        } catch (Exception ex) {
            log(ex.getLocalizedMessage(), true, true);
        }
    }

    private void cachePolicyRoots() {
        if (contextParams.isNoCacheRootList()){
            LOG.info("No-cache of rootlist is set. Using local file");
            return;
        }
        DerefUrl.downloadFile(contextParams.getTrustInfoUrl(), rootXmlFile);
        log("Caching Trust Anchor List");
        initialized = true;
    }

    private void getAvailablePolicyRoots() {

        rootInfo = new RootInfo(rootXmlFile);
        if (rootInfo.isInitialized()) {
            rootNames = rootInfo.getCaNames();
            rootCerts = rootInfo.getRootCerts();
            rootMap = rootInfo.getRootMap();
        }
    }

    private void cacheCApkcs7Files() throws Exception {

        if (rootInfo.isInitialized()) {
            rootNames = rootInfo.getCaNames();
            rootCerts = rootInfo.getRootCerts();
        } else {
            return;
        }
        log("Caching trusted certificate issued under each policy:");
        int rootCount = rootMap.size();
        int counter = 0;
        for (String name : rootNames) {
            if (!alive){
                break;
            }
            //Load Root
            X509Certificate root = KsCertFactory.getIaikCert(rootMap.get(name).getEncoded());
            log("Policy: " + name + ",  Root: " + root.getSubjectDN().getName());

            //Get caRepository URL from root SIA extension
            SubjectInfoAccess sia = (SubjectInfoAccess) root.getExtension(SubjectInfoAccess.oid);
            AccessDescription accessDesc = sia.getAccessDescription(ObjectID.caRepository);
            String pkcs7Url = accessDesc.getUriAccessLocation();

            //Get referenced pkcs7 file;
            String fileName = pkcs7Url.substring(pkcs7Url.lastIndexOf("/") + 1);
            File pkcs7File = new File(trustCacheDirName, fileName);
            DerefUrl.downloadFile(new URL(pkcs7Url), pkcs7File);
        }
    }

    /**
     * CRL Cache task
     */
    private void crlCacheTask() {

        StringBuilder sb = new StringBuilder();

        List<DbCrlCache> crlcacheList = dbCrlCache.getAllRecords();
        log("Recach mode: " + contextParams.getCrlCacheMode());
        int count = 0;
        for (DbCrlCache dbCrl : crlcacheList) {
            if (!alive) {
                break;
            }
            String urlString = dbCrl.getUrl();
            String key = dbCrl.getHash();
            X509CRL crl = getIaikCRLfromKey(key);
            long currentTime = System.currentTimeMillis();
            long nextUpdateTime = (crl != null && crl.getNextUpdate() != null) ? crl.getNextUpdate().getTime() : currentTime;
            long thisUpdateTime = (crl != null && crl.getThisUpdate() != null) ? crl.getThisUpdate().getTime() : currentTime;
            long halfTime = thisUpdateTime + ((nextUpdateTime - thisUpdateTime) / 2);
            boolean recache = false;
            currentTime += 1000; // Increase current time with 1 sec to make it bigger than the default values set above.

            if (crlMode.equals(CRL_CACHE_MODE_INSTANT)) {
                recache = true;
            }
            if (crlMode.equals(CRL_CACHE_MODE_HALF_WAY)) {
                if (currentTime > halfTime) {
                    recache = true;
                }
            }
            if (crlMode.equals(CRL_CACHE_MODE_EXPIRY)) {
                if (currentTime > (nextUpdateTime - idleTime * 2)) {
                    recache = true;
                }
            }
            if (recache) {
                log(tFormat.format(new Date()) + " Re-caching crl at: " + urlString);
                log((crl == null ? "Null CRL! -- " : "") + "This update: " + tFormat.format(new Date(thisUpdateTime))
                        + " Next update: " + tFormat.format(new Date(nextUpdateTime))
                        + " Half time: " + tFormat.format(new Date(halfTime)));
                recacheCrl(key, urlString);

                //Cleanup
                //If CRL is still NULL after recache, then delete cache record.
                crl = getIaikCRLfromKey(key);
                if (crl == null) {
                    dbCrlCache.deteleDbRecord(dbCrl);
                    log("Removed CRL record from cache");
                }

            } else {
                log(tFormat.format(new Date()) + " Not re-caching crl at: " + urlString);
                log("");
            }
            int complete = (++count * 100) / crlcacheList.size();
        }
    }

    private void recacheCrl(String hash, String uri) {
        SimpleDateFormat tFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar nextUpd = Calendar.getInstance();
        Calendar present = Calendar.getInstance();
        File crlFile = new File(contextParams.getCrlDirName(), hash + ".crl");

        URL url;
        try {
            url = new URL(uri);
            DerefUrl.downloadFile(url, crlFile);
            if (crlFile.canRead()) {
                X509CRL crl = KsCertFactory.getCRL(FileOps.readBinaryFile(crlFile));
                if (crl != null) {
                    DbCrlCache dbc = new DbCrlCache();
                    dbc.setHash(hash);
                    dbc.setUrl(uri);
                    nextUpd.setTime(crl.getNextUpdate());
                    dbc.setNextUpdate(nextUpd.getTimeInMillis());
                    //Checking if CRL is fresh
                    if (present.after(nextUpd)) {
                        log("CRL with next update " + tFormat.format(nextUpd.getTime()) + " is not up to date - CRL not cached");
                        dbCrlCache.deleteRecord("Hash", hash);
                    } else {
                        log("Sucessfully downloaded Cached CRL with Next Update:" + tFormat.format(nextUpd.getTime()));
                        dbCrlCache.addOrReplaceRecord(dbc);
                    }

                } else {
                    log("Failed to download and parse the CRL", false, true);
                }
            }
        } catch (MalformedURLException ex) {
            log("Malformed URL", false, true);
            dbCrlCache.deleteRecord("Hash", hash);
        }
    }

    public X509CRL getIaikCRLfromKey(String key) {
        DbCrlCache dbCrl = dbCrlCache.getDbRecord(key);
        if (dbCrl == null) {
            return null;
        }
        if (GeneralStaticUtils.getTime(dbCrl.getNextUpdate()).after(Calendar.getInstance())) {
            File crlFile = new File(contextParams.getCrlDirName(), key + ".crl");
            if (crlFile.canRead()) {
                X509CRL crl = KsCertFactory.getCRL(FileOps.readBinaryFile(crlFile));
                return crl;
            }
        }
        return null;
    }
}
