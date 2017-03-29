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
import se.tillvaxtverket.tsltrust.common.html.elements.ButtonElement;
import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.weblogic.content.HtmlConstants;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.weblogic.models.RequestModel;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.InputValidator;

/**
 * Content provider for the TSL view menu
 */
public class TslViewerArea extends WorkArea implements HtmlConstants {

    TslExtractorWeb tslExtractor;

    /**
     * Constructor
     * @param tslExtractor sub-content provider for the TSL viewer menu
     */
    public TslViewerArea(TslExtractorWeb tslExtractor) {
        this.tslExtractor = tslExtractor;
    }

    /**
     * Handles requests for data related to the TSL viewer menu
     * @param req http request data
     * @return response html data
     */
    @Override
    public String getHtmlData(RequestModel req) {
        SessionModel session = req.getSession();
        String html = "";

        tslExtractor.reloadTslData();

        if (req.getAction().equals("loadMain")) {
            return getMainTslViewArea(session, req.getWindowHeight());
        }

        if (req.getAction().equals("loadElement")) {
            return loadElementData(session, req.getId(), req.getParameter(), req.getWindowHeight());
        }

        if (req.getAction().equals("loadxml")) {
            return tslExtractor.getTslXMLData(getInt(req.getParameter()));
        }

        if (req.getAction().equals("loadFrameInfo")) {
            return tslExtractor.getInfoTable(session).toString();
        }

        if (req.getAction().equals("frameLoad")) {
            String parameter = req.getParameter();
            if (req.getId().equals(VIEW_BUTTON)) {
                setViewButtonParameter(parameter, session);
            }
            if (req.getId().equals(TSL_TABLE_DIV)) {
                session.setSelectedTsl(getInt(parameter));
            }
        }
        return html;
    }

    private String loadElementData(SessionModel session, String id, String parameter, int winHeight) {
        if (id.equals(TSL_TABLE_DIV)) {
            session.setSelectedTsl(getInt(parameter));
            session.setSelectedTsp(-1);
            session.setSelectedTs(-1);
            session.setTslButtonState("data");
            return getMainTslViewArea(session, winHeight);
        }
        if (id.equals(VIEW_BUTTON)) {
            setViewButtonParameter(parameter, session);
            return getMainTslViewArea(session, winHeight);
        }

        return "Not implemented response";
    }

    private void setViewButtonParameter(String parameter, SessionModel session) {
        parameter = InputValidator.filter(parameter, InputValidator.Rule.TEXT_LABEL);
        if (parameter.startsWith("cert")) {
            session.setTslButtonState("cert");
            session.setTslSelectedPemCert(parameter.substring(4));
        } else {
            session.setTslButtonState(parameter);
        }

    }

    private String getMainTslViewArea(SessionModel session, int winHeight) {
        HtmlElement mainTslDiv = new DivElement(MAIN_TSL_DIV);
        HtmlElement tslDiv = new DivElement(TSL_TABLE_DIV);
        HtmlElement infoHeadDiv = new DivElement(INFO_HEAD_DIV);
        HtmlElement infoBodyDiv = new DivElement(INFO_BODY_DIV);
        HtmlElement infoDiv = new DivElement(TSL_INFO_DIV);
        HtmlElement viewButtonDiv = new DivElement("xmlButtonDiv");
        HtmlElement tslDataFrame = new GenericHtmlElement("iframe");
        setupIframe(tslDataFrame, viewButtonDiv, session, winHeight);

        mainTslDiv.addHtmlElement(tslDiv);
        mainTslDiv.addHtmlElement(infoDiv);
        infoDiv.addHtmlElement(infoHeadDiv);
        infoDiv.addHtmlElement(infoBodyDiv);
        infoHeadDiv.addHtmlElement(viewButtonDiv);
        HtmlElement infoTitle = new GenericHtmlElement("span");
        infoTitle.setText("EU Trust Service List Information");
        infoTitle.addStyle("padding-left", "5px");

        //Add Head and Body to TSL Info
        infoHeadDiv.addHtmlElement(infoTitle);
        infoBodyDiv.addHtmlElement(tslDataFrame);


        tslDiv.addHtmlElement(tslExtractor.getTslTable(session));

        if (session.getTslButtonState().equals("xml")) {
            tslDataFrame.addAttribute("src", "xmlframe.jsp?selected=" + session.getSelectedTsl());
        }
        if (session.getTslButtonState().equals("data")) {
            tslDataFrame.addAttribute("src", "tslDataframe.jsp?parameter=" + session.getSelectedTsl());
        }
        if (session.getTslButtonState().equals("cert")) {
            tslDataFrame.addAttribute("src", "certDataframe.jsp?parameter=" + session.getTslSelectedPemCert());
        }
        return mainTslDiv.toString();
    }

    private void setupIframe(HtmlElement tslDataFrame, HtmlElement viewButtonDiv, SessionModel session, int winHeight) {

        ButtonElement xmlButton;
        if (!session.getTslButtonState().equals("data")) {
            xmlButton = new ButtonElement("Inspect TSL Info", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                        MAIN_DATA_AREA,
                        VIEW_BUTTON,
                        "data"});
        } else {
            xmlButton = new ButtonElement("Inspect XML Data", ONCLICK, LOAD_DATA_FUNCTION, new String[]{
                        MAIN_DATA_AREA,
                        VIEW_BUTTON,
                        "xml"});
        }
        xmlButton.addStyle("float", "right");
        viewButtonDiv.addHtmlElement(xmlButton);
        viewButtonDiv.addAttribute("width", "100%");
        int frmHeight = winHeight - 105;
        frmHeight = (frmHeight < 500) ? 500 : frmHeight;

        tslDataFrame.addAttribute("id", "tslIframe");
        tslDataFrame.addAttribute("frameborder", "0");
        tslDataFrame.addAttribute("height", String.valueOf(frmHeight));
        tslDataFrame.addAttribute("width", "100%");
    }
}
