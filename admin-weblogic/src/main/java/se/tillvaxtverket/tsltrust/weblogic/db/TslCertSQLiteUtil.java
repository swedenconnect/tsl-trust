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
package se.tillvaxtverket.tsltrust.weblogic.db;

/**
 * Utility class for access to TSL trust service database records
 */
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import java.io.File;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class TslCertSQLiteUtil {

    private static final Logger LOG = Logger.getLogger(TslCertSQLiteUtil.class.getName());
    private String userid = "iaik", password = "iaik";
    private String url;
    // String url = "jdbc:mySubprotocol:myDataSource"; ?
    private String[] tslCertCols = new String[]{"id", "tsp_name", "ts_name", "territory",
        "trust_service_type", "service_status", "tsl_date", "tsl_exp_date", "tsl_cert_hash", "tsl_certificate",
        "sdi_type", "tsl_cert_exp", "tsl_seq_no", "tsl_sha1", "extractor_status", "sign_status"};
    private String certCols;

    public TslCertSQLiteUtil(String ttDataDir) {
        url = "jdbc:sqlite://" + ttDataDir + "db/tslCertDb";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < tslCertCols.length; i++) {
            b.append(tslCertCols[i]).append(",");
        }
        b.deleteCharAt(b.lastIndexOf(","));
        certCols = b.toString();

        File dbFile = new File(ttDataDir + "db/tslCertDb");
        if (!dbFile.canRead()) {
            if (dbFile.getParentFile() != null) {
                dbFile.getParentFile().mkdirs();
            }
            createCATable();
        }
    }

    private Connection getConnection() {

        Connection con;
        try {
            Class.forName("org.sqlite.JDBC");	//Class.forName("myDriver.ClassName"); ?
        } catch (java.lang.ClassNotFoundException ex) {
            LOG.warning("ClassNotFoundException: ");
            LOG.warning(ex.getMessage());
            return null;
        }

        try {
            con = DriverManager.getConnection(url,
                    userid, password);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        return con;
    }

    // Create CA Table
    public final void createCATable() {
        Connection con = getConnection();
        Statement stmt;

        String createCertsTable;
        createCertsTable = "create table Certificates ("
                + "id INTEGER,"
                + "tsp_name VARCHAR(65535),"
                + "ts_name VARCHAR(65535),"
                + "territory VARCHAR(255),"
                + "trust_service_type VARCHAR(255),"
                + "service_status VARCHAR(255),"
                + "tsl_date BIGINT,"
                + "tsl_exp_date BIGINT,"
                + "tsl_cert_hash VARCHAR(255),"
                + "tsl_certificate VARCHAR(65535),"
                + "sdi_type INTEGER,"
                + "tsl_cert_exp BIGINT,"
                + "tsl_seq_no VARCHAR(255),"
                + "tsl_sha1 VARCHAR(255),"
                + "extractor_status VARCHAR(255),"
                + "sign_status VARCHAR(255),"
                + "PRIMARY KEY ( id ) )";

        try {
            stmt = con.createStatement();
            stmt.executeUpdate(createCertsTable);
            stmt.close();
            con.close();

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.info("DB: " + url + " created");
    }

    public void addCertificate(TslCertificates tc) {

        final TslCertificates ftc = tc;
        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "insert into Certificates values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
                prep.setInt(1, getIdFromRec(ftc));
                prep.setString(2, ftc.getTspName());
                prep.setString(3, ftc.getTsName());
                prep.setString(4, ftc.getTerritory());
                prep.setString(5, ftc.getTrustServiceType());
                prep.setString(6, ftc.getServiceStatus());
                prep.setLong(7, ftc.getTslDate());
                prep.setLong(8, ftc.getTslExpDate());
                prep.setString(9, ftc.getTslCertHash());
                prep.setString(10, ftc.getTslCertificate());
                prep.setShort(11, ftc.getSdiType());
                prep.setLong(12, ftc.getCertExpiry());
                prep.setString(13, ftc.getTslSeqNo());
                prep.setString(14, ftc.getTslSha1());
                prep.setString(15, ftc.getExtractorStatus());
                prep.setString(16, ftc.getSignStatus());
                return prep;
            }
        };
        sqlAction.dbAction();
    }

    public void addORreplaceCertificate(TslCertificates tc) {

        final TslCertificates ftc = tc;
        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "INSERT OR REPLACE INTO Certificates values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
                prep.setInt(1, getIdFromRec(ftc));
                prep.setString(2, ftc.getTspName());
                prep.setString(3, ftc.getTsName());
                prep.setString(4, ftc.getTerritory());
                prep.setString(5, ftc.getTrustServiceType());
                prep.setString(6, ftc.getServiceStatus());
                prep.setLong(7, ftc.getTslDate());
                prep.setLong(8, ftc.getTslExpDate());
                prep.setString(9, ftc.getTslCertHash());
                prep.setString(10, ftc.getTslCertificate());
                prep.setShort(11, ftc.getSdiType());
                prep.setLong(12, ftc.getCertExpiry());
                prep.setString(13, ftc.getTslSeqNo());
                prep.setString(14, ftc.getTslSha1());
                prep.setString(15, ftc.getExtractorStatus());
                prep.setString(16, ftc.getSignStatus());
                return prep;
            }
        };

        sqlAction.dbAction();
    }

    public List<TslCertificates> getCertificates(boolean defaultNull) {
        List<TslCertificates> defaultResult = defaultNull ? null : new LinkedList<TslCertificates>();
        return queryCertificates("SELECT * FROM Certificates ORDER BY " + "territory" + " ASC ;", defaultResult);
    }

    public List<TslCertificates> getCertificates(String column) {
        return queryCertificates("SELECT * FROM Certificates ORDER BY " + column + " ASC ;", new LinkedList<TslCertificates>());
    }

    public List<TslCertificates> selectCertificates(String column, String value) {
        return queryCertificates("SELECT * FROM Certificates WHERE " + column + " = \"" + value + "\" ;", new LinkedList<TslCertificates>());
    }

    private List<TslCertificates> queryCertificates(String query, List<TslCertificates> defaultResult) {

        SqLiteQuery<List<TslCertificates>> sqlQuery = new SqLiteQuery<List<TslCertificates>>(url, userid, password) {

            @Override
            List<TslCertificates> parseResultSet(ResultSet rs) throws SQLException {
                LinkedList<TslCertificates> certList = new LinkedList<TslCertificates>();
                while (rs.next()) {
                    TslCertificates tc = new TslCertificates();
                    tc.setId(rs.getInt(tslCertCols[0]));
                    tc.setTspName(rs.getString(tslCertCols[1]));
                    tc.setTsName(rs.getString(tslCertCols[2]));
                    tc.setTerritory(rs.getString(tslCertCols[3]));
                    tc.setTrustServiceType(rs.getString(tslCertCols[4]));
                    tc.setServiceStatus(rs.getString(tslCertCols[5]));
                    tc.setTslDate(rs.getLong(tslCertCols[6]));
                    tc.setTslExpDate(rs.getLong(tslCertCols[7]));
                    tc.setTslCertHash(rs.getString(tslCertCols[8]));
                    tc.setTslCertificate(rs.getString(tslCertCols[9]));
                    tc.setSdiType(rs.getShort(tslCertCols[10]));
                    tc.setCertExpiry(rs.getLong(tslCertCols[11]));
                    tc.setTslSeqNo(rs.getString(tslCertCols[12]));
                    tc.setTslSha1(rs.getString(tslCertCols[13]));
                    tc.setExtractorStatus(rs.getString(tslCertCols[14]));
                    tc.setSignStatus(rs.getString(tslCertCols[15]));
                    certList.add(tc);
                }
                return certList;
            }
        };
        return sqlQuery.dbQuery(query, defaultResult);
    }

    public int deleteCertificate(TslCertificates tc) {
        return deleteCertificates("id", String.valueOf(tc.getId()));
    }

    public int deleteCertificates(String column, String value) {
        Statement stmt;
        int cnt = 0;
        try {
            Connection con = getConnection();
            stmt = con.createStatement();
            cnt = stmt.executeUpdate("DELETE FROM Certificates WHERE " + column + " = \"" + value + "\" ;");
            con.close();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return cnt;
    }

    private static final byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value};
    }

    private static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    private int getIdFromRec(TslCertificates tc) {
        BigInteger idHash = FnvHash.getFNV1a(tc.getTslCertHash() + tc.getTrustServiceType() + tc.getTsName());
        int id = (int) (idHash.longValue() & 0xFFFFFFFF);
        return id;
    }
}
