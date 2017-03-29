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
package se.tillvaxtverket.tsltrust.weblogic.models;

import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.weblogic.data.AdminUser;
import iaik.x509.X509Certificate;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.tsl.TrustServiceList;

/*
 * This class is used to store information about the current browser session
 * such as menu selections, status of selectboxes etc.
 */
public class SessionModel {

    // Generic
    private static final Logger LOG = Logger.getLogger(SessionModel.class.getName());
    private BigInteger sessionID;
    private long lastUsed = 0;
    private Map<String, Map> tableFolds = new HashMap<String, Map>();
    String pemCert = "";
    Map<String, String> pemCertMap = new HashMap<String, String>();
    Map<String, Boolean> valPolixyPending = new HashMap<String, Boolean>();
    // Main menu
    private int selectedMenu = 0;
    // TSL Viewer
    private int selectedTsl = 999, selectedTsp = -1, selectedTs = -1;
    String tslButtonState = "data";
    String tslSelectedPemCert;
    // DB Table
    private List<Integer> selectedCols = new ArrayList<Integer>();
    private int sortColumn = 0;
    private String countryFilter, typeFilter, statusFilter, sigFilter;
    private String dbTableData = "";
    // AuthMenu
    private int authSubMenu = 0;
    List<AdminUser> adminActionList = new ArrayList<AdminUser>();
    // LogsMenu
    private int logsSubMenu = 0;
    // PolicyMenu
    private int policySubMenu = 0;
    private int selectedValPolicy = 0, selectedTslPolicy = 0;
    private int requestedPolicyAction;
    private boolean PolicyConfRequired = false, policyConfMode = false;
    private String policyButtonState = "data";
    private String extCertEntry = "";
    String policySelectedPemCert;
    boolean addNewPolicy = false;
//    String tempPolicyName="";
    //Cert management menu
    private String certButtonState = "data";
    private int selectedCertPolicy = 0;
    String certSelectedPemCert;
    int checkerSubmenu = 0, checkerSelected = 0, confCheckerSubMenu = 0;
    TrustServiceList uploadedTsl = null;
    private String checkButtonState = "data";
    String checkSelectedPemCert;
    byte[] conformanceData = null;

    public SessionModel() {
        this(new BigInteger(32, new Random(System.currentTimeMillis())));

    }

    public SessionModel(BigInteger sessionID) {
        this.sessionID = sessionID;
        int[] sel = new int[]{0, 1, 2, 3, 4, 5, 6};
        for (int i : sel) {
            selectedCols.add(i);
        }
    }

    //TableFolds
    public void setTableFold(String table, String nodeId) {
        String id = nodeId;
        boolean unfolded = true;
        if (nodeId.endsWith("fold")) {
            id = nodeId.substring(0, nodeId.length() - 4);
            unfolded = false;
        }

        Map<String, Boolean> tableMap;
        if (tableFolds.containsKey(table)) {
            tableMap = tableFolds.get(table);
        } else {
            tableMap = new HashMap<String, Boolean>();
            tableFolds.put(table, tableMap);
        }
        tableMap.put(id, unfolded);
    }

    public void resetTableFold(String table) {
        if (tableFolds.containsKey(table)) {
            tableFolds.remove(table);
        }
    }

    public boolean isTableNodeUnfolded(String table, String nodeId, boolean defaultState) {
        Map<String, Boolean> tableMap;
        if (tableFolds.containsKey(table)) {
            tableMap = tableFolds.get(table);
            if (tableMap.containsKey(nodeId)) {
                return tableMap.get(nodeId);
            }
        }
        return defaultState;
    }

    public void addPemCert(X509Certificate cert) {
        try {
            String pem = new String(Base64Coder.encode(cert.getEncoded()));
            String certHash = FnvHash.getFNV1aToHex(cert.getEncoded());
            pemCertMap.put(certHash, pem);
        } catch (CertificateEncodingException ex) {
            LOG.warning(ex.getMessage());
        }
    }

