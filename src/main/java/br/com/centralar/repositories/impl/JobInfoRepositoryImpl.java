package br.com.centralar.repositories.impl;

import br.com.centralar.dtos.JobInfoDto;
import br.com.centralar.dtos.LojaPesquisadaDto;
import br.com.centralar.dtos.PagedResult;
import br.com.centralar.enums.JobStatus;
import br.com.centralar.enums.Vendor;
import br.com.centralar.repositories.JobInfoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class JobInfoRepositoryImpl implements JobInfoRepository {

  private static final String SELECT_DTO =
      "select new br.com.centralar.dtos.JobInfoDto("
          + " j.id, j.jobStatus, j.tituloDaPesquisa, "
          + " j.dataCriacao, j.dataUltimaAlteracao, j.message, j.startedAt, j.finishedAt"
          + ") from JobInfoModel j ";
  private static final String ORDER_BY_STARTED_DESC = " order by j.startedAt desc";
  @Inject EntityManager em;

  // Helpers
  private static int normalizedPageIndex(int page) {
    return Math.max(0, page - 1);
  }

  private static int pageCount(long total, int size) {
    return (int) Math.ceil(total / (double) size);
  }

  private static PagedResult<JobInfoDto> emptyPage(int pageIndex, int size) {
    return new PagedResult<>(Collections.emptyList(), 0, pageIndex, size, 0);
  }

  private static PagedResult<JobInfoDto> pageWith(
      List<JobInfoDto> items, long total, int pageIndex, int size) {
    return new PagedResult<>(items, total, pageIndex, size, pageCount(total, size));
  }

  private static <T> List<T> ensureListInitialized(List<T> current) {
    if (current == null) return new ArrayList<>();
    return (current instanceof ArrayList) ? current : new ArrayList<>(current);
  }

  @Override
  public long bulkDeleteFinishedOlderThan(final Duration ttl) {
    final LocalDateTime cutoff = LocalDateTime.now().minus(ttl);
    return delete(
        "finishedAt IS NOT NULL AND finishedAt < ?1  AND jobStatus = ?2",
        cutoff,
        JobStatus.SUCCESS);
  }

  @Override
  public JobInfoDto findDtoById(final Long id) {
    final var query =
        em.createQuery(SELECT_DTO + "where j.id = :id", JobInfoDto.class).setParameter("id", id);

    final var dtoOpt = query.getResultStream().findFirst();
    if (dtoOpt.isEmpty()) return null;

    final var dto = dtoOpt.get();

    // CEPs
    final var qCeps =
        em.createQuery("select c from JobInfoModel j join j.ceps c where j.id = :id", String.class)
            .setParameter("id", id);
    final var ceps = qCeps.getResultList();
    dto.setCeps(ceps != null ? new ArrayList<>(ceps) : new ArrayList<>());

    // Lojas
    final var qLojas =
        em.createQuery(
                "select lp.id, lp.sku, lp.loja, lp.ultimaExecucao, lp.mensagemStatus "
                    + "from LojaPesquisadaModel lp where lp.jobInfo.id = :id",
                Object[].class)
            .setParameter("id", id);

    final var lojas = new ArrayList<LojaPesquisadaDto>();
    for (Object[] row : qLojas.getResultList()) {
      final var lpDto = new LojaPesquisadaDto();
      lpDto.setId((Long) row[0]);
      lpDto.setSku((String) row[1]);
      lpDto.setLoja((Vendor) row[2]);
      lpDto.setUltimaExecucao((LocalDateTime) row[3]);
      lpDto.setMensagemStatus((String) row[4]);
      lojas.add(lpDto);
    }
    ensureListInitialized(dto.getLojasPesquisadas()).addAll(lojas);

    return dto;
  }

  @Override
  public PagedResult<JobInfoDto> findPaged(int page, int size) {
    final int pageIndex = normalizedPageIndex(page);
    final int first = pageIndex * size;

    final Long total =
        em.createQuery("select count(j) from JobInfoModel j", Long.class).getSingleResult();

    if (total == 0) return emptyPage(pageIndex, size);

    final var q =
        em.createQuery(SELECT_DTO + ORDER_BY_STARTED_DESC, JobInfoDto.class)
            .setFirstResult(first)
            .setMaxResults(size);

    final var items = q.getResultList();
    if (items.isEmpty()) return pageWith(items, total, pageIndex, size);

    hydrate(items);
    return pageWith(items, total, pageIndex, size);
  }

  @Override
  public PagedResult<JobInfoDto> findByStatusPaged(JobStatus status, int page, int size) {
    final int pageIndex = normalizedPageIndex(page);
    final int first = pageIndex * size;

    final Long total =
        em.createQuery(
                "select count(j) from JobInfoModel j where j.jobStatus = :status", Long.class)
            .setParameter("status", status)
            .getSingleResult();

    if (total == 0) return emptyPage(pageIndex, size);

    final var q =
        em.createQuery(
                SELECT_DTO + "where j.jobStatus = :status " + ORDER_BY_STARTED_DESC,
                JobInfoDto.class)
            .setParameter("status", status)
            .setFirstResult(first)
            .setMaxResults(size);

    final var items = q.getResultList();
    if (items.isEmpty()) return pageWith(items, total, pageIndex, size);

    hydrate(items);
    return pageWith(items, total, pageIndex, size);
  }

  @Override
  public PagedResult<JobInfoDto> search(String tituloLike, JobStatus status, int page, int size) {
    final int pageIndex = normalizedPageIndex(page);
    final int first = pageIndex * size;

    // WITH 1=1 novamente
    StringBuilder where = new StringBuilder(" where 1=1 ");
    Map<String, Object> params = new HashMap<>();

    if (tituloLike != null && !tituloLike.isBlank()) {
      where.append(" and lower(j.tituloDaPesquisa) like :titulo ");
      params.put("titulo", "%" + tituloLike.toLowerCase().trim() + "%");
    }
    if (status != null) {
      where.append(" and j.jobStatus = :status ");
      params.put("status", status);
    }

    var countQl = "select count(j) from JobInfoModel j" + where;
    var countQuery = em.createQuery(countQl, Long.class);
    params.forEach(countQuery::setParameter);
    final Long total = countQuery.getSingleResult();

    if (total == 0) return emptyPage(pageIndex, size);

    var pageQl = SELECT_DTO + where + ORDER_BY_STARTED_DESC;
    var pageQuery = em.createQuery(pageQl, JobInfoDto.class);
    params.forEach(pageQuery::setParameter);
    pageQuery.setFirstResult(first);
    pageQuery.setMaxResults(size);

    final var items = pageQuery.getResultList();
    if (items.isEmpty()) return pageWith(items, total, pageIndex, size);

    hydrate(items);
    return pageWith(items, total, pageIndex, size);
  }

  private void hydrate(List<JobInfoDto> items) {
    if (items == null || items.isEmpty()) return;

    final var byJobId =
        items.stream()
            .collect(
                Collectors.toMap(JobInfoDto::getId, it -> it, (a, b) -> a, LinkedHashMap::new));

    final var jobIds = new ArrayList<>(byJobId.keySet());
    if (jobIds.isEmpty()) return;

    // CEPs
    final var qCeps =
        em.createQuery(
            "select j.id, c from JobInfoModel j join j.ceps c where j.id in :ids", Object[].class);
    qCeps.setParameter("ids", jobIds);
    for (Object[] row : qCeps.getResultList()) {
      final Long jobId = (Long) row[0];
      final String cep = (String) row[1];
      final var dto = byJobId.get(jobId);
      if (dto != null) ensureListInitialized(dto.getCeps()).add(cep);
    }

    // Lojas
    final var qLojas =
        em.createQuery(
            "select lp.id, lp.sku, lp.loja, lp.ultimaExecucao, lp.mensagemStatus, lp.jobInfo.id "
                + "from LojaPesquisadaModel lp where lp.jobInfo.id in :ids",
            Object[].class);
    qLojas.setParameter("ids", jobIds);

    final var lojasPorJob = new HashMap<Long, List<LojaPesquisadaDto>>();
    for (Object[] row : qLojas.getResultList()) {
      final Long lpId = (Long) row[0];
      final String sku = (String) row[1];
      final Vendor loja = (Vendor) row[2];
      final LocalDateTime ultima = (LocalDateTime) row[3];
      final String msg = (String) row[4];
      final Long jobId = (Long) row[5];

      final var lpDto = new LojaPesquisadaDto();
      lpDto.setId(lpId);
      lpDto.setSku(sku);
      lpDto.setLoja(loja);
      lpDto.setUltimaExecucao(ultima);
      lpDto.setMensagemStatus(msg);

      lojasPorJob.computeIfAbsent(jobId, k -> new ArrayList<>()).add(lpDto);
    }

    for (var job : items) {
      final var lojas = lojasPorJob.get(job.getId());
      if (lojas != null) ensureListInitialized(job.getLojasPesquisadas()).addAll(lojas);
    }
  }
}
