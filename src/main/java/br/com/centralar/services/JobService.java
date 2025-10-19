package br.com.centralar.services;

import br.com.centralar.dtos.JobInfoDto;
import br.com.centralar.dtos.PagedResult;
import br.com.centralar.dtos.PesquisaRequest;

public interface JobService {
  Long startJob(final PesquisaRequest pesquisaRequest);

  PagedResult<JobInfoDto> findAll(final int page, final int size);

  JobInfoDto findById(final Long id);
}
