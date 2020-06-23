package se.tillvaxtverket.ttsigvalws.resultpage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultSignatureData {

  private SigValidStatus status;
  private boolean coversAllData;
  private String idp;
  private String signingTime;
  private String loa;
  private String assertionRef;
  private String serviceProvider;
  List<DisplayAttribute> signerAttribute;

}
