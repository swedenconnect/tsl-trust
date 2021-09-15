package se.tillvaxtverket.ttsigvalws.ttwebservice;

import lombok.extern.java.Log;
import se.tillvaxtverket.ttsigvalws.resultpage.SigFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Log
public class PdfDataExportServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    HttpSession session = request.getSession();
    SigFile sigFile = (SigFile) session.getAttribute("sigFile");

    if (sigFile == null || !sigFile.getStorageFile().exists()){
      log.warning("Attempt to access current signed document but no such document exists");
      SigValHandler.nullResponse(response);
      return;
    }


    response.setContentType("application/pdf");
    //We don't want an attachment just the bytes for embedded pdf. Thus we comment away the regular attachment settings.
    response.addHeader("Content-Disposition", "inline; filename=" + sigFile.getFileName());
    response.setContentLength((int) sigFile.getStorageFile().length());

    FileInputStream fileInputStream = new FileInputStream(sigFile.getStorageFile());
    OutputStream responseOutputStream = response.getOutputStream();
    int bytes;
    while ((bytes = fileInputStream.read()) != -1) {
      responseOutputStream.write(bytes);
    }
  }
}
