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
import java.util.ArrayList;
import java.util.List;

/**
 * Data class holding certificate verification context parameters
 */
public class CertVerifyContext {

    private String statusCheckMethod = "";
    private List<String> crlKeys = new ArrayList<String>();
    private String ocspUrl = "";
    private List<String> cdpUrl = new ArrayList<String>();
    private boolean qualifiedCert = false;
    private OCSPVerifyContext ocspVerifyContext;
    private CertVerifyContext issuingCertContext;
    private boolean issuedByTa;
    private boolean crlStatusDetermined = false;
    private boolean noCheck = false;
    private boolean revoked = false;
    private boolean chainsToRoot = false;
    private boolean sigChainVerified = false;
    private boolean chainVerifyError=false;
    private String chainVerifyErrorMessage="";
    private boolean contextError = false;
    private boolean expired = false;
    private boolean notValidYet = false;
    private boolean statusCheckTimeOut = false;
    private List<X509Certificate> chain = new ArrayList<X509Certificate>();

    public CertVerifyContext() {
    }

    public void addCdpUrl(String urlString) {
        cdpUrl.add(urlString);
    }

    public List<String> getCdpUrl() {
        return cdpUrl;
    }

    public void setCdpUrl(List<String> cdpUrl) {
        this.cdpUrl = cdpUrl;
    }

    public boolean isIssuedByTa() {
        return issuedByTa;
    }

    public void setIssuedByTa(boolean issuedByTa) {
        this.issuedByTa = issuedByTa;
    }

    public CertVerifyContext getIssuingCertContext() {
        return issuingCertContext;
    }

    public void setIssuingCertContext(CertVerifyContext issuingCertContext) {
        this.issuingCertContext = issuingCertContext;
    }

    public boolean isNoCheck() {
        return noCheck;
    }

    public void setNoCheck(boolean noCheck) {
        this.noCheck = noCheck;
    }

    public List<X509Certificate> getChain() {
        return chain;
    }

    public void setChain(List<X509Certificate> chain) {
        this.chain = chain;
    }

    public boolean isContextError() {
        return contextError;
    }

    public void setContextError(boolean contextError) {
        this.contextError = contextError;
    }

    public boolean isChainsToRoot() {
        return chainsToRoot;
    }

    public void setChainsToRoot(boolean chainsToRoot) {
        this.chainsToRoot = chainsToRoot;
    }

    public List<String> getCrlKeys() {
        return crlKeys;
    }

    public void setCrlKeys(List<String> crlKeys) {
        this.crlKeys = crlKeys;
    }

    public String getOcspUrl() {
        return ocspUrl;
    }

    public void setOcspUrl(String ocspUrl) {
        this.ocspUrl = ocspUrl;
    }

    public OCSPVerifyContext getOcspVerifyContext() {
        return ocspVerifyContext;
    }

    public void setOcspVerifyContext(OCSPVerifyContext ocspVerifyContext) {
        this.ocspVerifyContext = ocspVerifyContext;
    }

    public boolean isQualifiedCert() {
        return qualifiedCert;
    }

    public void setQualifiedCert(boolean qualifiedCert) {
        this.qualifiedCert = qualifiedCert;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isSigChainVerified() {
        return sigChainVerified;
    }

    public void setSigChainVerified(boolean sigChainVerified) {
        this.sigChainVerified = sigChainVerified;
    }

    public String getStatusCheckMethod() {
        return statusCheckMethod;
    }

    public void setStatusCheckMethod(String statusCheckMethod) {
        this.statusCheckMethod = statusCheckMethod;
    }

    public boolean isCrlStatusDetermined() {
        return crlStatusDetermined;
    }

    public void setCrlStatusDetermined(boolean crlStatusDetermined) {
        this.crlStatusDetermined = crlStatusDetermined;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isNotValidYet() {
        return notValidYet;
    }

    public void setNotValidYet(boolean notValidYet) {
        this.notValidYet = notValidYet;
    }

    public boolean isStatusCheckTimeOut() {
        return statusCheckTimeOut;
    }

    public void setStatusCheckTimeOut(boolean statusCheckTimeOut) {
        this.statusCheckTimeOut = statusCheckTimeOut;
    }

    public String getChainVerifyErrorMessage() {
        return chainVerifyErrorMessage;
    }

    public void setChainVerifyErrorMessage(String chainVerifyError) {
        this.chainVerifyErrorMessage = chainVerifyError;
    }

    public boolean isChainVerifyError() {
        return chainVerifyError;
    }

    public void setChainVerifyError(boolean chainVerifyError) {
        this.chainVerifyError = chainVerifyError;
    }
    
    
}
