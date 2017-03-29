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
package se.tillvaxtverket.tsltrust.weblogic.hibernate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import noNamespace.HibernateConfigurationDocument;
import noNamespace.PropertyType;
import noNamespace.SessionFactoryDocument.SessionFactory;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;

/**
 * Class providing static methods for loading and generation of hibernate
 * configuration document. This call si used by the HibernateUtil classes
 * to get configuration properties for hibernate session factories
 */
public class HibernateConfigFactory {

    private static DbType logDbType = DbType.MySql;
    private static DbType policyDbType = DbType.MySql;
    public static final String DRIVER = "hibernate.connection.driver_class";
    public static final String CONNECTION = "hibernate.connection.url";
    public static final String DIALECT = "hibernate.dialect";
    public static final String USER_NAME = "hibernate.connection.username";
    public static final String PASSWORD = "hibernate.connection.password";
    public static final String CREATE_TABLE = "hibernate.hbm2ddl.auto";
    public static final String SHOW_SQL = "show_sql";
    public static final String FORMAT_SQL = "format_sql";
    private static String[] logMapping = new String[]{
        "ConsoleLog.hbm.xml",
        "AdminLog.hbm.xml",
        "MajorLog.hbm.xml"
    };
    private static String[] policyMapping = new String[]{
        "ValidationPolicy.hbm.xml",
        "TslPolicy.hbm.xml",
        "ExternalCert.hbm.xml",};
    private static String xmlHead =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE hibernate-configuration PUBLIC \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\" "
            + "\"http://hibernate.org/dtd/hibernate-configuration-3.0.dtd\">\n";
    private static XmlOptions xmlOptions;
    private static String logConnectionUrl;
    private static String logUserName = "";
    private static String logUserPassword = "";
    private static String policyConnectionUrl;
    private static String policyUserName = "";
    private static String policyUserPassword = "";
    private static String autoCreate = "false";
    private static boolean verboseLogging = false;
    private static File logHibConfFile = null, policyHibConfFile = null;

