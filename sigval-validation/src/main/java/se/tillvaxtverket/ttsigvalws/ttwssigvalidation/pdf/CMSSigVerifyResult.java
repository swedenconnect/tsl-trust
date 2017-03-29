/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author stefan
 */
public class CMSSigVerifyResult {

    private X509Certificate cert = null;
    private List<X509Certificate> certList = new ArrayList<>();
    private String status = "";
    private boolean valid = false;
    private byte[] signedData;
    private boolean pades;
    private boolean padesVerified;
    private Date claimedSigningTime;
    private PublicKeyType pkType;
    private EcCurve ecCurve;
    private SupportedSigAlgoritm sigAlgo;
    private DigestAlgorithm digestAlgo;
    private int keyLength;
    private SupportedSigAlgoritm cmsapSigAlgo;
    private DigestAlgorithm cmsapDigestAlgo;
    private boolean cmsAlgoProtection;
    private List<TimeStampResult> timStampResultList = new ArrayList<>();

    public CMSSigVerifyResult() {
    }

    public boolean hasValidTimeStamp() {
        if (timStampResultList == null || timStampResultList.isEmpty()) {
            return false;
        }
        return timStampResultList.stream().anyMatch((tsResult) -> (tsResult.isValidTimeStamp()));
    }
    
    public boolean isTimeStamped(){
        return timStampResultList != null && !timStampResultList.isEmpty();
    }
    
    public List<TimeStampResult> getValidTimeStamps(){
        List<TimeStampResult> validList = new ArrayList<>();
        getTimStampResultList().stream().filter((result) -> (result.isValidTimeStamp())).forEach((result) -> {
            validList.add(result);
        });
        return validList;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public void setCert(X509Certificate cert) {
        this.cert = cert;
    }

    public List<X509Certificate> getCertList() {
        return certList;
    }

    public void setCertList(List<X509Certificate> certList) {
        this.certList = certList;
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

    public byte[] getSignedData() {
        return signedData;
    }

    public void setSignedData(byte[] signedData) {
        this.signedData = signedData;
    }

    public boolean isPades() {
        return pades;
    }

    public void setPades(boolean pades) {
        this.pades = pades;
    }

    public Date getClaimedSigningTime() {
        return claimedSigningTime;
    }

    public void setClaimedSigningTime(Date claimedSigningTime) {
        this.claimedSigningTime = claimedSigningTime;
    }

    public PublicKeyType getPkType() {
        return pkType;
    }

    public void setPkType(PublicKeyType pkType) {
        this.pkType = pkType;
    }

    public EcCurve getEcCurve() {
        return ecCurve;
    }

    public void setEcCurve(EcCurve ecCurve) {
        this.ecCurve = ecCurve;
    }

    public SupportedSigAlgoritm getSigAlgo() {
        return sigAlgo;
    }

    public void setSigAlgo(SupportedSigAlgoritm sigAlgo) {
        this.sigAlgo = sigAlgo;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public SupportedSigAlgoritm getCmsapSigAlgo() {
        return cmsapSigAlgo;
    }

    public void setCmsapSigAlgo(SupportedSigAlgoritm cmsapSigAlgo) {
        this.cmsapSigAlgo = cmsapSigAlgo;
    }

    public DigestAlgorithm getCmsapDigestAlgo() {
        return cmsapDigestAlgo;
    }

    public void setCmsapDigestAlgo(DigestAlgorithm cmsapDigestAlgo) {
        this.cmsapDigestAlgo = cmsapDigestAlgo;
    }

    public DigestAlgorithm getDigestAlgo() {
        return digestAlgo;
    }

    public void setDigestAlgo(DigestAlgorithm digestAlgo) {
        this.digestAlgo = digestAlgo;
    }

    public boolean isCmsAlgoProtection() {
        return cmsAlgoProtection;
    }

    public void setCmsAlgoProtection(boolean cmsAlgoProtection) {
        this.cmsAlgoProtection = cmsAlgoProtection;
    }

    public boolean isPadesVerified() {
        return padesVerified;
    }

    public void setPadesVerified(boolean padesVerified) {
        this.padesVerified = padesVerified;
    }

    public List<TimeStampResult> getTimStampResultList() {
        return timStampResultList;
    }

    public void setTimStampResultList(List<TimeStampResult> timStampResultList) {
        this.timStampResultList = timStampResultList;
    }

}
