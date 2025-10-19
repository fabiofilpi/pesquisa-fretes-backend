package br.com.centralar.entities;

import br.com.centralar.dtos.PesquisaLojaRequest;
import br.com.centralar.enums.Vendor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "loja_pesquisada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LojaPesquisadaModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String sku;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Vendor loja;

  private LocalDateTime ultimaExecucao;
  private String mensagemStatus;

  /** Relacionamento: muitas lojas pesquisadas pertencem a um Job */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_info_id", nullable = false)
  private JobInfoModel jobInfo;

  /** Relacionamento: uma Loja Pesquisada pode ter várias cotacoes */
  @OneToMany(mappedBy = "lojaPesquisada", cascade = CascadeType.ALL, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<CotacaoDeFreteModel> cotacoesDeFrete = new ArrayList<>();

  public LojaPesquisadaModel(final PesquisaLojaRequest req) {
    this.sku = req.getSku();
    this.loja = req.getLoja();
  }
}
