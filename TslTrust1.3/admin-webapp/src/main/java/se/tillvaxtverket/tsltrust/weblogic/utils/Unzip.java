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
package se.tillvaxtverket.tsltrust.weblogic.utils;

import se.tillvaxtverket.tsltrust.weblogic.db.LogDbUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;

/**
 * Unzipping functions
 */
public class Unzip {

    private static final Logger LOG = Logger.getLogger(Unzip.class.getName());
    
    public static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    static public void unzipFile(File file, File target) {
        Enumeration entries;
        ZipFile zipFile;


        try {
            zipFile = new ZipFile(file);

            entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    (new File(entry.getName())).mkdir();
                    continue;
                }

                copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(target)));
            }

            zipFile.close();
        } catch (IOException ioe) {
            LOG.warning(ioe.getMessage());
            return;
        }
    }

    static public void unzipSingleXmlFile(File inFile, File outFile, LogDbUtil log) {
        Enumeration entries;
        ZipFile zipFile;


        try {
            zipFile = new ZipFile(inFile);
            entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                if (entry.isDirectory()) {
                    continue;
                }

                int nameLen = entry.getName().length();
                if (nameLen > 4) {
                    if (entry.getName().toLowerCase().endsWith(".xml")) {
                        log.addConsoleEvent(new ConsoleLogRecord("File unpacked", "Extracting single file: " + entry.getName() + " to: " + outFile.getName(), "Unzip"));

                        copyInputStream(zipFile.getInputStream(entry),
                                new BufferedOutputStream(new FileOutputStream(outFile)));
                        zipFile.close();
                        return;
                    }
                }
            }
            zipFile.close();
        } catch (IOException ioe) {
            log.addConsoleEvent(new ConsoleLogRecord("Error", "Unhandled exception: " + ioe.getMessage(), "Unzip"));
            LOG.warning(ioe.getMessage());
            return;
        }
    }
}
