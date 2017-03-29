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
import se.tillvaxtverket.tsltrust.weblogic.data.DbCAParam;
import se.tillvaxtverket.tsltrust.weblogic.db.CaSQLiteUtil;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.Name;
import iaik.asn1.structures.PolicyInformation;
import iaik.x509.V3Extension;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.BasicConstraints;
import iaik.x509.extensions.CertificatePolicies;
import iaik.x509.extensions.KeyUsage;
import iaik.x509.extensions.SubjectKeyIdentifier;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import iaik.asn1.structures.AccessDescription;
import iaik.asn1.structures.GeneralName;
import iaik.x509.extensions.SubjectInfoAccess;
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

            Name rootIssuer;
            rootIssuer = new Name();
            rootIssuer.addRDN(ObjectID.country, conf.getCaCountry());
            rootIssuer.addRDN(ObjectID.organization, conf.getCaOrganizationName());
            rootIssuer.addRDN(ObjectID.organizationalUnit, conf.getCaOrgUnitName());
            String modelName = conf.getCaCommonName();
            int idx = modelName.indexOf("####");
            String cName;
            if (idx > -1) {
                cName = modelName.substring(0, idx) + caName + modelName.substring(idx + 4);
            } else {
                cName = caName + " " + modelName;
            }
            rootIssuer.addRDN(ObjectID.commonName, cName);


            V3Extension[] extensions = new V3Extension[4];
            extensions[0] = new BasicConstraints(true);
            extensions[1] = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign | KeyUsage.digitalSignature);

            extensions[2] = getAnyCertificatePolicies();

            GeneralName generalName = new GeneralName(GeneralName.uniformResourceIdentifier, caRepSia);
            AccessDescription accessDesc = new AccessDescription(ObjectID.caRepository, generalName);
            extensions[3] = new SubjectInfoAccess(accessDesc);


            //
            // create self signed CA cert
            //
            X509Certificate caRoot = null;
            X509Certificate[] chain = new X509Certificate[1];
            // for verifying the created certificates

            System.out.println("create self signed RSA CA certificate...");
            caRoot = createRootCertificate(rootIssuer, ca_rsa.getPublic(),
                    ca_rsa.getPrivate(), AlgorithmID.sha256WithRSAEncryption, extensions);
            // verify the self signed certificate
            caRoot.verify();
            // set the CA cert as trusted root
            chain[0] = caRoot;
            addToKeyStore(ca_rsa, chain, ROOT);
            //System.out.println(caRoot.toString());
            //rootIssuer.removeRDN(ObjectID.commonName);

        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
        }
    }

    private static X509Certificate createRootCertificate(Name subjectIssuer, PublicKey publicKey,
            PrivateKey privateKey, AlgorithmID algorithm, V3Extension[] extensions) {

        // create a new certificate
        X509Certificate cert = new X509Certificate();

        try {
            // set the values
            cert.setSerialNumber(new BigInteger("1"));  //new BigInteger(20, new Random())
            cert.setSubjectDN(subjectIssuer);
            cert.setPublicKey(publicKey);
            cert.setIssuerDN(subjectIssuer);

            GregorianCalendar date = new GregorianCalendar();

            // ensure that EE certs are in the validity period of CA certs
            // not before two years ago
            date.add(Calendar.YEAR, -2);
            cert.setValidNotBefore(date.getTime());
            date.add(Calendar.YEAR, 5);
            cert.setValidNotAfter(date.getTime());
            if (extensions != null) {
                for (int i = 0; i < extensions.length; i++) {
                    cert.addExtension(extensions[i]);
                }
            }
            cert.addExtension(new SubjectKeyIdentifier(publicKey));
            // and sign the certificate
            cert.sign(algorithm, privateKey);
        } catch (Exception ex) {
            LOG.warning("Error creating the certificate: " + ex.getMessage());
            return null;
        }
        return cert;
    }

    /**
     * Add the private key and the certificate chain to the key store.
     */
    public static void addToKeyStore(KeyPair keyPair, X509Certificate[] chain, String alias) throws KeyStoreException {
        key_store.setKeyEntry(alias, keyPair.getPrivate(), KS_PASSWORD, chain);
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
        PolicyInformation policyInformation = new PolicyInformation(ObjectID.anyPolicy, null);
        CertificatePolicies certificatePolicies = new CertificatePolicies(new PolicyInformation[]{policyInformation});
        return certificatePolicies;
    }
}
