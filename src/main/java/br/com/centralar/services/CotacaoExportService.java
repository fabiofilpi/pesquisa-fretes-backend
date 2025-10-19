package br.com.centralar.services;

import jakarta.ws.rs.core.StreamingOutput;

public interface CotacaoExportService {
  StreamingOutput getCotacaoExportFile(final long idPesquisa);
}
