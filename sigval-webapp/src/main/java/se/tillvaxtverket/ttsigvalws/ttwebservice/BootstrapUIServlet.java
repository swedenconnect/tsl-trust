package se.tillvaxtverket.ttsigvalws.ttwebservice;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import se.tillvaxtverket.ttsigvalws.daemon.ServletListener;
import se.tillvaxtverket.ttsigvalws.resultpage.ResultPageData;
import se.tillvaxtverket.ttsigvalws.resultpage.ResultPageDataFactory;
import se.tillvaxtverket.ttsigvalws.ttwssigvalidation.marshaller.SignatureValidationReport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    HttpSession session = request.getSession();
    Locale lang = getLang(request);
    File sigFile = (File) session.getAttribute("sigFile");
    if (sigFile == null || !sigFile.exists()){
      response.sendRedirect("sigval.jsp");
      return;
    }
    request.setAttribute("fileName", sigFile.getName());
    request.setAttribute("lang", lang);
    SignatureValidationReport signatureValidationReport = sigValHandler.verifySignature(defaultPolicy, sigFile);

    ResultPageDataFactory factory = new ResultPageDataFactory(signatureValidationReport, sigFile.getName(), lang);
    ResultPageData resultPageData = factory.getResultPageData();
    request.setAttribute("result", resultPageData);

    forward("sigvalresult.jsp", request, response);
  }

  private Locale getLang(HttpServletRequest request) {
    Optional<Cookie> langSelectCookieOptional = Arrays.stream(request.getCookies())
      .filter(cookie -> cookie.getName().equals("langSelect"))
      .findFirst();
    if (langSelectCookieOptional.isPresent()){
      return new Locale(langSelectCookieOptional.get().getValue());
    }
    return new Locale("sv");
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
      File uploadDir = new File(ServletListener.baseModel.getConf().getDataDirectory(), ServletListener.baseModel.getDocumentFolderName());
      if (!uploadDir.exists()) {
        uploadDir.mkdirs();
      }

      boolean uploaded = false;
      String fileName = "";
      try {
        List<FileItem> formItems = upload.parseRequest(request);
        if (formItems != null && formItems.size() > 0) {
          for (FileItem item : formItems) {
            if (!item.isFormField()) {
              fileName = new File(item.getName()).getName();
              File storeFile = new File(uploadDir, fileName);
              if (storeFile.exists()){
                storeFile.delete();
              }
              item.write(storeFile);
              uploaded = true;
              request.getSession().setAttribute("sigFile", storeFile);
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
