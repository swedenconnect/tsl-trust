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
package se.tillvaxtverket.tsltrust.weblogic.hibernate;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Hibernate database access utility class, providing database access to dataobject
 * of the specified data class (E).
 */
public class HigernateDbUtil<E extends Object> {

    protected static final Logger LOG = Logger.getLogger(HigernateDbUtil.class.getName());
    protected String keyColName;
    protected String tableClass;
    protected HibernateUtil hibUtil;

    public HigernateDbUtil(String keyColName, String tableClass, HibernateUtil hibUtil) {
        this.keyColName = keyColName;
        this.tableClass = tableClass;
        this.hibUtil = hibUtil;
    }

    /**
     * Gets all database records in sequential order
     * @return A list of database records
     */
    public List<E> getAllRecords() {
        return getAllRecords(true, true, keyColName);
    }

    /**
     * Gets all database records optionally sorted by the key column
     * @param sorted setting this to true returns a sorted list in ascending order
     * @return A list of database records
     */
    public List<E> getAllRecords(boolean sorted) {
        return getAllRecords(sorted, true, keyColName);
    }

    /**
     * Gets all database records optionally sorted by the key column
     * @param sorted setting this to true returns a sorted list
     * @param ascending setting this to true returns the sorted list in ascending order
     * @return A list of database records
     */
    public List<E> getAllRecords(boolean sorted, boolean ascending) {
        return getAllRecords(sorted, ascending, keyColName);
    }
    
    /**
     * Gets all database records
     * @param sorted setting this to true returns a sorted list
     * @param ascending setting this to true returns the sorted list in ascending order
     * @param column column to sort by
     * @return A list of database records
     */
    public List<E> getAllRecords(boolean sorted, boolean ascending, String column) {
        List<E> resultList = new LinkedList<E>();
        String dirQs = ascending ? " asc" : " desc";
        String sortQs = sorted ? " order by " + column + dirQs : "";
        String qs = "from "+tableClass+" rec"+sortQs;

        try {
            Session session = hibUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Query q = session.createQuery(qs);
            List qList = q.list();
            session.getTransaction().commit();
            for (Object o : qList) {
                E record = (E) o;
                resultList.add(record);
            }
        } catch (HibernateException ex) {
//            ex.printStackTrace();
            LOG.warning(ex.getMessage());
        }
        return resultList;
    }

    /**
     * returns a single database record
     * @param value The key value of the target database record
     * @return Database record
     */
    public E getRecord(String value) {
        List<E> records = getRecords(keyColName, value);
        if (records.isEmpty()) {
            return null;
        } else {
            return records.get(0);
        }
    }

    /**
     * Get all records that matches the specified value
     * @param value The value in the specified main column to search for. Note that if the
     * main column holds a unique key, the list will only contain at most one value.
     * In this case use the function getRecord instead.
     * @return A list of database records
     */
    public List<E> getRecords(String value) {
        return getRecords(keyColName, value);
    }

    /**
     * Get all records where the specified column holds the specified value
     * @param column Name of the specified column
     * @param value The target value
     * @return A list of database records
     */
    public List<E> getRecords(String column, String value) {
        List<E> resultList = new LinkedList<E>();

        try {
            Session session = hibUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Query q = session.createQuery("from "+tableClass+" rec where " + column + " = '" + value + "'");
            List qList = q.list();
            session.getTransaction().commit();
            for (Object o : qList) {
                E record = (E) o;
                resultList.add(record);
            }
        } catch (HibernateException ex) {
            LOG.warning(ex.getMessage());
        }
        return resultList;
    }

    /**
     * Stores a database record. If a database record with identical key is in the database,
     * the existing record will be replaced
     * @param record the record to store
     */
    public void saveRecord(E record) {
        try {
            Session session = hibUtil.getSessionFactory().openSession();
            session.beginTransaction();
            session.saveOrUpdate(record);
            session.getTransaction().commit();
        } catch (HibernateException ex) {
            LOG.warning(ex.getMessage());
        }
    }

    /**
     * Delete records with the specified value in the key column
     * @param value the key column value
     * @return the number of deleted records
     */
    public int deleteRecords(String value) {
        return deleteRecords(keyColName, value);

    }

    /**
     * Delete records with the specified value in the specified column
     * @param column The column name
     * @param value The key column value
     * @return The number of deleted records
     */
    public int deleteRecords(String column, String value) {
        List<E> records = getRecords(column, value);
        int cnt = 0;
        for (E record : records) {
            deleteRecord(record);
            cnt++;
        }
        return cnt;
    }

    /**
     * Delete the provided database record
     * @param record Record to delete
     */
    public void deleteRecord(E record) {
        try {
            Session session = hibUtil.getSessionFactory().openSession();
            session.beginTransaction();
            session.delete(record);
            session.getTransaction().commit();
        } catch (HibernateException ex) {
            LOG.warning(ex.getMessage());
        }
    }

}
