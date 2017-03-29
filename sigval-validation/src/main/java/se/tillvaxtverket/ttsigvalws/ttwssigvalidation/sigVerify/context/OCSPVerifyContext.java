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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context;

import iaik.x509.X509Certificate;
import iaik.x509.ocsp.OCSPRequest;
import iaik.x509.ocsp.OCSPResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Data class holding context parameters for OCSP processing
 */
public class OCSPVerifyContext {
    private List<String> log = new ArrayList<String>();
    private List<String> exceptionLog = new ArrayList<String>();
    private CertVerifyContext certVerifyContxt;
    private boolean ocspCheckOK;
    private boolean ocspRevoked = false;
    private OCSPRequest request;
    private OCSPResponse response;
    private X509Certificate targetCert;
    private X509Certificate targetIssuer;
   
    public OCSPVerifyContext(){                
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }

    public boolean isOcspRevoked() {
        return ocspRevoked;
    }

    public void setOcspRevoked(boolean ocspRevoked) {
        this.ocspRevoked = ocspRevoked;
    }

    public CertVerifyContext getCertVerifyContxt() {
        return certVerifyContxt;
    }

    public void setCertVerifyContxt(CertVerifyContext certVerifyContxt) {
        this.certVerifyContxt = certVerifyContxt;
    }

    public List<String> getExceptionLog() {
        return exceptionLog;
    }

    public void setExceptionLog(List<String> exceptionLog) {
        this.exceptionLog = exceptionLog;
    }

    public boolean isOcspCheckOK() {
        return ocspCheckOK;
    }

    public void setOcspCheckOK(boolean ocspCheckOK) {
        this.ocspCheckOK = ocspCheckOK;
    }

    public OCSPRequest getRequest() {
        return request;
    }

    public void setRequest(OCSPRequest request) {
        this.request = request;
    }

    public OCSPResponse getResponse() {
        return response;
    }

    public void setResponse(OCSPResponse response) {
        this.response = response;
    }

    public X509Certificate getTargetCert() {
        return targetCert;
    }

    public void setTargetCert(X509Certificate targetCert) {
        this.targetCert = targetCert;
    }

    public X509Certificate getTargetIssuer() {
        return targetIssuer;
    }

    public void setTargetIssuer(X509Certificate targetIssuer) {
        this.targetIssuer = targetIssuer;
    }

    
    
    
}
