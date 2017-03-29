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
package se.tillvaxtverket.tsltrust.webservice.daemon.ca;

import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.WebXmlConstants;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCert;
import se.tillvaxtverket.tsltrust.weblogic.data.ExternalCert;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.ValidationPolicy;
import se.tillvaxtverket.tsltrust.weblogic.db.CaSQLiteUtil;
import se.tillvaxtverket.tsltrust.weblogic.db.LogDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDb;
import se.tillvaxtverket.tsltrust.weblogic.db.ValPoliciesDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.PolicyUtils;
import iaik.pkcs.PKCS7CertList;
import iaik.pkcs.PKCSException;
import iaik.x509.X509Certificate;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;
import se.tillvaxtverket.tsltrust.webservice.daemon.ContextParameters;

/**
 * This class determines which trust services that are compliant with the policies defined in the TSL Trust administration web service  
 */
public class CertAuthOperations implements WebXmlConstants {

    private static final Logger LOG = Logger.getLogger(CertAuthOperations.class.getName());
    private final TslTrustModel model;
    private TslTrustConfig conf;
    private final ValPoliciesDbUtil policyDb;
    private final TslCertDb tslCertDb;
    private final LogDbUtil logDb;
    private final PolicyUtils policyUtils;
    private Map<String, Map<BigInteger, X509Certificate>> tslCertsMap = new HashMap<String, Map<BigInteger, X509Certificate>>();
    private Map<String, String> caDirectories = new HashMap<String, String>();
    ;
    private List<ValidationPolicy> validationPolicies;
    private Map<String, CertificationAuthority> caMap;

    /**
     * Constructor
     */
    public CertAuthOperations() {
        this.model = ContextParameters.getModel();
        conf = (TslTrustConfig) model.getConf();
        logDb = model.getLogDb();
        tslCertDb = model.getTslCertDb();
        policyDb = model.getPolicyDb();
        policyUtils = new PolicyUtils(model);
    }

    private void con(String description) {
        con("Policy CA", description);
    }

    private void con(String event, String description) {
        logDb.addConsoleEvent(new ConsoleLogRecord(event, description, "daemon"));
    }

    public void run() {
        // Get caDirectories and base data
        con("Certificate maintenance operations");
        con("Getting base CA data...");
        if (!getBaseData()) {
            return;
        }
        // Create CA if necessary
        con("Deleting and creating CAs...");
        createOrDeleteCAs();
        // Get compliant certs
        con("Getting policy compliant certificates...");
        getCompliantCerts();
        // Get revoke action list and revoke
        con("Revoking non conformant certificates...");
        revokeNonConformantCerts();
        // Get issue action list and issue certs
        con("Issuing new conformant certificates...");
        issueMissingCerts();
        // Publish data
        con("Publishing data...");
        publishRootXMLFile();

        con("CA updates completed");

    }

    private void getCompliantCerts() {
        tslCertsMap.clear();
        List<ExternalCert> externalCerts = policyDb.getExternalCerts();
        // Get compliant certs
        for (ValidationPolicy vp : validationPolicies) {
            if (vp.getStatus().equals(ValidationPolicy.ENABLE_STATE)) {
                Map<BigInteger, X509Certificate> policyCertMap = new HashMap<BigInteger, X509Certificate>();
                List<TslCertificates> policyCompliantCerts = policyUtils.getPolicyCompliantCerts(vp);
                for (TslCertificates tc : policyCompliantCerts) {
                    X509Certificate cert = CertificateUtils.getCertificate(tc.getTslCertificate());
                    if (cert != null) {
                        policyCertMap.put(key(cert.getPublicKey().getEncoded()), cert);
                    }
                }
                List<String> addCertIds = vp.getAddCertIds();
                for (ExternalCert extCert : externalCerts) {
                    String certId = extCert.getCertificateId();
                    if (addCertIds.contains(certId)) {
                        X509Certificate cert = extCert.getCert();
                        if (cert != null) {
                            BigInteger pkID = key(cert.getPublicKey().getEncoded());
                            if (!policyCertMap.containsKey(pkID)) {
                                policyCertMap.put(pkID, cert);
                            }
                        }
                    }
                }
                tslCertsMap.put(vp.getPolicyName(), policyCertMap);
            }
        }
    }

