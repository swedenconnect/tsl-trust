package se.tillvaxtverket.ttsigvalws.resultpage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayAttribute {
  private String name;
  private String value;
  private int order;
}
