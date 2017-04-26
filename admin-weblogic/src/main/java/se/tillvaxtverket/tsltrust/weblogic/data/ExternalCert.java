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

import com.aaasec.lib.aaacert.AaaCertificate;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import se.tillvaxtverket.tsltrust.common.utils.core.CorePEM;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;

/**
 * Additional non-TLS certificate database record data class
 */
public class ExternalCert implements Serializable{
    public String certificateId="";
    public String b64Cert;
    AaaCertificate cert=null;

    public ExternalCert() {
    }

    //Custom getter and setter
    public String getB64Cert(){        
        return b64Cert;
    }
    
    public boolean setB64Cert (String pemCert){
        this.cert = CertificateUtils.getCertificate(pemCert);
        b64Cert="";
        String trimPemCert = CorePEM.trimPemCert(CorePEM.getPemCert(cert.getEncoded()));
        b64Cert=trimPemCert;

        if (cert!=null){
            return true;
        }
        return false;
    }

    // default getters and setters
    public AaaCertificate getCert() {
        return cert;
    }

    public void setCert(AaaCertificate cert) {
        this.cert = cert;
        b64Cert="";
        String trimPemCert = CorePEM.trimPemCert(CorePEM.getPemCert(cert.getEncoded()));
        b64Cert=trimPemCert;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String val) {
        this.certificateId = val==null?"":val;
    }
}
