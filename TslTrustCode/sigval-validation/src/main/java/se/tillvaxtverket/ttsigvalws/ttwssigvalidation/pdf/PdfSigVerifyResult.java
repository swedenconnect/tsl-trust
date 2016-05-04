/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefan
 */
public class PdfSigVerifyResult {

    private X509Certificate cert = null;
    private String status = "";
    private boolean valid = false;
    private List<CMSSigVerifyResult> resultList = new ArrayList<>();
    private int sigCnt;
    private int validSignatures;

    public PdfSigVerifyResult() {
    }

    /**
     * Sets the certificate and the status to valid
     *
     * @param cert
     */
    public PdfSigVerifyResult(X509Certificate cert) {
        this.status = "ok";
        this.cert = cert;
        this.valid = true;
    }

    /**
     * Sets the values of the signature verification result
     *
     * @param cert Certificate
     * @param status Status
     * @param valid true if the signature is valid.
     */
    public PdfSigVerifyResult(X509Certificate cert, String status, boolean valid) {
        this.status = status;
        this.cert = cert;
        this.valid = valid;
    }

    /**
     * Sets the status comment for failed validation
     *
     * @param status Status
     */
    public PdfSigVerifyResult(String status) {
        this.status = status;
        this.cert = null;
        this.valid = false;
    }

    /**
     * Analyzes the result of individual signatures and generates the summary
     * of the signature validation results.
     */
    public void consolidateResults() {
        if (resultList.isEmpty()) {
            valid = false;
            status = "No signature covers this document";
            sigCnt = 0;
            validSignatures = 0;
            return;
        }
        sigCnt = resultList.size();
        int firstValid = -1, idx = 0, validCnt = 0;
        for (CMSSigVerifyResult result : resultList) {
            if (result.isValid()) {
                validCnt++;
            }
            if (firstValid < 0 && result.isValid()) {
                firstValid = idx;
            }
            idx++;
        }
        validSignatures = validCnt;

        //Get the most relevant signature result
        if (firstValid < 0) {
            valid = false;
            status = resultList.get(0).getStatus();
        } else {
            CMSSigVerifyResult result = resultList.get(firstValid);
            valid = result.isValid();
            status = result.getStatus();
            cert = result.getCert();
        }
    }

    public CMSSigVerifyResult addNewIndividualSignatureResult() {
        CMSSigVerifyResult result = new CMSSigVerifyResult();
        resultList.add(result);
        return result;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public void setCert(X509Certificate cert) {
        this.cert = cert;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<CMSSigVerifyResult> getResultList() {
        return resultList;
    }

    public void setResultList(List<CMSSigVerifyResult> resultList) {
        this.resultList = resultList;
    }

    public int getSigCnt() {
        return sigCnt;
    }

    public void setSigCnt(int sigCnt) {
        this.sigCnt = sigCnt;
    }

    public int getValidSignatures() {
        return validSignatures;
    }

    public void setValidSignatures(int validSignatures) {
        this.validSignatures = validSignatures;
    }
    
}