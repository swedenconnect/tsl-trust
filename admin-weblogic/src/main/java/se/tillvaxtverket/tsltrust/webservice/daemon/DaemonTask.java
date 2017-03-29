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

import java.text.SimpleDateFormat;
import java.util.Date;
import se.tillvaxtverket.tsltrust.webservice.daemon.ca.CertAuthOperations;

/**
 * This class provides the functions performed by the servlet daemon
 */
public class DaemonTask extends ServletDaemon {

    private static final SimpleDateFormat tFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public DaemonTask() {
    }

    @Override
    void doDaemonTask() {
        long startTime = System.currentTimeMillis();
        TslCacheDaemon tslDaemon = new TslCacheDaemon();
        tslDaemon.run();
        LOG.info("TSL information recached... Updating Db");
        CertAuthOperations certDaemon = new CertAuthOperations();
        certDaemon.run();
        
        long elapsed = System.currentTimeMillis()-startTime;
        String nextUpdate = "Next TSL recache scheduled at "+tFormat.format(new Date(System.currentTimeMillis()+(threadSleep-elapsed)));

        LOG.info(alive ? "TSL Trust recache completed. " +nextUpdate: "TSL recache interrupted. "+nextUpdate);
        taskComplete = true;
    }

    private void log(String info) {
        log(info, true, false);
    }

    private void log(String info, boolean verbose, boolean warning) {
        if (verbose && !ContextParameters.isVerboseLogging()) {
            return;
        }

        if (verbose) {
            if (warning) {
                LOG.warning(info);
            } else {
                System.out.println(info);
            }
        } else {
            if (warning) {
                LOG.warning(info);
            } else {
                LOG.info(info);
            }
        }
    }
}
