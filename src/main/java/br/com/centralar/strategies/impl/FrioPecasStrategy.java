package br.com.centralar.strategies.impl;

import br.com.centralar.dtos.ShippingOptionDTO;
import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.enums.Vendor;
import br.com.centralar.utils.PesquisaFretesUtils;
import br.com.centralar.integrations.vtex.FrioPecasShippingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FrioPecasStrategy extends BaseStrategy {
  @Inject FrioPecasShippingService friopecasShippingService;

  @Override
  public Vendor getVendor() {
    return Vendor.FRIOPECAS;
  }

  @Override
  List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel) {
    final var resposta = friopecasShippingService.cotarFrete("BRA", "13087-500", "155530", "1", 1);
    final var lista = new ArrayList<CotacaoDeFreteModel>();
    for (final ShippingOptionDTO i : resposta) {
      final var sku = lojaPesquisadaModel.getSku();
      final var valor = PesquisaFretesUtils.fromCentsToReaisDouble(i.priceInCents());
      final var prazo = PesquisaFretesUtils.removeBdAndReturnInt(i.estimate());
      final var modo = i.friendlyName();
      final CotacaoDeFreteModel model =
          CotacaoDeFreteModel.builder()
              .valor(valor)
              .prazoEmDias(prazo)
              .skuProduto(sku)
              .cep(cep)
              .modo(modo)
              .resultado(ResultadoCotacao.SUCESSO)
              .loja(getVendor())
              .build();
      model.setLojaPesquisada(lojaPesquisadaModel);
      cotacaoDeFreteRepository.persist(model);
      lista.add(model);
    }
    return lista;
  }
}
