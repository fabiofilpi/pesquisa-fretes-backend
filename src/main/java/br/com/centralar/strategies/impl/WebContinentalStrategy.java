package br.com.centralar.strategies.impl;

import br.com.centralar.constants.PesquisaFretesConstants;
import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.enums.Vendor;
import br.com.centralar.integrations.vtex.WebContinentalShippingService;
import br.com.centralar.utils.PesquisaFretesUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class WebContinentalStrategy extends BaseStrategy {
  private static final String SELLER_ID = "006176";
  @Inject WebContinentalShippingService webContinentalShippingService;

  @Override
  public Vendor getVendor() {
    return Vendor.WEBCONTINENTAL;
  }

  @Override
  void validateParameters(String sku) throws IllegalArgumentException {
    final var resposta =
        webContinentalShippingService.cotarFrete(
            PesquisaFretesConstants.BRA, PesquisaFretesConstants.CEP_DE_EXEMPLO, sku, SELLER_ID, 1);
    log.info(resposta.toString());
  }

  @Override
  List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel) {
    final var resposta =
        webContinentalShippingService.cotarFrete(
            PesquisaFretesConstants.BRA, cep, lojaPesquisadaModel.getSku(), SELLER_ID, 1);
    final var lista = new ArrayList<CotacaoDeFreteModel>();
    for (final WebContinentalShippingService.ShippingOptionDTO i : resposta) {
      final var sku = lojaPesquisadaModel.getSku();
      final var valor = PesquisaFretesUtils.fromCentsToReaisDouble(i.priceInCents());
      final var prazo = PesquisaFretesUtils.removeBdAndReturnInt(i.estimate());
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
