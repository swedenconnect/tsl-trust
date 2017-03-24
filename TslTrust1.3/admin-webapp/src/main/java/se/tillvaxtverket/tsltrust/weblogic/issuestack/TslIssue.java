/*
 * Copyright 2015 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
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
package se.tillvaxtverket.tsltrust.weblogic.issuestack;

import java.util.Date;
import java.util.Map;

/**
 * Data class for storing information about TSL issues
 */


public class TslIssue {
    private TSLIssueID issueId;
    private TSLIssueSubcode subcode;
    private Date lastNotified;
    private Date nextNotification;
    private Map<String,String> paramMap;

    TslIssue(TSLIssueID tslIssueID) {
        this.issueId = tslIssueID;
    }

    public TSLIssueID getIssueId() {
        return issueId;
    }

    public void setIssueId(TSLIssueID issueId) {
        this.issueId = issueId;
    }

    public TSLIssueSubcode getSubcode() {
        return subcode;
    }

    public void setSubcode(TSLIssueSubcode subcode) {
        this.subcode = subcode;
    }

    public Date getLastNotified() {
        return lastNotified;
    }

    public void setLastNotified(Date lastNotified) {
        this.lastNotified = lastNotified;
    }

    public Date getNextNotification() {
        return nextNotification;
    }

    public void setNextNotification(Date nextNotification) {
        this.nextNotification = nextNotification;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }
    
}
