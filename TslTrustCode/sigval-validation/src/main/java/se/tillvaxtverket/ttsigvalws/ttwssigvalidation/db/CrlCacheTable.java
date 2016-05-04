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
