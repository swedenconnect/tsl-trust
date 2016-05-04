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
