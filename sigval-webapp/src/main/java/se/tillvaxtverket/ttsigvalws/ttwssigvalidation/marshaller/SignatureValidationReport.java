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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AccessDescription;
import iaik.asn1.structures.DistributionPoint;
import iaik.asn1.structures.GeneralName;
import iaik.asn1.structures.GeneralNames;
import iaik.asn1.structures.PolicyInformation;
import iaik.x509.V3Extension;
import iaik.x509.X509Certificate;
import iaik.x509.X509ExtensionInitException;
import iaik.x509.X509Extensions;
import iaik.x509.extensions.AuthorityInfoAccess;
import iaik.x509.extensions.BasicConstraints;
import iaik.x509.extensions.CRLDistributionPoints;
import iaik.x509.extensions.CertificatePolicies;
import iaik.x509.extensions.ExtendedKeyUsage;
import iaik.x509.extensions.KeyUsage;
import iaik.x509.extensions.SubjectAltName;
import iaik.x509.extensions.qualified.QCStatements;
import iaik.x509.extensions.qualified.structures.QCStatement;
import iaik.x509.extensions.qualified.structures.QCStatementInfo;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import se.elegnamnden.id.authCont.x10.saci.AttributeMappingType;
import se.elegnamnden.id.authCont.x10.saci.AuthContextInfoType;
import se.elegnamnden.id.authCont.x10.saci.SAMLAuthContextDocument;
import se.elegnamnden.id.authCont.x10.saci.SAMLAuthContextType;
import se.tillvaxtverket.tsltrust.common.iaik.AuthContextExtension;
import se.tillvaxtverket.tsltrust.common.iaik.AuthContextQCStatement;
import se.tillvaxtverket.tsltrust.common.iaik.PdsQCStatement;
import se.tillvaxtverket.tsltrust.common.iaik.SamlAssertionInfo;
import se.tillvaxtverket.tsltrust.common.utils.general.GeneralStaticUtils;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.TreeUtil;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.CertVerifyContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.OCSPVerifyContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.SignatureValidationContext;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.TimeStampContext;
import x0Assertion.oasisNamesTcSAML2.AttributeType;
import x0SigvalReport.seTillvaxtverketTsltrust1.AttributeValueType;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertStatusMethod;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertStatusValue;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertificateExtensionType;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertificateExtensionsType;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertificateInformationType;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertificateStatusType;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertificateType;
import x0SigvalReport.seTillvaxtverketTsltrust1.CertificateValidationSourceType;
import x0SigvalReport.seTillvaxtverketTsltrust1.DistinguishedNameType;
import x0SigvalReport.seTillvaxtverketTsltrust1.ExtensionParameterType;
import x0SigvalReport.seTillvaxtverketTsltrust1.OIDAlgorithmType;
import x0SigvalReport.seTillvaxtverketTsltrust1.OIDAlgorithmsType;
import x0SigvalReport.seTillvaxtverketTsltrust1.PublicKeyType;
import x0SigvalReport.seTillvaxtverketTsltrust1.SignatureResult;
import x0SigvalReport.seTillvaxtverketTsltrust1.SignatureValidationType;
import x0SigvalReport.seTillvaxtverketTsltrust1.SignedDocumentValidationType;
import x0SigvalReport.seTillvaxtverketTsltrust1.StatusMessagesType;
import x0SigvalReport.seTillvaxtverketTsltrust1.TimeStampInformationType;

/**
 * This class provides functions for generating the XML signature validation
 * report
 */
public class SignatureValidationReport implements SigreportConstants {

    SigValidationModel model;
    private static final Logger LOG = Logger.getLogger(SignatureValidationReport.class.getName());
    private SigReportGenerator reportGenerator;
    private SignedDocumentValidationType sigReport;
    private ResourceBundle text = ResourceBundle.getBundle("reportText");

    /**
     * Constructs a signature validation report generator, generating signature
     * validation reports from a signature validation model.
     *
     * @param model The model data used as basis of generating the signature
     * validation report
     */
    public SignatureValidationReport(SigValidationModel model) {
        this.model = model;
        //Register private QCStatements
        QCStatement.register(PdsQCStatement.statementID, PdsQCStatement.class);
        QCStatement.register(AuthContextQCStatement.statementID, AuthContextQCStatement.class);
        X509Extensions.register(AuthContextExtension.extensionOid, AuthContextExtension.class);
    }

