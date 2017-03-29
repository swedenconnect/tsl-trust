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
