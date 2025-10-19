package br.com.centralar.repositories.impl;

import br.com.centralar.dtos.CotacaoDeFreteDto;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.repositories.CotacaoDeFreteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import java.util.List;

@ApplicationScoped
public class CotacaoDeFreteRepositoryImpl implements CotacaoDeFreteRepository {
  /**
   * Busca as cotações de frete de uma pesquisa (job) projetando diretamente no DTO. Se {@code
   * resultadoCotacao} for {@code null}, não aplica o filtro por resultado.
   */
  @Override
  public List<CotacaoDeFreteDto> findCotacoesPorPesquisa(
      final long pesquisaId, final ResultadoCotacao resultadoCotacao) {

    final String jpql =
        "select new br.com.centralar.dtos.CotacaoDeFreteDto("
            + "  c.id, c.dataUltimaAlteracao, c.loja, c.cep, c.prazoEmDias, c.valor, "
            + "  c.modo, c.skuProduto, c.mensagemDeErro, c.resultado"
            + ") "
            + "from CotacaoDeFreteModel c "
            + "join c.lojaPesquisada lp "
            + "join lp.jobInfo j "
            + "where j.id = :pesquisaId "
            + "  and ( :resultado is null or c.resultado = :resultado ) "
            + "order by c.dataUltimaAlteracao desc, c.id desc";

    final TypedQuery<CotacaoDeFreteDto> q =
        getEntityManager().createQuery(jpql, CotacaoDeFreteDto.class);
    q.setParameter("pesquisaId", pesquisaId);
    q.setParameter("resultado", resultadoCotacao);

    return q.getResultList();
  }
}
