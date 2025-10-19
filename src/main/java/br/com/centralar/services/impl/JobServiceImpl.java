package br.com.centralar.services.impl;

import static io.quarkus.arc.ComponentsProvider.LOG;

import br.com.centralar.config.JobExecutor;
import br.com.centralar.dtos.JobInfoDto;
import br.com.centralar.dtos.PagedResult;
import br.com.centralar.dtos.PesquisaRequest;
import br.com.centralar.entities.CotacaoDeFreteModel;
import br.com.centralar.entities.JobInfoModel;
import br.com.centralar.enums.JobStatus;
import br.com.centralar.repositories.JobInfoRepository;
import br.com.centralar.services.JobService;
import br.com.centralar.services.LojaPesquisadaService;
import br.com.centralar.strategies.StrategyRegistry;
import br.com.centralar.utils.PesquisaFretesUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
public class JobServiceImpl implements JobService {

  @Inject EntityManager em;
  @Inject StrategyRegistry strategyRegistry;
  @Inject JobInfoRepository jobInfoRepository;
  @Inject LojaPesquisadaService lojaPesquisadaService;

  @Inject @JobExecutor ManagedExecutor executor;

  @Override
  public PagedResult<JobInfoDto> findAll(final int page, final int size) {
    return jobInfoRepository.findPaged(page, size);
  }

  @Override
  public JobInfoDto findById(final Long id) {
    return jobInfoRepository.findDtoById(id);
  }

  @Override
  public Long startJob(final PesquisaRequest pesquisaRequest) {
    final var job = criarJob(pesquisaRequest); // persiste em tx separada
    CompletableFuture.runAsync(() -> runJob(job.getId()), executor)
        .exceptionally(
            ex -> {
              LOG.errorf(ex, "Falha ao iniciar job %s", job.getId());
              markJobFailed(
                  job.getId(),
                  ex.getMessage()); // este metodo pode ser @Transactional(REQUIRES_NEW) também
              return null;
            });
    return job.getId();
  }

  @Transactional
  JobInfoModel criarJob(PesquisaRequest req) {
    JobInfoModel job = JobInfoModel.createNew(req);
    jobInfoRepository.persist(job);
    return job;
  }

  void runJob(Long jobId) {
    final var job = jobInfoRepository.findById(jobId);
    if (job == null) return;

    // NÃO persista aqui; apenas leia e dispare tasks
    final List<String> ceps = new ArrayList<>(job.getCeps());
    final var lojasSnapshot = new ArrayList<>(job.getLojasPesquisadas());

    try {
      // marque RUNNING em tx própria
      marcarJobRunning(job.getId());

      final var agregados = new ConcurrentLinkedQueue<CotacaoDeFreteModel>();
      final var tasks = new ArrayList<CompletableFuture<Void>>(lojasSnapshot.size());

      for (var loja : lojasSnapshot) {
        tasks.add(
            CompletableFuture.supplyAsync(() -> processarLoja(loja.getId(), ceps), executor)
                .thenAccept(agregados::addAll)
                .exceptionally(
                    ex -> {
                      lojaPesquisadaService.marcarFalha(loja.getId(), ex.getMessage());
                      return null;
                    }));
      }

      CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();

      marcarJobSuccess(job.getId(), agregados.size());
      LOG.infof("Job %s finalizado com sucesso (%d cotações)", job.getId(), agregados.size());

    } catch (final Exception e) {
      markJobFailed(job.getId(), e.getMessage());
      LOG.errorf(e, "Job %s falhou", job.getId());
    }
  }

  List<CotacaoDeFreteModel> processarLoja(final Long lojaId, final List<String> ceps) {
    final var lojaPesquisadaModel = lojaPesquisadaService.findById(lojaId);
    var strategy = strategyRegistry.get(lojaPesquisadaModel.getLoja());
    var cotacoes = strategy.cotarFretes(lojaPesquisadaModel, ceps);
    lojaPesquisadaService.marcarOk(lojaPesquisadaModel.getId(), cotacoes); // transação nova aqui
    return cotacoes;
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  void marcarJobRunning(final Long jobId) {
    final var job = jobInfoRepository.findById(jobId);
    if (job != null) {
      job.setJobStatus(JobStatus.RUNNING);
      job.setStartedAt(LocalDateTime.now());
      jobInfoRepository.persist(job);
    }
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  void markJobFailed(final Long jobId, String msg) {
    final var job = jobInfoRepository.findById(jobId);
    if (job != null) {
      job.setJobStatus(JobStatus.FAILED);
      job.setMessage(
          PesquisaFretesUtils.truncateMessage(msg, PesquisaFretesUtils.ERROR_MESSAGE_LENGTH));
      job.setFinishedAt(LocalDateTime.now());
      jobInfoRepository.persist(job);
    }
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  void marcarJobSuccess(final Long id, int totalItems) {
    final var job = jobInfoRepository.findById(id);
    if (job != null) {
      job.setJobStatus(JobStatus.SUCCESS);
      job.setMessage("Concluído com " + totalItems + " cotações");
      job.setFinishedAt(LocalDateTime.now());
      jobInfoRepository.persist(job);
    }
  }
}
