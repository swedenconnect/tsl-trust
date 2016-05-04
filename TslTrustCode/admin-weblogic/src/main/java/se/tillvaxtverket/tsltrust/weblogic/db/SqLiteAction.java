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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import se.tillvaxtverket.tsltrust.common.utils.core.SqLiteConnectionPool;

/**
 * Abstract class for performing non query actions on an SQLite database
 */
public abstract class SqLiteAction {

    private static final Logger LOG = Logger.getLogger(SqLiteAction.class.getName());
    private String userid, password;
    private String url;
    private static final long maxWaitTime = 10000;

    /**
     * Constructor
     * @param url Database URL
     * @param userid Database user id
     * @param password Database password
     */
    public SqLiteAction(String url, String userid, String password) {
        this.userid = userid;
        this.password = password;
        this.url = url;
    }

    /**
     * Performs the database action set by the abstract getPrepStatement() method
     */
    public void dbAction() {
        Statement stmt;
        boolean complete = false;
        long initTime = System.currentTimeMillis();
        while (!complete && System.currentTimeMillis() < initTime + maxWaitTime) {
            Connection con = null;
            try {
                con = SqLiteConnectionPool.getConnection(url, userid, password);

                if (con != null) {
                    // check the connection
                    if (con.getWarnings() != null) {
                        con.clearWarnings();
                        con.close();
                        con = SqLiteConnectionPool.getConnection(url, userid, password);
                    }

                    stmt = con.createStatement();
                    PreparedStatement prep = getPrepStatement(con);
                    prep.addBatch();

                    con.setAutoCommit(false);
                    prep.executeBatch();
                    con.setAutoCommit(true);
                    con.close();
                } else {
                    LOG.warning("Null SQLite connection. Aborting DB action");
                }
                complete = true;
            } catch (Exception ex) {
//                LOG.warning(ex.getMessage());
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (SQLException ex1) {
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ex1) {
                    LOG.log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    /**
     * Defines the database action prepared statement
     * @param con The database connection obtained for performing the action
     * @return A preparedStatement object defining the database action to be performed
     */
    abstract PreparedStatement getPrepStatement(Connection con) throws SQLException;
}
