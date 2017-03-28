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
import se.tillvaxtverket.tsltrust.common.tsl.sie.ServiceInfoExtension;
import se.tillvaxtverket.tsltrust.weblogic.content.*;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.tsl.ServiceHistoryInstance;
import se.tillvaxtverket.tsltrust.weblogic.utils.ExtractorUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.InfoTableUtils;

/**
 * Providing UI elements for displaying historical information
 */
public class HistoricalInfo implements HtmlConstants, TTConstants {

    private static final String[] ATTR = new String[]{ATTRIBUTE_NAME, ATTRIBUTE_VALUE};
    private static final String[] PROP = new String[]{PROPERTY_NAME, PROPERTY_VALUE};
    private static final String[] CERT_INFO = new String[]{INVERTED_HEAD, TABLE_SECTION_HEAD};
    private static final String[] EXT_ATTR = new String[]{EXTENSION_NAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE};
    private static final String[] EXT_PROP = new String[]{EXTENSION_NAME, PROPERTY_NAME, PROPERTY_VALUE};
    private static final Logger LOG = Logger.getLogger(HistoricalInfo.class.getName());

    private HistoricalInfo() {
    }

    public static void addHistoricalInfo(ServiceHistoryInstance sh, InfoTableSection historySect, InfoTableModel tm, InfoTableUtils itUtil) {

        //get fold element text
        String foldString = "";
        if (sh.getStatusStartingTime() != null) {
            foldString = TIME_FORMAT.format(sh.getStatusStartingTime());
        } else {
            foldString = sh.getName();
        }
        historySect.setFoldedElement(foldString);
        historySect.setKeepFoldableElement(true);
        historySect.setSectionHeadingClasses(new String[]{TABLE_SECTION_HEAD, TABLE_SECTION_HEAD});
        InfoTableElements elements = historySect.getElements();

        addSingleObject(elements, tm, "Service Name", sh.getName());
        addSingleObject(elements, tm, "Service Type", sh.getType(), true);
        addSingleObject(elements, tm, "Service Status", sh.getStatus(), true);
        addSingleObject(elements, tm, "Status valid from", sh.getStatusStartingTime());
        List<ServiceInfoExtension> serviceInfoExtensions = sh.getServiceInfoExtensions();
        for (ServiceInfoExtension sie : serviceInfoExtensions) {
            InfoTableSection extSect = elements.addNewSection(tm, sie.getName(), false);
            ServiceExtensioininfo.addExtensionInfo(sie, extSect, tm);
        }
        List<X509Certificate> certs = sh.getServiceDigitalIdentityCerts();
        if (!certs.isEmpty()) {
            for (X509Certificate cert : certs) {
                try {
                    itUtil.addCertificate(elements, cert, "Service Certificate", true);
                } catch (Exception ex) {
                    elements.addNewSection(tm, "Service Certificate").addNewElement(
                            new String[]{"Invalid Certificate"}, new String[]{ERROR});
                }
            }
        }
    }

    private static void addSingleObject(InfoTableElements elm, InfoTableModel tm, String label, Object o) {
        addSingleObject(elm, tm, label, o, false);
    }

    private static void addSingleObject(InfoTableElements elm, InfoTableModel tm, String label, Object o, boolean strip) {
        if (o == null) {
            return;
        }
        if (o instanceof String) {
            String str = (String) o;
            if (str.length() == 0) {
                return;
            }
        }
        if (strip) {
            elm.addNewSection(tm, label).addNewElement(ExtractorUtil.stripRefUrl((String) o));
        } else {
            elm.addNewSection(tm, label).addNewElement(o);
        }
    }
}