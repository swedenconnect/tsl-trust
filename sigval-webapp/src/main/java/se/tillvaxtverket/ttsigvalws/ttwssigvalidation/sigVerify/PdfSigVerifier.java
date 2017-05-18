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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify;

import iaik.x509.X509Certificate;
import iaik.x509.extensions.qualified.QCStatements;
import iaik.x509.extensions.qualified.structures.QCStatement;
import iaik.x509.extensions.qualified.structures.etsi.QcEuCompliance;
import iaik.x509.extensions.qualified.structures.etsi.QcEuSSCD;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import se.tillvaxtverket.tsltrust.common.iaik.KsCertFactory;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.SigDocument;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf.CMSSigVerifyResult;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf.PdfSigVerifyResult;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf.PdfSignatureVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf.TimeStampData;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.pdf.TimeStampResult;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.SignatureValidationContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.TimeStampContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck.CRLChecker;

/**
 * Signature verifier class for verification of signed PDF documents
 */
public class PdfSigVerifier extends SigVerifier {

    public PdfSigVerifier(SigValidationModel sigModel) {
        this.model = sigModel;
    }

    @Override
    protected void getSignatureContext() {

        SigValidationBaseModel basemodel = model.getBaseModel();
        SigDocument sigDocument = model.getSigDocument();
        try {
            PdfSigVerifyResult sigResult = PdfSignatureVerifier.verifyPdfSignatures(sigDocument, true);
            CRLChecker crlCache = basemodel.getCrlCache();
            KeyStore keyStore = basemodel.getTrustStore().getKeyStore(model.getPolicyName());
            boolean checkOcspAndCrl = model.isCheckOcspAndCrl();
            boolean prefOcsp = model.isPrefSpeed();

            crlCache.clearLog();
            List<SignatureValidationContext> svcList = new ArrayList<SignatureValidationContext>();
            CertChainVerifier certChainVerifier = new CertChainVerifier(model);
            if (!certChainVerifier.isInitialized()) {
                return;
            }
            certChainVerifier.setCheckAllRev(checkOcspAndCrl);
            certChainVerifier.setPreferSpeed(prefOcsp);

            List<CMSSigVerifyResult> resultList = sigResult.getResultList();
            int sigIndex = 1;
            for (CMSSigVerifyResult sig : resultList) {
                SignatureValidationContext svc = new SignatureValidationContext();
                svc.setSignatureName("Signature " + String.valueOf(sigIndex));
                svc.setCoversDoc(true);
                svc.setRevision(0);
                svc.setRevisions(0);

                svc.setSignaturePkAlgOID(sig.getSigAlgo().getSigAlgoOid());
                svc.setSignatureHashAlgOID(new ASN1ObjectIdentifier(sig.getSigAlgo().getDigestAlgo().getOid()));
                if (sig.getClaimedSigningTime() != null) {
                    Calendar claimedSigTime = Calendar.getInstance();
                    claimedSigTime.setTime(sig.getClaimedSigningTime());
                    svc.setSignDate(claimedSigTime);
                }
                svc.setTimestamped(sig.isTimeStamped());
                TimeStampContext tsCont = (svc.isTimestamped() ? new TimeStampContext() : null);
                svc.setTstContext(tsCont);
                try {
                    List<X509Certificate> unorderedpdfSignCerts = KsCertFactory.getIaikCertList(sig.getCertList());
                    X509Certificate signerCert = KsCertFactory.getIaikCert(sig.getCert().getEncoded());
                    List<X509Certificate> pdfSignCerts = KsCertFactory.getOrderedCertList(unorderedpdfSignCerts, signerCert);
                    svc.setProvidedChain(pdfSignCerts);
                    svc.setSignCert(KsCertFactory.getIaikCert(sig.getCert().getEncoded()));

                    // modified code
                    boolean signatureValid = sig.isValid();
                    svc.setSigValid(signatureValid);
                    svc.setDigestValid(signatureValid);

                    svc.setSignCertValidation(certChainVerifier.verifyChain(pdfSignCerts));
                    // Require that chain validation succeeds.
                    svc.setSigChainVerified(svc.getSignCertValidation().isSigChainVerified());

                    if (sig.isTimeStamped()) {
                        checkTimestamp(svc, sig, certChainVerifier);
                    }

                } catch (Exception ex) {
                    svc.setDigestValid(false);
                }
                //QC compliance test
                qcComplianceTest(svc);
                //Store result and move on to next signature
                svcList.add(svc);

                sigIndex++;
            }
            model.setSignatureContexts(svcList);
        } catch (IOException ex) {
            Logger.getLogger(PdfSigVerifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        model.setSignVerificationComplete(true);
        setChanged();
        if (running) {
            notifyObservers(COMPLETE);
        } else {
            notifyObservers(RETURN_FROM_ABORT);
        }
    }

    private void checkTimestamp(SignatureValidationContext svc, CMSSigVerifyResult sigResult, CertChainVerifier certChainVerifier) {
        TimeStampContext tsCont = svc.getTstContext();
        List<TimeStampResult> validTimeStamps = sigResult.getValidTimeStamps();
        if (validTimeStamps.isEmpty()) {
            tsCont.setTimeStampDate(null);
            tsCont.setTsSignValidated(false);
            return;
        }
        TimeStampResult tsResult = validTimeStamps.get(0);
        TimeStampData tsData = tsResult.getTsData();
        Calendar tsTime = Calendar.getInstance();
        tsTime.setTime(tsData.getTime());

        tsCont.setTimeStampDate(tsTime);

        CMSSigVerifyResult signatureVerification = tsResult.getSignatureVerification();
        tsCont.setTsSignValidated(signatureVerification.isValid());
        tsCont.setMessageImprintValidated(tsResult.isTimestampMatch());
        try {
            List<X509Certificate> unorderedCertList = KsCertFactory.getIaikCertList(signatureVerification.getCertList());
            X509Certificate signerCert = KsCertFactory.getIaikCert(signatureVerification.getCert().getEncoded());
            List<X509Certificate> certList = KsCertFactory.getOrderedCertList(unorderedCertList, signerCert);
            tsCont.setCertVerifyContext(certChainVerifier.verifyChain(certList));
        } catch (Exception ex) {
            tsCont.setTsSignValidated(false);
        }
        // Verify CRL
    }

    private void qcComplianceTest(SignatureValidationContext svc) {
        svc.setQualifiedCertificate(false);
        svc.setSscd(false);
        try {
            X509Certificate cert = svc.getSignCertValidation().getChain().get(0);
            QCStatements qcsExtension = (QCStatements) cert.getExtension(QCStatements.oid);
            QCStatement[] statements = qcsExtension.getQCStatements();
            for (QCStatement qcs : statements) {
                if (qcs.getStatementID().equals(QcEuCompliance.statementID)) {
                    svc.setQualifiedCertificate(true);
                }
                if (qcs.getStatementID().equals(QcEuSSCD.statementID)) {
                    svc.setSscd(true);
                }
            }
        } catch (Exception ex) {
        }
    }

}
