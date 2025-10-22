package br.com.centralar.strategies.impl;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.exceptions.InvalidCepException;
import br.com.centralar.repositories.CotacaoDeFreteRepository;
import br.com.centralar.strategies.PesquisaFretesStrategy;
import br.com.centralar.utils.CepUtils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseStrategy implements PesquisaFretesStrategy {
  @Inject CotacaoDeFreteRepository cotacaoDeFreteRepository;

  abstract List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel);

  abstract void validateParameters(final String sku) throws IllegalArgumentException;

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public List<CotacaoDeFreteModel> cotarFretes(
      final LojaPesquisadaModel lojaPesquisadaModel, final List<String> ceps) {
    final var list = new ArrayList<CotacaoDeFreteModel>();
    validateParameters(lojaPesquisadaModel.getSku());

    for (final String unformattedCep : ceps) {
      final String formattedCep;
      try {
        formattedCep = CepUtils.parseCep(unformattedCep);
      } catch (final InvalidCepException e) {
        log.error(e.getMessage());
        list.add(buildErrorModel(lojaPesquisadaModel, unformattedCep, e.getMessage()));
        continue;
      }
      try {
        list.addAll(getCotacaoDeFrete(formattedCep, lojaPesquisadaModel));
      } catch (final RuntimeException e) {
        log.error(e.getMessage());
        if (e.getCause() != null) {
          log.error(e.getCause().getMessage());
        }
        list.add(buildErrorModel(lojaPesquisadaModel, formattedCep, e.getMessage()));
      }
    }
    return list;
  }

  private CotacaoDeFreteModel buildErrorModel(
      final LojaPesquisadaModel lojaPesquisadaModel, final String cep, final String message) {
    final CotacaoDeFreteModel error =
        CotacaoDeFreteModel.builder()
            .skuProduto(lojaPesquisadaModel.getSku())
            .cep(cep)
            .resultado(ResultadoCotacao.ERRO)
            .loja(getVendor())
            .mensagemDeErro(message)
            .build();
    error.setLojaPesquisada(lojaPesquisadaModel);
    cotacaoDeFreteRepository.persist(error);
    return error;
  }
}
