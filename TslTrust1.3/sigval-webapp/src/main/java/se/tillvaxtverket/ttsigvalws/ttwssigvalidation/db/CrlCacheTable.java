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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.db;

import com.google.gson.Gson;
import java.util.logging.Level;
import java.sql.*;
import java.util.List;
import java.util.logging.Logger;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.core.DbCrlCache;

/**
 * Database handling class for the CRL cache database file
 */
public class CrlCacheTable extends SqliteUtil<DbCrlCache> {

    private static final Gson gson = new Gson();
    public static final String DATA_TABLE = "Crl_Cache";
    private static final String KEY_COL = "Hash";
    private static final String TABLE_CONSTRUCT =
                "Hash VARCHAR(255) not NULL,"
                + "Url VARCHAR(65535),"
                + "Update_Time LONG,";

    public CrlCacheTable(String dbFileName) {
        super(dbFileName, TABLE_CONSTRUCT, DATA_TABLE, KEY_COL);
    }

    @Override
    PreparedStatement dataStoragePreparedStatement(Connection con, DbCrlCache st, boolean replace) {
        String action = (replace) ? "INSERT OR REPLACE" : "INSERT";
        PreparedStatement prep = null;
        
        try {
            prep = con.prepareStatement(
                    action + " INTO " + table + " VALUES (?,?,?);");
            prep.setString(1, st.getHash());
            prep.setString(2, st.getUrl());
            prep.setLong(3, st.getNextUpdate());
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        return prep;
    }

    @Override
    void processDatabaseRecordValues(ResultSet rs, List<DbCrlCache> valueList) {
        try {
            while (rs.next()) {
                    DbCrlCache ks = new DbCrlCache();
                    ks.setHash(rs.getString(1));
                    ks.setUrl(rs.getString(2));
                    ks.setNextUpdate(rs.getLong(3));
                valueList.add(ks);
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(CrlCacheTable.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    @Override
    String getKeyColumnStringValue(DbCrlCache tr) {
        return tr.getHash();
    }
    
    private String toB64(byte[] data){
        if (data==null){
            return "";
        }
        return String.valueOf(Base64Coder.encode(data));
    }
    
    private byte[] fromB64(String str){
        return Base64Coder.decode(str);
    }
        
}
