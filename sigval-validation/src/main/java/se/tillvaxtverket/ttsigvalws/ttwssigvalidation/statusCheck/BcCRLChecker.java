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