    private void createOrDeleteCAs() {
        // Delete CAs
        for (ValidationPolicy vp : validationPolicies) {
            String caDir = caDirectories.get(vp.getPolicyName());
            File caDirectory = new File(caDir);
            String status = vp.getStatus();
            if (status.equals(ValidationPolicy.REMOVE_STATE)) {
                con("Delete CA", "Permanently Deleting " + vp.getPolicyName());
                FileUtils.deleteQuietly(caDirectory);
                policyDb.deteleRecord(vp);
                deleteExportFiles(vp.getPolicyName());
            }
            if (status.equals(ValidationPolicy.RECONSTRUCT_STATE)) {
                con("Delete CA", "Deleting " + vp.getPolicyName() + " for rebuild action");
                FileUtils.deleteQuietly(caDirectory);
                vp.setStatus(ValidationPolicy.ENABLE_STATE);
                policyDb.addOrReplaceValidationPolicy(vp, true);
                deleteExportFiles(vp.getPolicyName());
            }
            if (status.equals(ValidationPolicy.DISABLE_STATE)) {
                con("Disabled policy", vp.getPolicyName());
            }
        }
        //Create or open CA for all policies
        caMap = new HashMap<String, CertificationAuthority>();
        validationPolicies = policyDb.getValidationPolicies();
        for (ValidationPolicy vp : validationPolicies) {
            if (vp.getStatus().equals(ValidationPolicy.ENABLE_STATE)) {
                String name = vp.getPolicyName();
                String caDir = caDirectories.get(name);
                CertificationAuthority ca = new CertificationAuthority(name, caDir, model);
                ca.initKeyStore();
                if (!ca.isInitialized()) {
                    con("Create CA", "Generate new CA: " + vp.getPolicyName());
                    RootCAFactory.start(name, caDir, model);
                    ca.initKeyStore();
                }
                if (ca.isInitialized()) {
                    con("CA initialized", vp.getPolicyName());
                    caMap.put(name, ca);
                }
            }
        }
    }

    private boolean getBaseData() {
        // Test existence of the CA export directory
        File exportCrlDir = new File(conf.getCaFileStorageLocation(), "crl");
        File exportCertDir = new File(conf.getCaFileStorageLocation(), "certs");
        if (!exportCrlDir.exists()) {
            if (!exportCrlDir.mkdirs()) {
                return false;
            }
        }
        if (!exportCertDir.exists()) {
            exportCertDir.mkdirs();
        }
        // Test existence of the CA storage directory
        String caDirName = FileOps.getfileNameString(model.getDataLocation(), "CA");
        File caDir = new File(caDirName);
        if (!caDir.exists()) {
            if (!caDir.mkdirs()) {
                return false;
            }
        }
        caDirectories.clear();
        validationPolicies = policyDb.getValidationPolicies();
        for (ValidationPolicy vp : validationPolicies) {
            String pName = vp.getPolicyName();
            String dir = caDirName + "/" + pName;
            caDirectories.put(pName, dir);
        }
        return true;
    }

    private void revokeNonConformantCerts() {
        for (ValidationPolicy vp : validationPolicies) {
            if (vp.getStatus().equals(ValidationPolicy.ENABLE_STATE)) {
                int count = 0;
                String pName = vp.getPolicyName();
                Map<BigInteger, X509Certificate> policyCertMap = tslCertsMap.get(pName);
                CertificationAuthority ca = caMap.get(pName);
                String caDir = caDirectories.get(pName);
                List<DbCert> dbCertificates = CaSQLiteUtil.getCertificates(caDir, false);
                for (DbCert dbCert : dbCertificates) {
                    X509Certificate cert = dbCert.getCertificate();
                    BigInteger dbPkId = key(cert.getPublicKey().getEncoded());
                    if (!policyCertMap.containsKey(dbPkId)) {
                        //issued certificates is not in the list of policycompliant certs. Revoke
                        dbCert.setRevoked(DbCert.REVOKED);
                        dbCert.setRevDate(System.currentTimeMillis());
                        CaSQLiteUtil.replaceCertificate(dbCert, caDir);
                        count++;
                    }
                }
                if (count > 0) {
                    con("Revocation", "Revoking " + String.valueOf(count) + " certificates from " + vp.getPolicyName());
                }
                ca.revokeCertificates();
            }
        }
    }

