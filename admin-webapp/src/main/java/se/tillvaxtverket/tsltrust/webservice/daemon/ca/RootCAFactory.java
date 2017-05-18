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

import com.aaasec.lib.aaacert.AaaCertificate;
import com.aaasec.lib.aaacert.CertFactory;
import com.aaasec.lib.aaacert.data.CertRequestModel;
import com.aaasec.lib.aaacert.enums.OidName;
import com.aaasec.lib.aaacert.enums.SubjectDnType;
import com.aaasec.lib.aaacert.extension.missing.SubjectInformationAccess;
import com.aaasec.lib.aaacert.utils.CertReqUtils;
import com.aaasec.lib.aaacert.utils.CertUtils;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCAParam;
import se.tillvaxtverket.tsltrust.weblogic.db.CaSQLiteUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustConfig;

/**
 * This class creates policy root certification authorities for TSL Trust
 */
public class RootCAFactory implements CaKeyStoreConstants {

    private static final Logger LOG = Logger.getLogger(RootCAFactory.class.getName());
    private static KeyStore key_store;
    private static File keyStoreFile;
    private static KeyPair ca_rsa = null;
    private final static int CA_KEYLENGTH = 2048;
    private static String caName;
    private static String caRepSia;
    private static String caID;
    private static TslTrustModel model;
    private static TslTrustConfig conf;

    /**
     * Initiates generation of a root CA
     *
     * @param cAName The name of the policy for which a CA is to be created
     * @param caDir The directory where CA data is to be stored.
     * @param model TSL Trust application model data
     */
    public static void start(String cAName, String caDir, TslTrustModel model) {
        caName = cAName;
        RootCAFactory.model = model;
        conf = (TslTrustConfig) model.getConf();
        caID = FnvHash.getFNV1aToHex(caName);
        caRepSia = FileOps.getfileNameString(conf.getCaDistributionURL(), "certs/" + caID + ".p7b");
        keyStoreFile = new File(caDir, "ca.keystore");
        keyStoreFile.getParentFile().mkdirs();
        LOG.info("New keystore generated at: " + keyStoreFile.getAbsolutePath());

        if (!keyStoreFile.canRead()) {
            createKeyStore(keyStoreFile);
            CaSQLiteUtil.createCATable(caDir);
            DbCAParam cp = new DbCAParam();
            cp.setParamName("CertSerial");
            cp.setIntValue(2);
            CaSQLiteUtil.storeParameter(cp, caDir);
            cp = new DbCAParam();
            cp.setParamName("CRLSerial");
            cp.setIntValue(1);
            CaSQLiteUtil.storeParameter(cp, caDir);
        } else {
            LOG.warning("CA already exists. Aborting...");
            return;
        }
    }

    private static void createKeyStore(File keyStoreFile) {
        try {
            // get a new KeyStore onject
//            key_store = KeyStore.getInstance("IAIKKeyStore", "IAIK");
            key_store = KeyStore.getInstance("JKS");
            key_store.load(null, null);
            generateRootCertificate();
            saveKeyStore();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }

    }

    /**
     * Generate a KeyPair using the specified algorithm with the given size.
     *
     * @param algorithm the algorithm to use
     * @param bits the length of the key (modulus) in bits
     *
     * @return the KeyPair
     *
     * @exception NoSuchAlgorithmException if no KeyPairGenerator is available
     * for the requested algorithm
     */
    private static KeyPair generateKeyPair(String algorithm, int bits)
            throws NoSuchAlgorithmException {

        KeyPair kp = null;
        KeyPairGenerator generator = null;
//        try {
//            generator = KeyPairGenerator.getInstance(algorithm, "IAIK");
        generator = KeyPairGenerator.getInstance(algorithm);
//        } catch (NoSuchProviderException ex) {
//            throw new NoSuchAlgorithmException("Provider IAIK not found!");
//        }
        generator.initialize(bits);
        kp = generator.generateKeyPair();
        return kp;
    }

