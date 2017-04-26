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
package se.tillvaxtverket.tsltrust.weblogic.content.ts;

import com.aaasec.lib.aaacert.AaaCertificate;
import java.util.List;
import se.tillvaxtverket.tsltrust.common.tsl.ServiceHistoryInstance;
import se.tillvaxtverket.tsltrust.common.tsl.TrustService;
import se.tillvaxtverket.tsltrust.common.tsl.sie.ServiceInfoExtension;
import se.tillvaxtverket.tsltrust.common.tsl.sie.SieType;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import se.tillvaxtverket.tsltrust.weblogic.utils.ExtractorUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.HtmlUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.InfoTableUtils;

/**
 * Class providing information display of Trust Service data
 */
public class TrustServiceInformation implements HtmlConstants, TTConstants {

    /**
     * Dummy constructor preventing instantiation of this class
     */
    private TrustServiceInformation() {
    }

    public static void addTrustServiceInformation(InfoTableElements tsDataElm, TrustService ts, InfoTableModel tm, InfoTableUtils itUtil) {
        tsDataElm.addNewSection(tm, "Service Type").addNewElement(ExtractorUtil.stripRefUrl(ts.getType()));
        tsDataElm.addNewSection(tm, "Service Status").addNewElement(ExtractorUtil.stripRefUrl(ts.getStatus()));
        if (ts.getStatusStartingTime() != null) {
            tsDataElm.addNewSection(tm, "Status valid from").addNewElement(ts.getStatusStartingTime());
        }
        List<String> schemeServiceUris = ts.getSchemeServiceUris();
        if (!schemeServiceUris.isEmpty()) {
            InfoTableSection section = tsDataElm.addNewSection(tm, "Scheme Service URI", false);
            section.setFoldedElement(HtmlUtil.link(schemeServiceUris.get(0)));
            for (String str : schemeServiceUris) {
                section.addNewElement(HtmlUtil.link(str));
            }
        }
        List<String> tSPServiceUris = ts.getTSPServiceUris();
        if (!tSPServiceUris.isEmpty()) {
            InfoTableSection section = tsDataElm.addNewSection(tm, "TSP Service URI", false);
            section.setFoldedElement(HtmlUtil.link(tSPServiceUris.get(0)));
            for (String str : tSPServiceUris) {
                section.addNewElement(HtmlUtil.link(str));
            }
        }
        List<String> serviceSupplyPoints = ts.getServiceSupplyPoints();
        if (!serviceSupplyPoints.isEmpty()) {
            InfoTableSection section = tsDataElm.addNewSection(tm, "Supply Points");
            for (String str : serviceSupplyPoints) {
                section.addNewElement(HtmlUtil.link(str));
            }
        }

        List<ServiceHistoryInstance> serviceHistory = ts.getServiceHistory();
        for (ServiceHistoryInstance sh : serviceHistory) {
            InfoTableSection historySect = tsDataElm.addNewSection(tm, "Service History", false);
            HistoricalInfo.addHistoricalInfo(sh, historySect, tm, itUtil);
        }
        List<ServiceInfoExtension> serviceInfoExtensions = ts.getServiceInfoExtensions();
        for (ServiceInfoExtension sie : serviceInfoExtensions) {
            InfoTableSection extSect;
            if (sie.getType() == SieType.TakenOverBy || sie.getType() == SieType.Qualifications) {
                extSect = tsDataElm.addNewSection(tm, sie.getName(), true);
            } else {
                extSect = tsDataElm.addNewSection(tm, sie.getName(), false);
            }
            ServiceExtensioininfo.addExtensionInfo(sie, extSect, tm);
        }
        List<AaaCertificate> certs = ts.getServiceDigitalIdentityCerts();
        if (!certs.isEmpty()) {
            for (AaaCertificate cert : certs) {
                try {
                    itUtil.addCertificate(tsDataElm, cert, "Service Certificate", true);
                } catch (Exception ex) {
                    tsDataElm.addNewSection(tm, "Service Certificate").addNewElement(
                            new String[]{"Invalid Certificate"}, new String[]{ERROR});
                }
            }
        }
    }
}
