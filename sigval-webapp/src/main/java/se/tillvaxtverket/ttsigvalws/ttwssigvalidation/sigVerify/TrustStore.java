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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify;

import com.aaasec.lib.aaacert.AaaCertificate;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AccessDescription;
import iaik.pkcs.PKCS7CertList;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.SubjectInfoAccess;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.iaik.KsCertFactory;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.RootInfo;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.ConfigData;

/**
 * Class holding and loading trust data from the local trust cache. 
 */
public final class TrustStore {

    private static final Logger LOG = Logger.getLogger(TrustStore.class.getName());
    private List<String> rootNames;
    private List<AaaCertificate> rootCerts;
    private RootInfo rootInfo;
    private boolean initialized;
    private Map<String, AaaCertificate> rootMap;
    private Map<String, String> policyDescMap;
    Map<String, KeyStore> keyStoreMap;
    private final File rootXmlFile;
    private final String trustCacheDirName;

    public TrustStore(ConfigData conf) {
        this.keyStoreMap = new HashMap<String, KeyStore>();
        this.rootMap = new HashMap<String, AaaCertificate>();
        this.policyDescMap = new HashMap<String, String>();

        trustCacheDirName = FileOps.getfileNameString(conf.getDataDirectory(), "trustCache");
//        trustCacheDirName = conf.getDataDirectory() + "/trustCache";
        File trustCacheDir = new File(trustCacheDirName);
        rootXmlFile = new File(trustCacheDirName, "rootlist.xml");
        if (trustCacheDir.exists() && conf.isValid()) {
            try {
                getAvailablePolicyRoots();
                keyStoreMap = getCAKeyStores();
            } catch (Exception ex) {
                initialized = false;
            }
        } else {
            
            initialized = false;
        }


    }

    private void getAvailablePolicyRoots() {

        rootInfo = new RootInfo(rootXmlFile);
        if (rootInfo.isInitialized()) {
            rootNames = rootInfo.getCaNames();
            rootCerts = rootInfo.getRootCerts();
            rootMap = rootInfo.getRootMap();
            policyDescMap = rootInfo.getPolicyDescMap();
        }
    }

    private Map<String, KeyStore> getCAKeyStores() throws Exception {
        initialized = false;
        Map<String, KeyStore> ksMap = new HashMap<String, KeyStore>();

        if (rootInfo.isInitialized()) {
            rootNames = rootInfo.getCaNames();
            rootCerts = rootInfo.getRootCerts();
        } else {
            return null;
        }

        for (String name : rootNames) {
            KeyStore ks = null;
            ks = KeyStore.getInstance("jks");
            ks.load(null, null);
            //Load Root
            X509Certificate root = KsCertFactory.getIaikCert(rootMap.get(name).getEncoded());
            ks.setCertificateEntry("Root", KsCertFactory.getCertificate(root));

            //Get caRepository URL from root SIA extension
            SubjectInfoAccess sia = (SubjectInfoAccess) root.getExtension(SubjectInfoAccess.oid);
            AccessDescription accessDesc = sia.getAccessDescription(ObjectID.caRepository);
            String pkcs7Url = accessDesc.getUriAccessLocation();

            //Get referenced pkcs7 file;
            String fileName = pkcs7Url.substring(pkcs7Url.lastIndexOf("/") + 1);
            File pkcs7File = new File(trustCacheDirName, fileName);
            InputStream in = new FileInputStream(pkcs7File);
            PKCS7CertList p7b = new PKCS7CertList(in);
            X509Certificate[] certs = p7b.getCertificateList();


            for (int i = 0; i < certs.length; i++) {
                X509Certificate cert = certs[i];
                String alias = "ICA" + String.valueOf(i);
                ks.setCertificateEntry(alias, KsCertFactory.getCertificate(cert));
            }
            ksMap.put(name, ks);
        }
        initialized = true;
        return ksMap;
    }

    /**
     * Getter for validation policy root certs
     * @return A list of root certificates.
     */
    public List<AaaCertificate> getRootCerts() {
        return rootCerts;
    }

    /**
     * Getter for the root certificates for all validation policies
     * @return a hash map using the policy name as key holding the root certificate for each policy.
     */
    public Map<String, AaaCertificate> getRootMap() {
        return rootMap;
    }

    /**
     * Validation policy descriptions
     * @return A hash map with policy name as key, storing all policy descriptions
     */
    public Map<String, String> getPolicyDescMap() {
        return policyDescMap;
    }

    /**
     * Validation policy names
     * @return A list of all validation policy names, each having a unique root certificate
     */
    public List<String> getRootNames() {
        return rootNames;
    }

    /**
     * Getter for the validation policy key store, holding all issued policy certificates
     * @param policyName The name of the validation policy
     * @return key store for the named validation policy.
     */
    public KeyStore getKeyStore(String policyName) {
        if (initialized && keyStoreMap.containsKey(policyName)) {
            return keyStoreMap.get(policyName);
        } else {
            return null;
        }
    }

    /**
     * The root certificate for a named validation policy
     * @param policyName The name of the validation policy
     * @return root certificate
     */
    public AaaCertificate getRoot(String policyName) {
        if (initialized && rootMap.containsKey(policyName)) {
            return rootMap.get(policyName);
        } else {
            return null;
        }
    }

    /**
     * Status of this trust store
     * @return true of the trust store is initialized, false otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }
}
