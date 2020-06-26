package se.tillvaxtverket.ttsigvalws.ttwebservice;

import com.aaasec.lib.crypto.xml.XmlBeansUtil;
import lombok.extern.java.Log;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import se.tillvaxtverket.ttsigvalws.daemon.ServletListener;
import se.tillvaxtverket.ttsigvalws.resultpage.*;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.config.TTvalConfig;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller.SignatureValidationReport;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.models.SigValidationBaseModel;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;

@Log
public class BootstrapUIServlet extends HttpServlet {
  private static final int MEMORY_THRESHOLD = 20000000;
  private static final long MAX_FILE_SIZE = 10000000;
  private static final long MAX_REQUEST_SIZE = 20000000;
  private SigValHandler sigValHandler;
  private String defaultPolicy;



  @Override public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    sigValHandler = new SigValHandler();
    defaultPolicy = ServletListener.baseModel.getConf().getJsonConf().getDefaultPolicy();
  }

  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    String servletPath = request.getServletPath();
    HttpSession session = request.getSession();
    SigFile sigFile = (SigFile) session.getAttribute("sigFile");
    Locale lang = getLang(request);
    SigValidationBaseModel baseModel = ServletListener.baseModel;
    TTvalConfig ttValConfig = baseModel.getConf().getJsonConf();
    request.setAttribute("logoImage" , baseModel.getLogoImage().getDataUrl());
    request.setAttribute("secondaryLogoImage" , baseModel.getSecondaryLogoImage());
    request.setAttribute("lang", lang);
    request.setAttribute("bootstrapCss", ttValConfig.getBootstrapCss());
    request.setAttribute("htmlTitle", ttValConfig.getWebTitle());
    request.setAttribute("devmode", "true".equalsIgnoreCase(ttValConfig.getDevmode()));

    // Test if we are starting a new session by calling the main url
    if (servletPath.endsWith("main")){
      if (sigFile != null && sigFile.getStorageFile().exists()){
        sigFile.getStorageFile().delete();
      }
      session.removeAttribute("sigFile");
      forward("sigval.jsp", request, response);
      return;
    }

    if (sigFile == null || !sigFile.getStorageFile().exists()){
      response.sendRedirect("main");
      return;
    }
    request.setAttribute("fileName", sigFile.getFileName());
    SignatureValidationReport signatureValidationReport = sigValHandler.verifySignature(defaultPolicy, sigFile.getFileName(), sigFile.getStorageFile());

    if (signatureValidationReport == null) {
      request.setAttribute("error", SigValError.nullResult.name());
      forward("sigvalerror.jsp", request, response);
      return;
    }

    ResultPageDataFactory factory = new ResultPageDataFactory(signatureValidationReport, sigFile.getFileName(), lang);
    ResultPageData resultPageData = factory.getResultPageData();
    request.setAttribute("result", resultPageData);

    String documentType = resultPageData.getDocumentType();
    if (documentType != null && documentType.equalsIgnoreCase("XML")){
      String xmlPrettyPrint;
      try {
        xmlPrettyPrint = new String (XmlBeansUtil.getStyledBytes(XmlObject.Factory.parse(sigFile.getStorageFile())), StandardCharsets.UTF_8)
        .replaceAll("<", "&lt;").replaceAll(">", "&gt;");
      }
      catch (XmlException e) {
        log.log(Level.WARNING, "Unable to parse uploaded signed XML document", e);
        xmlPrettyPrint = "Unable to parse uploaded XML document";
      }
      request.setAttribute("xmlPrettyPrint", xmlPrettyPrint);
    }

    forward("sigvalresult.jsp", request, response);
  }

  private Locale getLang(HttpServletRequest request) {
    if (request.getCookies() != null){
      Optional<Cookie> langSelectCookieOptional = Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals("langSelect"))
        .findFirst();
      if (langSelectCookieOptional.isPresent()){
        return new Locale(langSelectCookieOptional.get().getValue());
      }
    }
    return new Locale(ServletListener.baseModel.getConf().getJsonConf().getLanguage());
  }

  @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");

    if (ServletFileUpload.isMultipartContent(request)) {

      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setSizeThreshold(MEMORY_THRESHOLD);
      factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setFileSizeMax(MAX_FILE_SIZE);
      upload.setSizeMax(MAX_REQUEST_SIZE);

      boolean uploaded = false;
      String fileName = "";
      try {
        List<FileItem> formItems = upload.parseRequest(request);
        if (formItems != null && formItems.size() > 0) {
          for (FileItem item : formItems) {
            if (!item.isFormField()) {
              fileName = new File(item.getName()).getName();
              SigFile sigFile = new SigFile(fileName);
              File storeFile = sigFile.getStorageFile();
              item.write(storeFile);
              uploaded = true;
              request.getSession().setAttribute("sigFile", sigFile);
              storeFile.deleteOnExit();
            }
          }
        }
      } catch (Exception ex) {

      }

      if (uploaded){
        // Store data in session
        response.getWriter().write("[]");
        return;
      }
    }
  }

  /**
   * Forward user to specified URL
   *
   * @param url Target URL
   * @param request HTTPServletRequest
   * @param response HTTPServletResponse
   * @throws ServletException
   * @throws IOException
   */
  public static void forward(String url, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.getRequestDispatcher(url).forward(request, response);
  }


}
