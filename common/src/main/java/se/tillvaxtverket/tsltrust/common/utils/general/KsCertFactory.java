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
package se.tillvaxtverket.tsltrust.common.utils.general;

import iaik.x509.X509Certificate;
import java.io.ByteArrayInputStream;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Certificate factory functions used to generate X.509 certificate objects
 */
public class KsCertFactory {

    public static Certificate getCertificate(iaik.x509.X509Certificate iaikCert) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(iaikCert.getEncoded()));
            return cert;
        } catch (CertificateException ex) {
        }
        return null;
    }

    public static Certificate getCertificate(byte[] certBytes) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(certBytes));
            return cert;
        } catch (CertificateException ex) {
        }
        return null;
    }

    public static X509Certificate getIaikCert(java.security.cert.Certificate inCert) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "IAIK");
            X509Certificate iaikCert = (iaik.x509.X509Certificate) cf.generateCertificate(new ByteArrayInputStream(inCert.getEncoded()));
            return iaikCert;
        } catch (Exception ex) {
        }
        return null;
    }

    public static iaik.x509.X509Certificate getIaikCert(byte[] certBytes) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "IAIK");
            X509Certificate iaikCert = (iaik.x509.X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
            return iaikCert;
        } catch (Exception ex) {
        }
        return null;
    }

    public static java.security.cert.X509Certificate getX509Cert(byte[] certData) {
        return getIaikCert(certData);

//        try {
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            try {
//                X509Cert = (java.security.cert.X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certData));
//                return X509Cert;
//            } catch (CertificateException e) {
//                return null;
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
    }

    public static CRL convertCRL(iaik.x509.X509CRL iaikCRL) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            CRL crl = cf.generateCRL(new ByteArrayInputStream(iaikCRL.getEncoded()));
            return crl;
        } catch (Exception ex) {
        }
        return null;

    }

    public static iaik.x509.X509CRL getCRL(byte[] crlBytes) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "IAIK");
            iaik.x509.X509CRL crl = (iaik.x509.X509CRL) cf.generateCRL(new ByteArrayInputStream(crlBytes));
            return crl;
        } catch (Exception ex) {
        }
        return null;

    }

    public static List<X509Certificate> getIaikCertList(List<java.security.cert.X509Certificate> javaCertList) {
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        for (Certificate javaCert : javaCertList) {
            try {
                X509Certificate cert = getIaikCert(javaCert.getEncoded());
                certList.add(cert);
            } catch (CertificateEncodingException ex) {
            }
        }
        return certList;
    }

    public static List<X509Certificate> getIaikCertList(Certificate[] pdfSignCerts) {
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        for (Certificate javaCert : pdfSignCerts) {
            try {
                X509Certificate cert = getIaikCert(javaCert.getEncoded());
                certList.add(cert);
            } catch (CertificateEncodingException ex) {
            }
        }
        return certList;
    }

    public static List<X509Certificate> getOrderedCertList(List<X509Certificate> unorderedpdfSignCerts, X509Certificate signerCert) {
        List<X509Certificate> orderedCertList = new ArrayList<X509Certificate>();

        for (X509Certificate cert : unorderedpdfSignCerts) {
            if (cert.equals(signerCert)) {
                orderedCertList.add(signerCert);
                break;
            }
        }

        if (orderedCertList.isEmpty()) {
            return orderedCertList;
        }

        boolean noParent = false;
        boolean selfSigned = false;
        X509Certificate target = signerCert;

        while (!noParent && !selfSigned) {
            for (X509Certificate cert : unorderedpdfSignCerts) {
                try {
                    target.verify(cert.getPublicKey());
                    orderedCertList.add(cert);
                    target = cert;
                    noParent = false;
                    selfSigned = isSelfSigned(cert);
                    break;
                } catch (Exception e) {
                    noParent = true;
                }
            }

        }
        return orderedCertList;

    }

    private static boolean isSelfSigned(X509Certificate cert) {
        try {
            cert.verify(cert.getPublicKey());
            return true;
        } catch (Exception e) {
        }
        return false;
    }
}
