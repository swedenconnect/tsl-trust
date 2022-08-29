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
package se.tillvaxtverket.tsltrust.weblogic.content;

import com.aaasec.lib.aaacert.AaaCertificate;
import com.aaasec.lib.crypto.xml.XmlBeansUtil;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import se.tillvaxtverket.tsltrust.weblogic.content.ts.TrustServiceInformation;
import se.tillvaxtverket.tsltrust.weblogic.data.TslMetaData;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.utils.DbTableFilter;
import se.tillvaxtverket.tsltrust.weblogic.utils.XmlFormatter;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDb;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElement;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableElements;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableModel;
import se.tillvaxtverket.tsltrust.weblogic.models.InfoTableSection;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.Locale;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import se.tillvaxtverket.tsltrust.common.html.elements.CheckboxElement;
import se.tillvaxtverket.tsltrust.common.html.elements.DivElement;
import se.tillvaxtverket.tsltrust.common.html.elements.GenericHtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.HtmlElement;
import se.tillvaxtverket.tsltrust.common.html.elements.SelectElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableCellElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TableRowElement;
import se.tillvaxtverket.tsltrust.common.html.elements.TextObject;
import java.util.ArrayList;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.weblogic.models.SessionModel;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import se.tillvaxtverket.tsltrust.weblogic.utils.HtmlUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.InfoTableUtils;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.common.utils.general.CertificateUtils;
import se.tillvaxtverket.tsltrust.common.utils.general.EuropeCountry;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.etsi.uri.x02231.v2.TrustStatusListType;
import se.tillvaxtverket.tsltrust.common.tsl.OtherTSLPointerData;
import se.tillvaxtverket.tsltrust.common.utils.general.XmlUtils;
import se.tillvaxtverket.tsltrust.common.tsl.TSLFactory;
import se.tillvaxtverket.tsltrust.common.tsl.TrustService;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceList;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceProvider;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.TSLIssueID;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.TSLIssueStack;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.TSLIssueSubcode;
import se.tillvaxtverket.tsltrust.weblogic.issuestack.TslIssue;
import se.tillvaxtverket.tsltrust.weblogic.utils.ExtractorUtil;
import se.tillvaxtverket.tsltrust.weblogic.utils.TslCache;

/**
 * Content provider for TSL related information. This class provides various
 * functions for generating tables and data related to TSLs
 */
public class TslExtractorWeb implements HtmlConstants, TTConstants {

    private static final Logger LOG = Logger.getLogger(TslExtractorWeb.class.getName());
    static final String[] TS_TABLE_CLASS = new String[]{TABLE_HEAD_CLASS, TABLE_NEUTRAL_CLASS, TABLE_STRIPED_CLASS};
    static final Locale SWEDISH = new Locale("sv");
    File recacheFile;
    List<TslMetaData> tslList;
    int logHash = 0;
    String lastrecache = "";
    TrustServiceList currentLotl;
    Font fontCourier = new Font("Courier", Font.PLAIN, 13), fontLucida12 = new Font("Lucida Grande", Font.PLAIN, 12), fontLucida13 = new Font("Lucida Grande", Font.PLAIN, 13);
//    SessionModel session;
    TslCertDb dbUtil;
    TslTrustModel model;
    TslLoader tslLoader;
    Map<String, AaaCertificate> otpCertMap;
    Thread tslReloadThread;
    Thread tslRecacheThread;
    TSLFactory tslFact = new TSLFactory();
    TslCache tCache;

    /**
     * Content provider for TSL related information. This class provides various
     * functions for generating tables and data related to TSLs
     *
     * @param model Application model data
     */
    public TslExtractorWeb(TslTrustModel model) {
        this.model = model;
        tCache = new TslCache(model);
        currentLotl = tCache.getLotl();
//        lolFile = new File(model.getTempDataLocation() + "lotl.xml");
        recacheFile = new File(model.getDataLocation() + "cfg/recacheTime");
        dbUtil = model.getTslCertDb();
        tslLoader = new TslLoader();
    }

    /**
     * A call to this method checks if the server deamon has updated the cached
     * TSL data and issuance of policy certificate. This data is reloaded into
     * the admin service if the server has been updated.
     */
    public void reloadTslData() {
        if (running(tslRecacheThread)) {
            return;
//            try {
//                tslRecacheThread.join();
//            } catch (InterruptedException ex) {
//                LOG.log(Level.WARNING, null, ex);
//            }
        }
        tslRecacheThread = new Thread(tslLoader);
        tslRecacheThread.setDaemon(true);
        tslRecacheThread.start();
    }

    private boolean running(Thread thread) {
        return (thread != null && thread.isAlive());
    }

    private TrustServiceList openTsl(File tslFile) {
        TrustServiceList trustServiceList;
        try {
            trustServiceList = tslFact.getTsl(tslFile);
        } catch (IOException e) {
            trustServiceList = null;
        }
        return trustServiceList;
    }

