package br.com.centralar.strategies.impl;

import br.com.centralar.constants.PesquisaFretesConstants;
import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.enums.Vendor;
import br.com.centralar.integrations.vtex.StrArShippingService;
import br.com.centralar.utils.PesquisaFretesUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class StrArStrategy extends BaseStrategy {
  private static final String SELLER_ID = "1";
  @Inject StrArShippingService strArShippingService;

  @Override
  void validateParameters(String sku) throws IllegalArgumentException {
    final var resposta =
        strArShippingService.cotarFrete(
            PesquisaFretesConstants.BRA, PesquisaFretesConstants.CEP_DE_EXEMPLO, sku, SELLER_ID, 1);
    log.info(resposta.toString());
  }

  @Override
  public Vendor getVendor() {
    return Vendor.STRAR;
  }

  @Override
  List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel) {
    final StrArShippingService.ShippingResult resposta =
        strArShippingService.cotarFrete(
            PesquisaFretesConstants.BRA, cep, lojaPesquisadaModel.getSku(), SELLER_ID, 1);
    final var lista = new ArrayList<CotacaoDeFreteModel>();
    for (final StrArShippingService.ShippingOptionDTO i : resposta.getOptions()) {
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
