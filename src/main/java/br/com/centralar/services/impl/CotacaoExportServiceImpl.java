package br.com.centralar.services.impl;

import br.com.centralar.dtos.CotacaoDeFreteDto;
import br.com.centralar.enums.ResultadoCotacao;
import br.com.centralar.repositories.CotacaoDeFreteRepository;
import br.com.centralar.services.CotacaoExportService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class CotacaoExportServiceImpl implements CotacaoExportService {
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Inject CotacaoDeFreteRepository cotacaoDeFreteRepository;

  @Override
  public StreamingOutput getCotacaoExportFile(final long idPesquisa) {
    final List<CotacaoDeFreteDto> cotacoesBemSucedidas =
        cotacaoDeFreteRepository.findCotacoesPorPesquisa(idPesquisa, ResultadoCotacao.SUCESSO);
    final List<CotacaoDeFreteDto> cotacoesComErros =
        cotacaoDeFreteRepository.findCotacoesPorPesquisa(idPesquisa, ResultadoCotacao.ERRO);

    return (OutputStream os) -> {
      try (final Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
        // Cabeçalho
        writer.write(
            "Loja;SKU;CEP;Modo;Prazo Em Dias;Valor;Resultado;Erro;Data ultima alteracao;ID\n");

        for (final CotacaoDeFreteDto c : cotacoesBemSucedidas) {
          writer.write(toCsvLine(c));
          writer.write("\n");
        }

        for (final CotacaoDeFreteDto c : cotacoesComErros) {
          writer.write(toCsvLine(c));
          writer.write("\n");
        }
      } catch (final Exception e) {
        throw new UncheckedIOException(new IOException("Falha ao gerar CSV", e));
      }
    };
  }

  private String toCsvLine(final CotacaoDeFreteDto c) {
    final String data =
        c.getDataUltimaAlteracao() != null ? FORMATTER.format(c.getDataUltimaAlteracao()) : "";
    final String loja = c.getLoja() != null ? c.getLoja().name() : "";
    final String resultado = c.getResultado() != null ? c.getResultado().name() : "";

    return String.join(
        ";",
        safe(loja),
        safe(c.getSkuProduto()),
        safe(c.getCep()),
        safe(c.getModo()),
        safe(c.getPrazoEmDias()),
        safe(c.getValor()),
        safe(resultado),
        safe(c.getMensagemDeErro()),
        safe(data),
        safe(c.getId()));
  }

  private String safe(final Object value) {
    if (value == null) return "";
    final String str = value.toString().replace("\"", "\"\""); // escapa aspas
    // se tiver ponto e vírgula, aspas ou quebra de linha, envolve em aspas
    if (str.contains(";") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
      return "\"" + str + "\"";
    }
    return str;
  }
}
