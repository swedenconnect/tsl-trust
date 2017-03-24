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
