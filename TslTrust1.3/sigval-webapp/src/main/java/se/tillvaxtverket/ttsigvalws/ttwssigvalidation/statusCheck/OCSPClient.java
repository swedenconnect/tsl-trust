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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck;

import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.OCSPVerifyContext;
import iaik.asn1.structures.AlgorithmID;
import iaik.pkcs.pkcs12.CertificateBag;
import iaik.pkcs.pkcs12.KeyBag;
import iaik.pkcs.pkcs12.PKCS12;
import iaik.utils.ASN1InputStream;
import iaik.utils.Util;
import iaik.x509.X509Certificate;
import iaik.x509.X509ExtensionInitException;
import iaik.x509.extensions.ExtendedKeyUsage;
import iaik.x509.ocsp.OCSPException;
import iaik.x509.ocsp.OCSPRequest;
import iaik.x509.ocsp.OCSPResponse;
import iaik.x509.ocsp.UnknownResponseException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * OCSP client.
 */
public class OCSPClient extends OCSP {

    // the url of the OCSP responder to connect to
    String responderUrl;

    /**
     * Default constructor.
     * Reads required keys and certificates from the demo keystore.
     *
     * @exception IOException if an error occurs when loading the keystore
     */
    public OCSPClient() throws IOException {
        super();
        responderUrl = "http://localhost:9999";
    }

    /**
     * Starts the OCSP client
     * @param targetCert The certificate being checked for status
     * @param issuerCert The issuer cert that validates the target cert
     * @param ocspUrl The URL to the OCSP responder
     * @param caCertList A list of trusted CA certificates
     * @param rootCert root certificate under which provided certificates can be validated
     * @param crlCache An object of the CRLchecker, providing CRL checking capability to responder path validation
     * @return Status information for the checked certificate
     * @throws OCSPException 
     */
    public OCSPVerifyContext start(X509Certificate targetCert, X509Certificate issuerCert, String ocspUrl, List<X509Certificate> caCertList, X509Certificate rootCert, CRLChecker crlCache) throws OCSPException {

        clearLog();
        logString("OCSP validation of certificate issued to:");
        logString(targetCert.getSubjectDN().getName());
        logString("-----------------------------------------");
        ocspVC = new OCSPVerifyContext();
        ocspVC.setOcspCheckOK(true);
        targetCerts_ = new X509Certificate[]{targetCert, issuerCert};
        ocspVC.setTargetCert(targetCert);
        ocspVC.setTargetIssuer(issuerCert);
        responderUrl = ocspUrl;
        targetCerts_ = Util.arrangeCertificateChain(targetCerts_, false);
        this.caCertList = caCertList;
        this.rootCert = rootCert;
        this.crlCache = crlCache;

        fixTrustAllCAs();


        String[] argv = new String[]{};
        // read in target certs
        try {
            // calculate the certID new for the target cert read in
            // hash algorithm for CertID
            AlgorithmID hashAlgorithm = AlgorithmID.sha1;
            try {
                reqCert_ = createReqCert(targetCerts_, hashAlgorithm);
            } catch (Exception ex) {
                throw new OCSPException("Cannot create cert id: " + ex.toString());
            }

            // For signed requests
            if ((argv.length == 4) || (argv.length == 5)) {
                String fileName = (argv.length == 4) ? argv[2] : argv[3];
                String pwd = (argv.length == 4) ? argv[3] : argv[4];
                logString("Reading requestor key from PKCS#12 file " + fileName + "...");
                readPKCS12File(fileName, pwd.toCharArray());
                signatureAlgorithm_ = AlgorithmID.sha1WithRSAEncryption;
                if (!(requestorKey_ instanceof java.security.interfaces.RSAPrivateKey)) {
                    if (requestorKey_ instanceof java.security.interfaces.DSAPrivateKey) {
                        signatureAlgorithm_ = AlgorithmID.dsaWithSHA1;
                    } else {
                        logString("Error in initialization. Unknown key algorithm: "
                                + requestorKey_.getAlgorithm());
                        Util.waitKey();
                        System.exit(-1);
                    }
                }

            } else {
                // unsigned request
                requestorKey_ = null;
                requestorCerts_ = null;
            }
            //Set the public keys of trusted responders            
            trustedResponderKeys = new LinkedList<PublicKey>();
            for (X509Certificate cert : caCertList) {
                if (ceckResponderEKU(cert)) {
                    trustedResponderKeys.add(cert.getPublicKey());
                    logString("Added trusted responder: " + cert.getSubjectDN().getName());
                }
            }

            // create request
            if (requestorKey_ == null) {
                logString("Creating unsigned OCSP request");
            } else {
                logString("Creating signed OCSP request");
            }
            boolean includeExtensions = false;
            OCSPRequest ocspRequest = createOCSPRequest(requestorKey_, requestorCerts_, includeExtensions);
            /*
            System.out.println("Creating signed OCSP request with extensions");
            OCSPRequest ocspRequest = createOCSPRequest(requestorKey, requestorCerts, true);
             */
            // encode request
            verboseLogString("Encode OCSP request" + (char) 10);
            byte[] request = ocspRequest.getEncoded();
            verboseLogString(iaik.utils.Util.toPemString(ocspRequest.getEncoded(), "OCSP REQUEST"));
            // send request
            logString("Send request to " + responderUrl);
            URL url = new URL(responderUrl);
            // register content handler factory for OCSP
            // HttpURLConnection.setContentHandlerFactory(new OCSPContentHandlerFactory());
            System.getProperties().put("java.content.handler.pkgs", "iaik.x509.ocsp.net");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // set content type
            con.setRequestProperty("Content-Type", "application/ocsp-request");
            con.setRequestProperty("Accept", "application/ocsp-response");
            // post request
            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));
            dataOut.write(request);
            dataOut.flush();
            dataOut.close();

