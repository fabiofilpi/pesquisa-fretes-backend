// LojaPesquisadaDto
package br.com.centralar.dtos;

import br.com.centralar.enums.Vendor;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LojaPesquisadaDto {
  private Long id;
  private String sku;
  private Vendor loja;
  private LocalDateTime ultimaExecucao;
  private String mensagemStatus;
}
