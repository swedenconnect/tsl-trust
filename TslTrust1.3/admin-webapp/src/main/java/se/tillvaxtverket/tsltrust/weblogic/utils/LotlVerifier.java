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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import se.tillvaxtverket.tsltrust.common.iaik.BasicCrlCheck;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import iaik.x509.SimpleChainVerifier;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;

/**
 * Utility class for verification of root Trusted lists
 */
public class LotlVerifier {

    /**
     * Verifies the Lotl signature certificate against the locally configured Lotl Cert.
     * Verification succeeds if the signature certificate is within its validity period and is found in the list of configured certificates.
     * Verification also succeeds if the signature certificate is issued under any of the configured certificates and is non-revoked.
     * @param signCert
     * @param model
     * @return true if the LotL signature is valid, else false
     */
    public static boolean validateLotlSignCert(X509Certificate signCert, TslTrustModel model) {
        iaik.x509.X509Certificate cert = KsCertFactory.getIaikCert(signCert);
        List<iaik.x509.X509Certificate> lotlCerts = model.getLotlSigCerts();
        List<iaik.x509.X509Certificate> chain = new ArrayList<iaik.x509.X509Certificate>();
        boolean foundParent = false;

        // First, check if cert has expired
        if (!isWithinValidityPeriod(cert)) {
            model.getLogDb().addConsoleEvent(new ConsoleLogRecord("Security Error", "LotL Signing certificate has expired", "TSL Extractor"));
            return false;
        }

        // Loop through the configured list of certificates to fina a direct match
        for (iaik.x509.X509Certificate lotlCert : lotlCerts) {
            //Check if any cert in the list is the lotl sign cert
            if (lotlCert.equals(cert)) {
                return true;
            }            
        }
        // Else - look for a matching CA in the trust store
        for (iaik.x509.X509Certificate lotlCert : lotlCerts) {
            //Check if any of the lotl certs is parent of the signCert IF the lotl cert is a CA certificate
            if (lotlCert.getBasicConstraints() > -1) {
                SimpleChainVerifier verifier = new SimpleChainVerifier();
                verifier.addTrustedCertificate(lotlCert);
                iaik.x509.X509Certificate[] certs = new iaik.x509.X509Certificate[]{cert};
                try {
                    verifier.verifyChain(certs);
                    foundParent = true;
                    chain.add(cert);
                    chain.add(lotlCert);
                    break;
                } catch (CertificateException ex) {
                }
            }
        }

        if (!foundParent) {
            model.getLogDb().addConsoleEvent(new ConsoleLogRecord("Security Error", "LotL Signing certificate fails path validation", "TSL Extractor"));
            return false;
        }

        // Check if cert id revoked
        if (BasicCrlCheck.checkLotlCertRevocation(chain, model.getDataLocation())) {
            model.getLogDb().addConsoleEvent(new ConsoleLogRecord("LotL validation", "LotL Signature is OK", "TSL Extractor"));
            return true;
        } else {
            model.getLogDb().addConsoleEvent(new ConsoleLogRecord("Security Error", "LotL Signing certificate is revoked", "TSL Extractor"));
        }
        return false;
    }

    private static boolean isWithinValidityPeriod(X509Certificate cert) {
        Calendar present = Calendar.getInstance();
        Calendar certNotBefore = Calendar.getInstance();
        certNotBefore.setTime(cert.getNotBefore());
        Calendar certNotAfter = Calendar.getInstance();
        certNotAfter.setTime(cert.getNotAfter());
        if (present.before(certNotBefore)) {
            return false;
        }
        if (present.after(certNotAfter)) {
            return false;
        }
        return true;
    }
}
