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

import se.tillvaxtverket.tsltrust.common.utils.general.ObserverConstants;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck.OCSPClient;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck.CRLChecker;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.OCSPVerifyContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.CertVerifyContext;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AccessDescription;
import iaik.x509.SimpleChainVerifier;
import iaik.x509.V3Extension;
import iaik.x509.X509ExtensionInitException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.AuthorityInfoAccess;
import iaik.x509.extensions.ExtendedKeyUsage;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.bouncycastle.cert.X509CertificateHolder;

/**
 * This class provides functions for verifying a certificate chain to a trusted
 * root authority supported by the trust data build from the local trust cache.
 * 
 * <p>
 * The trust cache is obtained from a TSL Trust policy administration service,
 * providing trust information for a number of defined policies
 * 
 * <P>
 * Certificate status checking is performed by means of CRL checking and OCSP
 * checking. These checks are carried out in parallel. It is configurable whether
 * the cert chain validation should complete both checks or whether to complete
 * as soon as the first conclusive revocation information has been processed.
 * 
 * <p>
 * CRLs are loaded from a local cache if available. If not, then that CRL
 * is downloaded.
 */
public class CertChainVerifier {

    private KeyStore keyStore;
    private boolean initialized;
    private X509Certificate rootCert;
    private List<X509Certificate> cACertList = new LinkedList<X509Certificate>();
    private CRLChecker crlCache;
    private boolean checkAllRev = false;
    private boolean preferSpeed = false;
    private boolean noOCSP = false;
    private SigValidationModel model;
    private long statusCheckTimeout;
    private final static Logger LOG = Logger.getLogger(CertChainVerifier.class.getName());

