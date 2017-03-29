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
