package se.tillvaxtverket.ttsigvalws.ttwebservice;

import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import se.tillvaxtverket.tsltrust.common.utils.general.FilenameFilterImpl;
import se.tillvaxtverket.ttsigvalws.daemon.ServletListener;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document.SigDocument;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller.SignatureValidationReport;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationModel;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifier;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.sigVerify.SigVerifierFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SigValHandler {

  private static final Logger LOG = Logger.getLogger(SigValHandler.class.getName());

  public SigValHandler() {
  }

  public String verifyServerDocSignature(HttpServletRequest request) {
    try {
      String sigFileName = getRequestFileName(request);
      String docName = request.getParameter("id");
      String policyName = request.getParameter("policy");
      policyName = policyName == null ? "" : policyName;

      return verifySignature(policyName, docName, new File(sigFileName)).generateReport();
    } catch (Exception ex) {
    }
    return "";

  }

  public String getRequestFileName(HttpServletRequest request) {
    return getFullSigFileName(request.getParameter("id"));
  }

  public String getFullSigFileName(String formFileName) {
    String fileName = "";
    File serverDocsDir = new File(ServletListener.baseModel.getConf().getDataDirectory(), ServletListener.baseModel.getDocumentFolderName());
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


  public SignatureValidationReport verifySignature(String policyName, String docName, File sigFile) {
    SigDocument sigDoc = new SigDocument(sigFile);
    return verifySignature(policyName, docName, sigDoc);
  }
  public SignatureValidationReport verifySignature(String policyName, String docName, byte[] sigBytes) {
    SigDocument sigDoc = new SigDocument(sigBytes);
    return verifySignature(policyName, docName, sigDoc);
  }

  public SignatureValidationReport verifySignature(String policyName, String docName, SigDocument sigDoc) {
    SigValidationModel model;
    Thread verifierTask;

    model = new SigValidationModel();
    model.setBaseModel(ServletListener.baseModel);
    model.setSigDocument(sigDoc);
    String pName = (policyName == null) ? "" : policyName;
    model.setPolicyName(pName);
    sigDoc.setDocName(docName == null ? "" : docName);
    String policyDesc;
    try {
      policyDesc = ServletListener.baseModel.getTrustStore().getPolicyDescMap().get(pName);
      model.setPolicyDescription(policyDesc);
    } catch (Exception ex) {
    }
    model.setCheckOcspAndCrl(true);
    model.setPrefSpeed(true);

    KeyStore keyStore = ServletListener.baseModel.getTrustStore().getKeyStore(pName);

    if (keyStore != null) {
      SigVerifier verifier = SigVerifierFactory.getSigVerifier(model);
      if (verifier != null) {
        try {
          //Verify Signature
          verifierTask = new Thread(verifier);
          verifierTask.start();
          verifierTask.join();
          SignatureValidationReport report = new SignatureValidationReport(model);
          return report;
        } catch (InterruptedException ex) {
          LOG.log(Level.SEVERE, "Failure to invoke signature validation process", ex);
        }
      }
    } else {
      LOG.log(Level.SEVERE, "Selected policy is not supported");
    }
    return null;
  }

  public static void nullResponse(HttpServletResponse response) {
    try {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      response.getWriter().write("");
      response.getWriter().close();
    } catch (Exception ex) {
    }
  }

  public void processValidationPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String dataStr = request.getParameter("data");
      String docName = request.getParameter("id");
      String policy = request.getParameter("policy");
      byte[] docBytes = Base64Coder.decode(dataStr);
      String verifyResult = verifySignature(policy, docName, docBytes).generateReport();
      sendValidationReport(verifyResult, response);
    } catch (Exception ex) {
      nullResponse(response);
    }
  }

  public void sendValidationReport(String verifyResult, HttpServletResponse response) {
    if (verifyResult != null) {
      response.setContentType("text/xml;charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      try {
        response.getWriter().write(verifyResult);
        return;
      } catch (IOException ex) {
        nullResponse(response);
      }
    } else {
      nullResponse(response);
    }

  }


}
