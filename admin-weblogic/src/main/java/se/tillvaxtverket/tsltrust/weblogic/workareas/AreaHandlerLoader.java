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
