package se.tillvaxtverket.ttsigvalws.resultpage;

import java.util.Locale;
import java.util.ResourceBundle;

public class UIText {

  private final ResourceBundle resultTextBundle;

  public UIText(Locale lang) {
    resultTextBundle = ResourceBundle.getBundle("resultPageText", lang);
  }

  public String get(String key){
    return UIUtils.fromIso(resultTextBundle.getString(key));
  }


}
