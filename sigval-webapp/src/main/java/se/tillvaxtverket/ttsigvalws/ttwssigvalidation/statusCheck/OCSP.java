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
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.CertVerifyContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AccessDescription;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.GeneralName;
import iaik.asn1.structures.Name;
import iaik.security.random.SecRandom;
import iaik.utils.CryptoUtils;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.AuthorityInfoAccess;
import iaik.x509.extensions.ExtendedKeyUsage;
import iaik.x509.ocsp.BasicOCSPResponse;
import iaik.x509.ocsp.CertID;
import iaik.x509.ocsp.CertStatus;
import iaik.x509.ocsp.OCSPException;
import iaik.x509.ocsp.OCSPRequest;
import iaik.x509.ocsp.OCSPResponse;
import iaik.x509.ocsp.ReqCert;
import iaik.x509.ocsp.Request;
import iaik.x509.ocsp.ResponderID;
import iaik.x509.ocsp.SingleResponse;
import iaik.x509.ocsp.UnknownInfo;
import iaik.x509.ocsp.UnknownResponseException;
import iaik.x509.ocsp.extensions.CrlID;
import iaik.x509.ocsp.extensions.Nonce;
import iaik.x509.ocsp.extensions.ServiceLocator;
import iaik.x509.ocsp.utils.TrustedResponders;
import java.util.ArrayList;
import java.util.List;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.CertChainVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.ValidationLogger;

/**
 * OCSP client super class
 */
public class OCSP extends ValidationLogger {

    /**
     * Calculates an ReqCert of type <code>certID</code> from the given target certificates.
     * 
     * @param targetCerts the target certificate chain containing the target certificate
     *                    (for which OCSP status information is requested) at index 0
     * @param hashAlgorithm the hash algorithm to be used
     * 
     * @return the ReqCert
     * 
     * @throws Exception if an exception occurs
     */
    final static ReqCert createReqCert(X509Certificate[] targetCerts, AlgorithmID hashAlgorithm)
            throws Exception {

        // issuer name
        Name issuerName = (Name) targetCerts[1].getSubjectDN();
        // issuer key
        PublicKey issuerKey = targetCerts[1].getPublicKey();
        // create the certID
        try {
            CertID certID = new CertID(hashAlgorithm, issuerName, issuerKey,
                    targetCerts[0].getSerialNumber());
            return new ReqCert(ReqCert.certID, certID);
        } catch (NoSuchAlgorithmException ex) {
            throw new NoSuchAlgorithmException("No implementation of SHA1");
        }

    }
    // private key of the requestor
    PrivateKey requestorKey_;
    // private key of responder
    PrivateKey responderKey_;
    // the signing certs of the requestor
    X509Certificate[] requestorCerts_;
    // the signing certs of the responder
    X509Certificate[] responderCerts_;
    // the target certs for which to get status information
    X509Certificate[] targetCerts_;
    // the signature algorithm
    AlgorithmID signatureAlgorithm_;
    // hash algorithm for CertID
    AlgorithmID hashAlgorithm_;
    // secure random number generator
    SecureRandom random_;
    // the reqCert of the target cert
    ReqCert reqCert_;
    // a nonce value
    byte[] nonce_;
    // trust repository for responders
    TrustedResponders trustedResponders_;
    List<PublicKey> trustedResponderKeys;
    List<X509Certificate> caCertList;
    X509Certificate rootCert;
    CRLChecker crlCache;
    OCSPVerifyContext ocspVC;

    /**
     * Setup the demo certificate chains.
     *
     * Keys and certificates are retrieved from the demo KeyStore.
     *
     * @exception IOException if a file read error occurs
     */
    public OCSP() throws IOException {
        super(25);
        random_ = SecRandom.getDefault();
        // defaults
        signatureAlgorithm_ = AlgorithmID.sha1WithRSAEncryption;
        hashAlgorithm_ = AlgorithmID.sha1;
    }

    /**
     * Creates an OCSPRequest.
     *
     * @param requestorKey the private key of the requestor, or <code>null</code>
     *                     if the request shall not be signed
     * @param requestorCerts if the request shall be signed (requestorKey != null)
     *                       and signer certs shall be included
     * @param includeExtensions if extensions shall be included
     *
     * @return the OCSPRequest created
     *
     * @exception OCSPException if an error occurs when creating the request
     */
    public OCSPRequest createOCSPRequest(PrivateKey requestorKey,
            X509Certificate[] requestorCerts, boolean includeExtensions) throws OCSPException {

        return createOCSPRequest(requestorKey, requestorCerts, targetCerts_, includeExtensions);
    }

