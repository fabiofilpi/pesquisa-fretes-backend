// JobInfoDto
package br.com.centralar.dtos;

import br.com.centralar.enums.JobStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobInfoDto {
  private Long id;
  private JobStatus jobStatus;
  private List<String> ceps = new ArrayList<>();
  private String tituloDaPesquisa;
  private LocalDateTime dataCriacao;
  private LocalDateTime dataUltimaAlteracao;
  private String message;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private List<LojaPesquisadaDto> lojasPesquisadas = new ArrayList<>();

  // Importante: tenha esse construtor (sem 'ceps').
  public JobInfoDto(
      Long id,
      JobStatus jobStatus,
      String tituloDaPesquisa,
      LocalDateTime dataCriacao,
      LocalDateTime dataUltimaAlteracao,
      String message,
      LocalDateTime startedAt,
      LocalDateTime finishedAt) {
    this.id = id;
    this.jobStatus = jobStatus;
    this.tituloDaPesquisa = tituloDaPesquisa;
    this.dataCriacao = dataCriacao;
    this.dataUltimaAlteracao = dataUltimaAlteracao;
    this.message = message;
    this.startedAt = startedAt;
    this.finishedAt = finishedAt;
    this.ceps = new java.util.ArrayList<>();
    this.lojasPesquisadas = new java.util.ArrayList<>();
  }
}
