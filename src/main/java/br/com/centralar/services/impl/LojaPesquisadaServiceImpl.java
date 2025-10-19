package br.com.centralar.services.impl;

import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.LojaPesquisadaModel;
import br.com.centralar.repositories.LojaPesquisadaRepository;
import br.com.centralar.services.LojaPesquisadaService;
import br.com.centralar.utils.PesquisaFretesUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class LojaPesquisadaServiceImpl implements LojaPesquisadaService {
  @Inject LojaPesquisadaRepository lojaPesquisadaRepository;

  @Override
  public LojaPesquisadaModel findById(final Object id) {
    return lojaPesquisadaRepository.findById((Long) id);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void persist(final Long lojaId) {
    final var lojaPesquisadaModel = lojaPesquisadaRepository.findById(lojaId);
    if (lojaPesquisadaModel != null) {
      lojaPesquisadaRepository.persist(lojaPesquisadaModel);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void marcarOk(final Long lojaId, final List<CotacaoDeFreteModel> cotacaoDeFreteModelList) {
    final var lojaPesquisadaModel = lojaPesquisadaRepository.findById(lojaId);
    final var qtd = cotacaoDeFreteModelList.size();
    if (lojaPesquisadaModel != null) {
      lojaPesquisadaModel.setUltimaExecucao(LocalDateTime.now());
      lojaPesquisadaModel.setMensagemStatus("OK (" + qtd + " cotações)");
      lojaPesquisadaRepository.persist(lojaPesquisadaModel);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void marcarFalha(final Long lojaId, final String msg) {
    final var lojaPesquisadaModel = lojaPesquisadaRepository.findById(lojaId);
    if (lojaPesquisadaModel != null) {
      lojaPesquisadaModel.setUltimaExecucao(LocalDateTime.now());
      lojaPesquisadaModel.setMensagemStatus(
          "FALHA: "
              + PesquisaFretesUtils.truncateMessage(msg, PesquisaFretesUtils.ERROR_MESSAGE_LENGTH));
      lojaPesquisadaRepository.persist(lojaPesquisadaModel);
    }
  }
}
