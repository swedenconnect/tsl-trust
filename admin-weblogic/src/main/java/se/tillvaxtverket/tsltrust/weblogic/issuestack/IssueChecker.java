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

import com.aaasec.lib.aaacert.AaaCertificate;
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

    public static void checkCertExpiry(EuropeCountry country, AaaCertificate usedSignCert) {
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
