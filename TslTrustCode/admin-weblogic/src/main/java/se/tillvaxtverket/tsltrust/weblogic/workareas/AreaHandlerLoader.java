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
package se.tillvaxtverket.tsltrust.weblogic.workareas;

import se.tillvaxtverket.tsltrust.weblogic.content.TslExtractorWeb;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;

/**
 * This class provides the functions for loading an appropriate content handler class to generate a suitable
 * response to the user's http request.
 */
public class AreaHandlerLoader {

    private TslExtractorWeb tslExtractor;
    private TslViewerArea tslViewerArea;
    private TSPRecordsArea tSPRecordsArea;
    private PolicyManagementArea policyManagementArea;
    private CertManagementArea certManagementArea;
    private LogsArea logsArea;
    private AuthArea authArea;

    /**
     * Provides an area handler object
     * @param model Static data for the application
     */
    public AreaHandlerLoader(TslTrustModel model) {
        tslExtractor = new TslExtractorWeb(model);
        tslExtractor.reloadTslData();

        //Load Areas
        tslViewerArea = new TslViewerArea(tslExtractor);
        tSPRecordsArea = new TSPRecordsArea(tslExtractor);
        policyManagementArea = new PolicyManagementArea(model);
        certManagementArea = new CertManagementArea(model);
        logsArea = new LogsArea(model);
        authArea = new AuthArea(model);
    }

    /**
     * Selects an appropriate content provider class for processing the user's http request.
     * @param req The user's http request model data
     * @return An object of an appropriate content provider class. 
     */
    public WorkArea getWorkAreaHandler(RequestModel req) {
        WorkArea area = null;
        SessionModel session = req.getSession();
        int selectedMenu = session.getSelectedMenu();

        switch (selectedMenu) {
            case 1:
                area = tslViewerArea;
                break;
            case 2:
                area = tSPRecordsArea;
                break;
            case 3:
                area = certManagementArea;
                break;
            case 4:
                area = authArea;
                break;
            case 5:
                area = logsArea;
                break;
            default:
                area = policyManagementArea;
        }

        return area;
    }
}
