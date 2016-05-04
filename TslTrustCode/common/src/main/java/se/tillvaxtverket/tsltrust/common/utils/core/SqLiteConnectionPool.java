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
package se.tillvaxtverket.tsltrust.common.utils.core;

import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Connection pool for SQLite databases
 */
public class SqLiteConnectionPool {

    private static Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
    private static final Logger LOG = Logger.getLogger(SqLiteConnectionPool.class.getName());

    /**
     * Gets a database connection from the connection pool for the provided connection URL.
     * If the connection URL is called for the first time, a new database connection pool
     * is created, else the connection pool created for that connection URL is used.
     * @param dbUrl The database source URL
     * @return Database connection
     */
    public static Connection getConnection(String dbUrl, String userName, String password) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        DataSource dataSource = getDataSource(dbUrl, userName, password);
        Connection conn = null;
        conn = dataSource.getConnection();
        return conn;
    }

    private static DataSource getDataSource(String dbUrl, String userName, String password) {
        if (dataSourceMap.containsKey(dbUrl)) {
            return dataSourceMap.get(dbUrl);
        }
        DataSource dataSource = setupDataSource(dbUrl, userName, password);
        dataSourceMap.put(dbUrl, dataSource);
        return dataSource;
    }

    private static DataSource setupDataSource(String connectURI, String userName, String password) {
        ObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, userName, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        return dataSource;
    }
}