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
import java.util.List;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.InputValidator;

/**
 * Content provider for the TSP records menu
 */
public class TSPRecordsArea extends WorkArea {

    TslExtractorWeb tslExtractor;

    /**
     * Constructor
     * @param tslExtractor Sub-content provider object for response data
     */
    public TSPRecordsArea(TslExtractorWeb tslExtractor) {
        this.tslExtractor = tslExtractor;
    }

    /**
     * Process requests related to the TSP records menu
     * @param req http request data
     * @return html response data
     */
    @Override
    public String getHtmlData(RequestModel req) {
        SessionModel session = req.getSession();
        String html = "";

        if (req.getAction().equals("loadMain")) {
            return getMainTslViewArea(session, req.getWindowHeight());
        }

        if (req.getAction().equals("loadElement")) {
            return loadElementData(session, req.getId(), req.getParameter(), req.getWindowHeight());
        }

        return html;

    }

    private String getMainTslViewArea(SessionModel session, int winHeight) {
        return tslExtractor.getDBTable(session, winHeight);
    }

    private String loadElementData(SessionModel session, String id, String parameter, int winHeight) {
        if (id.equals("dbSort")) {
            session.setSortColumn(getInt(parameter));
            return getMainTslViewArea(session, winHeight);
        }
        if (id.equals("dbSortButton")) {
            List<Integer> selectedCols = session.getSelectedCols();
            int param = 0;
            int max = 0;
            for (int i : selectedCols) {
                max = (i > max) ? i : max;
            }
            max++;
            int col = 0;
            for (int i = 0; i < max; i++) {
                if (selectedCols.contains(i)) {
                    if (getInt(parameter) == col) {
                        param = i;
                        break;
                    }
                    col++;
                }
            }

            session.setSortColumn(param);
            return getMainTslViewArea(session, winHeight);
        }
        if (id.equals("dbCheck")) {
            List<Integer> selectedCols = session.getSelectedCols();
            Integer i = getInt(parameter);
            if (!selectedCols.contains(i)) {
                selectedCols.add(i);
            } else {
                selectedCols.remove(i);
            }
            return getMainTslViewArea(session, winHeight);
        }
        if (id.startsWith("filter")) {
            parameter = InputValidator.filter(parameter, new InputValidator.Rule[]{InputValidator.Rule.PRINTABLE_ASCII, InputValidator.Rule.HTML_TAGS});
            if (id.endsWith("Country")) {
                session.setCountryFilter(parameter);
            }
            if (id.endsWith("Type")) {
                session.setTypeFilter(parameter);
            }
            if (id.endsWith("Status")) {
                session.setStatusFilter(parameter);
            }
            if (id.endsWith("Signature")) {
                session.setSigFilter(parameter);
            }
            return getMainTslViewArea(session, winHeight);
        }


//        if (container.equals("tslDiv")) {
//            return getMainTslViewArea(session);
//        }
        return "Not implemented response";

    }
}
