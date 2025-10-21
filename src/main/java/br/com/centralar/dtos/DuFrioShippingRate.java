package br.com.centralar.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DuFrioShippingRate {
    private String carrier;

    @JsonProperty("carrier_title")
    private String carrierTitle;

    @JsonProperty("method_title")
    private String methodTitle;

    @JsonProperty("method_description")
    private String methodDescription;

    // "price": 351.95
    private BigDecimal price;
}
