package br.com.centralar.dtos;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class DuFrioShippingQuoteResponse {
  // Ex.: {"intelipost":[{...}], "outroCarrier":[{...}]}
  private Map<String, List<DuFrioShippingRate>> carriers;
}
