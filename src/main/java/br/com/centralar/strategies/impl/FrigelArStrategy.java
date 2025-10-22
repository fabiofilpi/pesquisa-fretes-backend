package br.com.centralar.strategies.impl;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.Vendor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FrigelArStrategy extends BaseStrategy {
  @Override
  void validateParameters(String sku) throws IllegalArgumentException {}

  @Override
  public Vendor getVendor() {
    return Vendor.FRIGELAR;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel) {
    return List.of();
  }
}
