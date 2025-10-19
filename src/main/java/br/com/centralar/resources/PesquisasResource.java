package br.com.centralar.resources;

import br.com.centralar.dtos.*;
import br.com.centralar.services.JobService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/api/admin/pesquisas")
public class PesquisasResource {
  @Inject JobService jobService;

  @POST
  @Path("/run")
  public Response run(@Valid @NotNull final PesquisaRequest pesquisaRequest) {
    Long jobId;
    try {
      jobId = jobService.startJob(pesquisaRequest);
    } catch (final Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.accepted().entity("{\"jobId\":\"" + jobId + "\"}").build();
  }

  @GET
  @Path("/{id}")
  public JobInfoDto get(@PathParam("id") Long id) {
    return jobService.findById(id);
  }

  @GET
  public PagedResult<JobInfoDto> findAll(
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("10") int size) {

    // Busca paginada com ordenação por data de criação (ou outro campo que preferir)
    return jobService.findAll(page, size);
  }
}
