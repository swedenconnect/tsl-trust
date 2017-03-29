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

import se.tillvaxtverket.tsltrust.common.config.ConfigData;

/**
 *
 * @author stefan
 */
public class TslTrustConfig implements ConfigData {

    /**
     * Default:
     * <param-name>DataLocation</param-name>
     * <param-value>/opt/webapp/tsltrust/admin/</param-value>
     */
    private String Mode,
            LotlURL,
            MaxConsoleLogSize,
            MaxMajorLogAge,
            TSLrecacheTime,
            CaCountry,
            CaOrganizationName,
            CaOrgUnitName,
            CaSerialNumber,
            CaCommonName,
            CaFileStorageLocation,
            CaDistributionURL,
            LogDbConnectionUrl,
            LogDbUserName,
            LogDbPassword,
            PolicyDbConnectionUrl,
            PolicyDbUserName,
            PolicyDbPassword,
            DbAutoCreateTables,
            DbVerboseLogging,
            SuperAdminID,
            SuperAdminAttribute,
            SuperAdminIdP,
            DiscoFeedUrl;

    @Override
    public void setDefaults() {
        Mode = "devmode"; //"production"
        LotlURL = "https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml";
        MaxConsoleLogSize = "1000";
        MaxMajorLogAge = "90";
        TSLrecacheTime = "1";
        CaCountry = "SE";
        CaOrganizationName = "Swedish eSENS Pilot Test";
        CaOrgUnitName = "Signature validation";
        CaSerialNumber = "SE555555-5555";
        CaCommonName = "Swedish eSENS Pilot - Test validation service";
        CaFileStorageLocation = "/Users/stefan/Sites/tsltrust"; // "/opt/webapp/tsltrust/admin/";
        CaDistributionURL = "http://localhost/~stefan/tsltrust"; // "https/tsltrust.esp-ttadmin.se/trustinfo" 
        LogDbConnectionUrl = "jdbc:mysql://localhost:3306/tsltrust_log"; // "jdbc:mysql://192.168.160.2:3306/tsltrust_log"
        LogDbUserName = "tsltrust";
        LogDbPassword = "tsltrust987";
        PolicyDbConnectionUrl = "jdbc:mysql://localhost:3306/tsltrust_policy"; // // "jdbc:mysql://192.168.160.2:3306/tsltrust_policy"
        PolicyDbUserName = "tsltrust";
        PolicyDbPassword = "tsltrust987";
        DbAutoCreateTables = "false";
        DbVerboseLogging = "false";
        SuperAdminID = "198504051569";
        SuperAdminAttribute = "personalIdentityNumber";
        SuperAdminIdP = "https://idp.esp-idp.se/idp";
        DiscoFeedUrl = "https://ds.esp-disco.se/disco/json?action=idplist";
    }

    @Override
    public String getName() {
        return "tslTrustConfig";
    }

    public String getLotlURL() {
        return LotlURL;
    }

    public void setLotlURL(String LotlURL) {
        this.LotlURL = LotlURL;
    }

    public String getMaxConsoleLogSize() {
        return MaxConsoleLogSize;
    }

    public void setMaxConsoleLogSize(String MaxConsoleLogSize) {
        this.MaxConsoleLogSize = MaxConsoleLogSize;
    }

    public String getMaxMajorLogAge() {
        return MaxMajorLogAge;
    }

    public void setMaxMajorLogAge(String MaxMajorLogAge) {
        this.MaxMajorLogAge = MaxMajorLogAge;
    }

    public String getTSLrecacheTime() {
        return TSLrecacheTime;
    }

    public void setTSLrecacheTime(String TSLrecacheTime) {
        this.TSLrecacheTime = TSLrecacheTime;
    }

    public String getCaCountry() {
        return CaCountry;
    }

    public void setCaCountry(String CaCountry) {
        this.CaCountry = CaCountry;
    }

    public String getCaOrganizationName() {
        return CaOrganizationName;
    }

