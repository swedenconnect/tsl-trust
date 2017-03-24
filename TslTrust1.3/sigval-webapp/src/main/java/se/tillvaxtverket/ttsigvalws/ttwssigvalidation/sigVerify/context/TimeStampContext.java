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
