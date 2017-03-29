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
