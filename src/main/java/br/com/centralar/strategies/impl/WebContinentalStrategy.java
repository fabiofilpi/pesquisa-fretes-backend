package br.com.centralar.strategies.impl;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.enums.Vendor;
import br.com.centralar.utils.PesquisaFretesUtils;
import br.com.centralar.vtex.WebContinentalShippingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class WebContinentalStrategy extends BaseStrategy {
  @Inject WebContinentalShippingService webContinentalShippingService;

  @Override
  public Vendor getVendor() {
    return Vendor.WEBCONTINENTAL;
  }

  @Override
  List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel) {
    final var resposta = webContinentalShippingService.cotarFrete("BRA", "13087-500", "4638345", "006176", 1);
    final var lista = new ArrayList<CotacaoDeFreteModel>();
    for (final WebContinentalShippingService.ShippingOptionDTO dto : resposta) {
      final var sku = lojaPesquisadaModel.getSku();
      final var valor = PesquisaFretesUtils.fromCentsToReaisDouble(dto.priceInCents());
      final var prazo = PesquisaFretesUtils.removeBdAndReturnInt(dto.estimate());
      final CotacaoDeFreteModel model =
          CotacaoDeFreteModel.builder()
              .valor(valor)
              .prazoEmDias(prazo)
              .skuProduto(sku)
              .cep(cep)
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