    /**
     * @return XML Signature validation report
     */
    public String generateReport() {
        reportGenerator = new SigReportGenerator();
        sigReport = reportGenerator.getSigReport();

        setInitial();
        parseValidationContext();

        return reportGenerator.getValidationReport();
    }

    private void setInitial() {
        sigReport.setVersion(sigReport.getVersion());     //use default version
        sigReport.setLanguage(Locale.getDefault().getLanguage());
        sigReport.setDocumentType(model.getSigDocument().getDocType().name());
        sigReport.setDocumentName(model.getSigDocument().getDocName());
        sigReport.setPolicyName(model.getPolicyName());
        sigReport.setPolicyInformation(model.getPolicyDescription());
    }

    /**
     * Process the signature validation context objects to generate the XML
     * report
     */
    private void parseValidationContext() {
        List<SignatureValidationContext> sigSvcList = model.getSignatureContexts();

        if (sigSvcList.isEmpty()) {
            sigReport.setVerificationConclusion(text.getString("notSigned"));
            return;
        }

        //Traverse each signature
        for (SignatureValidationContext sc : sigSvcList) {
            //initialize
            boolean valid = true;
            boolean errorMessageSet = false;
            SignatureValidationType svt = sigReport.addNewSignatureValidation();

            CertVerifyContext cc = sc.getSignCertValidation();
            TimeStampContext tc = (sc.isTimestamped()) ? sc.getTstContext() : null;
            OCSPVerifyContext oc = cc.getOcspVerifyContext();
            StatusMessagesType sigStatus = svt.addNewValidationErrorMessages();

            //Sets the name of the signature
            svt.setSignatureName(sc.getSignatureName());

            boolean signatureCheck = sc.isSigChainVerified() && sc.isDigestValid() && sc.isSigValid();
            if (signatureCheck) {
            } else {
                if (!sc.isDigestValid()) {
                    addStatus(sigStatus, text.getString("modified"), ERROR);
                    errorMessageSet = true;
                }
                if (!sc.isSigChainVerified()) {
                    String errorMess;
                    if (cc.isChainVerifyError()) {
                        String pvem = cc.getChainVerifyErrorMessage();
                        if (pvem != null && pvem.length() > 0 && !pvem.equalsIgnoreCase("null")) {
                            errorMess = text.getString("pathErrorWithMess") + " " + pvem;
                        } else {
                            errorMess = text.getString("pathErrorNoMess");
                        }
                    } else {
                        errorMess = text.getString("invalidCertificate");
                    }
                    addStatus(sigStatus, errorMess, ERROR);
                    errorMessageSet = true;
                }
                valid = false;
            }
            if (!sc.isCoversDoc()) {
                addStatus(sigStatus, text.getString("partOfDoc"), WARNING);
            }
            // Sets signature algoritm information
            OIDAlgorithmsType signatureAlgs = svt.addNewSignatureAlgorithms();
            List<ASN1ObjectIdentifier> algOidList = getAlgOids(sc);
            for (ASN1ObjectIdentifier oid : algOidList) {
                OIDAlgorithmType sigAlg = signatureAlgs.addNewAlgorithm();
                sigAlg.setOID(oid.getId());
                sigAlg.setStringValue(new ObjectID(oid.getId()).getName());
            }

            //Determine revocation status
            boolean crlValid = cc.isCrlStatusDetermined() && !cc.isRevoked()
                    && !cc.isExpired() && !cc.isNotValidYet();
            boolean crlRevoked = cc.isCrlStatusDetermined() && cc.isRevoked();
            boolean ocspValid = oc == null ? false : oc.isOcspCheckOK();
            boolean ocspRevoked = oc == null ? false : oc.isOcspRevoked();

            if (crlValid || ocspValid) {
                svt.setSignerStatusCheck(CertStatusValue.VALID);
            } else {
                svt.setSignerStatusCheck(CertStatusValue.UNDETERMINED);
                addStatus(sigStatus, text.getString("undeterminedStatus"), ERROR);
                errorMessageSet = true;
                if (cc.isExpired() || cc.isNotValidYet()) {
                    if (cc.isExpired()) {
                        addStatus(sigStatus, text.getString("expiredStatus"), ERROR);
                        errorMessageSet = true;
                    } else {
                        addStatus(sigStatus, text.getString("notYetValidStatus"), ERROR);
                        errorMessageSet = true;
                    }
                } else {
                    if (crlRevoked || ocspRevoked) {
                        svt.setSignerStatusCheck(CertStatusValue.REVOKED);
                        addStatus(sigStatus, text.getString("revokedStatus"), ERROR);
                        errorMessageSet = true;
                    }
                }
                valid = false;
            }
            // Conclude error messages. Add one generic error message if no other has been added
            if (!valid && !errorMessageSet) {
                addStatus(sigStatus, text.getString("invalidSignature"), ERROR);
            }

            //Set the claimed signing time
            if (sc.getSignDate() != null) {
                svt.setClaimedSigningTime(sc.getSignDate());
            }

            //Set timestamp info
            if (sc.isTimestamped()) {
                TimeStampInformationType tstInfo = svt.addNewTimeStamp();
                StatusMessagesType tstStatus = tstInfo.addNewStatusMessages();

                try {
                    //Set time stamp time
                    tstInfo.setTime(tc.getTimeStampDate());

                    boolean tsSig = tc.isTsSignValidated();
                    boolean tsdoc = tc.isMessageImprintValidated();
                    CertVerifyContext tscc = tc.getCertVerifyContext();
                    boolean tsCertErr = tscc.isChainVerifyError();
                    String tsCertErrMess = tscc.getChainVerifyErrorMessage();
                    boolean tsCrl = (tscc.isCrlStatusDetermined() && !tscc.isRevoked());
                    OCSPVerifyContext ocspCont = tscc.getOcspVerifyContext();
                    boolean tsOcsp = false;
                    if (ocspCont != null) {
                        tsOcsp = ocspCont.isOcspCheckOK();
                    }

                    if (!(tsSig && tsdoc && (tsCrl || tsOcsp))) {
                        addStatus(sigStatus, text.getString("tstFail"), WARNING);
                        addStatus(tstStatus, text.getString("tstFail"), ERROR);
                        if (tsCertErr) {
                            addStatus(tstStatus, (tsCertErrMess != null && tsCertErrMess.length() > 0)
                                    ? text.getString("pathErrorWithMess") + tc.getCertVerifyContext().getChainVerifyErrorMessage()
                                    : text.getString("pathErrorNoMess"), ERROR);
                        }
                        addStatus(tstStatus, !tsSig ? text.getString("tstInvalidSignature")
                                : !(tsCrl || tsOcsp) ? text.getString("tsaNotTrusted")
                                : text.getString("tstDocMismatch"), ERROR);
                        tstInfo.setStatusMessages(tstStatus);
                    }
                } catch (NullPointerException ex) {
                    addStatus(tstStatus, text.getString("tstInsufficient"), ERROR);
                    tstInfo.setStatusMessages(tstStatus);
                }

                //If no status message was generated, delete the status messages element
                if (tstStatus.getMessageArray().length == 0) {
                    tstInfo.unsetStatusMessages();
                }
            }

            //QC and SSCD properties
            if (sc.isQualifiedCertificate() && sc.isSscd()) {
                svt.setEuQualifications(text.getString("QES"));
            } else {
                if (sc.isQualifiedCertificate()) {
                    svt.setEuQualifications(text.getString("AdES/QC"));
                } else {
                    svt.setEuQualifications(text.getString("noQC/sscd"));
                }
            }
            //Signer DN Attributes
            DistinguishedNameType subjectDN = svt.addNewSignerDistinguishedName();

            X509Certificate sigCert;
            try {
                sigCert = sc.getSignCert();
                Iterator<Entry<ObjectID, String>> rdns = TreeUtil.getCertNameAttributeSet(sigCert).iterator();
                while (rdns.hasNext()) {
                    Entry<ObjectID, String> entry = rdns.next();
                    AttributeValueType attr = subjectDN.addNewAttributeValue();
                    attr.setType(entry.getKey().getName());
                    attr.setStringValue(entry.getValue());
                }
                svt.setSignerDistinguishedName(subjectDN);
            } catch (Exception ex) {
                addStatus(sigStatus, text.getString("invalidSignCert"), ERROR);
            }

            // Set signature validation conclusion
            if (valid) {
                svt.setValidationResult(SignatureResult.VALID);
            } else {
                svt.setValidationResult(SignatureResult.INVALID);
            }

            //Remove error element if any errors were recorded.
            if (sigStatus.getMessageArray().length == 0) {
                svt.unsetValidationErrorMessages();
            }

            //Add signer certificate information
            addCertificateInfo(svt, cc);
//            if (signerCertInfo != null) {
//                svt.setSignerCertificateInfo(signerCertInfo);
//            }
        }
    }