    public void setCaOrganizationName(String CaOrganizationName) {
        this.CaOrganizationName = CaOrganizationName;
    }

    public String getCaOrgUnitName() {
        return CaOrgUnitName;
    }

    public void setCaOrgUnitName(String CaOrgUnitName) {
        this.CaOrgUnitName = CaOrgUnitName;
    }

    public String getCaSerialNumber() {
        return CaSerialNumber;
    }

    public void setCaSerialNumber(String CaSerialNumber) {
        this.CaSerialNumber = CaSerialNumber;
    }

    public String getCaCommonName() {
        return CaCommonName;
    }

    public void setCaCommonName(String CaCommonName) {
        this.CaCommonName = CaCommonName;
    }

    public String getCaFileStorageLocation() {
        return CaFileStorageLocation;
    }

    public void setCaFileStorageLocation(String CaFileStorageLocation) {
        this.CaFileStorageLocation = CaFileStorageLocation;
    }

    public String getCaDistributionURL() {
        return CaDistributionURL;
    }

    public void setCaDistributionURL(String CaDistributionURL) {
        this.CaDistributionURL = CaDistributionURL;
    }

    public String getLogDbConnectionUrl() {
        return LogDbConnectionUrl;
    }

    public void setLogDbConnectionUrl(String LogDbConnectionUrl) {
        this.LogDbConnectionUrl = LogDbConnectionUrl;
    }

    public String getLogDbUserName() {
        return LogDbUserName;
    }

    public void setLogDbUserName(String LogDbUserName) {
        this.LogDbUserName = LogDbUserName;
    }

    public String getLogDbPassword() {
        return LogDbPassword;
    }

    public void setLogDbPassword(String LogDbPassword) {
        this.LogDbPassword = LogDbPassword;
    }

    public String getPolicyDbConnectionUrl() {
        return PolicyDbConnectionUrl;
    }

    public void setPolicyDbConnectionUrl(String PolicyDbConnectionUrl) {
        this.PolicyDbConnectionUrl = PolicyDbConnectionUrl;
    }

    public String getPolicyDbUserName() {
        return PolicyDbUserName;
    }

    public void setPolicyDbUserName(String PolicyDbUserName) {
        this.PolicyDbUserName = PolicyDbUserName;
    }

    public String getPolicyDbPassword() {
        return PolicyDbPassword;
    }

    public void setPolicyDbPassword(String PolicyDbPassword) {
        this.PolicyDbPassword = PolicyDbPassword;
    }

    public String getDbAutoCreateTables() {
        return DbAutoCreateTables;
    }

    public void setDbAutoCreateTables(String DbAutoCreateTables) {
        this.DbAutoCreateTables = DbAutoCreateTables;
    }

    public String getDbVerboseLogging() {
        return DbVerboseLogging;
    }

    public void setDbVerboseLogging(String DbVerboseLogging) {
        this.DbVerboseLogging = DbVerboseLogging;
    }

    public String getMode() {
        return Mode;
    }

    public void setMode(String Mode) {
        this.Mode = Mode;
    }

    public String getSuperAdminID() {
        return SuperAdminID;
    }

    public void setSuperAdminID(String SuperAdminID) {
        this.SuperAdminID = SuperAdminID;
    }

    public String getSuperAdminAttribute() {
        return SuperAdminAttribute;
    }

    public void setSuperAdminAttribute(String SuperAdminAttribute) {
        this.SuperAdminAttribute = SuperAdminAttribute;
    }

    public String getSuperAdminIdP() {
        return SuperAdminIdP;
    }

    public void setSuperAdminIdP(String SuperAdminIdP) {
        this.SuperAdminIdP = SuperAdminIdP;
    }

    public String getDiscoFeedUrl() {
        return DiscoFeedUrl;
    }

    public void setDiscoFeedUrl(String DiscoFeedUrl) {
        this.DiscoFeedUrl = DiscoFeedUrl;
    }
}
