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
package se.tillvaxtverket.tsltrust.weblogic;

import se.tillvaxtverket.tsltrust.weblogic.workareas.AreaHandlerLoader;
import se.tillvaxtverket.tsltrust.weblogic.workareas.WorkArea;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;

/**
 * Main class for providing the UI of the TSL Trust admin service.
 */
public class MainTslTrust {

    AreaHandlerLoader areaLoader;

    /**
     * Creates an object of the MainTslTrust class
     * @param model Static data for the web service
     */
    public MainTslTrust(TslTrustModel model) {
        areaLoader = new AreaHandlerLoader(model);
    }

    /**
     * Process a http request from a user and returns an appropriate html response.
     * @param req model holding data related to the http request.
     * @return html data response.
     */
    public String loadData(RequestModel req) {
        WorkArea area = areaLoader.getWorkAreaHandler(req);
        String data = area.getHtmlData(req);
        return data;
    }
}
