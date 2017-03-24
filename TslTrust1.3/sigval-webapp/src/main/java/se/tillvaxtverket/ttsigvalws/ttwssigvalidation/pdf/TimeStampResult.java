/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf;

/**
 *
 * @author stefan
 */
public class TimeStampResult {

    private byte[] timestamp;
    private CMSSigVerifyResult signatureVerification;
    private boolean timestampMatch = false;
    private TimeStampData tsData;

    public TimeStampResult() {
    }

    /**
     * Test if Timestamp has a valid signature and if the timestamp matches the timestamped signature value
     * @return true if timestamp is valid
     */
    public boolean isValidTimeStamp() {
        if (!isTimestampMatch()) {
            return false;
        }
        if (tsData==null || tsData.getTime()==null){
            return false;
        }
        return getSignatureVerification() != null && getSignatureVerification().isValid();
    }

    public byte[] getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(byte[] timestamp) {
        this.timestamp = timestamp;
    }

    public CMSSigVerifyResult getSignatureVerification() {
        return signatureVerification;
    }

    public void setSignatureVerification(CMSSigVerifyResult signatureVerification) {
        this.signatureVerification = signatureVerification;
    }

    public boolean isTimestampMatch() {
        return timestampMatch;
    }

    public void setTimestampMatch(boolean timestampMatch) {
        this.timestampMatch = timestampMatch;
    }

    public TimeStampData getTsData() {
        return tsData;
    }

    public void setTsData(TimeStampData tsData) {
        this.tsData = tsData;
    }

}
