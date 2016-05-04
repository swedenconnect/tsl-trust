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
package se.tillvaxtverket.tsltrust.weblogic.db;

/**
 * SQLite database utility for access to certification authority databases
 */
import se.tillvaxtverket.tsltrust.weblogic.data.DbCALog;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCAParam;
import se.tillvaxtverket.tsltrust.weblogic.data.DbCert;
import iaik.x509.X509Certificate;
import java.io.File;
import java.util.logging.Logger;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CaSQLiteUtil {

    private final static Logger LOG = Logger.getLogger(CaSQLiteUtil.class.getName());
    static String userid = "iaik", password = "iaik";
    static String url;

    public static Connection getConnection() {
        Connection con = null;

        try {
            Class.forName("org.sqlite.JDBC");	//Class.forName("myDriver.ClassName"); ?
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
            return null;
        }

        try {
            con = DriverManager.getConnection(url,
                    userid, password);
        } catch (SQLException ex) {
            LOG.warning("SQLException: " + ex.getMessage());
            return null;
        }
        return con;
    }

    // Create CA Table
    public static void createCATable(String caDir) {
        url = "jdbc:sqlite://" + caDir + "/cadb";
        Connection con = getConnection();

        String createCertsTable;
        createCertsTable = "create table Certificates ("
                + "Serial BIGINT not NULL,"
                + "Certificate VARCHAR(65535),"
                + "Revoked INTEGER,"
                + "Revoke_Date BIGINT,"
                + "PRIMARY KEY ( Serial ) )";
        String createCaDataTable;
        createCaDataTable = "create table CA_Data ("
                + "Parameter VARCHAR(255) not NULL,"
                + "Int_value INTEGER,"
                + "Str_value VARCHAR(255),"
                + "PRIMARY KEY ( Parameter ) )";
        String createCaLogTable;
        createCaLogTable = "create table Log ("
                + "Code INTEGER not NULL,"
                + "Event VARCHAR(255),"
                + "Parameter BIGINT,"
                + "Log_Date BIGINT )";

        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(createCertsTable);
            stmt.executeUpdate(createCaDataTable);
            stmt.executeUpdate(createCaLogTable);
            stmt.close();
            con.close();

        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        LOG.info("DB table for CA at" + caDir + " created");
    }

    public static void addCertificate(X509Certificate cert, String caDir) {
        if (isCaDirInvalid(caDir)) {
            return;
        }
        DbCert dbCert = new DbCert(cert);
        addCertificate(dbCert, caDir);
    }

    public static void addCertificate(DbCert dbCert, String caDir) {
        if (isCaDirInvalid(caDir)) {
            return;
        }
        final DbCert dbo = dbCert;
        url = "jdbc:sqlite://" + caDir + "/cadb";

        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "insert into Certificates values (?, ?, ?, ?);");
                prep.setLong(1, dbo.getSerial());
                prep.setString(2, dbo.getPemCert());
                prep.setInt(3, dbo.getRevoked());
                prep.setLong(4, dbo.getRevDate());
                return prep;
            }
        };
        sqlAction.dbAction();
    }

    public static void replaceCertificate(DbCert dbCert, String caDir) {
        if (isCaDirInvalid(caDir)) {
            return;
        }

        final DbCert dbo = dbCert;
        url = "jdbc:sqlite://" + caDir + "/cadb";

        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "replace into Certificates values (?, ?, ?, ?);");
                prep.setLong(1, dbo.getSerial());
                prep.setString(2, dbo.getPemCert());
                prep.setInt(3, dbo.getRevoked());
                prep.setLong(4, dbo.getRevDate());
                return prep;
            }
        };
        sqlAction.dbAction();
    }

    public static List<DbCert> getCertificates(String caDir) {
        if (isCaDirInvalid(caDir)) {
            return new LinkedList<DbCert>();
        }
        url = "jdbc:sqlite://" + caDir + "/cadb";
        String query = "select * from Certificates order by Serial;";
        SqLiteQuery<List<DbCert>> sqlQuery = new SqLiteQuery<List<DbCert>>(url, userid, password) {

            @Override
            List<DbCert> parseResultSet(ResultSet rs) throws SQLException {
                List<DbCert> certList = new LinkedList<DbCert>();
                while (rs.next()) {
                    DbCert certData = new DbCert();
                    certData.setSerial(rs.getLong(1));
                    certData.setPemCert(rs.getString(2));
                    certData.setRevoked(rs.getInt(3));
                    certData.setRevDate(rs.getLong(4));
                    certList.add(certData);
                }
                return certList;
            }
        };
        return sqlQuery.dbQuery(query, new LinkedList<DbCert>());
    }

    public static DbCert getCertificates(String caDir, long serial) {
        if (isCaDirInvalid(caDir)) {
            return null;
        }
        url = "jdbc:sqlite://" + caDir + "/cadb";
        String query = "select * from Certificates where Serial = " + String.valueOf(serial) + ";";

        SqLiteQuery<List<DbCert>> sqlQuery = new SqLiteQuery<List<DbCert>>(url, userid, password) {

            @Override
            List<DbCert> parseResultSet(ResultSet rs) throws SQLException {
                List<DbCert> certList = new LinkedList<DbCert>();
                while (rs.next()) {
                    DbCert certData = new DbCert();
                    certData.setSerial(rs.getLong(1));
                    certData.setPemCert(rs.getString(2));
                    certData.setRevoked(rs.getInt(3));
                    certData.setRevDate(rs.getLong(4));
                    certList.add(certData);
                }
                return certList;
            }
        };
        List<DbCert> result = sqlQuery.dbQuery(query, new LinkedList<DbCert>());

        if (result.isEmpty()) {
            return null;
        }
        if (result.size() > 1) {
            System.out.println("Error: Serial number duplication");
        }
        return result.get(0);
    }

    public static List<DbCert> getCertificates(String caDir, boolean revoked) {
        if (isCaDirInvalid(caDir)) {
            return new LinkedList<DbCert>();
        }
        int rev = (revoked) ? 1 : 0;
        url = "jdbc:sqlite://" + caDir + "/cadb";
        String query = "select * from Certificates where Revoked = " + String.valueOf(rev) + " order by Serial;";

        SqLiteQuery<List<DbCert>> sqlQuery = new SqLiteQuery<List<DbCert>>(url, userid, password) {

            @Override
            List<DbCert> parseResultSet(ResultSet rs) throws SQLException {
                List<DbCert> certList = new LinkedList<DbCert>();
                while (rs.next()) {
                    DbCert certData = new DbCert();
                    certData.setSerial(rs.getLong(1));
                    certData.setPemCert(rs.getString(2));
                    certData.setRevoked(rs.getInt(3));
                    certData.setRevDate(rs.getLong(4));
                    certList.add(certData);
                }
                return certList;
            }
        };
        return sqlQuery.dbQuery(query, new LinkedList<DbCert>());
    }

    public static Map<String, DbCAParam> getAllParameters(String caDir) {
        url = "jdbc:sqlite://" + caDir + "/cadb";
        String query = "select * from CA_Data ;";

        SqLiteQuery<Map<String, DbCAParam>> sqlQuery = new SqLiteQuery<Map<String, DbCAParam>>(url, userid, password) {

            @Override
            Map<String, DbCAParam> parseResultSet(ResultSet rs) throws SQLException {
                Map<String, DbCAParam> paramMap = new HashMap<String, DbCAParam>();
                while (rs.next()) {
                    DbCAParam caData = new DbCAParam();
                    caData.setParamName(rs.getString(1));
                    caData.setIntValue(rs.getLong(2));
                    caData.setStrValue(rs.getString(3));
                    paramMap.put(caData.getParamName(), caData);
                }
                return paramMap;
            }
        };
        return sqlQuery.dbQuery(query, new HashMap<String, DbCAParam>());
    }

    public static DbCAParam getParameter(String caDir, String param) {
        Map<String, DbCAParam> cpMap = getAllParameters(caDir);
        if (cpMap.containsKey(param)) {
            DbCAParam cp = cpMap.get(param);
            return cp;
        }
        return null;
    }

    public static void storeParameter(DbCAParam dbParam, String caDir) {
        final DbCAParam dbo = dbParam;
        url = "jdbc:sqlite://" + caDir + "/cadb";

        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "replace into CA_Data values (?, ?, ?);");
                prep.setString(1, dbo.getParamName());
                prep.setLong(2, dbo.getIntValue());
                prep.setString(3, dbo.getStrValue());
                return prep;
            }
        };
        sqlAction.dbAction();

    }

    public static void addCertLog(DbCALog dbLog, String caDir) {
        final DbCALog dbo = dbLog;
        url = "jdbc:sqlite://" + caDir + "/cadb";

        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "insert into Log values (?, ?, ?, ?);");
                prep.setLong(1, dbo.getLogCode());
                prep.setString(2, dbo.getEventString());
                prep.setLong(3, dbo.getLogParameter());
                prep.setLong(4, dbo.getLogTime());
                return prep;
            }
        };
        sqlAction.dbAction();

    }

    public static List<DbCALog> getCertLogs(String caDir) {
        url = "jdbc:sqlite://" + caDir + "/cadb";
        String query = "select * from Log;";

        SqLiteQuery<List<DbCALog>> sqlQuery = new SqLiteQuery<List<DbCALog>>(url, userid, password) {

            @Override
            List<DbCALog> parseResultSet(ResultSet rs) throws SQLException {
                List<DbCALog> logList = new LinkedList<DbCALog>();
                while (rs.next()) {
                    DbCALog logData = new DbCALog();
                    logData.setLogCode(rs.getInt(1));
                    logData.setEventString(rs.getString(2));
                    logData.setLogParameter(rs.getLong(3));
                    logData.setLogTime(rs.getLong(4));
                    logList.add(logData);
                }
                return logList;
            }
        };
        return sqlQuery.dbQuery(query, new LinkedList<DbCALog>());
    }

    public static List<DbCALog> getCertLogs(String caDir, int eventType) {
        url = "jdbc:sqlite://" + caDir + "/cadb";
        String query = "select * from Log where Code =" + String.valueOf(eventType) + ";";

        SqLiteQuery<List<DbCALog>> sqlQuery = new SqLiteQuery<List<DbCALog>>(url, userid, password) {

            @Override
            List<DbCALog> parseResultSet(ResultSet rs) throws SQLException {
                List<DbCALog> logList = new LinkedList<DbCALog>();
                while (rs.next()) {
                    DbCALog logData = new DbCALog();
                    logData.setLogCode(rs.getInt(1));
                    logData.setEventString(rs.getString(2));
                    logData.setLogParameter(rs.getLong(3));
                    logData.setLogTime(rs.getLong(4));
                    logList.add(logData);
                }
                return logList;
            }
        };
        return sqlQuery.dbQuery(query, new LinkedList<DbCALog>());
    }

    private static boolean isCaDirInvalid(String caDir) {
        File dbFile = new File(caDir, "cadb");
        return !dbFile.canRead();
    }
}
