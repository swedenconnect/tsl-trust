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
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCALog;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCAParam;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCert;
import se.tillvaxtverket.tsltrust.weblogic.db.CaSQLiteUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.DistributionPoint;
import iaik.asn1.structures.GeneralName;
import iaik.asn1.structures.GeneralNames;
import iaik.asn1.structures.Name;
import iaik.asn1.structures.PolicyInformation;
import iaik.asn1.structures.PolicyQualifierInfo;
import iaik.utils.Util;
import iaik.x509.RevokedCertificate;
import iaik.x509.V3Extension;
import iaik.x509.X509CRL;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.AuthorityInfoAccess;
import iaik.x509.extensions.AuthorityKeyIdentifier;
import iaik.x509.extensions.CRLDistributionPoints;
import iaik.x509.extensions.CRLNumber;
import iaik.x509.extensions.CertificatePolicies;
import iaik.x509.extensions.IssuerAltName;
import iaik.x509.extensions.IssuingDistributionPoint;
import iaik.x509.extensions.ReasonCode;
import iaik.x509.extensions.SubjectKeyIdentifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRLEntry;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import iaik.x509.extensions.BasicConstraints;
import iaik.x509.extensions.PolicyConstraints;
import iaik.x509.extensions.PolicyMappings;
import iaik.x509.extensions.qualified.QCStatements;
import java.util.Date;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;

/**
 * This class implements the Certification Authority logic of TSL Trust.
 * Certificates compliant with defined policies are certified under a defined policy Root created
 * by TSL Trust.
 */
public class CertificationAuthority implements CaKeyStoreConstants {

    private static final Logger LOG = Logger.getLogger(CertificationAuthority.class.getName());
    private KeyStore key_store;
    private File keyStoreFile;
    private String caName;
    private String caID;
    private X509Certificate caRoot = null;
    private Name rootIssuer;
    private boolean initialized = false;
    private String caDir;
    private long nextSerial;
    private File crlFile;
    private File exportCrlFile;
    private String crlDpUrl;
    private X509CRL latestCrl = null;
    long crlValPeriod;

    public CertificationAuthority(String cAName, String caDir, TslTrustModel model) {
        this.caName = cAName;
        this.caID = FnvHash.getFNV1aToHex(caName);
        this.caDir = caDir;
        TslTrustConfig conf = (TslTrustConfig) model.getConf();
        crlValPeriod = model.getTslRefreshDelay() * 2 + (1000 * 60 * 60); //Make CRLs last the update period x 2 + 1 hour;
        keyStoreFile = new File(this.caDir, "ca.keystore");
        crlFile = new File(caDir, caID + ".crl");
        exportCrlFile = new File(FileOps.getfileNameString(conf.getCaFileStorageLocation() , "crl"), caID + ".crl");
        crlDpUrl = FileOps.getfileNameString(conf.getCaDistributionURL() , "crl/" + caID + ".crl");

    }

