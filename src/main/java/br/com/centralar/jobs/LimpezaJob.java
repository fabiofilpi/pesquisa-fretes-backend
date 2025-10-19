package br.com.centralar.jobs;

import br.com.centralar.repositories.JobInfoRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LimpezaJob {

  private static final Logger LOG = Logger.getLogger(LimpezaJob.class);

  @Inject JobInfoRepository jobInfoRepository;

  /** Roda todo dia às 03:00. Ajuste conforme sua janela de manutenção. */
  @Scheduled(cron = "0 0 3 * * ?")
  @Transactional
  void expurgar() {
    final Duration ttl = Duration.ofDays(30);

    final long deletados = jobInfoRepository.bulkDeleteFinishedOlderThan(ttl);

    LOG.infof("Expurgo concluído. %d JobInfoModel removidos (TTL=%s).", deletados, ttl);
  }
}
