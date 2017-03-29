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

import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.TslMetaData;
import java.util.List;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;

/**
 * Utility class for modifications to the TSL Trust Service database
 */
public class TslCertDbSqlite extends TslCertDb {

    TslCertSQLiteUtil sqlite;

    public TslCertDbSqlite(String ttDataDir) {
        sqlite = new TslCertSQLiteUtil(ttDataDir);
    }

    @Override
    public List<TslCertificates> getAllTslCertificate(boolean defaultNull){
        List<TslCertificates> dbCerts = sqlite.getCertificates(defaultNull);
        return dbCerts;
    }

    @Override
    public List<TslCertificates> getAllTslCertificate(String sortID) {
        List resultList = sqlite.getCertificates(sortID);
        return resultList;
    }

    @Override
    public void addCertificates(List<TslCertificates> newCertList, LogDbUtil log) {
        for (TslCertificates tc : newCertList) {
            sqlite.addCertificate(tc);
            log.addConsoleEvent(new ConsoleLogRecord("TSL Recache", "Added record for: " + tc.getTspName().trim(), "TSL Extractor"));
        }
    }

    @Override
    public void updateCertificates(List<TslCertificates> changedCerts, LogDbUtil log) {
        for (TslCertificates tc : changedCerts) {
            sqlite.addORreplaceCertificate(tc);
            log.addConsoleEvent(new ConsoleLogRecord("TSL Recache", "Updated record of: " + tc.getTspName().trim(), "TSL Extractor"));
        }
    }

    @Override
    public void updateAbsentStatus(List<TslMetaData> tslMdList, LogDbUtil log) {
        List<TslCertificates> dbCerts = sqlite.getCertificates(true);
        if (dbCerts != null) {
            List<TslCertificates> tcList = getAbsentCertificates(dbCerts, tslMdList, log);
            for (TslCertificates tc : tcList) {
                sqlite.addORreplaceCertificate(tc);
            }
        } else {
            LOG.warning("DB error, unable to retrieve TSL Certificates. Aborting DB update absent status");
        }
    }

    @Override
    public int deleteAbsentStatusRecords(List<TslMetaData> tslMdList, LogDbUtil log) {
        List<TslCertificates> dbCerts = sqlite.getCertificates(true);
        if (dbCerts != null) {
            List<TslCertificates> tcList = getAbsentCertificates(dbCerts, tslMdList, log);
            int totalDeleted = 0;
            for (TslCertificates tc : tcList) {
                if (tc.getExtractorStatus().equals("absent")) {
                    totalDeleted += sqlite.deleteCertificate(tc);
                    log.addConsoleEvent(new ConsoleLogRecord("TSL Recache",
                            "Deleted absent TS from: " + tc.getTspName().trim(),
                            "TSL Extractor"));
                }
            }
            return totalDeleted;
        } else {
            LOG.warning("DB error, unable to retrieve TSL Certificates. Aborting DB update absent status");
            return 0;
        }
    }
}
