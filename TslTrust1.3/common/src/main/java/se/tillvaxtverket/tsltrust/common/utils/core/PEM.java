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

/**
 * PEM certificate format functions
 */
public class PEM {

    private static final String LF = System.getProperty("line.separator");
    static final String beginReq = "-----BEGIN CERTIFICATE REQUEST-----";
    static final String endReq = "-----END CERTIFICATE REQUEST-----";
    static final String beginCert = "-----BEGIN CERTIFICATE-----";
    static final String endCert = "-----END CERTIFICATE-----";

    public PEM() {
    }

    public static String getPemReq(byte[] inpData) {
        return (getPemReq(inpData, inpData.length));
    }

    public static String getPemReq(byte[] inpData, int len) {
        StringBuilder b = new StringBuilder();
        b.append(beginReq).append(LF);
        b.append(Base64Coder.encodeLines(inpData, 0, len, 76, LF));
        b.append(endReq);
        return b.toString();
    }

    public static String getPemCert(byte[] inpData) {
        return (getPemCert(inpData, inpData.length));
    }

    public static String getPemCert(byte[] inpData, String lineSeparator) {
        return (getPemCert(inpData, inpData.length,lineSeparator));
    }

    public static String getPemCert(byte[] inpData, int len) {
        return (getPemCert(inpData, len, LF));
    }

    public static String getPemCert(byte[] inpData, int len, String lineSeparator) {
        StringBuilder b = new StringBuilder();
        b.append(beginCert).append(lineSeparator);
        b.append(Base64Coder.encodeLines(inpData, 0, len, 76, lineSeparator));
        b.append(endCert);
        return b.toString();
    }

    public static String trimPemCert(String pemCert) {
        if (pemCert == null) {
            return null;
        }
        String corePemCert = pemCert;
        for (int i = 0; i < pemCert.length(); i++) {
            if (pemCert.length() - i > beginCert.length()) {
                if (pemCert.substring(i, i + beginCert.length()).equalsIgnoreCase(beginCert)) {
                    corePemCert = pemCert.substring(i, pemCert.length());
                }
            }
        }

        return (removeString(removeString(corePemCert, beginCert), endCert));
    }

    public static String removeString(String inpString, String removeString) {
        StringBuilder b = new StringBuilder();
        if (inpString.length() > removeString.length()) {
            for (int i = 0; i < inpString.length(); i++) {
                if (inpString.length() - i >= removeString.length()) {
                    if (inpString.substring(i, i + removeString.length()).equalsIgnoreCase(removeString)) {
                        i = i + removeString.length();
                    } else {
                        b.append(inpString.charAt(i));
                    }
                } else {
                    b.append(inpString.charAt(i));
                }
            }
        }

        return b.toString();
    }
}
