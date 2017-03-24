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
