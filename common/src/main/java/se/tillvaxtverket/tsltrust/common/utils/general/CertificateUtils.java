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
package se.tillvaxtverket.tsltrust.common.utils.general;

import com.aaasec.lib.aaacert.AaaCertificate;
import com.aaasec.lib.aaacert.extension.ExtensionInfo;
import com.aaasec.lib.aaacert.extension.QCStatementsExt;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.core.CorePEM;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.Extension;

/**
 * X509 Certificate handling utilities
 */
public class CertificateUtils implements Constants {

    private static final Logger LOG = Logger.getLogger(CertificateUtils.class.getName());

    public static AaaCertificate getCertificate(String pemCert) {
        if (pemCert == null) {
            return null;
        }
        return (getCertificate(Base64Coder.decodeLines(CorePEM.trimPemCert(pemCert))));
    }

    public static AaaCertificate getCertificate(byte[] certData) {

        try {
            return new AaaCertificate(certData);

        } catch (Exception e) {
            return null;
        }

    }

    public static short getSdiType(java.security.cert.X509Certificate javaCert) {
        try {
            AaaCertificate cert = getCertificate(javaCert.getEncoded());
            if (cert != null) {
                return getSdiType(cert);
            }
        } catch (Exception ex) {
        }
        return 4;
    }

    public static short getSdiType(AaaCertificate cert) {
        boolean qualified = false;
        boolean rootCert = false;
        boolean eeCert = false;

        // CA test
        if (cert.getBasicConstraints() == -1) {
            eeCert = true;
        }

        // root test
        if (cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal())) {
            rootCert = true;
        }

        // qc test
        Map<String, ExtensionInfo> extensionsMap = cert.getExtensionsMap();
        if (extensionsMap.containsKey(Extension.qCStatements.getId())) {
            try {
                QCStatementsExt qcStatements = QCStatementsExt.getInstance(extensionsMap.get(Extension.qCStatements.getId()).getExtDataASN1());
                qualified = qcStatements.isQcCompliance();
            } catch (Exception e) {
            }
        }

        // return result
        short type = 0;
        if (!eeCert) {
            type = 1;
        }
        if (rootCert) {
            type = 2;
        }
        if (qualified) {
            type += 3;
        }
        return type;
    }

    public static String getSki(AaaCertificate cert) {

        byte[] skiBytes = cert.getExtensionValue("2.5.29.14");
        if (null != skiBytes) {
            String rawSkiData = getHex(skiBytes);
            if (rawSkiData.length() > 8) {
                return (rawSkiData.substring(8, rawSkiData.length()));
            } else {
                return "hash";
            }
        }
        return "hash";
    }

    public static String getHex(byte[] inpBytes) {
        StringBuffer b = new StringBuffer();
        for (byte val : inpBytes) {
            int a = (int) val & 255;
            String hex = Integer.toHexString(a);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            b.append(hex);
        }
        return b.toString();

    }

    /**
     * Get distinguished name component from X500 distinguished name
     *
     * @param distinguishedName The X500Principal holding the distinguished name
     * @param keyWord Keyword for the target name component (e.g. "CN" for the
     * common name component)
     * @return The target name compnent (null if name component was not present)
     */
    public static String getNameComponent(X500Principal distinguishedName, String keyWord) {

        String dNameText = distinguishedName.getName();
        int startIndex = 0;
        int endIndex = 0;
        int c;
        boolean done = false;
        String nameComponent = null;
        String testWord = keyWord + "=";

        //Check if dn starts with keyword
        if (testWord.length() < dNameText.length()) {
            if (dNameText.substring(0, testWord.length()).equals(testWord)) {
                startIndex = testWord.length();
            }
        }

        for (int i = 0; i < dNameText.length(); i++) {
            c = (int) dNameText.charAt(i);

            switch (c) {
                case COMMA:
                    if (i + 1 + testWord.length() < dNameText.length()) {
                        if (dNameText.substring(i + 1, i + 1 + testWord.length()).equals(testWord)) {
                            startIndex = i + 1 + testWord.length();
                        }
                    }

                    if (startIndex > 0 && i > startIndex && !done) { // If CN is being parsed
                        endIndex = i;
                    }
                    break;
                case PLUS:
                    if (i + 1 + testWord.length() < dNameText.length()) {
                        if (dNameText.substring(i + 1, i + 1 + testWord.length()).equals(testWord)) {
                            startIndex = i + 1 + testWord.length();
                        }
                    }
                    if (startIndex > 0 && i > startIndex && !done) { // If CN is being parsed
                        endIndex = i;
                    }
                    break;
                case EQUAL:
                    if (startIndex > 0 && endIndex > startIndex) {
                        done = true;
                        nameComponent = dNameText.substring(startIndex, endIndex).trim();
                    }
            }
        }
        if (startIndex > 0 && !done) { // if CN was found but no end was detected in the loop
            nameComponent = dNameText.substring(startIndex, dNameText.length()).trim();
        }

        return nameComponent;
    }

