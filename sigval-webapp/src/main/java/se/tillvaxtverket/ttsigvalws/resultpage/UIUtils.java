package se.tillvaxtverket.ttsigvalws.resultpage;

import java.nio.charset.StandardCharsets;

public class UIUtils {

  public static String fromIso(String isoStr){
    return new String(isoStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
  }
  public static String fromUtf(String utfStr){
    return new String(utfStr.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
  }

}
