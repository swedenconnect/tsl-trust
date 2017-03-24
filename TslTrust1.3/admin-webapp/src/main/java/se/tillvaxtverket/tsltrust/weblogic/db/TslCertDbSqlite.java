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
