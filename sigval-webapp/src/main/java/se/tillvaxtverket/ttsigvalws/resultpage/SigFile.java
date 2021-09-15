package se.tillvaxtverket.ttsigvalws.resultpage;

import lombok.Getter;
import se.tillvaxtverket.ttsigvalws.daemon.ServletListener;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

@Getter
public class SigFile {

  private static final Random RNG = new SecureRandom();
  String fileName;
  File storageFile;

  public SigFile(String fileName) {
    this.fileName = fileName;
    storageFile = new File(
      ServletListener.baseModel.getConf().getDataDirectory(),
      "uploads/" + new BigInteger(128, RNG).toString(32)
    );
  }
}
