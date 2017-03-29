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
package se.tillvaxtverket.ttsigvalws.ttwsconsole;

import iaik.asn1.ObjectID;
import iaik.asn1.structures.AccessDescription;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.SubjectInfoAccess;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.utils.core.DerefUrl;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.RootInfo;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.ConfigData;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.db.CrlCacheTable;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;

/**
 * This class provides the functions performed by the servlet daemon
 */
public class TrustCache {

    private static final Logger LOG = Logger.getLogger(TrustCache.class.getName());
    private static final SimpleDateFormat tFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String rootlistUrl;
    private CrlCacheTable dbCrlCache;
    private List<String> rootNames;
    private List<X509Certificate> rootCerts;
    private RootInfo rootInfo;
    private boolean initialized;
    private Map<String, X509Certificate> rootMap;
    Map<String, KeyStore> keyStoreMap;
    private final File rootXmlFile;
    private final String trustCacheDirName;

    public TrustCache(ConfigData conf , String rootlistUrl) {
        this.rootlistUrl = rootlistUrl;
        SigValidationBaseModel baseModel = new SigValidationBaseModel(conf);
        dbCrlCache = new CrlCacheTable(FileOps.getfileNameString(conf.getDataDirectory(), "CrlCache/crlDb"));
        trustCacheDirName = FileOps.getfileNameString(conf.getDataDirectory(), "trustCache");
        rootXmlFile = new File(trustCacheDirName, "rootlist.xml");
        if (!rootXmlFile.getParentFile().exists()){
            rootXmlFile.getParentFile().mkdirs();
        }
        File serverDocsDir = new File(conf.getDataDirectory(),"serverdocs");
        if (!serverDocsDir.exists()){
            serverDocsDir.mkdirs();
        }

    }

    private void log(String info) {
        log(info, true, false);
    }

    private void log(String info, boolean verbose, boolean warning) {
        if (verbose) {
            return;
        }

        if (warning) {
            LOG.warning(info);
        } else {
            LOG.info(info);
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
        URL url;
        try {
            url = new URL(rootlistUrl);
        } catch (MalformedURLException ex) {
            Logger.getLogger(TrustCache.class.getName()).log(Level.WARNING, null, ex);
            return;
        }
        DerefUrl.downloadFile(url, rootXmlFile);
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
            //Load Root
            X509Certificate root = rootMap.get(name);
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

}
