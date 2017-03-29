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

import iaik.asn1.ObjectID;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.qualified.QCStatements;
import iaik.x509.extensions.qualified.structures.QCStatement;
import iaik.x509.extensions.qualified.structures.etsi.QcEuCompliance;
import iaik.x509.extensions.qualified.structures.etsi.QcEuSSCD;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3.x2000.x09.xmldsig.SignedInfoType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import se.tillvaxtverket.tsltrust.common.utils.general.Algorithms;
import se.tillvaxtverket.tsltrust.common.utils.general.KsCertFactory;
import se.tillvaxtverket.tsltrust.common.xmldsig.SigVerifyResult;
import se.tillvaxtverket.tsltrust.common.xmldsig.XMLSign;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.SignatureValidationContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.TimeStampContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.statusCheck.CRLChecker;

/**
 * Verifier of XML signed documents
 */
public class XmlSigVerifier extends SigVerifier {

    public XmlSigVerifier(SigValidationModel sigModel) {
        this.model = sigModel;
    }

    @Override
    protected void getSignatureContext() {

        SigValidationBaseModel basemodel = model.getBaseModel();
        CRLChecker crlCache = basemodel.getCrlCache();
        KeyStore keyStore = basemodel.getTrustStore().getKeyStore(model.getPolicyName());
        boolean checkOcspAndCrl = model.isCheckOcspAndCrl();
        boolean prefOcsp = model.isPrefSpeed();

        crlCache.clearLog();
        List<SignatureValidationContext> svcList = new ArrayList<SignatureValidationContext>();
        CertChainVerifier certChainVerifier = new CertChainVerifier(model);
        if (!certChainVerifier.isInitialized()) {
            errClose();
            return;
        }
        certChainVerifier.setCheckAllRev(checkOcspAndCrl);
        certChainVerifier.setPreferSpeed(prefOcsp);
        byte[] signedData = model.getSigDocument().getDocBytes();
        Document signedDoc = null;
        try {
            signedDoc = XMLSign.getDoc(signedData);
        } catch (Exception ex) {
            Logger.getLogger(XmlSigVerifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (signedDoc == null) {
            errClose();
            model.setSignVerificationComplete(false);
        }

        SigVerifyResult sigResults = XMLSign.verifySignature(signedDoc);
        List<SigVerifyResult.IndivdualSignatureResult> resultList = sigResults.resultList;
        int sigIndex = 0;
        for (SigVerifyResult.IndivdualSignatureResult result : resultList) {
            SignatureValidationContext svc = new SignatureValidationContext();
            svc.setSignatureName(getSigName(sigIndex++));
            svc.setCoversDoc(true);
            svc.setRevision(-1);
            svc.setRevisions(-1);

            String sigAlgOid = getSigAlgOid(result.thisSignatureNode);
            if (sigAlgOid == null) {
                svc.setSigValidationError(new String[]{"Unknown algorithm"});
            }

            svc.setSignaturePkAlgOID(null);
            svc.setSignatureAlgOID(new ASN1ObjectIdentifier(sigAlgOid));
            svc.setTimestamped(false);
            TimeStampContext tsCont = null;
            svc.setTstContext(tsCont);
            try {
                svc.setProvidedChain(getCertList(result));
//                svc.setProvidedChain(KsCertFactory.getIaikCertList(verifySignature.keyInfo.getCertificates()));
                svc.setSignCert(KsCertFactory.getIaikCert(result.thisCert));

                // modified code
                boolean signatureValid = result.thisValid;
                svc.setSigValid(signatureValid);
                svc.setDigestValid(signatureValid);

                //if (svc.isSigValid()) {
                svc.setSignCertValidation(certChainVerifier.verifyChain(svc.getProvidedChain()));
                // Require that chain validation succeeds.
                svc.setSigChainVerified(svc.getSignCertValidation().isSigChainVerified());

            } catch (Exception ex) {
                svc.setDigestValid(false);
            }
            //QC compliance test
            qcComplianceTest(svc);
            //Store result and move on to next signature
            svcList.add(svc);
        }

        model.setSignVerificationComplete(true);
        model.setSignatureContexts(svcList);
        setChanged();
        if (running) {
            notifyObservers(COMPLETE);
        } else {
            notifyObservers(RETURN_FROM_ABORT);
        }
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

    private String getSigName(int sigIndex) {
        return textBundle.getString("signature") + " " + String.valueOf(sigIndex + 1);
    }

    private String getSigAlgOid(Node sigNode) {
        String sigAlgOid;
        try {
            SignatureType sig = SignatureDocument.Factory.parse(sigNode).getSignature();
            SignedInfoType signedInfo = sig.getSignedInfo();
            String algorithm = signedInfo.getSignatureMethod().getAlgorithm();
            sigAlgOid = Algorithms.xmlAlgIds.get(algorithm);
        } catch (Exception ex) {
            return null;
        }
        return sigAlgOid;
    }

    private List<X509Certificate> getCertList(SigVerifyResult.IndivdualSignatureResult result) {
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        List<byte[]> certByteList = result.getCertList();
        for (byte[] certBytes : certByteList) {
            X509Certificate iaikCert = KsCertFactory.getIaikCert(certBytes);
            if (iaikCert != null) {
                certList.add(iaikCert);
            }
        }
        return certList;
    }
}