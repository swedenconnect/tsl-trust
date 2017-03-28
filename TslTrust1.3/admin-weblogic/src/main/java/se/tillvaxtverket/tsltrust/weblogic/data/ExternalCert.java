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

import iaik.x509.X509Certificate;
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
    X509Certificate cert=null;

    public ExternalCert() {
    }

    //Custom getter and setter
    public String getB64Cert(){        
        return b64Cert;
    }
    
    public boolean setB64Cert (String pemCert){
        this.cert = CertificateUtils.getCertificate(pemCert);
        b64Cert="";
        try {
            String trimPemCert = CorePEM.trimPemCert(CorePEM.getPemCert(cert.getEncoded()));
            b64Cert=trimPemCert;
        } catch (CertificateEncodingException ex) {
        }

        if (cert!=null){
            return true;
        }
        return false;
    }

    // default getters and setters
    public X509Certificate getCert() {
        return cert;
    }

    public void setCert(X509Certificate cert) {
        this.cert = cert;
        b64Cert="";
        try {
            String trimPemCert = CorePEM.trimPemCert(CorePEM.getPemCert(cert.getEncoded()));
            b64Cert=trimPemCert;
        } catch (CertificateEncodingException ex) {
        }
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String val) {
        this.certificateId = val==null?"":val;
    }
}
