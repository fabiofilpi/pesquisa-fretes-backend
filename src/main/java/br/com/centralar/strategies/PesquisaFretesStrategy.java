package br.com.centralar.strategies;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.enums.Vendor;
import java.util.List;

public interface PesquisaFretesStrategy {
  Vendor getVendor();

  List<CotacaoDeFreteModel> cotarFretes(
      final LojaPesquisadaModel lojaPesquisadaModel, final List<String> ceps);
}
