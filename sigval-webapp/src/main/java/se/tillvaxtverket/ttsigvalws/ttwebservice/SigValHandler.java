package se.tillvaxtverket.ttsigvalws.ttwebservice;

import se.tillvaxtverket.tsltrust.common.utils.general.FilenameFilterImpl;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.SigDocument;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller.SignatureValidationReport;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifierFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SigValHandler {

  private static final Logger LOG = Logger.getLogger(SigValHandler.class.getName());
  private final SigValidationBaseModel baseModel;
  private final String documentFolderName;

  public SigValHandler(SigValidationBaseModel baseModel, String documentFolderName) {
    this.baseModel = baseModel;
    this.documentFolderName = documentFolderName;
  }

  public String verifyServerDocSignature(HttpServletRequest request) {
    try {
      String sigFileName = getRequestFileName(request);
      String docName = request.getParameter("id");
      String policyName = request.getParameter("policy");
      policyName = policyName == null ? "" : policyName;

      return verifySignature(request, policyName, docName, sigFileName, null);
    } catch (Exception ex) {
    }
    return "";

  }

  public String getRequestFileName(HttpServletRequest request) {
    return getFullSigFileName(request.getParameter("id"));
  }

  public String getFullSigFileName(String formFileName) {
    String fileName = "";
    File serverDocsDir = new File(baseModel.getConf().getDataDirectory(), documentFolderName);
    if (serverDocsDir.canRead()) {
      File[] fileList = serverDocsDir.listFiles(new FilenameFilterImpl("."));
      for (File listedFile : fileList) {
        if (listedFile.getName().equalsIgnoreCase(formFileName)) {
          fileName = listedFile.getAbsolutePath();
        }
      }
    }
    return fileName;
  }


  public String verifySignature(HttpServletRequest request, String policyName, String docName, String sigFileName, byte[] sigBytes) {
    SigValidationModel model;
    Thread verifierTask;

    model = new SigValidationModel();
    model.setBaseModel(baseModel);
    SigDocument sigDoc = null;
    if (sigBytes == null) {
      sigDoc = new SigDocument(new File(sigFileName));
      model.setSigDocument(sigDoc);
    } else {
      sigDoc = new SigDocument(sigBytes);
      model.setSigDocument(sigDoc);
    }
    String pName = (policyName == null) ? "" : policyName;
    model.setPolicyName(pName);
    sigDoc.setDocName(docName == null ? "" : docName);
    String policyDesc;
    try {
      policyDesc = baseModel.getTrustStore().getPolicyDescMap().get(pName);
      model.setPolicyDescription(policyDesc);
    } catch (Exception ex) {
    }
    model.setCheckOcspAndCrl(false);
    model.setPrefSpeed(true);

    KeyStore keyStore = baseModel.getTrustStore().getKeyStore(pName);

    if (keyStore != null) {
      SigVerifier verifier = SigVerifierFactory.getSigVerifier(model);
      if (verifier != null) {
        try {
          //Verify Signature
          verifierTask = new Thread(verifier);
          verifierTask.start();
          verifierTask.join();
          SignatureValidationReport report = new SignatureValidationReport(model);
          return report.generateReport();
        } catch (InterruptedException ex) {
          LOG.log(Level.WARNING, null, ex);
        }
      }
    }
    return null;
  }


}
