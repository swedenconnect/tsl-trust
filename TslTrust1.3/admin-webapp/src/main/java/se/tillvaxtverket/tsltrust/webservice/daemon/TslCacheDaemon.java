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
package se.tillvaxtverket.tsltrust.webservice.daemon;

import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.common.utils.general.WebXmlConstants;
import se.tillvaxtverket.tsltrust.weblogic.data.TslCertificates;
import se.tillvaxtverket.tsltrust.weblogic.data.TslMetaData;
import se.tillvaxtverket.tsltrust.weblogic.db.LogDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDb;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDbSqlite;
import se.tillvaxtverket.tsltrust.weblogic.models.TslTrustModel;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import se.tillvaxtverket.tsltrust.common.tsl.TSLFactory;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.data.MajorLogRecord;
import se.tillvaxtverket.tsltrust.weblogic.utils.TslCache;

/**
 * Daemon class for caching trust information in the form of EU Trusted Lists
 */
public class TslCacheDaemon implements WebXmlConstants {

    private static final Logger LOG = Logger.getLogger(TslCacheDaemon.class.getName());
    private final LogDbUtil logDb;
    private List<TslCertificates> newCertList;
    private List<TslCertificates> changedCertList;
    private final File recacheFile;
    private final TslTrustModel model;
    private TslCertDb certDb;
    private List<TslMetaData> tslList = null;
    private final TSLFactory tslFact = new TSLFactory();
    private final TslCache tslCache;
    private boolean taskComplete;
    private final long threadSleep;
    private static final SimpleDateFormat tFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a TSL Cache daemon object
     * @param taskComplete
     * @param threadSleep 
     */
    public TslCacheDaemon(boolean taskComplete, long threadSleep) {
        this.model = ContextParameters.getModel();
        this.certDb = model.getTslCertDb();
        this.logDb = model.getLogDb();
        recacheFile = new File(model.getDataLocation() + "cfg/recacheTime");
        tslCache = new TslCache(model);
        this.taskComplete = taskComplete;
        this.threadSleep = threadSleep;
    }

    private void con(String description) {
        con("TSL Recache", description);
    }

    private void con(String event, String description) {
        logDb.addConsoleEvent(new ConsoleLogRecord(event, description, "daemon"));
    }

    public void run() {
        con("Trust Service List recache operations");
        long startTime = System.currentTimeMillis();

        getTslData();
        logDb.deleteExcessEventRecords();
        logDb.deleteOldAccessRecords();
        if (!recacheFile.canRead()) {
            try {
                FileUtils.forceMkdir(recacheFile.getParentFile());
            } catch (IOException ex) {
                LOG.warning(ex.getMessage());
            }
        }
        FileOps.saveTxtFile(recacheFile, String.valueOf(System.currentTimeMillis()));
        long elapsed = System.currentTimeMillis() - startTime;
        con("Next TSL recache scheduled at " + tFormat.format(new Date(System.currentTimeMillis() + (threadSleep - elapsed))));
    }

    private void getTslData() {
        con("Root TSL", "Downloading root TSL");
        if (!tslCache.recacheLotl()) {
            con("Error", "Failed to obtain valid EU root TSL and no one in cache.. Aborting");
            return;
        }

        //Signature check of Lotl
        con("Root TSL Signature check");
        if (!tslCache.lotlSignatureCheck()) {
            con("Error", "EU root TSL signature is invalid");
        }

        //Recache Tsls from lotl (never returns null - at minimum an empty list)
        taskComplete = true;
        con("Downloading national TSLs...");
        tslCache.recacheTsl();
        tslList = tslCache.getCachedTslList();

        if (tslList.isEmpty()) {
            return;
        }

        //Update trust service certificate database
        taskComplete = false;
        con("Updating TSL database records...");
        int deletedRecords = certDb.deleteAbsentStatusRecords(tslList, logDb);
        if (deletedRecords > 0) {
            model.getLogDb().addMajorEvent(new MajorLogRecord("Tsl DB Update", "Deleted "
                    + String.valueOf(deletedRecords) + " un-listed Trust Services", "TSL Extractor"));
        }
        boolean updated = checkForUpdates();

        // if db was updated. check that update is complete
        if (updated) {
            updated = checkForUpdates();
            if (updated) {
                // db is corrupt. Recreate database
                File dbFile = new File(model.getDataLocation() + "db/tslCertDb");
                dbFile.delete();
                model.getLogDb().addMajorEvent(new MajorLogRecord("Tsl DB Error", "Database is corrupt. Recreating database...", "TSL Extractor"));
                certDb = new TslCertDbSqlite(model.getDataLocation());
                checkForUpdates();
            }
        }



        con("TSL recache complete");
        taskComplete = true;
    }

    private boolean checkForUpdates() {
        boolean update = false;
        certDb.updateAbsentStatus(tslList, logDb);
        List<TslCertificates> tcList = certDb.getAllTslCertificate(true);
        if (tcList != null) {
            newCertList = certDb.getNewCertificates(tcList, tslList, logDb);
            changedCertList = TslCertDb.getChangedCertificates(tcList, tslList, logDb);


            if (newCertList.size() > 0 || changedCertList.size() > 0) {
                updateDatabase();
                update = true;
            }
            return update;
        } else {
            LOG.warning("SQLite DB Error. Could not retrieve TSL Certificates. Db update aborted");
            con("DB Error", "DB Connection failure. Aborting update.");
            return false;
        }

    }

    private void updateDatabase() {
        certDb.addCertificates(newCertList, logDb);
        certDb.updateCertificates(changedCertList, logDb);
        StringBuilder b = new StringBuilder();
        b.append("TSL Database updated with: ");
        b.append(newCertList.size());
        b.append(" new Trust Services, ");
        b.append(changedCertList.size());
        b.append(" updated Trust Services");
        model.getLogDb().addMajorEvent(new MajorLogRecord("Tsl DB Update", b.toString(), "TSL Extractor"));
    }
}