    public boolean initKeyStore() {
        try {
            if (keyStoreFile.canRead()) {
                key_store = KeyStore.getInstance("JKS");
//                key_store = KeyStore.getInstance("IAIKKeyStore", "IAIK");
                key_store.load(new FileInputStream(keyStoreFile), KS_PASSWORD);
                if (crlFile.canRead()) {
                    latestCrl = new X509CRL(new FileInputStream(crlFile));
                }
                X509Certificate root = getSelfSignedCert();
                if (root != null) {
                    initialized = true;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return initialized;
    }

    public X509Certificate getSelfSignedCert() {
        try {
            X509Certificate cert = Util.convertCertificate(key_store.getCertificate(ROOT));
            caRoot = cert;
        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
            caRoot = null;
        }
        return caRoot;
    }

    public String getCaName() {
        return caName;
    }

    public String getCaDir() {
        return caDir;
    }

    public String getCaID() {
        return caID;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public X509CRL getLatestCrl() {
        return latestCrl;
    }

    public File getExportCrlFile() {
        return exportCrlFile;
    }

    public X509Certificate issueXCert(X509Certificate orgCert) {

        DbCAParam cp = CaSQLiteUtil.getParameter(caDir, CERT_SERIAL_KEY);
        if (cp == null) {
            return null;
        }
        nextSerial = cp.getIntValue();

        BigInteger certSerial = BigInteger.valueOf(nextSerial);
        List<V3Extension> extList = new LinkedList<V3Extension>();
        Enumeration e = orgCert.listExtensions();

        //System.out.println("Original cert extensions:");
        //Get extensions form orgCert
        boolean policy = false;
        if (e != null) {
            while (e.hasMoreElements()) {
                V3Extension ext = (V3Extension) e.nextElement();
                //System.out.println(ext.getObjectID().getNameAndID() + " " + ext.toString());
                //Replace policy with AnyPolicy
                ObjectID extOID = ext.getObjectID();
                if (extOID == CertificatePolicies.oid) {
                    ext = getAnyCertificatePolicies();
                    policy = true;
                }
                // Ignore the following extensions
                if (extOID == IssuerAltName.oid
                        || extOID.equals(CRLDistributionPoints.oid)
                        || extOID.equals(AuthorityInfoAccess.oid)
                        || extOID.equals(AuthorityKeyIdentifier.oid)
                        || extOID.equals(PolicyConstraints.oid)
                        || extOID.equals(PolicyMappings.oid)
                        || extOID.equals(QCStatements.oid)
                        || extOID.getID().equals("1.3.6.1.4.1.8301.3.5") // German signature law validation rules
                        ) {
                    continue;
                }
                extList.add(ext);
            }
        } else {
            V3Extension bc = new BasicConstraints(false);
            extList.add(bc);
            policy = true;
        }
        // If no policy in orgCert then add AnyPolicy to list
        if (!policy) {
            extList.add(getAnyCertificatePolicies());
        }

        //Copy to extension list
        V3Extension[] extensions = new V3Extension[extList.size()];
        for (int i = 0; i < extList.size(); i++) {
            V3Extension ext = extList.get(i);
            extensions[i] = ext;
        }
        X509Certificate xCert = createCertificate(orgCert, certSerial, caRoot, AlgorithmID.sha256WithRSAEncryption, extensions);
        //System.out.println((char) 10 + "Issued XCert" + (char) 10 + xCert.toString(true));
        CaSQLiteUtil.addCertificate(xCert, caDir);

        //update log 
        DbCALog caLog = new DbCALog();
        caLog.setLogCode(ISSUE_EVENT);
        caLog.setEventString("Certificate issued");
        caLog.setLogParameter(nextSerial);
        caLog.setLogTime(System.currentTimeMillis());
        CaSQLiteUtil.addCertLog(caLog, caDir);

        //Store next serial number
        cp.setIntValue(nextSerial + 1);
        CaSQLiteUtil.storeParameter(cp, caDir);
        return xCert;
    }

    public X509Certificate createCertificate(X509Certificate orgCert, BigInteger certSerial,
            X509Certificate issuerCert, AlgorithmID algorithm, V3Extension[] extensions) {

        // create a new certificate
        X509Certificate cert = new X509Certificate();
        PublicKey publicKey = orgCert.getPublicKey();

        try {
            // set cert values
            cert.setSerialNumber(certSerial);
            cert.setSubjectDN(orgCert.getSubjectDN());
            cert.setPublicKey(publicKey);
            cert.setIssuerDN(issuerCert.getSubjectDN());
            cert.setValidNotBefore(orgCert.getNotBefore());
            if (issuerCert.getNotAfter().after(orgCert.getNotAfter())) {
                cert.setValidNotAfter(orgCert.getNotAfter());
            } else {
                cert.setValidNotAfter(issuerCert.getNotAfter());
            }

            // Add other extensions
            if (extensions != null) {
                for (int i = 0; i < extensions.length; i++) {
                    cert.addExtension(extensions[i]);
                }
            }
            // Add AKI
            byte[] keyID = ((SubjectKeyIdentifier) issuerCert.getExtension(SubjectKeyIdentifier.oid)).get();
            cert.addExtension(new AuthorityKeyIdentifier(keyID));

            String[] uriStrings = new String[]{crlDpUrl};
            DistributionPoint distPoint = new DistributionPoint();
            distPoint.setDistributionPointNameURIs(uriStrings);
            cert.addExtension(new CRLDistributionPoints(distPoint));

            // and sign the certificate
            cert.sign(algorithm, (PrivateKey) key_store.getKey(ROOT, KS_PASSWORD));
        } catch (Exception ex) {
            cert = null;
            LOG.warning("Error creating the certificate: " + ex.getMessage());
        }

        return cert;
    }

    /**
     * Add the private key and the certificate chain to the key store.
     */
    public void addToKeyStore(KeyPair keyPair, X509Certificate[] chain, String alias) throws KeyStoreException {
        key_store.setKeyEntry(alias, keyPair.getPrivate(), KS_PASSWORD, chain);
    }

    private void saveKeyStore() {
        try {
            // write the KeyStore to disk
            FileOutputStream os = new FileOutputStream(keyStoreFile);
            key_store.store(os, KS_PASSWORD);
            os.close();
        } catch (Exception ex) {
            LOG.warning("Error saving KeyStore! " + ex.getMessage());
        }
    }

    private CertificatePolicies getDefCertificatePolicies() {
        PolicyQualifierInfo policyQualifierInfo = new PolicyQualifierInfo(null, null, "This certificate may be used for demonstration purposes only.");
        PolicyInformation policyInformation = new PolicyInformation(new ObjectID("1.3.6.1.4.1.2706.2.2.1.1.1.1.1"), new PolicyQualifierInfo[]{policyQualifierInfo});
        CertificatePolicies certificatePolicies = new CertificatePolicies(new PolicyInformation[]{policyInformation});
        return certificatePolicies;
    }

    private CertificatePolicies getAnyCertificatePolicies() {
        PolicyInformation policyInformation = new PolicyInformation(ObjectID.anyPolicy, null);
        CertificatePolicies certificatePolicies = new CertificatePolicies(new PolicyInformation[]{policyInformation});
        return certificatePolicies;
    }

    public X509CRL revokeCertificates() {
        long currentTime = System.currentTimeMillis();
        long nextUpdateTime = currentTime + crlValPeriod;
        List<DbCert> certList = CaSQLiteUtil.getCertificates(caDir, true);

        DbCAParam cp = CaSQLiteUtil.getParameter(caDir, CRL_SERIAL_KEY);
        if (cp == null) {
            return null;
        }
        long nextCrlSerial = cp.getIntValue();

        try {

            X509CRL crl = new X509CRL();

            crl.setIssuerDN((Name) caRoot.getSubjectDN());
            crl.setThisUpdate(new Date(currentTime));
            crl.setNextUpdate(new Date(nextUpdateTime));
            crl.setSignatureAlgorithm(AlgorithmID.sha256WithRSAEncryption);

            // Add AKI
            byte[] keyID = ((SubjectKeyIdentifier) caRoot.getExtension(SubjectKeyIdentifier.oid)).get();
            crl.addExtension(new AuthorityKeyIdentifier(keyID));

            // CRLNumber to be adjusted to an incremental number
            CRLNumber cRLNumber = new CRLNumber(BigInteger.valueOf(nextCrlSerial));
            crl.addExtension(cRLNumber);

            // IssuingDistributionPoint
            GeneralNames distributionPointName = new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crlDpUrl));
            IssuingDistributionPoint issuingDistributionPoint = new IssuingDistributionPoint();
            issuingDistributionPoint.setDistributionPointName(distributionPointName);

            issuingDistributionPoint.setCritical(true);
            //issuingDistributionPoint.setOnlyContainsCaCerts(true);
            crl.addExtension(issuingDistributionPoint);

            for (DbCert dbCert : certList) {
                GregorianCalendar revTime = new GregorianCalendar();
                RevokedCertificate rc = new RevokedCertificate(dbCert.getCertificate(), new Date(dbCert.getRevDate()));

                // ReasonCode
                rc.addExtension(new ReasonCode(ReasonCode.privilegeWithdrawn));
                crl.addCertificate(rc);

            }


            crl.sign((PrivateKey) key_store.getKey(ROOT, KS_PASSWORD));

            byte[] crlBytes = crl.toByteArray();
            // send CRL to ...
            iaik.utils.Util.saveToFile(crlBytes, crlFile.getAbsolutePath());
            logRevocation(certList);

            // receive CRL
            latestCrl = new X509CRL(crlBytes);
            cp.setIntValue(nextCrlSerial + 1);
            CaSQLiteUtil.storeParameter(cp, caDir);
            //System.out.println(newCrl.toString(true));
            // Store CRL
            FileOps.saveByteFile(FileOps.readBinaryFile(crlFile), exportCrlFile);
//            FTPops.uploadCRL(caName, caDir);
            return latestCrl;

        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
            return null;
        }
    }

    public void logRevocation(List<DbCert> revCertList) {
        List<Long> crlList = new LinkedList<Long>();
        if (latestCrl != null) {
            Enumeration crlEntries = latestCrl.listCertificates();

            while (crlEntries.hasMoreElements()) {
                long revokedSerial = ((X509CRLEntry) crlEntries.nextElement()).getSerialNumber().longValue();
                crlList.add(revokedSerial);
            }
        }

        for (DbCert dbCert : revCertList) {
            if (!crlList.contains((long) dbCert.getSerial())) {
                //update log 
                DbCALog caLog = new DbCALog();
                caLog.setLogCode(REVOKE_EVENT);
                caLog.setEventString("Certificate revoked");
                caLog.setLogParameter(dbCert.getSerial() * 256 + ReasonCode.privilegeWithdrawn);
                caLog.setLogTime(dbCert.getRevDate());
                CaSQLiteUtil.addCertLog(caLog, caDir);
            }
        }
    }

    public List<DbCert> getAllCertificates() {
        return CaSQLiteUtil.getCertificates(caDir);
    }

    public List<DbCert> getAllCertificates(boolean revoked) {
        return CaSQLiteUtil.getCertificates(caDir, revoked);
    }

    public DbCert getCertificateBySerial(long serial) {
        return CaSQLiteUtil.getCertificates(caDir, serial);
    }

    public void replaceCertificateData(DbCert certData) {
        CaSQLiteUtil.replaceCertificate(certData, caDir);
    }

    public List<DbCALog> getCertLogs() {
        return CaSQLiteUtil.getCertLogs(caDir);
    }

    public List<DbCALog> getCertLogs(int eventType) {
        return CaSQLiteUtil.getCertLogs(caDir, eventType);
    }

    public String getFormatedLogList() {
        return getFormattedLogList(getCertLogs());
    }

    public String getFormattedLogList(int eventType) {
        return getFormattedLogList(getCertLogs(eventType));
    }

    public String getFormattedLogList(List<DbCALog> logs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");
        GregorianCalendar gc = new GregorianCalendar();
        StringBuilder b = new StringBuilder();
        for (DbCALog log : logs) {
            gc.setTimeInMillis(log.getLogTime());
            b.append(dateFormat.format(gc.getTime()));
            b.append("    ");
            b.append(log.getEventString());
            b.append(" -- ");
            if (log.getLogCode() == ISSUE_EVENT) {
                b.append("Certificate Serial Number=").append(log.getLogParameter());
            }
            if (log.getLogCode() == REVOKE_EVENT) {
                b.append("Certificate Serial Number=").append(log.getLogParameter() / 256);
                b.append(", Revocation Reason=");
                long rc = log.getLogParameter() % 256;
                b.append((rc < 11) ? REV_REASON[(int) rc] : String.valueOf(rc));
            }
            b.append((char) 10);
        }
        return b.toString();
    }
}