    /**
     * Generates the TSP Service table for the TSP records menu
     *
     * @param session Session data
     * @param winHeight client browser window height in pixels
     * @return table html data
     */
    public String getDBTable(SessionModel session, int winHeight) {
        StringBuilder b = new StringBuilder();
        List<Integer> selectedCols = session.getSelectedCols();

        HtmlElement optionsPanel = new DivElement("dbOptions");
        HtmlElement checkBoxes = new DivElement("dbCheck");
        HtmlElement filters = new DivElement("dbFilters");

        //Add optionpanel divs
        optionsPanel.addHtmlElement(checkBoxes);
        optionsPanel.addHtmlElement(filters);

        // DIsplay names
        String[] columnName = new String[]{"Country", "TSP Name", "Trust Service", "Service Type",
            "Status", "Signature", "TSL Date", "TSL Expiry Date", "TSL Seq#", "Cert Type",
            "Cert expiry"};
        // Column class names (for hide or show)
        String[] columnID = new String[]{"Country", "TspName", "TrustService", "ServiceType",
            "Status", "Signature", "TslDate", "TslExpDate", "TslSeqNo", "CertType",
            "CertExp"};
        // Sord db query names for each column element
        String[] dbQueryId = new String[]{"territory", "tsp_name", "ts_name", "trust_service_type",
            "service_status", "sign_status", "tsl_date", "tsl_exp_date", "tsl_seq_no", "sdi_type",
            "tsl_cert_exp"};

        //Get the class names of all checked columns
        List<String> dbChecked = new ArrayList<String>();
        for (int i : selectedCols) {
            dbChecked.add(columnID[i]);
        }

        SelectElement sortSelect = new SelectElement("dbSort");
        boolean[] disp = new boolean[columnName.length];
        for (int i = 0; i < columnName.length; i++) {
            disp[i] = dbChecked.contains(columnID[i]);
            //get checkboxes for display options
            checkBoxes.addHtmlElement(new CheckboxElement(
                    "db" + columnID[i], //element id
                    "dbCheck", //element class
                    ONCLICK, //element action
                    LOAD_DATA_FUNCTION, //javascript function
                    new String[]{ //function args
                        MAIN_DATA_AREA, //load element for response html
                        "dbCheck", //Ajax request id
                        String.valueOf(i) //Ajax req param (index of checked element)
                    },
                    disp[i])); //whether the checkbox is to be shown checked or unchecked
            checkBoxes.addHtmlElement(new TextObject(columnName[i]));

            //popolate selectbox options
            if (dbChecked.contains(columnID[i])) {
                boolean selected = (i == session.getSortColumn());
                sortSelect.addOption(
                        columnName[i], //Option display text 
                        selected); //whether this option is to be preselected
            }
        }
        //Add JavaScript function on selectbox change
        filters.addHtmlElement(new TextObject("Sort by "));
        filters.addHtmlElement(sortSelect);
        sortSelect.addAction(ONCHANGE, EXECUTE_OPTION_FUNCTION, new String[]{
            MAIN_DATA_AREA, //Load element for ajax response
            "dbSortButton", //Ajax request id
            "dbSort" //Element holding the selected sort column
        });

        //Add Filters
        filters.addHtmlElement(new TextObject("&nbsp;&nbsp;Filters "));
        DbTableFilter countryFilter = new DbTableFilter("filterCountry", "-All States-", session.getCountryFilter());
        filters.addHtmlElement(countryFilter);
        DbTableFilter typeFilter = new DbTableFilter("filterType", "-All Types-", session.getTypeFilter());
        filters.addHtmlElement(typeFilter);
        DbTableFilter statusFilter = new DbTableFilter("filterStatus", "-All Status-", session.getStatusFilter());
        filters.addHtmlElement(statusFilter);
        DbTableFilter sigFilter = new DbTableFilter("filterSignature", "-All Signature-", session.getSigFilter());
        filters.addHtmlElement(sigFilter);
        List<TslCertificates> tcList = dbUtil.getAllTslCertificate(false);
        for (TslCertificates tc : tcList) {
            countryFilter.addFilterOption(tc.getTerritory());
            typeFilter.addUrlFilterOption(tc.getTrustServiceType());
            statusFilter.addUrlFilterOption(tc.getServiceStatus());
            sigFilter.addFilterOption(tc.getSignStatus());
        }

        //Add options panel to output
        HtmlElement dbTableDiv = new DivElement("dbTableDiv");
        //Set div height
        int tblHeight = winHeight - 145;
        tblHeight = (tblHeight < 500) ? 500 : tblHeight;
        dbTableDiv.addStyle("max-height", tblHeight + "px");
        TableElement dbTable = new TableElement();
        dbTableDiv.addHtmlElement(dbTable);

        // Set classes array
        dbTable.addRow(reduceArray(columnName, disp), TS_TABLE_CLASS[0]);

        // Get sorted DB records
        tcList = dbUtil.getAllTslCertificate(dbQueryId[session.getSortColumn()]);

        int row = 0;
        for (TslCertificates tc : tcList) {
            String[] rowValues = new String[]{
                tc.getTerritory(),
                tc.getTspName(),
                tc.getTsName(),
                ExtractorUtil.stripRefUrl(tc.getTrustServiceType()),
                ExtractorUtil.stripRefUrl(tc.getServiceStatus()),
                tc.getSignStatus(),
                DATE_FORMAT.format(new Date(tc.getTslDate())),
                DATE_FORMAT.format(new Date(tc.getTslExpDate())),
                tc.getTslSeqNo(),
                tc.getSdiTypeString(),
                DATE_FORMAT.format(new Date(tc.getCertExpiry()))
            };

            for (int i = 0; i < rowValues.length; i++) {
                if (rowValues[i] == null) {
                    rowValues[i] = "";
                }
            }

            if (countryFilter.isFilterMatch(tc.getTerritory())
                    && statusFilter.isUrlFilterMatch(tc.getServiceStatus())
                    && typeFilter.isUrlFilterMatch(tc.getTrustServiceType())
                    && sigFilter.isFilterMatch(tc.getSignStatus())) {
                if (row++ % 2 == 0) {
                    dbTable.addRow(reduceArray(rowValues, disp), TS_TABLE_CLASS[1]);
                } else {
                    dbTable.addRow(reduceArray(rowValues, disp), TS_TABLE_CLASS[2]);
                }
            }
        }

        HtmlElement rowCount = new DivElement();
        rowCount.addStyle("float", "right");
        rowCount.addHtmlElement(new TextObject("#Records=<b>" + String.valueOf(row) + "</b>"));
        checkBoxes.addHtmlElement(rowCount);

        b.append(optionsPanel.toString());
        b.append(dbTableDiv.toString());
        return b.toString();

    }