    private void addStatus(StatusMessagesType sigStatus, String message, String level) {
        AttributeValueType error = sigStatus.addNewMessage();
        error.setType(level);
        error.setStringValue(message);
    }

    private XMLGregorianCalendar getXMLDateTime(long timeInMillis) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeInMillis);
        return new XMLGregorianCalendarImpl(cal);
    }

    /**
     * Adding signer certificate information to the signature validation report
     *
     * @param certParent The object holding the data for the signature validation
     * report
     * @param cc The verification context
     */
    private void addCertificateInfo(Object certParent, CertVerifyContext cc) {
        //Make sure there is a certificate to parse
        X509Certificate cert;
        try {
            cert = cc.getChain().get(0);
        } catch (Exception ex) {
            return;
        }

        //Create cert object
        CertificateInformationType certInfo = null;
        if (certParent instanceof SignatureValidationType) {
            certInfo = ((SignatureValidationType) certParent).addNewSignerCertificateInfo();
        }
        if (certParent instanceof CertificateInformationType) {
            certInfo = ((CertificateInformationType) certParent).addNewIssuerCertificateInfo();
        }
        if (certInfo == null) {
            return;
        }

        CertificateType certType = certInfo.addNewCertificate();

        //Add Subject and Issuer Names
        addDistinguishedName(certType.addNewSubjectName(), cert.getSubjectX500Principal());
        addDistinguishedName(certType.addNewIssuerName(), cert.getIssuerX500Principal());

        //Add Validity period
        certType.setNotValidBefore(GeneralStaticUtils.getTime(cert.getNotBefore()));
        certType.setNotValidAfter(GeneralStaticUtils.getTime(cert.getNotAfter()));

        //AddStatusInfo
        addCertStatusInfo(certType.addNewCertificateStatus(), cert, cc);

        //Add PublicKeyInfo
        addPublicKeyInfo(certType.addNewPublicKeyAlgorithm(), cert);
//        certType.setPublicKeyAlgorithm(addPublicKeyInfo(cert));

        //Add ExtensionInfo
        addCertificateExtensionInfo(certType, cert);

        //Add Issuer certificate infor
        CertVerifyContext issuerCont = cc.getIssuingCertContext();
        if (issuerCont != null) {
            addCertificateInfo(certInfo, issuerCont);
        }
    }

    public void addDistinguishedName(DistinguishedNameType subjectDN, X500Principal dn) {
        //Signer DN Attributes
//        List<AttributeValueType> dnAttributes = subjectDN.getAttributeValue();

        Iterator<Entry<ObjectID, String>> rdns = TreeUtil.getCertNameAttributeSet(dn).iterator();
        while (rdns.hasNext()) {
            Entry<ObjectID, String> entry = rdns.next();
            AttributeValueType attr = subjectDN.addNewAttributeValue();
            attr.setType(entry.getKey().getName());
            attr.setStringValue(entry.getValue());
        }
    }

    private void addCertStatusInfo(CertificateStatusType certStatus, X509Certificate cert, CertVerifyContext cc) {
//        List<CertificateValidationSourceType> validationSource = certStatus.getValidationSource();
        OCSPVerifyContext oc = cc.getOcspVerifyContext();

        boolean crlValid = cc.isCrlStatusDetermined() && !cc.isRevoked()
                && !cc.isExpired() && !cc.isNotValidYet();
        boolean crlRevoked = cc.isCrlStatusDetermined() && cc.isRevoked();
        boolean ocspValid = oc == null ? false : oc.isOcspCheckOK();
        boolean ocspRevoked = oc == null ? false : oc.isOcspRevoked();

        if (crlValid || ocspValid) {
            certStatus.setValidityStatus(CertStatusValue.VALID);
        } else {
            if (crlRevoked || ocspRevoked) {
                certStatus.setValidityStatus(CertStatusValue.REVOKED);
            } else {
                certStatus.setValidityStatus(CertStatusValue.UNDETERMINED);
            }
        }


        if (cc.isCrlStatusDetermined()) {
            //check for crlDPS
            try {
                CRLDistributionPoints cdp = (CRLDistributionPoints) cert.getExtension(CRLDistributionPoints.oid);
                if (cdp != null) {

                    Enumeration dPoints = cdp.getDistributionPoints();

                    //For every distribution point
                    while (dPoints.hasMoreElements()) {
                        DistributionPoint dp = (DistributionPoint) dPoints.nextElement();
                        String[] uris = dp.getDistributionPointNameURIs();

                        //For every URI
                        for (String uri : uris) {
                            if (uri.toLowerCase().startsWith("http")) {
                                CertificateValidationSourceType source = certStatus.addNewValidationSource();
                                source.setType(CertStatusMethod.CRL);
                                source.setStringValue(uri);
                            }
                        }
                    }
                }

            } catch (X509ExtensionInitException ex) {
            }

        }

        if (ocspValid || ocspRevoked) {
            try {
                AuthorityInfoAccess aia = (AuthorityInfoAccess) cert.getExtension(AuthorityInfoAccess.oid);
                AccessDescription aDesc = aia.getAccessDescription(ObjectID.ocsp);
                String ocspURI = aDesc.getUriAccessLocation();
                if (ocspURI.length() > 0) {
                    CertificateValidationSourceType source = certStatus.addNewValidationSource();
                    source.setType(CertStatusMethod.OCSP);
                    source.setStringValue(ocspURI);
                }
            } catch (Exception ex) {
            }
        }
    }

    private void addPublicKeyInfo(PublicKeyType pkInfo, X509Certificate cert) {
        PublicKey publicKey = cert.getPublicKey();
        pkInfo.setStringValue(publicKey.getAlgorithm());
        try {
            ASN1 pkasn1 = new ASN1(publicKey.getEncoded());
            ASN1Object algID = pkasn1.getComponentAt(0);
            ASN1Object pk = pkasn1.getComponentAt(1);
            ASN1Object algOID = algID.getComponentAt(0);
            ObjectID oid = new ObjectID((String) algOID.getValue());
            int keydatalen = ((byte[]) pk.getValue()).length;
            int keyBits = (keydatalen - 12) * 8;
            if (keydatalen > 127 && keydatalen < 150) {
                keyBits = 1024;
            }
            if (keydatalen > 255 && keydatalen < 280) {
                keyBits = 2048;
            }
            if (keydatalen > 511 && keydatalen < 540) {
                keyBits = 4096;
            }
            pkInfo.setOID(oid.getID());
            pkInfo.setKeyLength(keyBits);
            pkInfo.setStringValue(oid.getName());
        } catch (CodingException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }

    private void addCertificateExtensionInfo(CertificateType certType, X509Certificate cert) {
        Enumeration<V3Extension> e = cert.listExtensions();
        if (e == null) {
            return;
        }
        CertificateExtensionsType extensions = certType.addNewCertificateExtensions();

        List<V3Extension> extList = new ArrayList<V3Extension>();
        while (e.hasMoreElements()) {
            extList.add(e.nextElement());
        }

        for (V3Extension rawExt : extList) {

            //Basic Constraints
            if (rawExt.getObjectID().equals(BasicConstraints.oid)) {
                BasicConstraints bc = (BasicConstraints) rawExt;
                CertificateExtensionType xmlext = extensions.addNewCertificateExtension();
                addExtNameAndOID(xmlext, rawExt);
                // set property
                ExtensionParameterType param = xmlext.addNewParameter();
                param.setType("cA");
                param.setStringValue(String.valueOf(bc.ca()));
            }

            //Key Usage
            if (rawExt.getObjectID().equals(KeyUsage.oid)) {
                KeyUsage ku = (KeyUsage) rawExt;
                CertificateExtensionType xmlext = extensions.addNewCertificateExtension();
                addExtNameAndOID(xmlext, rawExt);
                int i = 0;
                String[] label = new String[]{"digitalSignature", "nonRepudiation", "keyEncipherment", "dataEncipherment", "keyAgreement", "keyCertSign", "cRLSign", "encipherOnly", "decipherOnly"};
                for (boolean usage : ku.getBooleanArray()) {
                    if (usage) {
                        ExtensionParameterType param = xmlext.addNewParameter();
                        param.setType(label[i]);
                        param.setStringValue(String.valueOf(usage));
                    }
                    i++;
                }
            }

            //QcStatements
            if (rawExt.getObjectID().equals(QCStatements.oid)) {
                QCStatements qc = (QCStatements) rawExt;
                CertificateExtensionType xmlext = extensions.addNewCertificateExtension();
                addExtNameAndOID(xmlext, rawExt);
                // set property
                QCStatement[] qCStatements = qc.getQCStatements();
                for (QCStatement statement : qCStatements) {
                    ExtensionParameterType param = xmlext.addNewParameter();
                    param.setType(statement.getStatementID().getName());
                    String statementString = getQcStatementString(statement.getStatementInfo());
                    param.setStringValue(statementString);
                }
            }


            // AuthContextExtension
            if (rawExt instanceof AuthContextExtension) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss z");
                AuthContextExtension authCont = (AuthContextExtension) rawExt;
                List<SAMLAuthContextDocument> statementInfoList = authCont.getStatementInfoList();
                if (!statementInfoList.isEmpty()) {
                    try {
                        SAMLAuthContextDocument contInfoDoc = statementInfoList.get(0);
                        SAMLAuthContextType samlAuthContext = contInfoDoc.getSAMLAuthContext();
                        AuthContextInfoType aci = samlAuthContext.getAuthContextInfo();
                        AttributeMappingType[] idAttributes = samlAuthContext.getIdAttributes().getAttributeMappingArray();
                        CertificateExtensionType xmlExt = extensions.addNewCertificateExtension();
                        addExtNameAndOID(xmlExt, rawExt);
                        ExtensionParameterType param;
                        //Param IdP
                        if (aci.getIdentityProvider() != null) {
                            param = xmlExt.addNewParameter();
                            param.setType("Identity Provider");
                            param.setStringValue(aci.getIdentityProvider());
                        }
                        //Param Time
                        if (aci.getAuthenticationInstant() != null) {
                            param = xmlExt.addNewParameter();
                            param.setType("Authentication Time");
                            param.setStringValue(sdf.format(aci.getAuthenticationInstant().getTime()));
                        }
                        //Param Loa
                        if (aci.getAuthnContextClassRef() != null) {
                            param = xmlExt.addNewParameter();
                            param.setType("Level of Assurance");
                            param.setStringValue(aci.getAuthnContextClassRef());
                        }
                        //Param Assertion ref
                        if (aci.getAssertionRef() != null) {
                            param = xmlExt.addNewParameter();
                            param.setType("Assertion Ref");
                            param.setStringValue(aci.getAssertionRef());
                        }
                        //Param Service ID
                        if (aci.getServiceID() != null) {
                            param = xmlExt.addNewParameter();
                            param.setType("Authenticating Service");
                            param.setStringValue(aci.getServiceID());
                        }

                        if (idAttributes != null && idAttributes.length > 0) {
                            param = xmlExt.addNewParameter();
                            param.setType("Identity Attributes");
                            param.setStringValue(getExtIdAttrInfoTable(idAttributes));
                        }
                    } catch (Exception ex) {
                    }

                }
            }

            //EKU
            if (rawExt.getObjectID().equals(ExtendedKeyUsage.oid)) {
                ExtendedKeyUsage eku = (ExtendedKeyUsage) rawExt;
                CertificateExtensionType xmlext = extensions.addNewCertificateExtension();
                addExtNameAndOID(xmlext, rawExt);
                // set property
                ObjectID[] keyPurposeIDs = eku.getKeyPurposeIDs();
                for (ObjectID oid : keyPurposeIDs) {
                    ExtensionParameterType param = xmlext.addNewParameter();
                    param.setType(oid.getName());
                    param.setStringValue(oid.getID());
                }
            }

            //CertificatePolicies
            if (rawExt.getObjectID().equals(CertificatePolicies.oid)) {
                CertificatePolicies cp = (CertificatePolicies) rawExt;
                CertificateExtensionType xmlext = extensions.addNewCertificateExtension();
                addExtNameAndOID(xmlext, rawExt);
                // set property
                PolicyInformation[] policyInformation = cp.getPolicyInformation();
                for (PolicyInformation pi : policyInformation) {
                    ObjectID oid = pi.getPolicyIdentifier();
                    ExtensionParameterType param = xmlext.addNewParameter();
                    param.setType("policy");
                    param.setStringValue(oid.getNameAndID());
                }
            }

            //SubjectAlterantive Name
            /**
             * GeneralName ::= CHOICE { otherName [0] OtherName, rfc822Name [1]
             * IA5String, dNSName [2] IA5String, x400Address [3] ORAddress,
             * directoryName [4] Name, ediPartyName [5] EDIPartyName,
             * uniformResourceIdentifier [6] IA5String, iPAddress [7] OCTET
             * STRING, registeredID [8] OBJECT IDENTIFIER }
             */
            if (rawExt.getObjectID().equals(SubjectAltName.oid)) {
                SubjectAltName san = (SubjectAltName) rawExt;
                CertificateExtensionType xmlext = extensions.addNewCertificateExtension();
                addExtNameAndOID(xmlext, rawExt);
                // set property
                String[] nameType = new String[]{"otherName", "rfc822Name", "dNSName", "x400Address", "directoryName", "ediPartyName", "uniformResourceIdentifier", "iPAddress", "registeredID"};
                GeneralNames generalNames = san.getGeneralNames();
                Enumeration<GeneralName> names = generalNames.getNames();
                while (names.hasMoreElements()) {
                    GeneralName name = names.nextElement();
                    int type = name.getType();
                    if (type == 1 || type == 2 || type == 6 || type == 7) {
                        ExtensionParameterType param = xmlext.addNewParameter();
                        param.setType(nameType[type]);
                        param.setStringValue(name.getName().toString());
                    }
                }
            }
        }
    }

    public void addExtNameAndOID(CertificateExtensionType xmlext, V3Extension rawExt) {
        xmlext.setOID(rawExt.getObjectID().getID());
        xmlext.setName(rawExt.getObjectID().getName());
        xmlext.setCritical(rawExt.isCritical());
        return;
    }

    private String normalized(String toString) {
        char[] chars = toString.trim().toCharArray();
        StringBuilder b = new StringBuilder();
        boolean cc = false;
        for (char c : chars) {
            if ((int) c > 30) {
                cc = false;
                b.append(c);
            } else {
                //prevent multipple control characters from creating multipple commas
                if (!cc) {
                    b.append(", ");
                }
                cc = true;
            }
        }
        return b.toString();
    }

    private List<ASN1ObjectIdentifier> getAlgOids(SignatureValidationContext sc) {
        List<ASN1ObjectIdentifier> oidList = new ArrayList<>();
        ASN1ObjectIdentifier[] oids = new ASN1ObjectIdentifier[]{sc.getSignatureAlgOID(), sc.getSignaturePkAlgOID(), sc.getSignatureHashAlgOID()};
        for (ASN1ObjectIdentifier oid : oids) {
            if (oid != null) {
                oidList.add(oid);
            }
        }
        return oidList;
    }

    private String getExtIdAttrInfoTable(AttributeMappingType[] idAttrList) {
        StringBuilder b = new StringBuilder();

        b.append("<table>");
//        b.append("<tr><td colespan='4'><b>").append(text.getString("subjectSAMLIdentity")).append("</b></td></tr>");
        b.append("<tr>");
        b.append("<td>").append("<b><u>Attribute</u></b>").append("</td>");
        b.append("<td>").append("<b><u>SAML Name</u></b>").append("</td>");
        b.append("<td>").append("<b><u>Value</u></b>").append("</td>");
        b.append("<td>").append("<b><u>Name Type</u></b>").append("</td>");
        b.append("<td>").append("<b><u>Cert Ref</u></b>").append("</td>");
        b.append("</tr>");
        for (AttributeMappingType attr : idAttrList) {
            AttributeType samlAttr = attr.getAttribute();
            String attrVal = "";
            try {
                XmlString xmlString = (XmlString) samlAttr.getAttributeValueArray(0);
                attrVal = xmlString.getStringValue();
            } catch (Exception ex) {
            }
            String certRef = (attr.getRef().startsWith("urn:") ? attr.getRef().substring(4) : attr.getRef());
            String certType = "DN Attribute";
            AttributeMappingType.Type.Enum certNameType = attr.getType();
            if (certNameType.equals(AttributeMappingType.Type.SAN)) {
                certType = "SubjAlt Name";
                if (certRef.equals("1")) {
                    certRef = "rfc822Name";
                }
            }
            if (certNameType.equals(AttributeMappingType.Type.SDA)) {
                certType = "Subj Dir Attribute";
            }
            b.append("<tr>");
            b.append("<td>").append(samlAttr.getFriendlyName()).append("</td>");
            b.append("<td>").append(samlAttr.getName()).append("</td>");
            b.append("<td>").append(attrVal).append("</td>");
            b.append("<td>").append(certType).append("</td>");
            b.append("<td>").append(certRef).append("</td>");
            b.append("</tr>");
        }
        b.append("</table>");

        return b.toString();
    }

    private String getQcStatementString(QCStatementInfo statementInfo) {

        if (statementInfo == null) {
            return "true";
        }
        StringBuilder b = new StringBuilder();
        String val = statementInfo.toString();
        String name = statementInfo.getName();

        if (statementInfo instanceof PdsQCStatement) {
            PdsQCStatement ps = (PdsQCStatement) statementInfo;
            HashMap<String, String> pdsURLs = ps.getPdsURLs();
            Set<String> keySet = pdsURLs.keySet();
            b.append(text.getString("pdsQcStatementText")).append("(");
            for (String key : keySet) {
                b.append("<a href='").append(pdsURLs.get(key)).append("'>");
                b.append(key).append("</a>");
                b.append(", ");
            }
            if (b.length() > 1) {
                b.delete(b.length() - 2, b.length());
                b.append(")");
            }

            return b.toString();
        }

        if (statementInfo instanceof AuthContextQCStatement) {
            AuthContextQCStatement as = (AuthContextQCStatement) statementInfo;
            SamlAssertionInfo samlSi = as.getStatement();
            List<SamlAssertionInfo.Attribute> contAttrList = samlSi.authContextInfo;
            List<SamlAssertionInfo.Attribute> idAttrList = samlSi.idAttributes;

            //Get Statement Header
            b.append("<i><b>eID2SAMLContext</b> (1.2.752.59.54138.2.1.1)</i><br />");

            //ContextTable
            b.append("<table>");
            b.append("<tr><td colespan='2'><b>").append(text.getString("sAMLAuthContext")).append("</b></td></tr>");
            for (SamlAssertionInfo.Attribute attr : contAttrList) {
                b.append("<tr>");
                b.append("<td>").append(attr.name).append("</td>");
                b.append("<td>").append(attr.value).append("</td>");
                b.append("</tr>");
            }
            b.append("</table><br />");

            //IdTable
            b.append("<table>");
            b.append("<tr><td colespan='3'><b>").append(text.getString("subjectSAMLIdentity")).append("</b></td></tr>");
            b.append("<tr>");
            b.append("<td>").append("<b><u>Attribute</u></b>").append("</td>");
            b.append("<td>").append("<b><u>SAML Name</u></b>").append("</td>");
            b.append("<td>").append("<b><u>Value</u></b>").append("</td>");
            b.append("<td>").append("<b><u>Cert OID</u></b>").append("</td>");
            b.append("</tr>");
            for (SamlAssertionInfo.Attribute attr : idAttrList) {
                String oid = (attr.id.startsWith("urn:") ? attr.id.substring(4) : attr.id);
                b.append("<tr>");
                b.append("<td>").append(attr.name).append("</td>");
                b.append("<td>").append(oid).append("</td>");
                b.append("<td>").append(attr.value).append("</td>");
                b.append("<td>").append(attr.certOid).append("</td>");
                b.append("</tr>");
            }
            b.append("</table>");
            return b.toString();
        }
        // None of the above, return standard normalized string
        return normalized(val);
    }
}
