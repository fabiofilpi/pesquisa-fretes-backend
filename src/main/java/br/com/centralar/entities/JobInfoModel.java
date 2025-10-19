package br.com.centralar.entities;

import br.com.centralar.dtos.PesquisaRequest;
import br.com.centralar.enums.JobStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "job_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobInfoModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private JobStatus jobStatus;

  /** Lista de CEPs pesquisados. Usa @ElementCollection para armazenar em tabela auxiliar. */
  @ElementCollection
  @CollectionTable(
      name = "job_info_ceps",
      joinColumns = @JoinColumn(name = "job_info_id", nullable = false))
  @OnDelete(action = OnDeleteAction.CASCADE)
  @Column(name = "cep")
  private List<String> ceps;

  // Título da pesquisa
  @Column(name = "titulo_da_pesquisa", length = 255)
  private String tituloDaPesquisa;

  // Auditoria
  @CreationTimestamp
  @Column(name = "data_criacao", updatable = false)
  private LocalDateTime dataCriacao;

  @UpdateTimestamp
  @Column(name = "data_ultima_alteracao")
  private LocalDateTime dataUltimaAlteracao;

  @Column(columnDefinition = "TEXT")
  private String message;

  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;

  /** Relacionamento: um Job pode ter várias lojas pesquisadas */
  @OneToMany(mappedBy = "jobInfo", cascade = CascadeType.ALL, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<LojaPesquisadaModel> lojasPesquisadas = new ArrayList<>();

  public static JobInfoModel createNew(final PesquisaRequest pesquisaRequest) {
    final JobInfoModel jobInfoModel =
        JobInfoModel.builder()
            .tituloDaPesquisa(pesquisaRequest.getTitulo())
            .ceps(pesquisaRequest.getCeps())
            .jobStatus(JobStatus.PENDING)
            .startedAt(LocalDateTime.now())
            .build();
    if (pesquisaRequest.getSkus() != null) {
      List<LojaPesquisadaModel> lojas =
          pesquisaRequest.getSkus().stream()
              .map(LojaPesquisadaModel::new) // usa o construtor que criamos
              .peek(loja -> loja.setJobInfo(jobInfoModel)) // vincula o job
              .collect(Collectors.toList());

      jobInfoModel.setLojasPesquisadas(lojas);
    }
    return jobInfoModel;
  }
}
