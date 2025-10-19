package br.com.centralar.repositories;

import br.com.centralar.dtos.CotacaoDeFreteDto;
import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.enums.ResultadoCotacao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.List;

public interface CotacaoDeFreteRepository extends PanacheRepository<CotacaoDeFreteModel> {
  List<CotacaoDeFreteDto> findCotacoesPorPesquisa(
      final long pesquisaId, final ResultadoCotacao resultadoCotacao);
}
