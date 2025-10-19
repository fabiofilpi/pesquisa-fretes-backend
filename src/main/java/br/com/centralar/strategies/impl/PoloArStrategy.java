package br.com.centralar.strategies.impl;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.enums.Vendor;
import br.com.centralar.utils.PesquisaFretesUtils;
import br.com.centralar.vtex.PoloArShippingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PoloArStrategy extends BaseStrategy {
  @Inject
  PoloArShippingService poloarShippingService;

  @Override
  public Vendor getVendor() {
    return Vendor.POLOAR;
  }

  @Override
  List<CotacaoDeFreteModel> getCotacaoDeFrete(
      final String cep, final LojaPesquisadaModel lojaPesquisadaModel) {
    final var resposta = poloarShippingService.cotarFrete("BRA", "13087-500", "1263", "1", 1);
    final var lista = new ArrayList<CotacaoDeFreteModel>();
    for (final PoloArShippingService.ShippingOptionDTO dto : resposta.options) {
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
