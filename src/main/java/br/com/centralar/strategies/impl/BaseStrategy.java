package br.com.centralar.strategies.impl;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.repositories.CotacaoDeFreteRepository;
import br.com.centralar.strategies.PesquisaFretesStrategy;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseStrategy implements PesquisaFretesStrategy {
  @Inject CotacaoDeFreteRepository cotacaoDeFreteRepository;

  abstract List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel);

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public List<CotacaoDeFreteModel> cotarFretes(
      final LojaPesquisadaModel lojaPesquisadaModel, final List<String> ceps) {
    final var list = new ArrayList<CotacaoDeFreteModel>();
    for (final String cep : ceps) {
      list.addAll(getCotacaoDeFrete(cep, lojaPesquisadaModel));
    }
    return list;
  }
}