    /**
     * Creates an OCSPRequest.
     *
     * @param requestorKey the private key of the requestor, or <code>null</code>
     *                     if the request shall not be signed
     * @param requestorCerts if the request shall be signed (requestorKey != null)
     *                       and signer certs shall be included
     * @param targetCerts the certs for which status information shall be included
     * @param includeExtensions if extensions shall be included
     *
     * @return the OCSPRequest created
     *
     * @exception OCSPException if an error occurs when creating the request
     */
    public OCSPRequest createOCSPRequest(PrivateKey requestorKey,
            X509Certificate[] requestorCerts, X509Certificate[] targetCerts,
            boolean includeExtensions) throws OCSPException {

        if (targetCerts != null) {
            targetCerts_ = targetCerts;
            try {
                reqCert_ = createReqCert(targetCerts, hashAlgorithm_);
            } catch (Exception ex) {
                throw new OCSPException("Error creating cert id: " + ex.toString());
            }
        }

        if (reqCert_ == null) {
            throw new OCSPException("Cannot create ocsp request from null cert id!");
        }

        try {

            // create a single request for the target cert identified by the reqCert
            Request request = new Request(reqCert_);

            if (includeExtensions) {
                if (responderCerts_ != null) {
                    // include service locator
                    ObjectID accessMethod = ObjectID.caIssuers;
                    GeneralName accessLocation = new GeneralName(GeneralName.uniformResourceIdentifier, "http://www.testResponder.at");
                    AccessDescription accessDescription = new AccessDescription(accessMethod, accessLocation);
                    AuthorityInfoAccess locator = new AuthorityInfoAccess(accessDescription);
                    ServiceLocator serviceLocator = new ServiceLocator((Name) responderCerts_[0].getSubjectDN());
                    serviceLocator.setLocator(locator);
                    request.setServiceLocator(serviceLocator);
                }
            }

            // create the OCSPRequest
            OCSPRequest ocspRequest = new OCSPRequest();

            // set the requestList
            ocspRequest.setRequestList(new Request[]{request});

            if (includeExtensions) {
                // we only accept basic OCSP responses
                ocspRequest.setAcceptableResponseTypes(new ObjectID[]{BasicOCSPResponse.responseType});

                // set a nonce value
                nonce_ = new byte[16];
                random_.nextBytes(nonce_);
                ocspRequest.setNonce(nonce_);
            }

            if (requestorKey != null) {
                if ((requestorCerts == null) || (requestorCerts.length == 0)) {
                    throw new NullPointerException("Requestor certs must not be null if request has to be signed!");
                }
                // set the requestor name
                ocspRequest.setRequestorName(new GeneralName(GeneralName.directoryName, requestorCerts[0].getSubjectDN()));
                // include signing certificates
                if (requestorCerts != null) {
                    ocspRequest.setCertificates(requestorCerts);
                }
                // sign the request
                ocspRequest.sign(signatureAlgorithm_, requestorKey);
            }
            logString("Request created:");
            logString(ocspRequest.toString(true));
            ocspVC.setRequest(ocspRequest);

            return ocspRequest;

        } catch (Exception ex) {
            throw new OCSPException(ex.toString());
        }

    }

