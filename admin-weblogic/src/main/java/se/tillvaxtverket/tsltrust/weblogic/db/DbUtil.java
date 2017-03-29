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
