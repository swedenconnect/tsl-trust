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
package se.tillvaxtverket.tsltrust.common.utils.core;

import java.awt.Desktop;
import java.net.*;
import java.io.*;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Static functions for dereferencing URLs
 */
public class DerefUrl {

    private static final Logger LOG = Logger.getLogger(DerefUrl.class.getName());

    static public String getData(URL url) {
        return getData(url, SslSecurityPolicy.ACCEPT_ALL);
    }

    static public String getData(URL url, SslSecurityPolicy policy) {
        switch (policy) {
            case ACCEPT_ALL:
                trustAllCAs();
        }

        StringBuilder b = new StringBuilder();
        try {
            // Create an URL instance
            // Get an input stream for reading
            InputStream in = url.openStream();
            // Create a buffered input stream for efficency
            BufferedInputStream bufIn = new BufferedInputStream(in);
            // Repeat until end of file
            for (;;) {
                int data = bufIn.read();
                // Check for EOF
                if (data == -1) {
                    break;
                } else {
                    b.append((char) data);
                }
            }
        } catch (MalformedURLException ex) {
            LOG.warning(ex.getMessage());
        } catch (IOException ex) {
            LOG.warning(ex.getMessage());
        }
        return b.toString();
    }

    static public byte[] getBytes(URL url) {
        return getBytes(url, SslSecurityPolicy.ACCEPT_ALL);
    }

    static public byte[] getBytes(URL url, SslSecurityPolicy policy) {
        switch (policy) {
            case ACCEPT_ALL:
                trustAllCAs();
        }

        try {
            // Create an URL instance
            // Get an input stream for reading
            InputStream in = url.openStream();
            // Create a buffered input stream for efficency
            BufferedInputStream bufIn = new BufferedInputStream(in);
            // Repeat until end of file
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[100];
                for (;;) {
                    int len = bufIn.read(b);
                    if (len == -1) {
                        break;
                    } else {
                        bos.write(b, 0, len);
                    }
                }
                byte[] data = bos.toByteArray();
                bos.close();
                return data;
            } catch (IOException ex) {
                System.out.println(" I/O Error - " + ex);
            }
        } catch (MalformedURLException mue) {
            System.out.println("Invalid URL: " + url.toString());
        } catch (IOException ioe) {
            System.out.println("I/O Error - " + ioe);
        }
        return null;
    }

    static public boolean downloadFile(URL url, File file) {
        return downloadFile(url, file, SslSecurityPolicy.ACCEPT_ALL);
    }

    static public boolean downloadFile(URL url, File file, SslSecurityPolicy policy) {
        switch (policy) {
            case ACCEPT_ALL:
                trustAllCAs();
        }

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
                LOG.warning(ex.getMessage());
                return false;
            }
        } catch (MalformedURLException ex) {
            LOG.warning(ex.getMessage());
            return false;
        } catch (IOException ex) {
            LOG.warning(ex.getMessage());
            return false;
        }
    }

    public static void browse(URI uri) throws IOException {
        Desktop dt = Desktop.getDesktop();
        dt.browse(uri);
    }

    public static void browse(String uriString) throws IOException {
        URI uri;
        try {
            uri = new URI(uriString);
            Desktop dt = Desktop.getDesktop();
            dt.browse(uri);
        } catch (URISyntaxException ex) {
            LOG.warning(ex.getMessage());
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

    public static enum SslSecurityPolicy {

        SYSTEM_DEF, ACCEPT_ALL
    }
}
