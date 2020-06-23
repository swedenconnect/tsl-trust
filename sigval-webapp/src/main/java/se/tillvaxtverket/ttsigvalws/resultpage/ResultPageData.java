package se.tillvaxtverket.ttsigvalws.resultpage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultPageData {

  private DocValidStatus status;
  private int validSignatures;
  private int numberOfSignatures;
  private String documentType;
  private String documentName;
  private List<ResultSignatureData> resultSignatureDataList;
}
