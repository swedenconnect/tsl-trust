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
import java.net.*;
import java.io.*;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import se.tillvaxtverket.tsltrust.weblogic.data.ConsoleLogRecord;

/**
 * Download functions for TSL data
 */
public class TslDownload {

    private static final Logger LOG = Logger.getLogger(TslDownload.class.getName());

    static public boolean getTsl(URL url, File file) {
        return getTsl(url, file, null);
    }

    static public boolean getTsl(URL url, File file, LogDbUtil log) {
        trustAllCAs();
        try {
            // Create an URL instance
            // Get an input stream for reading
            InputStream in = url.openStream();
            // Create a buffered input stream for efficency
            BufferedInputStream bufIn = new BufferedInputStream(in);
            // Repeat until end of file
            try {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] b = new byte[100];
                for (;;) {
                    int len = bufIn.read(b);
                    if (len == -1) {
                        break;
                    } else {
                        fos.write(b, 0, len);
                    }
                }
                fos.close();
                return true;
            } catch (IOException ex) {
                if (log != null) {
                    log.addConsoleEvent(new ConsoleLogRecord("Error", "I/O Error: " + url.toString() + " " + ex.getMessage(), "Download Utils"));
                } else {
                    LOG.warning(ex.getMessage());
                }
                return false;
            }
        } catch (MalformedURLException ex) {
            if (log != null) {
                log.addConsoleEvent(new ConsoleLogRecord("Error", "Invalid URL: " + url.toString(), "Download Utils"));
            } else {
                LOG.warning(ex.getMessage());
            }
            return false;
        } catch (IOException ex) {
            if (log != null) {
                log.addConsoleEvent(new ConsoleLogRecord("Error", "I/O Error: " + url.toString() + " " + ex.getMessage(), "Download Utils"));
            } else {
                LOG.warning(ex.getMessage());
            }
            return false;
        }
    }

    public static void trustAllCAs() {
        try {
            /*
             *  fix for
             *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
             *       sun.security.validator.ValidatorException:
             *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
             *               unable to find valid certification path to requested target
             */
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
        }
    }
}
