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
package se.tillvaxtverket.tsltrust.common.utils.general;

import iaik.x509.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic static utility functions
 */
public class GeneralStaticUtils implements Constants {

    private static final String LF = System.getProperty("line.separator");
    static final String beginCert = "-----BEGIN CERTIFICATE-----";
    static final String endCert = "-----END CERTIFICATE-----";

    /**
     * Get a list of individual PEM certificates from a concatenated string of PEM certificates
     * @param pemCerts concatenated string och PEM certificates
     * @return list of PEM certificate strings
     */
    public static List<String> getPemCerts(String pemCerts) {
        List<String> pemList = new LinkedList<String>();
        int i = 0;
        while (i < pemCerts.length()) {
            if (!pemCerts.startsWith(beginCert)) {
                pemCerts = pemCerts.substring(1);
                i = 0;
                continue;
            }
            if (pemCerts.substring(0, i).endsWith(endCert)) {
                pemList.add(pemCerts.substring(0, i));
                pemCerts = pemCerts.substring(i);
                i = 0;
                continue;
            }
            i++;
        }
        return pemList;
    }

    /**
     * Derives a Calendar object from a millisecond time value
     * @param timeInMs milliseconds since Jan 1, 1970
     * @return Calendar object of the provided time
     */
    public static Calendar getTime(long timeInMs) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timeInMs);
        return time;
    }

    /**
     * Derives a Calendar object from a Date object
     * @param date date object
     * @return Calendar object
     */
    public static Calendar getTime(Date date) {
        return getTime(date.getTime());
    }

    /**
     * Gets a list of X509 vertificates from a String representation of a collection of certificates in PEM format
     * @param pemCerts A concatenated string of PEM certificates
     * @return a list of X509Certificates
     */
    public static List<X509Certificate> getCertsFromPemList(String pemCerts) {
        List<X509Certificate> certList = new LinkedList<X509Certificate>();
        List<String> pemList = getPemCerts(pemCerts);
        for (String pemCert : pemList) {
            certList.add(CertificateUtils.getCertificate(pemCert));
        }
        return certList;
    }

//    public static List<X509Certificate> getTestCertificates(String homeDir) {
//        File testCertFile = new File(homeDir, "_inCerts/certs.pem");
//        String pemCerts = FileOps.readTextFile(testCertFile);
//        return getCertsFromPemList(pemCerts);
//    }
//
//    public static String selectPaneRow(JTextPane pane) {
//        String text = pane.getText();
//        List<String> rows = CertificateUtils.getLines(text);
//        int cp = pane.getCaretPosition();
//
//        int i = 0;
//        for (String row : rows) {
//            if (cp < (i + row.length())) {
//                pane.setCaretPosition(i);
//                pane.setSelectionStart(i);
//                pane.setSelectionEnd(i + row.length());
//                break;
//            }
//            i += row.length() + 1;
//        }
//        String result = "" + pane.getSelectedText();
//        if (result.trim().length() == 0) {
//            return selectPaneRow(pane, true);
//        }
//        return pane.getSelectedText();
//    }
//
//    public static String selectPaneRow(JTextPane pane, boolean selectFirst) {
//        String text = pane.getText();
//        List<String> rows = CertificateUtils.getLines(text);
//        int cp = 0;
//
//        int i = 0;
//        String row = rows.get(0);
//        pane.setCaretPosition(i);
//        pane.setSelectionStart(i);
//        pane.setSelectionEnd(i + row.length());
//        i += row.length() + 1;
//        return pane.getSelectedText();
//    }
//
    public static String urlEncode(String str) {
        try {
            String urlEncodeStr = URIComponentCoder.encodeURIComponent(str);
            return urlEncodeStr;
//            return str.replace((char) 32, (char) UNDERSCORE_DASH);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String urlDecode(String str) {
        try {
            String decode = URIComponentCoder.decodeURIComponent(str);
            return decode;
//            return str.replace((char) UNDERSCORE_DASH, (char) 32);
        } catch (Exception ex) {
            return null;
        }
    }

    public static List<String> splitSemicolonString(String semicolonString) {
        List<String> stringList = new ArrayList<String>();
        if (semicolonString != null && semicolonString.length() > 0) {
            String[] split = semicolonString.split(";");
            for (String val : split) {
                stringList.add(val.trim());
            }
        }
        return stringList;
    }

    public static String getSemicolonString(List<String> stringList) {
        String semicolonString = "";
        if (stringList != null && !stringList.isEmpty()) {
            StringBuilder b = new StringBuilder();
            for (String val : stringList) {
                b.append(val.trim()).append(";");
            }
            b.deleteCharAt(b.lastIndexOf(";"));
            semicolonString = b.toString();
        }
        return semicolonString;
    }
}
