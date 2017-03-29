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
