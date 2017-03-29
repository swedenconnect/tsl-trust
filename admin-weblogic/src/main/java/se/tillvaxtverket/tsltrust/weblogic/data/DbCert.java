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
package se.tillvaxtverket.tsltrust.weblogic.data;

import se.tillvaxtverket.tsltrust.common.utils.core.PEM;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import iaik.x509.X509Certificate;
import java.security.cert.CertificateEncodingException;

/**
 * Certificate record data class
 */
public final class DbCert {

    public static final int REVOKED = 1, NOT_REVOKED = 0;
    private long serial;
    private String pemCert;
    private int revoked = 0;
    private long revDate = 0;
    private X509Certificate certificate;

    public DbCert() {
        serial = 0;
        pemCert = "";
        certificate = null;
    }

    public DbCert(String pemCert) {
        setPemCert(pemCert);
        if (certificate != null) {
            serial = certificate.getSerialNumber().longValue();
        } else {
            serial = 0;
        }
    }

    public DbCert(X509Certificate certificate) {
        setCertificate(certificate);
        if (certificate != null) {
            serial = certificate.getSerialNumber().longValue();
        } else {
            serial = (long)0;
        }
    }

    public String getPemCert() {
        return pemCert;
    }

    public void setPemCert(String pemCert) {
        this.pemCert = pemCert;
        certificate = CertificateUtils.getCertificate(pemCert);
    }

    public long getRevDate() {
        return revDate;
    }

    public void setRevDate(long revDate) {
        this.revDate = revDate;
    }

    public int getRevoked() {
        return revoked;
    }

    public void setRevoked(int revoked) {
        this.revoked = revoked;
    }

    public long getSerial() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial = serial;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
        try {
            pemCert = PEM.getPemCert(certificate.getEncoded());
        } catch (CertificateEncodingException ex) {
            certificate = null;
            pemCert = "";
        }
    }
    
}
