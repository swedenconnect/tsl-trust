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