            // read response
            if (con.getResponseCode() / 100 != 2) {
                logString("Error connecting to " + responderUrl + ":");
                logString(con.getResponseMessage());
                ocspVC.setOcspCheckOK(false);
            } else {
                logString("Parse response: ");
                OCSPResponse ocspResponse = (OCSPResponse) con.getContent();
                // or instead of getContent() here you may call:
                //OCSPResponse ocspResponse = new OCSPResponse(con.getInputStream());
                logString("");
                verboseLogString(iaik.utils.Util.toPemString(ocspResponse.getEncoded(), "OCSP RESPONSE"));
                parseOCSPResponse(ocspResponse, includeExtensions);
            }
        } catch (UnknownResponseException ex) {
            logString("Response successful but contains an unknown response type:");
            UnknownResponseException unknown = (UnknownResponseException) ex;
            logString("Unknown type: " + unknown.getResponseType());
            logString("ASN.1 structure:");
            logString(unknown.getUnknownResponse().toString());
            ocspVC.setOcspCheckOK(false);
        } catch (Exception ex) {
            logString("OCSP request failed: " + ex.getMessage());
            logException(ex);
            ocspVC.setOcspCheckOK(false);
        }

        logString("");
        ocspVC.setLog(log);
        ocspVC.setExceptionLog(exceptionLog);
        return ocspVC;
    }

    /**
     * Reads a PKCS12 object from the given file.
     *
     * @param fileName the name of the PKCS#12 file
     * @param password the password to be used for decryption
     */
    private void readPKCS12File(String fileName, char[] password) {
        InputStream is = null;
        PKCS12 pkcs12 = null;
        try {
            is = new FileInputStream(fileName);
            pkcs12 = new PKCS12(new ASN1InputStream(is));
            if (!pkcs12.verify(password)) {
                logString("Cannot read PKCS12 object: MAC verification error!");
                Util.waitKey();
                System.exit(-1);
            }
            pkcs12.decrypt(password);

            // get the requestor key
            KeyBag kB = pkcs12.getKeyBag();
            requestorKey_ = kB.getPrivateKey();

            // get the requestor certs
            CertificateBag[] certBag = pkcs12.getCertificateBags();
            java.security.cert.Certificate[] certChain =
                    CertificateBag.getCertificates(certBag);
            try {
                requestorCerts_ = Util.convertCertificateChain(certChain);
            } catch (Exception ex) {
                logString("Error reading certificates from PKCS#12 file:");
                ex.printStackTrace();
                Util.waitKey();
                System.exit(-1);
            }
            requestorCerts_ = Util.arrangeCertificateChain(requestorCerts_, false);
            if (requestorCerts_ == null) {
                logString("Cannot sort certificates included in PKCS#12 object!");
                Util.waitKey();
                System.exit(-1);
            }
            if (requestorKey_ == null) {
                logString("Cannot create client. Missing requestor key!");
                Util.waitKey();
                System.exit(-1);
            }
            if ((requestorCerts_ == null) || (requestorCerts_.length < 1)) {
                logString("Cannot create client. Missing requestor certs!");
                Util.waitKey();
                System.exit(-1);
            }

        } catch (Exception ex) {
            logString("Error reading PKCS12 file " + fileName + ":");
            logException(ex);
            return;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

    }

    private void fixTrustAllCAs() {
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
            logException(ex);
        }
    }

    private boolean ceckResponderEKU(X509Certificate cert) {
        try {
            ExtendedKeyUsage eku = (ExtendedKeyUsage) cert.getExtension(ExtendedKeyUsage.oid);
            boolean responderEKU = (eku != null && eku.contains(ExtendedKeyUsage.ocspSigning));
            boolean issuedByCertIssuer = cert.getIssuerDN().equals(targetCerts_[0].getIssuerDN());
            return (responderEKU && issuedByCertIssuer);
        } catch (X509ExtensionInitException ex) {
        }
        return false;
    }
}
