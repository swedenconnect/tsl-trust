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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 *
 * @author stefan
 */
public class BcCRLChecker {
    private static Logger LOG = Logger.getLogger(BcCRLChecker.class.getName());
    
    public static boolean validateCertificateByCrl(byte[] certBytes, byte[] crlBytes, PublicKey crlSignerPk) {
        try {
            X509Certificate cert = getCert(certBytes);
            X509CRL crl = getCRLFromBytes(crlBytes);
            crl.verify(crlSignerPk);
            if (!crl.isRevoked(cert)){
                return true;
            }
        } catch (Exception e) {
            LOG.warning("BC CRL check failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Parse CRL bytes to a CRL.
     */
    private static X509CRL getCRLFromBytes(byte[] crlBytes)
            throws MalformedURLException, IOException, CertificateException,
            CRLException {
        InputStream crlStream = new ByteArrayInputStream(crlBytes);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL crl = (X509CRL) cf.generateCRL(crlStream);
            return crl;
        } finally {
            crlStream.close();
        }
    }
    
    private static X509Certificate getCert(byte[] certBytes) throws CertificateException, IOException {
        InputStream inStream = null;
        try {
            inStream = new ByteArrayInputStream(certBytes);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
            return cert;
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
    }
    
}
