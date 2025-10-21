package br.com.centralar.dtos;

import java.math.BigDecimal;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdiasShippingOption {
    /** Ex.: "Previsão de 6 dias úteis para entrega" */
    private String description;
    /** Ex.: 6 (se identificado) */
    private Integer businessDays;
    /** Preço em BRL, ex.: 119.90 */
    private BigDecimal price;
}