    public String getPemCert(String certId) {
        String key = certId;
        if (certId.startsWith("cert")) {
            key = certId.substring(4);
        }
        if (pemCertMap.containsKey(key)) {
            return pemCertMap.get(key);
        }
        return null;
    }

    public void setPolicyPending(String policyName, boolean pending) {
        valPolixyPending.put(policyName, pending);
    }

    public boolean isPolicyPending(String policyName) {
        if (valPolixyPending.containsKey(policyName)) {
            return (boolean) valPolixyPending.get(policyName);
        } else {
            return false;
        }
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public BigInteger getSessionID() {
        return sessionID;
    }

    public void setSessionID(BigInteger sessionID) {
        this.sessionID = sessionID;
    }

    public int getSelectedMenu() {
        return selectedMenu;
    }

    public void setSelectedMenu(int selectedMenu) {
        this.selectedMenu = selectedMenu;
    }

    public int getSelectedTs() {
        return selectedTs;
    }

    public void setSelectedTs(int selectedTs) {
        this.selectedTs = selectedTs;
    }

    public int getSelectedTsl() {
        return selectedTsl;
    }

    public void setSelectedTsl(int selectedTsl) {
        this.selectedTsl = selectedTsl;
    }

    public int getSelectedTsp() {
        return selectedTsp;
    }

    public void setSelectedTsp(int selectedTsp) {
        this.selectedTsp = selectedTsp;
    }

    public List<Integer> getSelectedCols() {
        return selectedCols;
    }

    public void setSelectedCols(List<Integer> selectedCols) {
        this.selectedCols = selectedCols;
    }

    public int getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(int sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getCountryFilter() {
        return countryFilter;
    }

    public void setCountryFilter(String countryFilter) {
        this.countryFilter = countryFilter;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public String getTypeFilter() {
        return typeFilter;
    }

    public void setTypeFilter(String typeFilter) {
        this.typeFilter = typeFilter;
    }

    public String getSigFilter() {
        return sigFilter;
    }

    public void setSigFilter(String sigFilter) {
        this.sigFilter = sigFilter;
    }

    public String getTslButtonState() {
        return tslButtonState;
    }

    public void setTslButtonState(String tslButtonState) {
        this.tslButtonState = tslButtonState;
    }

    public String getDbTableData() {
        return dbTableData;
    }

    public void setDbTableData(String dbTableData) {
        this.dbTableData = dbTableData;
    }

    public int getAuthSubMenu() {
        return authSubMenu;
    }

    public void setAuthSubMenu(int authSubMenu) {
        this.authSubMenu = authSubMenu;
    }

    public List<AdminUser> getAdminActionList() {
        return adminActionList;
    }

    public void setAdminActionList(List<AdminUser> adminActionList) {
        this.adminActionList = adminActionList;
    }

    public int getLogsSubMenu() {
        return logsSubMenu;
    }

    public void setLogsSubMenu(int logsSubMenu) {
        this.logsSubMenu = logsSubMenu;
    }

    public String getPemCert() {
        return pemCert;
    }

    public void setPemCert(String pemCert) {
        this.pemCert = pemCert;
    }

    public Map<String, Map> getTableFolds() {
        return tableFolds;
    }

    public void setTableFolds(Map<String, Map> tableFolds) {
        this.tableFolds = tableFolds;
    }

    public int getPolicySubMenu() {
        return policySubMenu;
    }

    public void setPolicySubMenu(int policySubMenu) {
        this.policySubMenu = policySubMenu;
    }

    public int getSelectedTslPolicy() {
        return selectedTslPolicy;
    }

    public void setSelectedTslPolicy(int selectedTslPolicy) {
        this.selectedTslPolicy = selectedTslPolicy;
    }

    public int getSelectedValPolicy() {
        return selectedValPolicy;
    }

    public void setSelectedValPolicy(int selectedValPolicy) {
        this.selectedValPolicy = selectedValPolicy;
    }

    public boolean isPolicyConfRequired() {
        return PolicyConfRequired;
    }

    public void setPolicyConfRequired(boolean PolicyConfRequired) {
        this.PolicyConfRequired = PolicyConfRequired;
    }

    public int getRequestedPolicyAction() {
        return requestedPolicyAction;
    }

    public void setRequestedPolicyAction(int requestedPolicyAction) {
        this.requestedPolicyAction = requestedPolicyAction;
    }

    public String getPolicyButtonState() {
        return policyButtonState;
    }

    public void setPolicyButtonState(String policyButtonState) {
        this.policyButtonState = policyButtonState;
    }

    public String getExtCertEntry() {
        return extCertEntry;
    }

    public void setExtCertEntry(String extCertEntry) {
        this.extCertEntry = extCertEntry;
    }

    public String getCertButtonState() {
        return certButtonState;
    }

    public void setCertButtonState(String certButtonState) {
        this.certButtonState = certButtonState;
    }

    public int getSelectedCertPolicy() {
        return selectedCertPolicy;
    }

    public void setSelectedCertPolicy(int selectedCertPolicy) {
        this.selectedCertPolicy = selectedCertPolicy;
    }

    public String getCertSelectedPemCert() {
        return certSelectedPemCert;
    }

    public void setCertSelectedPemCert(String certSelectedPemCert) {
        this.certSelectedPemCert = certSelectedPemCert;
    }

    public String getPolicySelectedPemCert() {
        return policySelectedPemCert;
    }

    public void setPolicySelectedPemCert(String policySelectedPemCert) {
        this.policySelectedPemCert = policySelectedPemCert;
    }

    public String getTslSelectedPemCert() {
        return tslSelectedPemCert;
    }

    public void setTslSelectedPemCert(String tslSelectedPemCert) {
        this.tslSelectedPemCert = tslSelectedPemCert;
    }

    public boolean isAddNewPolicy() {
        return addNewPolicy;
    }

    public void setAddNewPolicy(boolean addNewPolicy) {
        this.addNewPolicy = addNewPolicy;
    }

    /**
     * Get Policy configuration mode
     * @return true if policy configuration mode is enabled
     */
    public boolean isPolicyConfMode() {
        return policyConfMode;
    }

    /**
     * Set policy configuration mode
     * @param policyConfMode true = policy configuration mode
     */
    public void setPolicyConfMode(boolean policyConfMode) {
        this.policyConfMode = policyConfMode;
    }

    public int getCheckerSubmenu() {
        return checkerSubmenu;
    }

    public void setCheckerSubmenu(int checkerSubmenu) {
        this.checkerSubmenu = checkerSubmenu;
    }

    public TrustServiceList getUploadedTsl() {
        return uploadedTsl;
    }

    public void setUploadedTsl(TrustServiceList uploadedTsl) {
        this.uploadedTsl = uploadedTsl;
    }

    public int getCheckerSelected() {
        return checkerSelected;
    }

    public void setCheckerSelected(int checkerSelected) {
        this.checkerSelected = checkerSelected;
    }

    public String getCheckButtonState() {
        return checkButtonState;
    }

    public void setCheckButtonState(String checkButtonState) {
        this.checkButtonState = checkButtonState;
    }

    public String getCheckSelectedPemCert() {
        return checkSelectedPemCert;
    }

    public void setCheckSelectedPemCert(String checkSelectedPemCert) {
        this.checkSelectedPemCert = checkSelectedPemCert;
    }

    public int getConfCheckerSubMenu() {
        return confCheckerSubMenu;
    }

    public void setConfCheckerSubMenu(int confCheckerSubMenu) {
        this.confCheckerSubMenu = confCheckerSubMenu;
    }

    public byte[] getConformanceData() {
        return conformanceData;
    }

    public void setConformanceData(byte[] conformanceData) {
        this.conformanceData = conformanceData;
    }
}
