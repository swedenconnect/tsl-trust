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