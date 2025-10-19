package br.com.centralar.entities;

import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.enums.Vendor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "cotacao_frete")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotacaoDeFreteModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Auditoria
  @CreationTimestamp
  @Column(name = "data_criacao", updatable = false)
  private LocalDateTime dataCriacao;

  @UpdateTimestamp
  @Column(name = "data_ultima_alteracao")
  private LocalDateTime dataUltimaAlteracao;

  private double valor;
  private int prazoEmDias;
  private String modo = "PADRAO";
  private String mensagemDeErro;
  private String skuProduto;
  private String cep;

  /** Relacionamento: muitas cotacoes pertencem a uma Loja Pesquisada */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loja_pesquisada_id", nullable = false)
  private LojaPesquisadaModel lojaPesquisada;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Vendor loja;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ResultadoCotacao resultado;
}
