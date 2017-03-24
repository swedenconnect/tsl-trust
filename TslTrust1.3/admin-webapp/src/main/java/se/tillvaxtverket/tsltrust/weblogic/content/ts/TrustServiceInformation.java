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
package se.tillvaxtverket.tsltrust.weblogic.content.ts;

import iaik.x509.X509Certificate;
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
        List<X509Certificate> certs = ts.getServiceDigitalIdentityCerts();
        if (!certs.isEmpty()) {
            for (X509Certificate cert : certs) {
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