    static {
        xmlOptions = new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4);
    }

    /**
     * Sets auto-create table mode.
     * @param autoCreateOption "false" disables autocreate. "update" creates non-existing tables and "create" drops old tables and creates new
     */
    public static void setAutoCreate(String autoCreateOption) {
        autoCreate = "false";
        if (autoCreateOption.toLowerCase().equals("update")) {
            autoCreate = "update";
        }
        if (autoCreateOption.toLowerCase().equals("create")) {
            autoCreate = "create";
        }
    }

    /**
     * Set the db connectioin URL
     * @param logConUrl the connection url, starting with "jdbc:"
     */
    public static void setLogConnectionUrl(String logConUrl) {
        logConnectionUrl = logConUrl;
        logDbType = DbType.Null;
        if (logConUrl.startsWith("jdbc:sqlite")) {
            logDbType = DbType.SqLite;
        }
        if (logConUrl.startsWith("jdbc:oracle")) {
            logDbType = DbType.Oracle;
        }
        if (logConUrl.startsWith("jdbc:mysql")) {
            logDbType = DbType.MySql;
        }
        if (logDbType == DbType.Null) {
            logHibConfFile = new File(logConUrl);
            if (logHibConfFile.canRead()) {
                logDbType = DbType.External;
            }
        }
    }

    /**
     * Sets log database user name
     * @param logUsrName user name
     */
    public static void setLogUserName(String logUsrName) {
        logUserName = logUsrName;
    }

    /**
     * Sets log database password
     * @param logUsrPassword log database password
     */
    public static void setLogUserPassword(String logUsrPassword) {
        logUserPassword = logUsrPassword;
    }

    /**
     * Sets the policy database connection url
     * @param policyConUrl the connection url, starting with "jdbc:"
     */
    public static void setPolicyConnectionUrl(String policyConUrl) {
        policyConnectionUrl = policyConUrl;
        policyDbType = DbType.Null;
        if (policyConUrl.startsWith("jdbc:sqlite")) {
            policyDbType = DbType.SqLite;
        }
        if (policyConUrl.startsWith("jdbc:oracle")) {
            policyDbType = DbType.Oracle;
        }
        if (policyConUrl.startsWith("jdbc:mysql")) {
            policyDbType = DbType.MySql;
        }
        if (policyDbType == DbType.Null) {
            policyHibConfFile = new File(policyConUrl);
            if (policyHibConfFile.canRead()) {
                policyDbType = DbType.External;
            }
        }

    }

    /**
     * Sets policy database user name
     * @param policyUsrName user name
     */
    public static void setPolicyUserName(String policyUsrName) {
        policyUserName = policyUsrName;
    }

    /**
     * Sets policy database password
     * @param policyUsrPassword policy database password
     */
    public static void setPolicyUserPassword(String policyUsrPassword) {
        policyUserPassword = policyUsrPassword;
    }

    /**
     * Setting verbose logging option
     * @param verboseLog a value of "true" activates verbose logging
     */
    public static void setVerboseLogging(String verboseLog) {
        verboseLogging = (verboseLog != null && verboseLog.toLowerCase().equals("true"));
    }

    /**
     * Loads or generates hibernate configuration data for the log database
     * @return A Hibernate config document for the log database
     */
    public static Document getLogHibernateConfigDoc() {
        if (!isInitialized()) {
            return null;
        }
        if (logDbType == DbType.External){
            return getDoc(logHibConfFile);
        }
        HibernateConfigurationDocument hibernateConfDoc = HibernateConfigurationDocument.Factory.newInstance();
        SessionFactory hsFact = hibernateConfDoc.addNewHibernateConfiguration().addNewSessionFactory();

        addProperty(hsFact, DRIVER, logDbType.getDriver());
        addProperty(hsFact, CONNECTION, logConnectionUrl);
        addProperty(hsFact, DIALECT, logDbType.getDialect());

        switch (logDbType) {
            case MySql:
            case Oracle:
                addProperty(hsFact, USER_NAME, logUserName);
                addProperty(hsFact, PASSWORD, logUserPassword);
        }

        addOptions(hsFact);
        addMappings(hsFact, logMapping);

        String xmlString = xmlHead + hibernateConfDoc.xmlText(xmlOptions);
        return getDoc(xmlString);
    }

    /**
     * Loads or generates configuration data for the policy database
     * @return A Hibernate config document for the policy database
     */
    public static Document getPolicyHibernateConfigDoc() {
        if (!isInitialized()) {
            return null;
        }
        if (policyDbType == DbType.External){
            return getDoc(policyHibConfFile);
        }
        HibernateConfigurationDocument hibernateConfDoc = HibernateConfigurationDocument.Factory.newInstance();
        SessionFactory hsFact = hibernateConfDoc.addNewHibernateConfiguration().addNewSessionFactory();

        addProperty(hsFact, DRIVER, policyDbType.getDriver());
        addProperty(hsFact, CONNECTION, policyConnectionUrl);
        addProperty(hsFact, DIALECT, policyDbType.getDialect());

        switch (policyDbType) {
            case MySql:
            case Oracle:
                addProperty(hsFact, USER_NAME, policyUserName);
                addProperty(hsFact, PASSWORD, policyUserPassword);
        }

        addOptions(hsFact);
        addMappings(hsFact, policyMapping);

        String xmlString = xmlHead + hibernateConfDoc.xmlText(xmlOptions);
        return getDoc(xmlString);
    }

    private static boolean isInitialized() {
        return policyConnectionUrl != null && logConnectionUrl != null;
    }

    private static void addOptions(SessionFactory hsFact) {
        if (!autoCreate.equals("false")) {
            addProperty(hsFact, CREATE_TABLE, autoCreate);
        }
        if (verboseLogging) {
            addProperty(hsFact, SHOW_SQL, "true");
            addProperty(hsFact, FORMAT_SQL, "true");
        }
    }

    private static void addProperty(SessionFactory hsFact, String name, String value) {
        PropertyType prop = hsFact.addNewProperty();
        prop.addNewName().setStringValue(name);
        prop.setStringValue(value);
    }

    private static void addMappings(SessionFactory hsFact, String[] mappings) {
        for (String mp : mappings) {
            hsFact.addNewMapping().addNewResource().setStringValue(mp);
        }
    }

    private static Document getDoc(File xmlFile) {
        try {
            return getDoc(FileOps.readBinaryFile(xmlFile));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(HibernateConfigFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(HibernateConfigFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HibernateConfigFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static Document getDoc(String xml) {
        try {
            return getDoc(xml.getBytes("UTF-8"));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(HibernateConfigFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(HibernateConfigFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HibernateConfigFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static Document getDoc(byte[] xmlData) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(xmlData);
        Document doc = builder.parse(is);
        return doc;
    }

    /**
     * Enumeration of databases that are supported with programmatically generated
     * hibernate configuration data. The parameters of each enumeration are the
     * database driver class and the dialect class for each supported database.
     * Databases not listed (or not compatible with the settings in this enumeration
     * must be supported by en external hibernate.cfg.xml file to work.
     */
    public enum DbType {

        MySql("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect"),
        SqLite("org.sqlite.JDBC", "se.tillvaxtverket.tsltrust.weblogic.hibernate.SQLiteDialect"),
        Oracle("oracle.jdbc.driver.OracleDriver", "org.hibernate.dialect.Oracle10gDialect"),
        External("", ""),
        Null("", "");
        private String driver;
        private String dialect;

        private DbType(String driver, String dialect) {
            this.driver = driver;
            this.dialect = dialect;
        }

        public String getDialect() {
            return dialect;
        }

        public String getDriver() {
            return driver;
        }
    }
}