    /**
     * Creates an ocsp response answering the given ocsp request.
     *
     * @param is the encoded OCSP request supplied from an input stream
     * @param requestorKey the signing key of the requestor (may be supplied
     *        for allowing to verify a signed request with no certificates included)
     * @param includeExtensions if extensions shall be included
     *
     * @return the DER encoded OCSPResponse
     */
    public byte[] createOCSPResponse(InputStream is, PublicKey requestorKey, boolean includeExtensions)/* throws OCSPException */ {

        OCSPResponse ocspResponse = null;
        OCSPRequest ocspRequest = null;

        // first parse the request
        int responseStatus = OCSPResponse.successful;
        logString("Parsing request...");

        try {
            ocspRequest = new OCSPRequest(is);
            if (ocspRequest.containsSignature()) {
                logString("Request is signed.");

                boolean signatureOk = false;
                if (requestorKey != null) {
                    logString("Verifying signature using supplied requestor key.");
                    try {
                        ocspRequest.verify(requestorKey);
                        signatureOk = true;
                        logString("Signature ok");

                    } catch (Exception ex) {
                    }
                }
                if (!signatureOk && ocspRequest.containsCertificates()) {
                    logString("Verifying signature with included signer cert...");

                    X509Certificate signerCert = ocspRequest.verify();
                    logString("Signature ok from request signer " + signerCert.getSubjectDN());
                    signatureOk = true;
                }
                if (!signatureOk) {
                    logString("Request signed but cannot verify signature since missing signer key. Sending malformed request!");
                    responseStatus = OCSPResponse.malformedRequest;
                }
            } else {
                logString("Unsigned request!");
            }

        } catch (IOException ex) {
            logString("Encoding error; sending malformedRequest " + ex.getMessage());
            responseStatus = OCSPResponse.malformedRequest;
        } catch (NoSuchAlgorithmException ex) {
            logString("Cannot verify; sending internalError: " + ex.getMessage());
            responseStatus = OCSPResponse.internalError;
        } catch (OCSPException ex) {
            logString("Included certs do not belong to signer; sending malformedRequest : " + ex.getMessage());
            responseStatus = OCSPResponse.malformedRequest;
        } catch (InvalidKeyException ex) {
            logString("Signer key invalid; sending malformedRequest : " + ex.getMessage());
            responseStatus = OCSPResponse.malformedRequest;
        } catch (SignatureException ex) {
            logString("Signature verification error; sending malformedRequest : " + ex.getMessage());
            responseStatus = OCSPResponse.malformedRequest;
        } catch (Exception ex) {
            ex.printStackTrace();
            logString("Some error occured during request parsing/verification; sending tryLater " + ex.getMessage());
            responseStatus = OCSPResponse.tryLater;
        }
        if (responseStatus != OCSPResponse.successful) {
            ocspResponse = new OCSPResponse(responseStatus);
            return ocspResponse.getEncoded();
        }

        try {
            // does client understand Basic OCSP response type?
            ObjectID[] accepatablResponseTypes = ocspRequest.getAccepatableResponseTypes();
            if ((accepatablResponseTypes != null) && (accepatablResponseTypes.length > 0)) {
                boolean supportsBasic = false;
                for (int i = 0; i < accepatablResponseTypes.length; i++) {
                    if (accepatablResponseTypes[i].equals(BasicOCSPResponse.responseType)) {
                        supportsBasic = true;
                        break;
                    }
                }
                if (!supportsBasic) {
                    // what to do if client does not support basic OCSP response type??
                    // we send an basic response anyway, since there seems to be no proper status message
                    logString("Warning! Client does not support basic response type. Using it anyway...");
                }
            }
        } catch (Exception ex) {
            // ignore this
        }
        // successfull
        ocspResponse = new OCSPResponse(OCSPResponse.successful);
        // now we build the basic ocsp response
        BasicOCSPResponse basicOCSPResponse = new BasicOCSPResponse();

        try {
            // responder ID
            ResponderID responderID = new ResponderID((Name) responderCerts_[0].getSubjectDN());
            basicOCSPResponse.setResponderID(responderID);

            GregorianCalendar date = new GregorianCalendar();
            // producedAt date
            Date producedAt = date.getTime();
            basicOCSPResponse.setProducedAt(producedAt);

            // thisUpdate date
            Date thisUpdate = date.getTime();
            // nextUpdate date
            date.add(Calendar.MONTH, 1);
            Date nextUpdate = date.getTime();
            // archiveCutoff
            date.add(Calendar.YEAR, -3);
            Date archivCutoffDate = date.getTime();

            // create the single responses for requests included
            Request[] requests = ocspRequest.getRequestList();
            SingleResponse[] singleResponses = new SingleResponse[requests.length];

            for (int i = 0; i < requests.length; i++) {
                Request request = requests[i];
                CertStatus certStatus = null;
                // check the service locator
                ServiceLocator serviceLocator = request.getServiceLocator();
                if (serviceLocator != null) {
                    logString("Request No. " + i + " contains the ServiceLocator extension:");
                    logString(serviceLocator + "\n");

                    Name issuer = serviceLocator.getIssuer();
                    if (!issuer.equals(responderCerts_[0].getSubjectDN())) {
                        // client does not trust our responder; but we are not able to forward it
                        // --> CertStatus unknown
                        certStatus = new CertStatus(new UnknownInfo());
                    }
                }
                if (certStatus == null) {
                    // here now the server checks the status of the cert
                    // we only can give information about one cert
                    if (request.getReqCert().isReqCertFor(targetCerts_[0],
                            targetCerts_[1],
                            null)) {
                        // we assume "good" here
                        certStatus = new CertStatus();
                    } else {
                        certStatus = new CertStatus(new UnknownInfo());
                    }
                }
                singleResponses[i] = new SingleResponse(request.getReqCert(), certStatus, thisUpdate);
                singleResponses[i].setNextUpdate(nextUpdate);

                if (includeExtensions) {
                    singleResponses[i].setArchiveCutoff(archivCutoffDate);
                    CrlID crlID = new CrlID();
                    crlID.setCrlUrl("http://www.testResponder.at/clrs/crl1.crl");
                    singleResponses[i].setCrlID(crlID);
                }

            }
            // set the single responses
            basicOCSPResponse.setSingleResponses(singleResponses);

        } catch (Exception ex) {
            ex.printStackTrace();

            logString("Some error occured; sending tryLater " + ex.getMessage());
            return (new OCSPResponse(OCSPResponse.tryLater)).getEncoded();

        }

        try {
            // Nonce included?
            Nonce nonce = (Nonce) ocspRequest.getExtension(Nonce.oid);
            if (nonce != null) {
                basicOCSPResponse.addExtension(nonce);
            }
        } catch (Exception ex) {
            // can only ignore this
            logString("Error in setting Nonce for response (ignore this): " + ex.getMessage());
        }

        // sign the response
        basicOCSPResponse.setCertificates(responderCerts_);
        try {
            basicOCSPResponse.sign(signatureAlgorithm_, responderKey_);
        } catch (Exception ex) {
            logString("Error signing response: " + ex.getMessage());
            logString("Send tryLater response");
            return (new OCSPResponse(OCSPResponse.tryLater)).getEncoded();
        }

        ocspResponse.setResponse(basicOCSPResponse);
        return ocspResponse.getEncoded();

    }

