package br.com.centralar.services;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import java.util.List;

public interface LojaPesquisadaService {
  LojaPesquisadaModel findById(final Object id);

  void persist(final Long lojaId);

  void marcarFalha(final Long lojaId, final String msg);

  void marcarOk(final Long lojaId, final List<CotacaoDeFreteModel> cotacaoDeFreteModelList);
}
