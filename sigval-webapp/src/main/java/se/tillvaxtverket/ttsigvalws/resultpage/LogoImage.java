package se.tillvaxtverket.ttsigvalws.resultpage;

import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

@Log
public class LogoImage {

  @Getter
  private String dataUrl;

  public LogoImage(File logoFile) {
    try {
      byte[] logoBytes = IOUtils.toByteArray(new FileInputStream(logoFile));
      String logoFileName = logoFile.getName();
      String ext = logoFileName.substring(logoFileName.lastIndexOf(".") +1);
      switch (ext.toLowerCase()){
      case "png":
        dataUrl = "data:image/png;base64," + Base64.toBase64String(logoBytes);
        break;
      case "svg":
        dataUrl = "data:image/svg+xml;base64," + Base64.toBase64String(logoBytes);
      }
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to read logo file at: " + logoFile.getAbsolutePath(), e);
    }

  }
}
