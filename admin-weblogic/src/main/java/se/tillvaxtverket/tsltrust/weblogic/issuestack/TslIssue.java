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
