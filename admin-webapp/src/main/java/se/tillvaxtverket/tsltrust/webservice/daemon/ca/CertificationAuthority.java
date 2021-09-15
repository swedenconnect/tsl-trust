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

import com.aaasec.lib.aaacert.AaaCRL;
import com.aaasec.lib.aaacert.AaaCertificate;
import com.aaasec.lib.aaacert.CertFactory;
import com.aaasec.lib.aaacert.data.CRLEntryData;
import com.aaasec.lib.aaacert.data.CertRequestModel;
import com.aaasec.lib.aaacert.enums.OidName;
import com.aaasec.lib.aaacert.enums.SupportedExtension;
import com.aaasec.lib.aaacert.extension.ExtensionInfo;
import com.aaasec.lib.aaacert.utils.CertUtils;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCALog;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCAParam;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCert;
import se.tillvaxtverket.tsltrust.weblogic.db.CaSQLiteUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;
import java.util.Iterator;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509CRLEntryHolder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;

/**
 * This class implements the Certification Authority logic of TSL Trust.
 * Certificates compliant with defined policies are certified under a defined
 * policy Root created by TSL Trust.
 */
public class CertificationAuthority implements CaKeyStoreConstants {

    private static final Logger LOG = Logger.getLogger(CertificationAuthority.class.getName());
    private KeyStore key_store;
    private final File keyStoreFile;
    private final String caName;
    private final String caID;
    private AaaCertificate caRoot = null;
    private X500Principal rootIssuer;
    private boolean initialized = false;
    private final String caDir;
    private long nextSerial;
    private final File crlFile;
    private final File exportCrlFile;
    private final String crlDpUrl;
    private X509CRLHolder latestCrl = null;
    long crlValPeriod;

    public CertificationAuthority(String cAName, String caDir, TslTrustModel model) {
        this.caName = cAName;
        this.caID = FnvHash.getFNV1aToHex(caName);
        this.caDir = caDir;
        TslTrustConfig conf = (TslTrustConfig) model.getConf();
        crlValPeriod = model.getTslRefreshDelay() * 2 + (1000 * 60 * 60); //Make CRLs last the update period x 2 + 1 hour;
        keyStoreFile = new File(this.caDir, "ca.keystore");
        crlFile = new File(caDir, caID + ".crl");
        exportCrlFile = new File(FileOps.getfileNameString(conf.getCaFileStorageLocation(), "crl"), caID + ".crl");
        crlDpUrl = FileOps.getfileNameString(conf.getCaDistributionURL(), "crl/" + caID + ".crl");

    }

