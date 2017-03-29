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

import java.util.Calendar;
import org.bouncycastle.tsp.TimeStampToken;

/**
 * Data class holding context parameters for Time Stamp validation.
 */
public class TimeStampContext {
    private TimeStampToken timeStampToken;
    private boolean tsSignValidated = false;
    private boolean messageImprintValidated=false;
    private Calendar timeStampDate;
    private CertVerifyContext certVerifyContext;

    public TimeStampContext() {
    }

    public CertVerifyContext getCertVerifyContext() {
        return certVerifyContext;
    }

    public void setCertVerifyContext(CertVerifyContext certVerifyContext) {
        this.certVerifyContext = certVerifyContext;
    }

    public boolean isMessageImprintValidated() {
        return messageImprintValidated;
    }

    public void setMessageImprintValidated(boolean messageImprintValidated) {
        this.messageImprintValidated = messageImprintValidated;
    }

    public Calendar getTimeStampDate() {
        return timeStampDate;
    }

    public void setTimeStampDate(Calendar timeStampDate) {
        this.timeStampDate = timeStampDate;
    }

    public TimeStampToken getTimeStampToken() {
        return timeStampToken;
    }

    public void setTimeStampToken(TimeStampToken timeStampToken) {
        this.timeStampToken = timeStampToken;
    }

    public boolean isTsSignValidated() {
        return tsSignValidated;
    }

    public void setTsSignValidated(boolean tsSignValidated) {
        this.tsSignValidated = tsSignValidated;
    }
    
    
}