//    /**
//     * Formats a text representation of the certificate data using openSSL certificate formating options
//     * @param pemCert
//     * The pem formatted certificate to be displayed
//     * @param tempDirName
//     * The directory where the temporary files will be stored during the openSSL operations
//     * @param textOptions
//     * OpenSSL option paramaters. Intended to provide -certopt options, such as "-certopt no_signature"
//     * @return
//     * The display ready formatted certificate content
//     */
//    
//    public static String formatCert(String pemCert, String tempDirName, String textOptions) {
//        tempDirName += "TempFiles/XcTemp/";
//        File tempDir = new File(tempDirName);
//        File pemCertFile = new File(tempDirName + "tempCert.pem");
//        File result = new File(tempDirName + "tempCert.txt");
//        try {
//            FileUtils.forceMkdir(tempDir);
//            CoreFileUtls.saveTxtFile(pemCertFile, pemCert);
//            String execString = "openssl x509 -in tempCert.pem -out tempCert.txt -nameopt multiline,utf8,-esc_msb,-esc_ctrl -text " + textOptions;
//            try {
//                Runtime rt = Runtime.getRuntime();
//                Process proc = rt.exec(execString, null, tempDir);
//                int status = proc.waitFor();
//                String certTxt = CoreFileUtls.readTextFile(result);
//                //FileUtils.deleteQuietly(result);
//                return fixValidity(certTxt);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(CertificateUtils.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
//
//    private static String fixValidity(String formattedCert) {
//        String str = "Validity";
//        StringBuffer b = new StringBuffer();
//        if (formattedCert == null) {
//            return null;
//        }
//        for (int i = 0; i < formattedCert.length(); i++) {
//            if (i + str.length() < formattedCert.length()) {
//                if (formattedCert.substring(i, i + str.length()).equals(str)) {
//                    b.append("Validity:" + LF + " ");
//                    i += str.length() + 1;
//                } else {
//                    b.append(formattedCert.charAt(i));
//                }
//            } else {
//                b.append(formattedCert.charAt(i));
//            }
//        }
//
//        return b.toString();
//    }
//
//    public static ColorPane getColorCert(String textCert, ColorPane cp) {
//
//        StringBuffer b = new StringBuffer();
//        boolean match = false;
//        int endIndex;
//        boolean ext = false;
//        String[] matchString = {"Certificate:", "Data:", "Version:", "Serial Number:", "Signature Algorithm:", "Issuer:", "Validity:", "Subject:", "Subject Public Key Info:", "X509v3 extensions:", "Signature Algorithm:"};
//        //,"Not Before:", "Not After :","Public Key Algorithm:","RSA Public Key:"
//        String[] matchStyle = {"BlueBoldUnderline", "Bold", "BlueBold", "BlueBold", "BlueBold", "BlueBold", "BlueBold", "BlueBold", "BlueBold", "BlueBold", "BlueBold"};
//        //,"Green","Green","Green","Green"
//        for (String line : getLines(textCert)) {
//            match = false;
//            int styleIndex = 0;
//            for (String mstr : matchString) {
//                endIndex = getEndIndex(line, mstr);
//                if (endIndex > 0 && !match) {
//                    cp.addStyledText(line.substring(0, endIndex), matchStyle[styleIndex]);
//                    cp.addPlainText(line.substring(endIndex));
//                    cp.addLF();
//                    match = true;
//                }
//                styleIndex++;
//            }
//            if (!match) {
//                endIndex = getExtEndIndex(line, "X509v3");
//                if (endIndex > 0) {
//                    cp.addStyledText(line.substring(0, endIndex), "GreenBold");
//                    cp.addPlainText(line.substring(endIndex));
//                    cp.addLF();
//                } else {
//                    cp.addPlainText(line);
//                    cp.addLF();
//                }
//
//            }
//        }
//        return cp;
//    }
    public static List<String> getLines(String inpString) {
        List lines = new LinkedList<String>();
        InputStream in = new ByteArrayInputStream(inpString.getBytes());
        Reader rdr = new InputStreamReader(in);
        BufferedReader input = new BufferedReader(rdr);

        try {
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            LOG.warning(ex.getMessage());
        }
        return lines;
    }

    private static int getEndIndex(String line, String matchString) {
        int endIndex = -1;
        if (matchString.length() > line.length()) {
            return -1;
        }
        for (int i = 0; i < line.length(); i++) {
            if (i + matchString.length() < line.length() + 1) {
                if (line.substring(i, i + matchString.length()).equals(matchString)) {
                    endIndex = i + matchString.length();
                }
            }
        }
        return endIndex;
    }

    private static int getExtEndIndex(String line, String matchString) {
        int endIndex = -1;
        if (matchString.length() > line.length()) {
            return -1;
        }
        for (int i = 0; i < line.length(); i++) {
            if (i + matchString.length() < line.length()) {
                if (line.substring(i, i + matchString.length()).equals(matchString)) {
                    for (int j = i + i + matchString.length(); j < line.length(); j++) {
                        int c = (int) line.charAt(j);
                        if (c == COLON) {
                            endIndex = j + 1;
                            break;
                        }
                    }
                }
            }
        }
        return endIndex;
    }
}
