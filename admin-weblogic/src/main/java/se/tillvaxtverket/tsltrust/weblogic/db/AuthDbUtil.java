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
 * Database utilities for the user and authorization database
 */
import se.tillvaxtverket.tsltrust.common.utils.core.FnvHash;
import se.tillvaxtverket.tsltrust.weblogic.data.AdminUser;
import java.io.File;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class AuthDbUtil {

    private static final Logger LOG = Logger.getLogger(AuthDbUtil.class.getName());
    private String userid = "iaik", password = "iaik";
    private String url;
    // String url = "jdbc:mySubprotocol:myDataSource"; ?
    private String[] authCols = new String[]{"id", "displayName", "identifier", "attribute", "idpEntityId", "idpDisplayName", "authLevel", "targets"};

    public AuthDbUtil(String ttDataDir) {
        url = "jdbc:sqlite://" + ttDataDir + "db/authDb";
        StringBuilder b = new StringBuilder();

        File dbFile = new File(ttDataDir + "db/authDb");
        if (!dbFile.canRead()) {
            if (dbFile.getParentFile() != null) {
                dbFile.getParentFile().mkdirs();
            }
            createDbTable();
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
    public final void createDbTable() {
        Connection con = getConnection();
        Statement stmt;

        String authTable;
        authTable = "create table Authorization ("
                + "id INTEGER,"
                + "displayName VARCHAR(255),"
                + "identifier VARCHAR(255),"
                + "attribute VARCHAR(255),"
                + "idpEntityId VARCHAR(255),"
                + "idpDisplayName VARCHAR(255),"
                + "authLevel INTEGER,"
                + "targets VARCHAR(65535),"
                + "PRIMARY KEY ( id ) )";

        try {
            stmt = con.createStatement();
            stmt.executeUpdate(authTable);
            stmt.close();
            con.close();

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        System.out.println("DB: " + url + " created");
    }

    public void addAdminUser(AdminUser adminUser) {

        final AdminUser au = adminUser;
        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "insert into Authorization values (?,?,?,?,?,?,?,?);");
                prep.setInt(1, getIdFromRec(au));
                prep.setString(2, au.getDisplayName());
                prep.setString(3, au.getIdentifier());
                prep.setString(4, au.getAttributeId());
                prep.setString(5, au.getIdpEntityId());
                prep.setString(6, au.getIdpDisplayName());
                prep.setInt(7, au.getAuthLevel());
                prep.setString(8, au.getTargets());

                return prep;
            }
        };
        sqlAction.dbAction();
    }

    public void addORreplaceAdminUser(AdminUser adminUser) {

        final AdminUser au = adminUser;
        SqLiteAction sqlAction = new SqLiteAction(url, userid, password) {

            @Override
            PreparedStatement getPrepStatement(Connection con) throws SQLException {
                PreparedStatement prep = con.prepareStatement(
                        "INSERT OR REPLACE INTO Authorization values (?,?,?,?,?,?,?,?);");
                prep.setInt(1, getIdFromRec(au));
                prep.setString(2, au.getDisplayName());
                prep.setString(3, au.getIdentifier());
                prep.setString(4, au.getAttributeId());
                prep.setString(5, au.getIdpEntityId());
                prep.setString(6, au.getIdpDisplayName());
                prep.setInt(7, au.getAuthLevel());
                prep.setString(8, au.getTargets());
                return prep;
            }
        };
        sqlAction.dbAction();
    }

    public List<AdminUser> getAdminUsers() {
        return getAdminUsers("displayName");
    }

    public List<AdminUser> getAdminUsers(String column) {

        String query = "SELECT * FROM Authorization ORDER BY " + column + " ASC ;";

        SqLiteQuery<List<AdminUser>> sqlQuery = new SqLiteQuery<List<AdminUser>>(url, userid, password) {

            @Override
            List<AdminUser> parseResultSet(ResultSet rs) throws SQLException {
                List<AdminUser> adminList = new LinkedList<AdminUser>();
                while (rs.next()) {
                    AdminUser au = new AdminUser();
                    au.setId(rs.getInt(authCols[0]));
                    au.setDisplayName(rs.getString(authCols[1]));
                    au.setIdentifier(rs.getString(authCols[2]));
                    au.setAttributeId(rs.getString(authCols[3]));
                    au.setIdpEntityId(rs.getString(authCols[4]));
                    au.setIdpDisplayName(rs.getString(authCols[5]));
                    au.setAuthLevel(rs.getInt(authCols[6]));
                    au.setTargets(rs.getString(authCols[7]));
                    adminList.add(au);
                }
                return adminList;
            }
        };
        List<AdminUser> result = sqlQuery.dbQuery(query, new LinkedList<AdminUser>());

        return result;
    }

    public void deleteAdminUser(AdminUser user) {
        Statement stmt;
        List<AdminUser> adminList = new LinkedList<AdminUser>();
        try {
            Connection con = getConnection();
            stmt = con.createStatement();
            int cnt = stmt.executeUpdate("DELETE FROM Authorization WHERE id = " + String.valueOf(user.getId()) + " ;");
            con.close();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value};
    }

    private static int byteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    private int getIdFromRec(AdminUser au) {
        BigInteger idHash = FnvHash.getFNV1a(au.getIdentifier() + au.getIdpEntityId());
        int id = (int) (idHash.longValue() & 0xFFFFFFFF);
        return id;
    }
}
