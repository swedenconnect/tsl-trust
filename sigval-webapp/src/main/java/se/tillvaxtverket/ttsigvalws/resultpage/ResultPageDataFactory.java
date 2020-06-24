package se.tillvaxtverket.ttsigvalws.resultpage;

import iaik.x509.X509Certificate;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.xmlbeans.XmlAnySimpleType;
import se.elegnamnden.id.authCont.x10.saci.AttributeMappingType;
import se.elegnamnden.id.authCont.x10.saci.AuthContextInfoType;
import se.elegnamnden.id.authCont.x10.saci.SAMLAuthContextType;
import se.tillvaxtverket.ttsigvalws.resultpage.cert.CertUtils;
import se.tillvaxtverket.ttsigvalws.resultpage.cert.SubjectDnAttribute;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller.SignatureValidationReport;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.context.SignatureValidationContext;
import x0Assertion.oasisNamesTcSAML2.AttributeType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class ResultPageDataFactory {

  private final ResourceBundle samlAttrNameBundle;
  private final ResourceBundle x509AttrNameBundle;
  private final SignatureValidationReport svr;
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm z");
  @Getter
  ResultPageData resultPageData;

  public ResultPageDataFactory(SignatureValidationReport svr, String docName, Locale locale) {
    this.svr = svr;

    samlAttrNameBundle = ResourceBundle.getBundle("samlAttrName", locale);
    x509AttrNameBundle = ResourceBundle.getBundle("x509AttrName", locale);

    resultPageData = ResultPageData.builder()
      .validSignatures(0)
      .numberOfSignatures(0)
      .status(DocValidStatus.unsigned)
      .documentName(docName)
      .resultSignatureDataList(new ArrayList<>())
      .build();

    generateResultPageData();

  }


  private void generateResultPageData() {
    if (svr == null){
      return;
    }

    SigValidationModel model = svr.getModel();
    List<SignatureValidationContext> sigResultList = model.getSignatureContexts();
    if (sigResultList == null || sigResultList.size() == 0){
      try {
        // Attempt to set document type
        resultPageData.setDocumentType(model.getSigDocument().getDocType().name());
      } catch (Exception ignored){
      }
      return;
    }
    resultPageData.setDocumentType(model.getSigDocument().getDocType().name());
    resultPageData.setNumberOfSignatures(sigResultList.size());
    List<ResultSignatureData> signatureData = sigResultList.stream()
      .map(signatureValidationContext -> getSignatureResult(signatureValidationContext))
      .collect(Collectors.toList());
    resultPageData.setResultSignatureDataList(signatureData);

    boolean oneSigCoversAlldata = signatureData.stream()
      .filter(resultSignatureData -> resultSignatureData.isCoversAllData())
      .findFirst().isPresent();
    List<ResultSignatureData> validSignatures = signatureData.stream()
      .filter(resultSignatureData -> resultSignatureData.getStatus().equals(SigValidStatus.ok))
      .collect(Collectors.toList());
    int validSigCount = validSignatures.size();
    resultPageData.setValidSignatures(validSigCount);
    if (validSigCount >0 && oneSigCoversAlldata) {
      if (validSigCount == sigResultList.size()){
        resultPageData.setStatus(DocValidStatus.ok);
      } else {
        resultPageData.setStatus(DocValidStatus.someinvalid);
      }
    } else {
      resultPageData.setStatus(DocValidStatus.invalid);
    }
  }

  private ResultSignatureData getSignatureResult(SignatureValidationContext signatureValidationContext) {

    X509Certificate signCert = signatureValidationContext.getSignCert();
    if (signCert == null) {
      return ResultSignatureData.builder()
        .status(SigValidStatus.sigerror)
        .build();
    }

    SAMLAuthContextType authContextExtData = CertUtils.getAuthContextExtData(signCert);
    List<DisplayAttribute> attributeList = authContextExtData == null
      ? getAttrsFromSubjectField(signCert)
      : getAttrsFromAuthContextExt(signCert, authContextExtData);

    ResultSignatureData.ResultSignatureDataBuilder builder = ResultSignatureData.builder()
      .signerAttribute(attributeList)
      .coversAllData(signatureValidationContext.isCoversDoc());

    addAuthnContextResult(authContextExtData, builder);

    if (signatureValidationContext.isSigValid() && signatureValidationContext.isCoversDoc()) {
      if (signatureValidationContext.isSigChainVerified()){
        builder.status(SigValidStatus.ok);
      } else {
        builder.status(SigValidStatus.invalidCert);
      }
    } else {
      builder.status(SigValidStatus.sigerror);
    }
    return builder.build();
  }

  private void addAuthnContextResult(SAMLAuthContextType authContextExtData, ResultSignatureData.ResultSignatureDataBuilder builder) {
    if (authContextExtData == null) {
      return;
    }
    try {
      AuthContextInfoType authContextInfo = authContextExtData.getAuthContextInfo();
      builder.idp(authContextInfo.getIdentityProvider());
      builder.assertionRef(authContextInfo.getAssertionRef());
      builder.loa(authContextInfo.getAuthnContextClassRef());
      builder.signingTime(dateFormat.format(authContextInfo.getAuthenticationInstant().getTime()));
      builder.serviceProvider(authContextInfo.getServiceID());
    } catch (Exception ex){
      log.log(Level.SEVERE, "Unable to parse Authn context data", ex);
    }
  }

  private List<DisplayAttribute> getAttrsFromAuthContextExt(X509Certificate signCert, SAMLAuthContextType authContextExtData) {
    try {
      AttributeMappingType[] attributeMappingArray = authContextExtData.getIdAttributes().getAttributeMappingArray();
      return Arrays.stream(attributeMappingArray)
        .map(attributeMappingType -> {
          AttributeType attribute = attributeMappingType.getAttribute();
          SamlAttribute samlAttr = SamlAttribute.getAttributeFromSamlName(attribute.getName());
          String attributeValue = ((XmlAnySimpleType) attribute.getAttributeValueArray(0)).getStringValue();
          return new DisplayAttribute(UIUtils.fromIso(samlAttrNameBundle.getString(samlAttr.name())),attributeValue,samlAttr.displayOrder);
        })
        .sorted(Comparator.comparingInt(DisplayAttribute::getOrder))
        .collect(Collectors.toList());
    } catch (Exception ex){
      log.log(Level.SEVERE, "Unable to parse subject name from authContextExtension" , ex);
      return new ArrayList<>();
    }
  }

  private List<DisplayAttribute> getAttrsFromSubjectField(X509Certificate signCert) {
    try {
      Map<SubjectDnAttribute, String> subjectAttributes = CertUtils.getSubjectAttributes(signCert);
      return subjectAttributes.keySet().stream()
        .map(subjectDnAttribute -> {
          String name = subjectDnAttribute.equals(SubjectDnAttribute.unknown)
            ? subjectDnAttribute.getOid()
            : UIUtils.fromIso(x509AttrNameBundle.getString(subjectDnAttribute.name()));
          return new DisplayAttribute(name,subjectAttributes.get(subjectDnAttribute), subjectDnAttribute.getOrder());
        })
        .sorted(Comparator.comparingInt(DisplayAttribute::getOrder))
        .collect(Collectors.toList());
    } catch (IOException ex) {
      log.log(Level.SEVERE, "Unable to parse subject name from certificate" , ex);
      return new ArrayList<>();
    }
  }


}