    private void issueMissingCerts() {
        for (ValidationPolicy vp : validationPolicies) {
            if (vp.getStatus().equals(ValidationPolicy.ENABLE_STATE)) {
                int count = 0;
                String pName = vp.getPolicyName();
                Map<BigInteger, X509Certificate> policyCertMap = tslCertsMap.get(pName);
                CertificationAuthority ca = caMap.get(pName);
                String caDir = caDirectories.get(pName);
                List<DbCert> dbCertificates = CaSQLiteUtil.getCertificates(caDir, false);
                List<BigInteger> issuedCertIdList = getIssuedCertIDs(dbCertificates);
                Set<BigInteger> policyCertIdSet = policyCertMap.keySet();
                for (BigInteger certId : policyCertIdSet) {
                    if (!issuedCertIdList.contains(certId)) {
                        //Certificate is missing. Issue certificates
                        ca.issueXCert(policyCertMap.get(certId));
                        count++;
                    }
                }
                if (count > 0) {
                    con("Issue", String.valueOf(count) + " new certificates issued for " + vp.getPolicyName());
                }
                exportCerts(ca);
            }
        }
    }

    private List<BigInteger> getIssuedCertIDs(List<DbCert> dbCertificates) {
        List<BigInteger> dbCertIDs = new LinkedList<BigInteger>();
        for (DbCert dbCert : dbCertificates) {
            X509Certificate cert = dbCert.getCertificate();
            BigInteger certId = key(cert.getPublicKey().getEncoded());
            dbCertIDs.add(certId);
        }
        return dbCertIDs;
    }

    private void exportCerts(CertificationAuthority ca) {
        List<DbCert> isssueList = ca.getAllCertificates(false);
        X509Certificate[] certificates = new X509Certificate[isssueList.size()];
        for (int i = 0; i < isssueList.size(); i++) {
            DbCert dbCert = isssueList.get(i);
            certificates[i] = dbCert.getCertificate();
        }
        PKCS7CertList pkcs7 = new PKCS7CertList();
        pkcs7.setCertificateList(certificates);
        File localCertFile = new File(ca.getCaDir(), ca.getCaID() + ".p7b");
        File exportCertFile = new File(FileOps.getfileNameString(conf.getCaFileStorageLocation(), "certs"), ca.getCaID() + ".p7b");
        try {
            FileOps.saveByteFile(pkcs7.toByteArray(), localCertFile);
            FileOps.saveByteFile(pkcs7.toByteArray(), exportCertFile);
        } catch (PKCSException ex) {
            LOG.log(Level.WARNING, null, ex);
        }

    }

    private void publishRootXMLFile() {
        List<CertificationAuthority> caList = new ArrayList<CertificationAuthority>();
        Set<String> keySet = caMap.keySet();
        for (String caKey : keySet) {
            caList.add(caMap.get(caKey));
        }
        String rootXML = RootXMLFactory.generateRootInfo(caList, validationPolicies);
        File xmlFile = new File(conf.getCaFileStorageLocation(), "rootlist.xml");
        FileOps.saveTxtFile(xmlFile, rootXML);
    }

    private BigInteger key(byte[] data) {
        return FnvHash.getFNV1a(data);
    }

    private void deleteExportFiles(String policyName) {
        String caID = FnvHash.getFNV1aToHex(policyName);
        File exportCrlFile = new File(FileOps.getfileNameString(conf.getCaFileStorageLocation(), "crl"), caID + ".crl");
        File exportCertFile = new File(FileOps.getfileNameString(conf.getCaFileStorageLocation(), "certs"), caID + ".p7b");
        FileUtils.deleteQuietly(exportCrlFile);
        FileUtils.deleteQuietly(exportCertFile);
    }
}
