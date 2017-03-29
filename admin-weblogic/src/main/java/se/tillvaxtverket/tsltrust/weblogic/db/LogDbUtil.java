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

import java.util.ArrayList;
import java.util.List;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.data.AdminLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.data.LogInfo;
import se.tillvaxtverket.tsltrust.weblogic.data.MajorLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HibernateDbFactory;
import se.tillvaxtverket.tsltrust.weblogic.hibernate.HigernateDbUtil;

/**
 * Database utility class for access to log databases through hibernate
 */
public class LogDbUtil implements TTConstants{

    private int maxDetailLogSize;
    private long maxSumLogAge;
    HigernateDbUtil<AdminLogRecord> dbAdminLog = HibernateDbFactory.getDbAdminLog();
    HigernateDbUtil<MajorLogRecord> dbMajorLog = HibernateDbFactory.getDbMajorEventLog();
    HigernateDbUtil<ConsoleLogRecord> dbConsoleLog = HibernateDbFactory.getDbConsoleLog();

    /**
     * Constructor with default values 
     * 1000 console records and 90 days retention of major log events
     */
    public LogDbUtil() {
        setIntValues("1000", "90");
    }

    /**
     * Constructor with specific parameters
     * @param maxSize The maximum number of events in the console log
     * @param maxAge number of days major log records will be kept 
     */
    public LogDbUtil(String maxSize, String maxAge) {
        setIntValues(maxSize, maxAge);
    }

    public void addAdminEvent(AdminLogRecord logInfo) {
        dbAdminLog.saveRecord(logInfo);
    }

    public void addMajorEvent(MajorLogRecord logInfo) {
        dbMajorLog.saveRecord(logInfo);
    }

    public void addConsoleEvent(ConsoleLogRecord logInfo) {
        dbConsoleLog.saveRecord(logInfo);
    }

    public List<AdminLogRecord> getAdminEvents() {

        return dbAdminLog.getAllRecords(true, false);
    }

    public List<MajorLogRecord> getMajorEvents() {
        return dbMajorLog.getAllRecords(true, false);
    }

    public List<ConsoleLogRecord> getConsoleEvents() {
        return dbConsoleLog.getAllRecords(true, false);
    }



    public void deleteOldAccessRecords() {
        long time = System.currentTimeMillis() - maxSumLogAge;
        deleteOldAccessRecords(time, "Access");
    }

    public void deleteOldAccessRecords(long time) {
        deleteOldAccessRecords(time, "Access");
    }

    public void deleteExcessEventRecords() {
        List<ConsoleLogRecord> records = getConsoleEvents();
        if (records.size() > maxDetailLogSize) {
            for (int i = maxDetailLogSize; i < records.size(); i++) {
                dbConsoleLog.deleteRecord(records.get(i));
            }
        }
    }

    private void deleteOldAccessRecords(long time, String table) {
        List<MajorLogRecord> records = getMajorEvents();
        for (MajorLogRecord record : records) {
            if (record.getTime() < time) {
                dbMajorLog.deleteRecord(record);
            }
        }
    }

    private void setIntValues(String maxSize, String maxAge) {

        try {
            maxDetailLogSize = Integer.valueOf(maxSize);
        } catch (Exception ex) {
            maxDetailLogSize = 1000;
        }
        try {
            maxSumLogAge = Long.valueOf(maxAge) * 1000 * 3600 * 24;
        } catch (Exception ex) {
            maxDetailLogSize = 90 * 1000 * 3600 * 24; // 90 days default
        }
    }

    public List<LogInfo> getRecords(String dbTable) {
        List<LogInfo> logList = new ArrayList<LogInfo>();
        if (dbTable.equals(ADMIN_LOG_TABLE)){
            List<AdminLogRecord> allRecords = dbAdminLog.getAllRecords(true,false);
            for (AdminLogRecord rec:allRecords){
                LogInfo liRec = (LogInfo) rec;
                logList.add(liRec);
            }
            return logList;
        }
        if (dbTable.equals(MAJOR_LOG_TABLE)){
            List<MajorLogRecord> allRecords = dbMajorLog.getAllRecords(true,false);
            for (MajorLogRecord rec:allRecords){
                LogInfo liRec = (LogInfo) rec;
                logList.add(liRec);
            }
            return logList;
        }
        if (dbTable.equals(CONSOLE_LOG_TABLE)){
            List<ConsoleLogRecord> allRecords = dbConsoleLog.getAllRecords(true,false);
            for (ConsoleLogRecord rec:allRecords){
                LogInfo liRec = (LogInfo) rec;
                logList.add(liRec);
            }
            return logList;
        }
        return logList;
    }
}