    /**
     * Constructs a cert chain validation object
     * @param model Model holding signature validation data
     */
    public CertChainVerifier(SigValidationModel model) {
        this.model = model;
        SigValidationBaseModel baseModel = model.getBaseModel();
        this.crlCache = baseModel.getCrlCache();
        this.keyStore = baseModel.getTrustStore().getKeyStore(model.getPolicyName());
        this.statusCheckTimeout = baseModel.getConf().getValidationTimeout();
        try {
            rootCert = KsCertFactory.getIaikCert(keyStore.getCertificate("Root"));
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (alias.equalsIgnoreCase("Root")) {
                    continue;
                }
                X509Certificate cert = KsCertFactory.getIaikCert(keyStore.getCertificate(alias));
                cACertList.add(cert);
            }

        } catch (KeyStoreException ex) {
            initialized = false;
        }
        initialized = true;
    }

    /**
     * Constructs a cert chain validation object
     * @param rootCert Root certificate
     * @param caCertList a list of acceptable intermediary CA certificates
     * @param crlCache 
     */
    public CertChainVerifier(X509Certificate rootCert, List<X509Certificate> caCertList, CRLChecker crlCache) {
        this.crlCache = crlCache;
        this.rootCert = rootCert;
        this.cACertList = caCertList;
        initialized = true;
    }

    public boolean isCheckAllRev() {
        return checkAllRev;
    }

    public void setCheckAllRev(boolean checkAllRev) {
        this.checkAllRev = checkAllRev;
    }

    public boolean isPreferSpeed() {
        return preferSpeed;
    }

    public void setPreferSpeed(boolean preferOCSP) {
        this.preferSpeed = preferOCSP;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isNoOCSP() {
        return noOCSP;
    }

    public void setNoOCSP(boolean noOCSP) {
        this.noOCSP = noOCSP;
    }

    public CertVerifyContext verifyChain(ArrayList<X509CertificateHolder> tsCertList) {
        List<X509Certificate> certList = new LinkedList<X509Certificate>();
        for (X509CertificateHolder certHolder : tsCertList) {
            try {
                certList.add(KsCertFactory.getIaikCert(certHolder.getEncoded()));
            } catch (IOException ex) {
            }
        }
        return verifyChain(certList);
    }

    public CertVerifyContext verifyChain(java.security.cert.Certificate[] certChain) {
        List<X509Certificate> certList = new LinkedList<X509Certificate>();
        for (java.security.cert.Certificate cert : certChain) {
            certList.add(KsCertFactory.getIaikCert(cert));
        }
        return verifyChain(certList);
    }

    public CertVerifyContext verifyChain(List<X509Certificate> certList) {
        CertVerifyContext cvCont = new CertVerifyContext();
        if (certList.isEmpty()) {
            cvCont.setChainsToRoot(false);
            return (cvCont);
        }

        // Get chain to chainVerifier trusted root
        getCertificateChain(cvCont, certList);
        if (!cvCont.isChainsToRoot()) {
            return cvCont;
        }
        // Check cert chain
        validateCertChain(cvCont);

        // check revstatuses
        checkCertificateValidity(cvCont);

        return cvCont;
    }

    private void getCertificateChain(CertVerifyContext cvCont, List<X509Certificate> certList) {

        List<X509Certificate> chain = new ArrayList<X509Certificate>();
        for (X509Certificate cert : certList) {
            // Test if the public key of this cert matches a public key in the XCA List
            X509Certificate certFromTrustedCAs = getTargetFromStore(cert);
            if (certFromTrustedCAs != null) {
                chain.add(certFromTrustedCAs);
                chain.add(rootCert);
                cvCont.setChain(chain);
                cvCont.setChainsToRoot(true);
                cvCont.setIssuedByTa(true);
                break;
            }
            chain.add(cert);
            // Test if the issuer of this certificate is on the XCA list 
            certFromTrustedCAs = getIssuerFromStore(cert);
            if (certFromTrustedCAs != null) {
                chain.add(certFromTrustedCAs);
                chain.add(rootCert);
                cvCont.setChain(chain);
                cvCont.setChainsToRoot(true);
                cvCont.setIssuedByTa(false);
                break;
            }
        }

        if (!cvCont.isChainsToRoot() || cvCont.isIssuedByTa()) {
            return;
        }
        cvCont.setIssuingCertContext(verifyChain(chain.subList(1, chain.size())));
    }

    private void checkCertificateValidity(CertVerifyContext cvCont) {
        X509Certificate baseCert = cvCont.getChain().get(0);

        // Skips validity check if chain is less than two certificates (just the root)
        if (cvCont.getChain().size() < 2) {
            return;
        }

        // If the certificate has the OCSP No Check extensioin and the OCSP EKU, skip validity check
        if (testOcspNoCheck(baseCert)) {
            // But only if the issuer cert of the No Check certificate is valid.
            CertVerifyContext issuerCont = cvCont.getIssuingCertContext();
            if (issuerCont != null) {
                boolean issuerCRLchk = (issuerCont.isCrlStatusDetermined() && !issuerCont.isRevoked());
                boolean issuerOCSPchk = (issuerCont.getOcspVerifyContext() != null && issuerCont.getOcspVerifyContext().isOcspCheckOK());
                if (issuerCRLchk || issuerOCSPchk) {
                    cvCont.setNoCheck(true);
                    cvCont.setCrlStatusDetermined(true);
                    cvCont.setRevoked(false);
                    cvCont.setStatusCheckMethod("OCSP No Check");
                }
            }
            return;
        }

        // Begin status checking

        StatusContext context = new StatusContext(cvCont);
        StatusCheck statusCheck = new StatusCheck(context);
        context.statusThread = new Thread(statusCheck);
        context.statusThread.start();
        try {
            context.statusThread.join();

        } catch (InterruptedException ex) {
            LOG.warning("Signature verification aborted");
        }

    }

    private void crlCheck(CertVerifyContext cvCont) {
        //CRL Check
        crlCache.derefCRL(cvCont);
        crlCache.checkRevocation(cvCont);
    }

    private void ocspCheck(CertVerifyContext cvCont) {
        //OCSP Check
        X509Certificate baseCert = cvCont.getChain().get(0);
        X509Certificate issuerCert = cvCont.getChain().get(1);
        OCSPVerifyContext ocspVC;
        try {
            AuthorityInfoAccess aia = (AuthorityInfoAccess) baseCert.getExtension(AuthorityInfoAccess.oid);
            AccessDescription aDesc = aia.getAccessDescription(ObjectID.ocsp);
            String ocspURI = aDesc.getUriAccessLocation();
            if (ocspURI.length() > 0) {
                try {
                    OCSPClient ocsp = new OCSPClient();
                    ocspVC = ocsp.start(baseCert, issuerCert, ocspURI, cACertList, rootCert, crlCache);
                    if (!model.isSignVerificationComplete()) {
                        cvCont.setOcspVerifyContext(ocspVC);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(CertChainVerifier.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (Exception ex) {
        }
    }

    private X509Certificate getTargetFromStore(X509Certificate targetCert) {
        for (X509Certificate caCert : cACertList) {
            if (targetCert.getPublicKey().equals(caCert.getPublicKey())) {
                return caCert;
            }
        }
        return null;
    }

    private X509Certificate getIssuerFromStore(X509Certificate targetCert) {
        for (X509Certificate caCert : cACertList) {
            if (targetCert.getIssuerDN().equals(caCert.getSubjectDN())) {
                return caCert;
            }
        }
        return null;
    }

    private boolean testOcspNoCheck(X509Certificate cert) {
        boolean ocspSign = false;
        boolean noCheck = false;
        try {
            ExtendedKeyUsage eku = (ExtendedKeyUsage) cert.getExtension(ExtendedKeyUsage.oid);
            ocspSign = (eku != null && eku.contains(ExtendedKeyUsage.ocspSigning)) ? true : false;
        } catch (X509ExtensionInitException ex) {
        }
        try {
            V3Extension noCheckExt = cert.getExtension(new ObjectID("1.3.6.1.5.5.7.48.1.5"));
            noCheck = (noCheckExt != null) ? true : false;
        } catch (X509ExtensionInitException ex) {
            noCheck = true;
        }
        return (ocspSign && noCheck);
    }

    private void validateCertChain(CertVerifyContext cvCont) {
        if (!isWithinValidityPeriod(cvCont)) {
            return;
        }
        SimpleChainVerifier verifier = new SimpleChainVerifier();
        verifier.addTrustedCertificate(rootCert);
        List<X509Certificate> chain = cvCont.getChain();
        int i = 0;
        X509Certificate[] certs = new X509Certificate[chain.size()];
        for (X509Certificate cert : chain) {
            certs[i++] = cert;
        }
        try {
            verifier.verifyChain(certs);
            cvCont.setSigChainVerified(true);
        } catch (CertificateException ex) {
            cvCont.setChainVerifyError(true);

            if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                String exMess = ex.getCause().getMessage();
                if (ex.getMessage() != null) {
                    exMess += " (" + ex.getMessage() + ")";
                }
                cvCont.setChainVerifyErrorMessage(exMess);
            } else {
                if (ex.getMessage() != null) {
                    cvCont.setChainVerifyErrorMessage(ex.getMessage());
                }
            }
            cvCont.setSigChainVerified(false);
        }
    }

    private boolean isWithinValidityPeriod(CertVerifyContext cvCont) {

        X509Certificate cert = cvCont.getChain().get(0);
        Calendar present = Calendar.getInstance();
        Calendar certNotBefore = Calendar.getInstance();
        certNotBefore.setTime(cert.getNotBefore());
        Calendar certNotAfter = Calendar.getInstance();
        certNotAfter.setTime(cert.getNotAfter());
        if (present.before(certNotBefore)) {
            cvCont.setNotValidYet(true);
            cvCont.setChainVerifyError(true);
            cvCont.setChainVerifyErrorMessage("Certificate is not valid yet");
            return false;
        }
        if (present.after(certNotAfter)) {
            cvCont.setExpired(true);
            cvCont.setChainVerifyError(true);
            cvCont.setChainVerifyErrorMessage("Certificate has expired");
            return false;
        }
        return true;
    }

    class StatusCheck implements Runnable, ObserverConstants, Observer {

        StatusContext context;
        private CrlCheck crlCheck;
        private OcspCheck ocspCheck;
        private long startTime;
        boolean complete = false;

        public StatusCheck(StatusContext context) {
            this.context = context;
        }

        public void run() {
            crlCheck = new CrlCheck(context);
            crlCheck.addObserver(this);
            ocspCheck = new OcspCheck(context);
            ocspCheck.addObserver(this);

            if (checkAllRev || preferSpeed) {
                checkAll();
            } else { // Prefer CRL
                checkCRLfirst();
            }

            startTime = System.currentTimeMillis();
            waitForResult();
        }

        private void waitForResult() {
            boolean crl;
            boolean ocsp;

            long timeOutEnd = statusCheckTimeout + startTime;
            while (System.currentTimeMillis() < timeOutEnd) {
                if (complete) {
                    break;
                }
                crl = (context.crlThread != null && context.crlThread.isAlive());
                ocsp = (context.ocspThread != null && context.ocspThread.isAlive());
                if (!(crl || ocsp)) {
                    break;
                }
            }
            if (System.currentTimeMillis() > timeOutEnd) {
                context.cvCont.setStatusCheckTimeOut(true);
            }
        }

        private void checkAll() {
            context.crlThread = new Thread(crlCheck);
            context.crlThread.start();

            if (!noOCSP) {
                context.ocspThread = new Thread(ocspCheck);
                context.ocspThread.start();
            }
        }

        /**
         * Check CRL first and wait for result before initiating OCSP
         * only if CRL check was inconclusive.
         */
        private void checkCRLfirst() {
            context.crlThread = new Thread(crlCheck);
            context.crlThread.start();
            try {
                context.crlThread.join();
                if (context.cvCont.isCrlStatusDetermined()) {
                    complete = true;
                    return;
                }
                if (!noOCSP) {
                    context.ocspThread = new Thread(ocspCheck);
                    context.ocspThread.start();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(CertChainVerifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void update(Observable o, Object arg) {
            if (o instanceof OcspCheck) {
                if (arg.equals(COMPLETE)) {
                    if (preferSpeed) {
                        if (context.cvCont.getOcspVerifyContext() != null) {
                            if (context.cvCont.getOcspVerifyContext().isOcspCheckOK()) {
                                complete = true;
                            }
                            if (context.cvCont.getOcspVerifyContext().isOcspRevoked()) {
                                complete = true;
                            }
                        }

                    }
                }
            }
            if (o instanceof CrlCheck) {
                if (arg.equals(COMPLETE)) {
                    if (preferSpeed) {
                        if (context.cvCont.isCrlStatusDetermined() || !(context.cvCont.isExpired() || context.cvCont.isNotValidYet())) {
                            complete = true;
                        }
                    }
                }
            }
        }
    }

    class CrlCheck extends Observable implements Runnable, ObserverConstants {

        StatusContext context;

        public CrlCheck(StatusContext context) {
            this.context = context;
        }

        public void run() {
            crlCheck(context.cvCont);
            setChanged();
            notifyObservers(COMPLETE);
        }
    }

    class OcspCheck extends Observable implements Runnable, ObserverConstants {

        StatusContext context;

        public OcspCheck(StatusContext context) {
            this.context = context;
        }

        public void run() {
            ocspCheck(context.cvCont);
            setChanged();
            notifyObservers(COMPLETE);
        }
    }

    class StatusContext {

        CertVerifyContext cvCont;
        Thread crlThread;
        Thread ocspThread;
        Thread statusThread;

        public StatusContext(CertVerifyContext cvCont) {
            this.cvCont = cvCont;
        }
    }
}
