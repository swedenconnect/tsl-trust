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
package se.tillvaxtverket.tsltrust.weblogic.issuestack;

import iaik.x509.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceList;
import se.tillvaxtverket.tsltrust.common.utils.general.EuropeCountry;

/**
 *
 * @author stefan
 */
public class IssueChecker {

    public static void checkTslExpiry(EuropeCountry country, TrustServiceList tsl) {
        if (tsl == null) {
            return;
        }
        Date nextUpdate = tsl.getNextUpdate();
        TSLIssueSubcode expiryWarning = checkDateForExpiry(nextUpdate, 15);
        if (expiryWarning == null) {
            TSLIssueStack.clear(country, TSLIssueID.tslExpiry);
            return;
        }

        TSLIssueStack.push(country, TSLIssueID.tslExpiry, expiryWarning, null);
    }

    public static void checkCertExpiry(EuropeCountry country, X509Certificate usedSignCert) {
        Date notAfter = usedSignCert.getNotAfter();
        TSLIssueSubcode expiryWarning = checkDateForExpiry(notAfter, 45);
        if (expiryWarning == null) {
            TSLIssueStack.clear(country, TSLIssueID.tslCertExpiry);
            return;
        }

        TSLIssueStack.push(country, TSLIssueID.tslCertExpiry, expiryWarning, null);
    }

    private static TSLIssueSubcode checkDateForExpiry(Date expiryDate, int triggerDays) {
        Calendar expiryTime = Calendar.getInstance();
        expiryTime.setTime(expiryDate);
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.add(Calendar.DAY_OF_MONTH, triggerDays);

        if (expiryTime.after(triggerTime)) {
            return null;
        }

        Calendar currentTime = Calendar.getInstance();
        if (expiryTime.before(currentTime)) {
            return TSLIssueSubcode.expired;
        }

        Calendar shortTime = Calendar.getInstance();
        shortTime.add(Calendar.DAY_OF_MONTH, 3);
        if (expiryTime.before(shortTime)) {
            return TSLIssueSubcode.shortNotify;
        }

        Calendar mediumTime = Calendar.getInstance();
        mediumTime.add(Calendar.DAY_OF_MONTH, 15);
        if (expiryTime.before(mediumTime)) {
            return TSLIssueSubcode.mediumNotify;
        }

        Calendar longTime = Calendar.getInstance();
        longTime.add(Calendar.DAY_OF_MONTH, 45);
        if (expiryTime.before(longTime)) {
            return TSLIssueSubcode.earlyNotify;
        }
        
        return null;
    }

}