    /**
     * Generates the TSL selection table for the TSL viewer menu
     *
     * @param session session model data
     * @return table html data
     */
    public HtmlElement getTslTable(SessionModel session) {
        String[] tableClass = new String[]{TABLE_ODD_CLASS, TABLE_EVEN_CLASS};
        TableElement tslTable = new TableElement("data");

        if (tslList == null) {
            return (new TextObject("No data available"));
        }
        // Make TSL Overview selector
        String rowClass = session.getSelectedTsl() == 999 ? "dataSelected" : TABLE_EVEN_CLASS;
        tslTable.addRow(new String[]{"<b>----</b>", "<strong>TSL OVERVIEW</strong>"}, rowClass);
        TableRowElement overviewRow = tslTable.getLastTableRow();
        overviewRow.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{
            MAIN_DATA_AREA,
            TSL_TABLE_DIV,
            "999"
        });

        for (int i = 1; i < tslList.size() + 1; i++) {
            TrustServiceList tsl;
            if (i == 0) {
                tsl = currentLotl;
                if (tsl == null) {
                    return new TextObject("No List of TSLs available");
                }
            } else {
                tsl = tslList.get(i - 1).getTsl();
            }
            HtmlElement tr = new TableRowElement();
            HtmlElement td1 = new TableCellElement("<b>" + tsl.getSchemeTerritory() + "</b>");
            HtmlElement td2 = new TableCellElement(tsl.getSchemeOperatorName(Locale.ENGLISH).trim());
            tr.addHtmlElement(td1);
            tr.addHtmlElement(td2);
            tslTable.addHtmlElement(tr);

            if (i == session.getSelectedTsl()) {
                tr.addAttribute("class", "dataSelected");
            } else {
                tr.addAttribute("class", tableClass[i % 2]);
            }
            tr.addAction(ONCLICK, LOAD_DATA_FUNCTION, new String[]{MAIN_DATA_AREA, TSL_TABLE_DIV, String.valueOf(i)});
        }
        return tslTable;
    }

    private String[] reduceArray(String[] columnName, boolean[] disp) {
        ArrayList<String> reducedList = new ArrayList<String>();
        for (int i = 0; i < columnName.length; i++) {
            boolean display = true;
            try {
                if (!disp[i]) {
                    display = false;
                }
            } catch (Exception ex) {
            }
            if (display) {
                reducedList.add(columnName[i]);
            }
        }
        String[] reduced = new String[reducedList.size()];
        reducedList.toArray(reduced);
        return reduced;
    }

    /**
     * Generates the TSL content table for the TSL view menu
     *
     * @param session session model data
     * @return table html data
     */
    public HtmlElement getInfoTable(SessionModel session) {
        int selectedTsl = session.getSelectedTsl();
        if (selectedTsl == -1) {
            return getEmptyInfoTable();
        }
        if (selectedTsl == 999) {
            return getOverviewTable();
        }
        return getTSLInfo(selectedTsl, session);
    }

    private TableElement getEmptyInfoTable() {
        TableElement infotable = new TableElement("tslInfoTable");
        infotable.addRow("<b>Select TSL<b>", 3);
        return infotable;
    }

    public TableElement getTSLInfo(int selectTsl, SessionModel session) {
        TrustServiceList tsl;
        String tslSig = "";

        try {
            if (selectTsl == 0) {
                tsl = currentLotl;
                tslSig = model.isValidLotl() ? SIGNSTATUS_VERIFIED : SIGNSTATUS_INVALID;
            } else {
                tsl = tslList.get(selectTsl - 1).getTsl();
                tslSig = tslList.get(selectTsl - 1).getSignStatus();
            }
        } catch (Exception ex) {
            tsl = null;
        }
        String tableName = "tslTable";
        try {
            tableName = "tslTable" + tsl.getSchemeTerritory().trim();
        } catch (Exception ex) {
        }

        return getTSLInfo(tsl, tslSig, session, tableName);

    }

    public TableElement getTSLInfo(TrustServiceList tsl, String tslSig, SessionModel session, String tableName) {
        String[] attrClass = new String[]{ATTRIBUTE_NAME, ATTRIBUTE_VALUE};

        StringBuilder b = new StringBuilder();
        InfoTableModel tm = new InfoTableModel(tableName);
        tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
        InfoTableUtils itUtil = new InfoTableUtils(tm, session);
        itUtil.setAttrClass(attrClass);

        if (tsl == null) {
            tm.setTableHeading("No Trusted List information available");
            InfoTableFactory fact = new InfoTableFactory(tm, session);
            return fact.getTable();
        }

        InfoTableElements infoElements = tm.getElements();

        //TSL Info section
        InfoTableSection tslSection = infoElements.addNewSection(tm, true);
        tslSection.setFoldedElement("TSL Information", tm.getSectionHeadingClass()[0]);
        tslSection.setKeepFoldableElement(true);
        InfoTableElements tslElm = tslSection.getElements();

        // Basic TSL nodes
        tslElm.addNewSection(tm, "TSL Signature").addNewElement(HtmlUtil.getSignStatusMessage(tslSig, 17));
        tslElm.addNewSection(tm, "TSL Issuer").addNewElement(tsl.getSchemeOperatorName());
        tslElm.addNewSection(tm, "Territory").addNewElement(tsl.getSchemeTerritory());
        tslElm.addNewSection(tm, "Issue Date").addNewElement(tsl.getIssueDate());
        tslElm.addNewSection(tm, "Expiry Date").addNewElement(tsl.getNextUpdate());
        tslElm.addNewSection(tm, "Sequence number").addNewElement(tsl.getSequenceNumber());
        itUtil.addInformationUri(tslElm, tsl.getSchemeInformationUris());
        itUtil.addElectronicAddress(tslElm, tsl.getSchemeOperatorElectronicAddresses(), false);
        itUtil.addPostalAddress(tslElm, tsl.getSchemeOperatorPostalAddress(Locale.ENGLISH));

        // Scheme information node
        InfoTableSection schInfo = tslElm.addNewSection(tm, "Scheme Information");
        schInfo.setFoldedElement(tsl.getType());
        InfoTableElements schElm = schInfo.getElements();
        schElm.addNewSection(tm, "TSL Type").addNewElement(tsl.getType());
        schElm.addNewSection(tm, "Status Determination").addNewElement(tsl.getStatusDeterminationApproach());
        InfoTableSection stcr = schElm.addNewSection(tm, "Community Rules", true);
        b = new StringBuilder();
        for (String comRules : tsl.getSchemeTypes()) {
            stcr.addNewElement(comRules);
            b.append(ExtractorUtil.stripRefUrl(comRules)).append(", ");
        }
        b.deleteCharAt(b.lastIndexOf(","));
        stcr.setFoldedElement(b.toString());

        if (tsl.getSchemeName() != null) {
            InfoTableSection schName = schElm.addNewSection(tm, "Scheme Name", true);
            if (tsl.getSchemeName().length() > 40) {
                schName.setFoldedElement(tsl.getSchemeName().substring(0, 40) + "...");
            }
            schName.addNewElement(tsl.getSchemeName());
        }

        if (tsl.getLegalNotice() != null) {
            InfoTableSection legNotice = schElm.addNewSection(tm, "Legal Notice", true);
            if (tsl.getLegalNotice().length() > 40) {
                legNotice.setFoldedElement(tsl.getLegalNotice().substring(0, 40) + "...");
            }
            legNotice.addNewElement(tsl.getLegalNotice());
        }
        //If this is the LotL, then add Other Pointers information, else add TSP informaiton if present
        List<OtherTSLPointerData> otherTSLPointers = tsl.getOtherTSLPointers();
        if (!otherTSLPointers.isEmpty()) {
            addOtherTslPointerInformation(infoElements, tsl, tm, itUtil);
        }
        List<TrustServiceProvider> tsp = tsl.getTrustServiceProviders();
        if (!(tsp == null || tsp.isEmpty())) {
            addTSPInformation(infoElements, tsl, tm, itUtil);
        }

        // Set table row classes
        tm.setTableRowClasses(TABLE_SECTION_ROW_EVEN);
        //get table
        InfoTableFactory fact = new InfoTableFactory(tm, session);
        TableElement tslTable = fact.getTable();
        return tslTable;
    }

    /**
     * TSP Information
     */
    private void addTSPInformation(InfoTableElements infoElements,
            TrustServiceList tsl, InfoTableModel tm, InfoTableUtils itUtil) {
        // Foldable Heading
        InfoTableSection tspSect = infoElements.addNewSection(tm, true);
        tspSect.setFoldedElement("Trust Service Providers", tm.getSectionHeadingClass()[0]);
        tspSect.setKeepFoldableElement(true);

        // Construct list of TSPs
        InfoTableElements tspElm = tspSect.getElements();
        List<TrustServiceProvider> tspList = tsl.getTrustServiceProviders();
        for (TrustServiceProvider tsp : tspList) {
            InfoTableSection tspDataSect = tspElm.addNewSection(tm);
            tspDataSect.setFoldedElement(itUtil.getTSPFoldHeading(tsp));
            tspDataSect.setKeepFirstFoldableCell(true);
            tspDataSect.setKeepFoldableElement(true);

            // TSP Information node
            InfoTableElements tspDataElm = tspDataSect.getElements();
            InfoTableSection tspInfoSect = tspDataElm.addNewSection(tm, "TSP Information", true);
            tspInfoSect.setFoldedElement(itUtil.getTSPInfoHeading(tsp));
            tspInfoSect.setKeepFirstFoldableCell(true);
            InfoTableElements tspInfoElm = tspInfoSect.getElements();

            //Generate TSP information table content
            if (tsp.getTradeName() != null && tsp.getTradeName().length() > 0) {
                tspInfoElm.addNewSection(tm, "Trade Name").addNewElement(tsp.getTradeName());
            }
            itUtil.addInformationUri(tspInfoElm, tsp.getInformationUris());
            itUtil.addElectronicAddress(tspInfoElm, tsp.getElectronicAddress(), false);
            itUtil.addPostalAddress(tspInfoElm, tsp.getPostalAddress());

            /**
             * Trust service information
             */
            InfoTableSection tsSect = tspDataElm.addNewSection(tm, "Trust Services");
            InfoTableElements tsElm = tsSect.getElements();
            List<TrustService> tsList = tsp.getTrustServices();
            for (TrustService ts : tsList) {
                InfoTableSection tsDataSect = tsElm.addNewSection(tm, false);
                try {
                    Date expDate = CertificateUtils.getCertificate(ts.getServiceDigitalIdentityData()).getNotAfter();
                    String certExpiry = DATE_FORMAT.format(expDate);
                    tsDataSect.setFoldedElement(new InfoTableElement(new String[]{
                        ts.getName(), certExpiry, ExtractorUtil.stripRefUrl(ts.getType()), ExtractorUtil.stripRefUrl(ts.getStatus())}, new String[]{
                        "", ATTRIBUTE_NAME, PROPERTY_NAME, PROPERTY_NAME}));
                    tsDataSect.setKeepFirstFoldableCell(true);
                } catch (Exception ex) {
                    tsDataSect.setFoldedElement(ts.getName());
                }
                tsDataSect.setKeepFoldableElement(true);
                tsDataSect.setSectionHeadingClasses(new String[]{TABLE_SECTION_HEAD, TABLE_SECTION_HEAD});

                InfoTableElements tsDataElm = tsDataSect.getElements();
                
                TrustServiceInformation.addTrustServiceInformation(tsDataElm, ts, tm, itUtil);

//                tsDataElm.addNewSection(tm, "Service Type").addNewElement(ExtractorUtil.stripRefUrl(ts.getType()));
//                tsDataElm.addNewSection(tm, "Service Status").addNewElement(ExtractorUtil.stripRefUrl(ts.getStatus()));
//                tsDataElm.addNewSection(tm, "Status valid from").addNewElement(ts.getStatusStartingTime());
//
//                try {
//                    itUtil.addCertificate(tsDataElm, ts.getServiceDigitalIdentityData(), "Service Certificate", true);
//                } catch (Exception ex) {
//                    tsDataElm.addNewSection(tm, "Service Certificate").addNewElement(
//                            new String[]{"Invalid Certificate"}, new String[]{ERROR});
//                }
            }
        }
    }

    /**
     * Add Lotl Other TSL Pointer information.
     */
    private void addOtherTslPointerInformation(InfoTableElements infoElements,
            TrustServiceList tsl, InfoTableModel tm, InfoTableUtils itUtil) {

        otpCertMap = new HashMap<String, AaaCertificate>();
        InfoTableSection otpSect = infoElements.addNewSection(tm, true);
        otpSect.setFoldedElement("Other TSL Pointers", tm.getSectionHeadingClass()[0]);
        otpSect.setKeepFoldableElement(true);
        InfoTableElements otpElms = otpSect.getElements();

        List<OtherTSLPointerData> otpList = tsl.getOtherTSLPointers();
        for (OtherTSLPointerData otp : otpList) {
            //Get certificate
            List<AaaCertificate> certs = otp.getOtpCertificates();
            for (AaaCertificate cert : certs) {
                String certHash = FnvHash.getFNV1aToHex(cert.getEncoded());
                otpCertMap.put(certHash, cert);
            }

            // Get URL;
            String tSLLocation = otp.getTSLLocation();
            // Get Additional info
            List<Object[]> addInfoList = new ArrayList<Object[]>();
            otp.getTslType();
            addOtherInfoObjects(addInfoList, "TSL Type", otp.getTslType());
            addOtherInfoObjects(addInfoList, "Scheme Operator Name", otp.getSchemeOperatorName());
            addOtherInfoObjects(addInfoList, "Scheme Territory", otp.getSchemeTerritory());
            addOtherInfoObjects(addInfoList, "Scheme Type Community Rules", otp.getSchemeTypeCommunityRules());
            if (otp.isMimeTypePresent()) {
                String contentType = otp.isMrTslPointer() ? "Machine Readable (XML)" : "Human Readable (PDF)";
                addOtherInfoObjects(addInfoList, "Content Type", contentType);
            }

            itUtil.addOtherTslPointerInfo(otpElms, tSLLocation, addInfoList, certs);

        }
    }

    private void addOtherInfoObjects(List<Object[]> addInfoList, String type, Object val) {
        if (val == null) {
            return;
        }
        if (val instanceof String) {
            if (((String) val).length() == 0) {
                return;
            }
        }
        addInfoList.add(new Object[]{type, val});
    }

    /**
     * Provides the XML data of a selected TSL to be displayed as raw XML.
     *
     * @param index the TSL selection index
     * @return a string representation of the raw XML data
     */
    public String getTslXMLData(int index) {
        TrustServiceList tsl;
        try {
            if (index == 0) {
                tsl = currentLotl;
            } else {
                tsl = tslList.get(index - 1).getTsl();
            }
        } catch (Exception ex) {
            return "TSL not available";
        }
        return getTslXMLData(tsl);

    }

    /**
     * Provides the XML data of a selected TSL to be displayed as raw XML.
     *
     * @param tsl the TSL to be returned in XML form
     * @return a string representation of the raw XML data
     */
    public String getTslXMLData(TrustServiceList tsl) {
        //Output raw XML data
        try {
            return new String(XmlBeansUtil.getStyledBytes(XmlObject.Factory.parse(new ByteArrayInputStream(tsl.getBytes()))), StandardCharsets.UTF_8);
        } catch (XmlException | IOException ex) {
            return "<Error>" + ex.getMessage() + "<Error>";
        }
    }

    /**
     * Provides base64 encoded certificate data for TSL certificates and
     * certificates within the other TSL pointers in the EU list.
     *
     * @param certHash the hex string representation of the 64 FNV1a hash of the
     * selected certificate
     * @return base64 string
     */
    public String getBase64Cert(String certHash) {
        List<TslCertificates> dbCertList = dbUtil.getAllTslCertificate(false);
        if (otpCertMap != null) {
            if (otpCertMap.containsKey(certHash)) {
                return new String(Base64Coder.encode(otpCertMap.get(certHash).getEncoded()));
            }
        }
        for (TslCertificates tc : dbCertList) {
            if (tc.getTslCertHash().equals(certHash)) {
                try {
                    byte[] cert = CertificateUtils.getCertificate(tc.getTslCertificate()).getEncoded();
                    return new String(Base64Coder.encode(cert));
                } catch (Exception ex) {
                }
            }
        }
        return "";
    }

    private HtmlElement getOverviewTable() {
        HtmlElement overviewDiv = new DivElement();
        TableElement ovTable = new TableElement();
        String[] heading = new String[]{
            "Territory",
            "CountryName",
            "SigStatus",
            "IssueDate",
            "ExpiryDate",
            "Seq#",
            "QcTspCount",
            "ServiceCount",
            "QcCACount",
            "TslSigCertExp",
            "TslVer",
            "Alerts"
        };
        ovTable.addRow(heading, TABLE_HEAD_CLASS);

        List<TslCertificates> allTc = dbUtil.getAllTslCertificate(false);
        Map<String, List<BigInteger>> qcTspMap = new HashMap<String, List<BigInteger>>();
        Map<String, List<BigInteger>> serviceMap = new HashMap<String, List<BigInteger>>();
        Map<String, List<BigInteger>> caQcMapMap = new HashMap<String, List<BigInteger>>();

        // Get statistics
        for (TslCertificates tc : allTc) {
            if (ExtractorUtil.stripRefUrl(tc.getTrustServiceType()).equals("CA/QC")) {
                put(key(tc.getTspName()), tc.getTerritory(), qcTspMap);
            }
            put(key(tc.getTsName() + tc.getTspName()), tc.getTerritory(), serviceMap);
            if (tc.getSdiType() > 3) {
                put(key(tc.getTsName() + tc.getTspName()), tc.getTerritory(), caQcMapMap);
            }
        }

        // Collect data
        int rowIndex = 0;
        if (tslList == null) {
            overviewDiv.addHtmlElement(new TextObject("Server data reload - please retry in a short moment"));
            return overviewDiv;
        }
        for (TslMetaData tm : tslList) {
            int[] error = new int[11];
            for (int i = 0; i < 11; i++) {
                error[i] = 1;
            }
            TrustServiceList tsl = tm.getTsl();
            String territory = tsl.getSchemeTerritory();
            String countryName = "";
            try {
                EuropeCountry country = EuropeCountry.valueOf(territory);
                countryName = country.getShortEnglishName();
            } catch (Exception ex) {
            }
            String sigStatus = tm.getSignStatus();
            String issueDate = getDateString(tsl.getIssueDate());
            String expiryDate = getDateString(tsl.getNextUpdate());
            String sequenceNumber = tsl.getSequenceNumber().toString();
            String qcTspCount = getCount(territory, qcTspMap);
            String serviceCount = getCount(territory, serviceMap);
            String qcCaCount = getCount(territory, caQcMapMap);
            String tslSigCertExpiry = "";
            String tslVersion = getTslVersion(tsl.getTslData());
            try {
                AaaCertificate usedTslSigCert = new AaaCertificate(tm.getUsedTslSigCert().getEncoded());
                Date certExpiry = usedTslSigCert.getNotAfter();
                tslSigCertExpiry = getDateString(certExpiry);
                if (certExpiry.before(new Date())) {
                    error[9] = 3;
                }
            } catch (Exception ex) {
            }

            //Validate data
            if (tsl.getNextUpdate() == null || tsl.getNextUpdate().before(new Date())) {
                error[4] = 3;
            }
            if (sigStatus.equals(SIGNSTATUS_VERIFIED)) {
                error[2] = 0;
            } else {
                if (tm.getSignStatus().equals(SIGNSTATUS_UNVERIFIABLE)) {
                    error[2] = 2;
                } else {
                    error[2] = 3;
                }
            }
            if (!qcCaCount.equals("0")) {
                error[8] = 3;
            }

            String issue = getIssues(tm.getCountry());

            String[] rowClasses = getRowClasses(error, rowIndex++);

            String[] cellData = new String[]{
                territory,
                countryName,
                sigStatus,
                issueDate,
                expiryDate,
                sequenceNumber,
                qcTspCount,
                serviceCount,
                qcCaCount,
                tslSigCertExpiry,
                tslVersion,
                issue
            };
            ovTable.addRow(cellData, rowClasses, new boolean[]{});

            ovTable.getLastTableRow().addAction(ONCLICK, FRAME_LOAD_FUNCTION, new String[]{
                "index.jsp",
                TSL_TABLE_DIV,
                String.valueOf(rowIndex)
            });
        }

        /**
         * "Territory", "SigStatus", "IssueDate", "ExpiryDate", "Seq#",
         * "QcTspCount", "ServiceCount", "QcCaCount", "TslSigCertExp"
         */
        TableElement termsTable = new TableElement();
        String[] termsClasses = new String[]{TABLE_NEUTRAL_CLASS, PROPERTY_NAME, PROPERTY_VALUE};
        termsTable.addRow(new String[]{
            "Territory",
            "Two letter ISO 3166 country code"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "SigStatus",
            "Status of the TSL signature ("
            + getQuotedSpan("verified", GOOD) + " = Signature is verified, "
            + getQuotedSpan("unverifiable", WARNING) + " = The TSL is signed but the signature certificate is not published on the EU TSL, "
            + getQuotedSpan("absent", ERROR) + " = The TSL is not signed, "
            + getQuotedSpan("syntax", ERROR) + " = The signature validation process could not parse this signature, "
            + getQuotedSpan("invalid", ERROR) + " = The signature certificate is invalid or the signature does not match the TSL content)"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "IssueDate",
            "TSL issue date"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "ExpiryDate",
            "TSL expiry date"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "Seq#",
            "Sequence number of the TSL"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "QcTspCount",
            "The number Trust Service providers having at least one Qualified Certificate services"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "ServiceCount",
            "The number of trust services"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "QcCaCount",
            "Number of trust services having a CA certificate (or root certificate) issued as a Qualified Certificate"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "TslSigCertExp",
            "Expirydate of the certificate used to validate the TSL sigature"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "TslVer",
            "TSL version"
        }, termsClasses, new boolean[]{});
        termsTable.addRow(new String[]{
            "Alerts",
            "Issue types are: "
            + getQuotedSpan("unknownSigCert", ERROR) + " (Signature cert is not on the EU list), "
            + getQuotedSpan("unsigned", ERROR) + " (TSL is unsigned), "
            + getQuotedSpan("sigSyntax", ERROR) + " (TSL Signature has structural errors), "
            + getQuotedSpan("invalidSignature", ERROR) + " (Invalid TSL signature), "
            + getQuotedSpan("unavailable", ERROR) + " (TSL can't be downloaded from the location specified in the EU list), "
            + getQuotedSpan("tslExpiry", WARNING) + " (The TSL has expired, or will expire soon), "
            + getQuotedSpan("tslCertExpiry", WARNING) + " (The tsl certificate has expired, or will expire soon)"
        }, termsClasses, new boolean[]{});

        overviewDiv.addHtmlElement(ovTable);
        overviewDiv.addHtmlElement(new TextObject("<br /><b>Terminology</b>"));
        overviewDiv.addHtmlElement(termsTable);

        return overviewDiv;
    }

    private String getDateString(Date date) {
        return date == null
          ? "NULL"
          : DATE_FORMAT.format(date);
    }

    private String getQuotedSpan(String text, String className) {
        HtmlElement span = new GenericHtmlElement("span");
        span.setText(text);
        span.addAttribute("class", className);
        return "\"" + span.toString() + "\"";
    }

    private String getUnQuotedSpan(String text, String className) {
        HtmlElement span = new GenericHtmlElement("span");
        span.setText(text);
        span.addAttribute("class", className);
        return span.toString();
    }

    private String getCount(String terr, Map<String, List<BigInteger>> map) {
        int cnt = 0;
        if (map.containsKey(terr)) {
            List<BigInteger> list = map.get(terr);
            cnt = list.size();
        }
        return String.valueOf(cnt);
    }

    private BigInteger key(String data) {
        return FnvHash.getFNV1a(data);
    }

    private boolean put(BigInteger key, String terr, Map<String, List<BigInteger>> map) {
        List<BigInteger> keyList = new LinkedList<BigInteger>();
        if (map.containsKey(terr)) {
            keyList = map.get(terr);
        }
        listAdd(key, keyList);
        map.put(terr, keyList);
        return true;
    }

    private boolean listAdd(BigInteger item, List<BigInteger> list) {
        if (list.contains(item)) {
            return false;
        }
        list.add(item);
        return true;
    }

    private String[] getRowClasses(int[] error, int rowIdx) {
        String[] cellClass = new String[]{GOOD, "", WARNING, ERROR};
        String rowClass = rowIdx % 2 == 0 ? TABLE_EVEN_CLASS : TABLE_ODD_CLASS;

        String[] rowClasses = new String[]{
            rowClass,
            cellClass[error[0]],
            cellClass[error[1]],
            cellClass[error[2]],
            cellClass[error[3]],
            cellClass[error[4]],
            cellClass[error[5]],
            cellClass[error[6]],
            cellClass[error[7]],
            cellClass[error[8]],
            cellClass[error[9]],};
        return rowClasses;
    }

    public List<String> getTSLCountryList() {
        List<String> tslCountryList = new ArrayList<String>();

        if (tslList == null || tslList.isEmpty()) {
            return null;
        }
        tslCountryList.add("EU Commission LotL");

        for (TslMetaData tslMd : tslList) {
            TrustServiceList tsl = tslMd.getTsl();
            String country = "Unknown Country";
            try {
                String territory = tsl.getSchemeTerritory();
                EuropeCountry euc = EuropeCountry.valueOf(territory);
                country = euc.getShortEnglishName();
            } catch (Exception ex) {
            }
            tslCountryList.add(country);
        }
        return tslCountryList;
    }

    public TrustServiceList getTslObject(int index) {
        TrustServiceList tsl;
        try {
            if (index == 0) {
                tsl = currentLotl;
            } else {
                tsl = tslList.get(index - 1).getTsl();
            }
        } catch (Exception ex) {
            return null;
        }
        return tsl;
    }

    private String getTslVersion(TrustStatusListType tslData) {
        String version = "undefined";

        try {
            version = tslData.getSchemeInformation().getTSLVersionIdentifier().toString();
        } catch (Exception ex) {
        }
        return version;
    }

    private String getIssues(EuropeCountry country) {
        String issueStr = "-";
        Map<EuropeCountry, Map<TSLIssueID, TslIssue>> tslIssueMap = TSLIssueStack.getTslIssueMap();
        if (!tslIssueMap.containsKey(country)) {
            return "-";
        }
        Map<TSLIssueID, TslIssue> issueMap = tslIssueMap.get(country);
        if (issueMap.isEmpty()) {
            return "-";
        }
        int issueCnt = 0;
        for (TSLIssueID issueId : issueMap.keySet()) {
            TslIssue issue = issueMap.get(issueId);
            switch (issueId) {
                case tslExpiry:
                case tslCertExpiry:
                    TSLIssueSubcode subcode = issue.getSubcode();
                    if (subcode.equals(TSLIssueSubcode.expired)) {
                        issueStr = getUnQuotedSpan(issueId.name() + "(expired)", ERROR) + ",";
                    } else {
                        issueStr = getUnQuotedSpan(issueId.name(), WARNING) + ",";
                    }
                    issueCnt++;
                    break;
                case illegalCountry:
                    continue;
                default:
                    issueStr = getUnQuotedSpan(issueId.name(), ERROR) + ",";
                    issueCnt++;
            }
        }
        if (issueCnt>0){
            issueStr = issueStr.substring(0,issueStr.length()-1);
        } else {
            return "-";
        }
        return issueStr;
    }

    class TslLoader implements Runnable {

        @Override
        public void run() {
            if (recacheFile.canRead()) {
                String recacheTime = FileOps.readTextFile(recacheFile).trim();
                if (!recacheTime.equals(lastrecache)) {
                    lastrecache = recacheTime;
                    recache();
                }
            }
        }

        private void recache() {
            TrustServiceList reCachedLotl = null;
            List<TslMetaData> reCachedTslList = null;

            tCache.recacheLotl();
            currentLotl = tCache.getLotl();
            tCache.lotlSignatureCheck();
            tCache.loadTslData();
            tslList = tCache.getCachedTslList();
        }
    }
}
