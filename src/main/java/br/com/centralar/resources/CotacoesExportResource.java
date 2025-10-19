package br.com.centralar.resources;

import br.com.centralar.services.CotacaoExportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/api/cotacoes")
public class CotacoesExportResource {
  @Inject CotacaoExportService cotacaoExportService;

  @GET
  @Path("/export.csv")
  @Produces("text/csv; charset=UTF-8")
  public Response exportarCsv(@QueryParam("id") long id) {
    final var stream = cotacaoExportService.getCotacaoExportFile(id);
    return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=\"cotacoes.csv\"")
        .build();
  }
}
