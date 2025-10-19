package br.com.centralar.dtos;

import br.com.centralar.constants.PesquisaFretesConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PesquisaRequest {

  @NotEmpty(message = "A lista de SKUs não pode ser vazia.")
  private List<@Valid @NotNull(message = "SKU não pode ser nulo.") PesquisaLojaRequest> skus;

  @NotEmpty(message = "A lista de CEPs não pode ser vazia.")
  private List<@NotNull(message = "CEP não pode ser nulo.") String> ceps;

  private String titulo = PesquisaFretesConstants.TITULO_PESQUISA_DEFAULT;
}
