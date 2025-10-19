package br.com.centralar.strategies.impl;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.Vendor;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class LeverosStrategy extends BaseStrategy {
  @Override
  public Vendor getVendor() {
    return Vendor.LEVEROS;
  }
    @Override
    List<CotacaoDeFreteModel> getCotacaoDeFrete(final String cep, final LojaPesquisadaModel lojaPesquisadaModel) {
        return List.of();
    }

}