    public boolean initKeyStore() {
        try {
            if (keyStoreFile.canRead()) {
                key_store = KeyStore.getInstance("JKS");
//                key_store = KeyStore.getInstance("IAIKKeyStore", "IAIK");
                key_store.load(new FileInputStream(keyStoreFile), KS_PASSWORD);
                if (crlFile.canRead()) {
                    latestCrl = new AaaCRL(crlFile).getCrl();
                }
                AaaCertificate root = getSelfSignedCert();
                if (root != null) {
                    initialized = true;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return initialized;
    }

    public AaaCertificate getSelfSignedCert() {
        try {
            AaaCertificate cert = new AaaCertificate(key_store.getCertificate(ROOT).getEncoded());
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

    public X509CRLHolder getLatestCrl() {
        return latestCrl;
    }

    public File getExportCrlFile() {
        return exportCrlFile;
    }

    public AaaCertificate issueXCert(AaaCertificate orgCert) throws IOException {

        DbCAParam cp = CaSQLiteUtil.getParameter(caDir, CERT_SERIAL_KEY);
        if (cp == null) {
            return null;
        }
        nextSerial = cp.getIntValue();

        BigInteger certSerial = BigInteger.valueOf(nextSerial);
        List<Extension> extList = new ArrayList<>();
        Iterator<ExtensionInfo> e = orgCert.getExtensionInfoList().iterator();

        //System.out.println("Original cert extensions:");
        //Get extensions form orgCert
        boolean policy = false;
        if (e != null) {
            while (e.hasNext()) {
                ExtensionInfo ext = e.next();
                //System.out.println(ext.getObjectID().getNameAndID() + " " + ext.toString());
                //Replace policy with AnyPolicy
                if (ext.getExtensionType().equals(SupportedExtension.certificatePolicies)){
                    CertificatePolicies cpe = getAnyCertificatePolicies();
                    ext.setExtDataASN1(cpe.toASN1Primitive());
                    ext.setExtData(cpe.getEncoded());
                    policy=true;
                }
                
                switch (ext.getExtensionType()){
                    case cRLDistributionPoints:
                    case authorityInfoAccess:
                    case authorityKeyIdentifier:
                    case policyConstraints:
                    case policyMappings:
                    case qCStatements:
                        break;
                    case basicConstraints:
                        extList.add(new Extension(Extension.basicConstraints, false, new BasicConstraints(true).getEncoded("DER")));
                        break;
                    default:
                        if (ext.getOid().getId().equalsIgnoreCase("1.3.6.1.4.1.8301.3.5")){
                            // German signature law validation rules
                            break;
                        }
                        extList.add(new Extension(ext.getOid(), ext.isCritical(), ext.getExtData()));
                    
                }
                
              
            }
        } else {
            extList.add(new Extension(Extension.basicConstraints, false, new BasicConstraints(true).getEncoded("DER")));
            policy = false;
        }
        // If no policy in orgCert then add AnyPolicy to list
        if (!policy) {
            CertificatePolicies cpe = getAnyCertificatePolicies();
            extList.add(new Extension(Extension.certificatePolicies, false, cpe.getEncoded("DER")));
        }

        //Copy to extension list
//        V3Extension[] extensions = new V3Extension[extList.size()];
//        for (int i = 0; i < extList.size(); i++) {
//            V3Extension ext = extList.get(i);
//            extensions[i] = ext;
//        }
        AaaCertificate xCert = createCertificate(orgCert, certSerial, caRoot, CertFactory.SHA256WITHRSA, extList);
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

    public AaaCertificate createCertificate(AaaCertificate orgCert, BigInteger certSerial,
            AaaCertificate issuerCert, String algorithm, List<Extension> extensions) {

        AaaCertificate cert = null;
        // create a new certificate
        try {
            CertRequestModel reqModel = new CertRequestModel();
            reqModel.setIssuerDN(issuerCert.getSubject());
            reqModel.setPublicKey(orgCert.getCert().getPublicKey());
            reqModel.setSerialNumber(certSerial);
            reqModel.setSubjectDN(orgCert.getSubject());
            reqModel.setNotBefore(orgCert.getNotBefore());
            if (issuerCert.getNotAfter().after(orgCert.getNotAfter())) {
                reqModel.setNotAfter(orgCert.getNotAfter());
            } else {
                reqModel.setNotAfter(issuerCert.getNotAfter());
            }
            
            // Add AKI
            X509ExtensionUtils extUtil = CertUtils.getX509ExtensionUtils();
            AuthorityKeyIdentifier aki = extUtil.createAuthorityKeyIdentifier(issuerCert);
            extensions.add(new Extension(Extension.authorityKeyIdentifier, false, aki.getEncoded("DER")));
            
            DistributionPoint dp = new DistributionPoint(new DistributionPointName(new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crlDpUrl))), null, null);
            CRLDistPoint cdp = new CRLDistPoint(new DistributionPoint[]{dp});
            extensions.add(new Extension(Extension.cRLDistributionPoints, false, cdp.getEncoded("DER")));
                        
            reqModel.setExtensionList(extensions);
            reqModel.setSigner(new JcaContentSignerBuilder(algorithm).build((PrivateKey) key_store.getKey(ROOT, KS_PASSWORD)));
            
            cert = new AaaCertificate(reqModel);
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
        PolicyQualifierInfo policyQualifierInfo = CertUtils.getUserNotice("This certificate may be used for demonstration purposes only.");
        ASN1EncodableVector pqSeq = new ASN1EncodableVector();
        pqSeq.add(policyQualifierInfo);
        PolicyInformation policyInformation = new PolicyInformation(new ASN1ObjectIdentifier("1.3.6.1.4.1.2706.2.2.1.1.1.1.1"), new DERSequence(pqSeq));
        CertificatePolicies certificatePolicies = new CertificatePolicies(new PolicyInformation[]{policyInformation});
        return certificatePolicies;
    }

    private CertificatePolicies getAnyCertificatePolicies() {
        PolicyInformation policyInformation = new PolicyInformation(new ASN1ObjectIdentifier(OidName.cp_anyPolicy.getOid()), null);
        CertificatePolicies certificatePolicies = new CertificatePolicies(new PolicyInformation[]{policyInformation});
        return certificatePolicies;
    }

    public X509CRLHolder revokeCertificates() {
        long currentTime = System.currentTimeMillis();
        long nextUpdateTime = currentTime + crlValPeriod;
        List<DbCert> certList = CaSQLiteUtil.getCertificates(caDir, true);

        DbCAParam cp = CaSQLiteUtil.getParameter(caDir, CRL_SERIAL_KEY);
        if (cp == null) {
            return null;
        }
        long nextCrlSerial = cp.getIntValue();

        try {

            AaaCRL crl = new AaaCRL(new Date(currentTime), new Date(nextUpdateTime), caRoot, (PrivateKey) key_store.getKey(ROOT, KS_PASSWORD), CertFactory.SHA256WITHRSA, crlFile);

            List<Extension> extList = new ArrayList<Extension>();
            // Add AKI
            X509ExtensionUtils extu = CertUtils.getX509ExtensionUtils();
            AuthorityKeyIdentifier aki = extu.createAuthorityKeyIdentifier(caRoot);
            extList.add(new Extension(Extension.authorityKeyIdentifier, false, aki.getEncoded("DER")));

            // CRLNumber to be adjusted to an incremental number
            CRLNumber crlNumber = new CRLNumber(BigInteger.valueOf(nextCrlSerial));
            extList.add(new Extension(Extension.cRLNumber, false, crlNumber.getEncoded("DER")));

            GeneralNames distributionPointName = new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crlDpUrl));
            DistributionPointName dpn = new DistributionPointName(distributionPointName);
            IssuingDistributionPoint idp = new IssuingDistributionPoint(dpn, false, false);
            extList.add(new Extension(Extension.issuingDistributionPoint, true, idp.getEncoded("DER")));

            // IssuingDistributionPoint
            List<CRLEntryData> crlEdList = new ArrayList<>();

            certList.forEach((dbCert) -> {
                Date revTime = new Date();
                BigInteger serialNumber = dbCert.getCertificate().getSerialNumber();
                crlEdList.add(new CRLEntryData(serialNumber, new Date(dbCert.getRevDate()), CRLReason.privilegeWithdrawn));
            });

            crl.updateCrl(new Date(currentTime), new Date(nextUpdateTime), crlEdList, extList);

            logRevocation(certList);

            // receive CRL
            latestCrl = crl.getCrl();
            cp.setIntValue(nextCrlSerial + 1);
            CaSQLiteUtil.storeParameter(cp, caDir);
            // Store CRL
            FileOps.saveByteFile(FileOps.readBinaryFile(crlFile), exportCrlFile);
            return latestCrl;

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CRLException | CertificateException | OperatorCreationException ex) {
            LOG.warning(ex.getMessage());
            return null;
        }
    }

    public void logRevocation(List<DbCert> revCertList) {
        List<Long> crlList = new LinkedList<Long>();
        if (latestCrl != null) {
            X509CRLEntryHolder revokedCertificate = latestCrl.getRevokedCertificate(BigInteger.ZERO);
            latestCrl.getRevokedCertificates().iterator().forEachRemaining((crlEntryObj) -> {
                X509CRLEntryHolder crlEntry = (X509CRLEntryHolder) crlEntryObj;
                long revokedSerial = crlEntry.getSerialNumber().longValue();
                crlList.add(revokedSerial);
            });
        }

        for (DbCert dbCert : revCertList) {
            if (!crlList.contains((long) dbCert.getSerial())) {
                //update log 
                DbCALog caLog = new DbCALog();
                caLog.setLogCode(REVOKE_EVENT);
                caLog.setEventString("Certificate revoked");
                caLog.setLogParameter(dbCert.getSerial() * 256 + CRLReason.privilegeWithdrawn
                );
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
