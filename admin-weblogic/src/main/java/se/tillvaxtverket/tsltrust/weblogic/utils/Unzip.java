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
