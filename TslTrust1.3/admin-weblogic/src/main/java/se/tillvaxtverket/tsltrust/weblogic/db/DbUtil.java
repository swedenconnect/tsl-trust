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
 * Abstract SQLite database utility class. This class is subclassed by several database utility classes
 * using SQLite. This class is not used by databases that are accessed through hibernate.
 */
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import se.tillvaxtverket.tsltrust.common.utils.core.SqLiteConnectionPool;

public abstract class DbUtil {

    protected static final Logger LOG = Logger.getLogger(DbUtil.class.getName());
    protected String userid = "iaik", password = "iaik";
    protected String url, dbFileName, ttDataDir;
    protected String[] tableStruct;

    protected void init() {
        url = "jdbc:sqlite://" + ttDataDir + "db/" + dbFileName;
        File dbFile = new File(ttDataDir + "db/" + dbFileName);
        if (!dbFile.canRead()) {
            if (dbFile.getParentFile() != null) {
                dbFile.getParentFile().mkdirs();
            }
            createDbTable();
        }
    }

    /**
     * Create Db table according to the array of tableStruct strings
     */
    protected void createDbTable() {
        Statement stmt;
        try {
            Connection con = SqLiteConnectionPool.getConnection(url, userid, password);
            stmt = con.createStatement();
            for (String struct : tableStruct) {
                stmt.executeUpdate(struct);
            }
            stmt.close();
            con.close();

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.info("DB: " + url + " created");
    }
}
