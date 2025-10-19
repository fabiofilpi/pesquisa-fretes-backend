package br.com.centralar.repositories;

import br.com.centralar.dtos.JobInfoDto;
import br.com.centralar.dtos.PagedResult;
import br.com.centralar.entities.JobInfoModel;
import br.com.centralar.enums.JobStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.time.Duration;

public interface JobInfoRepository extends PanacheRepository<JobInfoModel> {
  PagedResult<JobInfoDto> findPaged(int page, int size);

  JobInfoDto findDtoById(Long id);

  PagedResult<JobInfoDto> findByStatusPaged(JobStatus status, int page, int size);

  PagedResult<JobInfoDto> search(String tituloLike, JobStatus status, int page, int size);

  long bulkDeleteFinishedOlderThan(final Duration ttl);
}
