package se.tillvaxtverket.ttsigvalws.ttwebservice;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class BootstrapUIServlet extends HttpServlet {
  private static final int MEMORY_THRESHOLD = 20000000;
  private static final long MAX_FILE_SIZE = 10000000;
  private static final long MAX_REQUEST_SIZE = 20000000;
  private static final String UPLOAD_DIRECTORY = "/opt/webapp/tsltrust/sigval/uploads";


  @Override public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
  }

  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    HttpSession session = request.getSession();
    String fileName = (String) session.getAttribute("fileName");
    if (fileName == null){
      response.sendRedirect("sigval.jsp");
      return;
    }
    request.setAttribute("fileName", fileName);
    forward("sigvalresult.jsp", request, response);
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
      File uploadDir = new File(UPLOAD_DIRECTORY);
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
              String filePath = UPLOAD_DIRECTORY + File.separator + fileName;
              File storeFile = new File(filePath);
              item.write(storeFile);
              uploaded = true;
              request.getSession().setAttribute("fileName", fileName);
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
