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

/**
 * Issue ID codes
 */


public enum TSLIssueID {
    unknownSigCert(new TSLIssueSubcode[]{
        TSLIssueSubcode.NULL
    }),
    unsigned(new TSLIssueSubcode[]{
        TSLIssueSubcode.NULL
    }),
    sigSyntax(new TSLIssueSubcode[]{
        TSLIssueSubcode.NULL
    }),
    unavailable(new TSLIssueSubcode[]{
        TSLIssueSubcode.NULL
    }),
    invalidSignature(new TSLIssueSubcode[]{
        TSLIssueSubcode.NULL
    }),
    tslExpiry(new TSLIssueSubcode[]{
        TSLIssueSubcode.mediumNotify,
        TSLIssueSubcode.shortNotify,
        TSLIssueSubcode.expired
    }),
    tslCertExpiry(new TSLIssueSubcode[]{
        TSLIssueSubcode.earlyNotify,
        TSLIssueSubcode.expired
    }),
    illegalCountry(new TSLIssueSubcode[]{
        TSLIssueSubcode.NULL
    });
    
    TSLIssueSubcode[] allowedSubCodes;

    private TSLIssueID(TSLIssueSubcode[] allowedSubCodes) {
        this.allowedSubCodes = allowedSubCodes;
    }

    public TSLIssueSubcode[] getAllowedSubCodes() {
        return allowedSubCodes;
    }
    
    
    
}