    private static void generateRootCertificate() {

        try {
            // Generate root key
            System.out.println("Generating Root RSA key...");
            ca_rsa = generateKeyPair("RSA", CA_KEYLENGTH);
            // Now create the certificates

            //CertRequestModel reqMod = new CertRequestModel();
            Map<SubjectDnType, String> subjNameMap = new HashMap<>();
            subjNameMap.put(SubjectDnType.country, conf.getCaCountry());
            subjNameMap.put(SubjectDnType.orgnaizationName, conf.getCaOrganizationName());
            subjNameMap.put(SubjectDnType.orgnaizationalUnitName, conf.getCaOrgUnitName());

//            Name rootIssuer;
//            rootIssuer = new Name();
//            rootIssuer.addRDN(ObjectID.country, conf.getCaCountry());
//            rootIssuer.addRDN(ObjectID.organization, conf.getCaOrganizationName());
//            rootIssuer.addRDN(ObjectID.organizationalUnit, conf.getCaOrgUnitName());
            String modelName = conf.getCaCommonName();
            int idx = modelName.indexOf("####");
            String cName;
            if (idx > -1) {
                cName = modelName.substring(0, idx) + caName + modelName.substring(idx + 4);
            } else {
                cName = caName + " " + modelName;
            }
            subjNameMap.put(SubjectDnType.cn, cName);
            X500Name subjectAndIssuer = CertReqUtils.getDn(subjNameMap);

//            rootIssuer.addRDN(ObjectID.commonName, cName);
            List<Extension> extList = new ArrayList<>();
            extList.add(new Extension(Extension.basicConstraints, false, new BasicConstraints(true).getEncoded("DER")));
            extList.add(new Extension(Extension.keyUsage, false, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign | KeyUsage.digitalSignature).getEncoded("DER")));
            extList.add(new Extension(Extension.certificatePolicies, false, getAnyCertificatePolicies().getEncoded("DER")));

            GeneralName generalName = new GeneralName(GeneralName.uniformResourceIdentifier, caRepSia);
            SubjectInformationAccess sia = new SubjectInformationAccess(SubjectInformationAccess.caRepository, generalName);
            extList.add(new Extension(Extension.subjectInfoAccess, false, sia.getEncoded("DER")));

            //
            // create self signed CA cert
            //
            AaaCertificate caRoot = createRootCertificate(subjectAndIssuer, ca_rsa.getPublic(), ca_rsa.getPrivate(), CertFactory.SHA256WITHRSA, extList);
            // set the CA cert as trusted root
            X509Certificate[] chain = new X509Certificate[]{caRoot.getCert()};
            addToKeyStore(ca_rsa, chain, ROOT);
            //System.out.println(caRoot.toString());
            //rootIssuer.removeRDN(ObjectID.commonName);

        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
        }
    }

    private static AaaCertificate createRootCertificate(X500Name subjectIssuer, PublicKey publicKey,
            PrivateKey privateKey, String algorithm, List<Extension> extensions) throws OperatorCreationException, IOException, CertificateException {

        CertRequestModel reqMod = new CertRequestModel();
        reqMod.setSubjectDN(subjectIssuer);
        reqMod.setIssuerDN(subjectIssuer);
        reqMod.setSerialNumber(BigInteger.ONE);
        reqMod.setPublicKey(publicKey);
        
        //Add Signer
        ContentSigner rooSigner = new JcaContentSignerBuilder(algorithm).build(privateKey);
        reqMod.setSigner(rooSigner);

        // ensure that EE certs are in the validity period of CA certs
        GregorianCalendar notBefore = new GregorianCalendar();
        GregorianCalendar notAfter = new GregorianCalendar();
        notBefore.add(Calendar.YEAR, -2);
        notAfter.add(Calendar.YEAR, 5);
        reqMod.setNotBefore(notBefore.getTime());
        reqMod.setNotAfter(notAfter.getTime());

        X509ExtensionUtils extUtil = CertUtils.getX509ExtensionUtils();
        SubjectKeyIdentifier ski = extUtil.createSubjectKeyIdentifier(CertUtils.getPublicKeyInfo(publicKey));
        extensions.add(new Extension(Extension.subjectKeyIdentifier, false, ski.getEncoded("DER")));

        reqMod.setExtensionList(extensions);

        AaaCertificate cert = new AaaCertificate(reqMod);
        return cert;
    }

    /**
     * Add the private key and the certificate chain to the key store.
     *
     * @param keyPair
     * @param chain
     * @param alias
     * @throws java.security.KeyStoreException
     */
    public static void addToKeyStore(KeyPair keyPair, X509Certificate[] chain, String alias) throws KeyStoreException {
        key_store.setKeyEntry(alias, keyPair.getPrivate(), KS_PASSWORD, chain);
    }

    /**
     * Add the private key and the certificate chain to the key store.
     *
     * @param keyPair
     * @param chain
     * @param alias
     * @throws java.security.KeyStoreException
     */
    public static void addToKeyStore(KeyPair keyPair, AaaCertificate[] chain, String alias) throws KeyStoreException {
        List<X509Certificate> certList = new ArrayList<>();
        for (AaaCertificate acert : chain) {
            certList.add(acert.getCert());
        }
        key_store.setKeyEntry(alias, keyPair.getPrivate(), KS_PASSWORD, certList.toArray(new X509Certificate[]{}));
    }

    private static void saveKeyStore() {
        try {
            // write the KeyStore to disk
            FileOutputStream os = new FileOutputStream(keyStoreFile);
            key_store.store(os, KS_PASSWORD);
            os.close();
        } catch (Exception ex) {
            LOG.warning("Error saving KeyStore! " + ex.getMessage());
        }
    }

    private static CertificatePolicies getAnyCertificatePolicies() {
        PolicyInformation policyInformation = new PolicyInformation(new ASN1ObjectIdentifier(OidName.cp_anyPolicy.getOid()), null);
        CertificatePolicies certificatePolicies = new CertificatePolicies(new PolicyInformation[]{policyInformation});
        return certificatePolicies;
    }
}