    /**
     * Parses an ocsp response received and looks for the
     * single responses included.
     *
     * @param ocspResponse the OCSP response
     * @param includeExtensions whether there have been extensions included in
     *                          the request and therefore have to be checked now
     *                         (Nonce)
     *
     * @exception OCSPException if an error occurs when creating the response
     */
    public void parseOCSPResponse(OCSPResponse ocspResponse,
            boolean includeExtensions) throws OCSPException {

        try {

            // get the response status:
            int responseStatus = ocspResponse.getResponseStatus();
            if (responseStatus != OCSPResponse.successful) {
                logString("Not successful; got response status: "
                        + ocspResponse.getResponseStatusName());
                return;
            }
            // response successful
            logString("Succesful OCSP response:");
            logString(ocspResponse.toString());
            ocspVC.setResponse(ocspResponse);

            // get the basic ocsp response (the only type we support; otherwise an
            // UnknownResponseException would have been thrown during parsing the response
            BasicOCSPResponse basicOCSPResponse =
                    (BasicOCSPResponse) ocspResponse.getResponse();

            // we verify the response
            try {
                if (basicOCSPResponse.containsCertificates()) {
                    X509Certificate signerCert = basicOCSPResponse.verify();
                    logString("Signature ok from response signer " + signerCert.getSubjectDN());

                    // trusted responder?
                    if (!signerCert.equals(targetCerts_[1])) {
                        // authorized for signing
                        ExtendedKeyUsage extendedKeyUsage =
                                (ExtendedKeyUsage) signerCert.getExtension(ExtendedKeyUsage.oid);

                        boolean ocspSigning = (extendedKeyUsage != null && extendedKeyUsage.contains(ExtendedKeyUsage.ocspSigning) ? true : false);

                        if (!trustedResponderKeys.contains(signerCert.getPublicKey())) {
                            if (validateSignerCert(basicOCSPResponse.getCertificates())) {
                                logString("Responder certificate chains to trusted root and is not revoked");
                            } else {
                                if (ocspSigning) {
                                    if (signerCert.getIssuerDN().equals(targetCerts_[1].getSubjectDN())) {
                                        logString("WARNING: Responder authorized by target cert issuer, but no trust information available!");
                                        ocspVC.setOcspCheckOK(false);
                                    } else {
                                        logString("WARNING: Responder not trusted! Reject response!!!");
                                        logString("SignCert Issuer: " + signerCert.getIssuerDN().getName());
                                        ocspVC.setOcspCheckOK(false);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    logString("Certificates not included; try to verify with issuer target cert...");
                    basicOCSPResponse.verify(targetCerts_[1].getPublicKey());
                    logString("Signature ok!");
                }
            } catch (SignatureException ex) {
                logString("Signature verification error!!!");
                ocspVC.setOcspCheckOK(false);
            }

            logString("Response produced at :" + basicOCSPResponse.getProducedAt());

            ResponderID responderID = basicOCSPResponse.getResponderID();
            logString("ResponderID: " + responderID);

            // look if we got an answer for our request:
            SingleResponse singleResponse = null;
            try {
                singleResponse = basicOCSPResponse.getSingleResponse(reqCert_);
            } catch (OCSPException ex) {
                logString(ex.getMessage());
                logString("Try again...");
                singleResponse = basicOCSPResponse.getSingleResponse(
                        targetCerts_[0],
                        targetCerts_[1],
                        null);
            }

            if (singleResponse != null) {
                logString("Status information got for cert: ");
                logString(singleResponse.getCertStatus().toString());
                if (singleResponse.getCertStatus().getCertStatus() != CertStatus.GOOD) {
                    ocspVC.setOcspCheckOK(false);
                }
                if (singleResponse.getCertStatus().getCertStatus() == CertStatus.REVOKED) {
                    ocspVC.setOcspRevoked(true);
                }

                logString("This Update: " + singleResponse.getThisUpdate());
                Date now = new Date();
                // next update included?
                Date nextUpdate = singleResponse.getNextUpdate();
                if (nextUpdate != null) {
                    logString("Next Update: " + nextUpdate);
                    if (nextUpdate.before(now)) {
                        logString("WARNING: There must be more recent information available!");
                        ocspVC.setOcspCheckOK(false);
                    }
                }
                // check thisUpdate date
                Date thisUpdate = singleResponse.getThisUpdate();
                if (thisUpdate == null) {
                    logString("Error: Missing thisUpdate information!");
                    ocspVC.setOcspCheckOK(false);
                } else {
                    if (thisUpdate.after(now)) {
                        logString("WARNING: Response yet not valid! thisUpdate (" + thisUpdate + ") is somewhere in future (current date is: " + now + ")!");
                        ocspVC.setOcspCheckOK(false);
                    }
                }
                // archive cutoff included?
                Date archiveCutoffDate = singleResponse.getArchiveCutoff();
                if (archiveCutoffDate != null) {
                    logString("archivCutoffDate: " + archiveCutoffDate);
                }
                // crl id included?
                CrlID crlID = singleResponse.getCrlID();
                if (crlID != null) {
                    logString("crlID: " + crlID);
                }
            } else {
                logString("No response got for our request!");
                ocspVC.setOcspCheckOK(false);
            }

            // nonce check
            byte[] respondedNonce = basicOCSPResponse.getNonce();
            if (respondedNonce != null) {
                if (!CryptoUtils.secureEqualsBlock(nonce_, respondedNonce)) {
                    logString("Error!!! Nonce values do not match!");
                    ocspVC.setOcspCheckOK(false);
                }
            } else {
                if ((includeExtensions == true) && (nonce_ != null)) {
                    logString("Error!!! Nonce not returned in server response!");
                    ocspVC.setOcspCheckOK(false);
                }
            }

        } catch (UnknownResponseException ex) {
            ocspVC.setOcspCheckOK(false);
            logString("This response is successful but contains an unknown response type:");
            UnknownResponseException unknown = (UnknownResponseException) ex;
            logString("Unknown type: " + unknown.getResponseType());
            logString("ASN.1 structure:");
            logString(unknown.getUnknownResponse().toString());
        } catch (NoSuchAlgorithmException ex) {
            ocspVC.setOcspCheckOK(false);
            logException("Error while verifying signature: " + ex.getMessage());
        } catch (InvalidKeyException ex) {
            ocspVC.setOcspCheckOK(false);
            logException("Error while verifying signature: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            ocspVC.setOcspCheckOK(false);
            logException(ex.getMessage());
        }
    }

    /**
     * Validate the OCSP signing certificate chain according to local policy.
     * This local policy is to trust any OCSP responder that chains to a trusted 
     * service in the list of cross certified TSL trust services.
     * @param signerCerts the OCSP response signature certificate chain starting with
     * the responder certificate as index 0.
     * @return true if the validation is successful, otherwise false.
     */
    private boolean validateSignerCert(X509Certificate[] signerCerts) {
        logString("Chainvalidation of OCSP Response with response cert chain:");
        CertChainVerifier ccv = new CertChainVerifier(rootCert, caCertList, crlCache);
        ccv.setNoOCSP(true);
        List<X509Certificate> chain = new ArrayList<X509Certificate>();
        for (X509Certificate cert : signerCerts) {
            chain.add(cert);
            logString(cert.getSubjectDN().getName());
            verboseLogString(cert.toString(false) + "\n");
        }
        CertVerifyContext cvContext = ccv.verifyChain(chain);
        ocspVC.setCertVerifyContxt(cvContext);
        if (cvContext.isChainsToRoot() && cvContext.isCrlStatusDetermined() && !cvContext.isRevoked()) {
            if (cvContext.isNoCheck()) {
                logString("Responder certificate has NoCheck extension\n");
            }
            return true;
        }
        logString("OCSP Responder certificate not verifiable - OCSP check failed");
        ocspVC.setOcspCheckOK(false);
        return false;
    }
}
