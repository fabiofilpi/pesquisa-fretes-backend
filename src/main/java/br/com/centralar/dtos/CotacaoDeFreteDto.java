// CotacaoDeFreteDto
package br.com.centralar.dtos;

import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.enums.Vendor;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CotacaoDeFreteDto {
  private Long id;
  private LocalDateTime dataUltimaAlteracao;
  private Vendor loja;
  private String cep;
  private int prazoEmDias;
  private double valor;
  private String modo;
  private String skuProduto;
  private String mensagemDeErro;
  private ResultadoCotacao resultado;
}
