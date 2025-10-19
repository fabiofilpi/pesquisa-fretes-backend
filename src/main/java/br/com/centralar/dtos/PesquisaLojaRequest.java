package br.com.centralar.dtos;

import br.com.centralar.enums.Vendor;
import lombok.Getter;

@Getter
public class PesquisaLojaRequest {
  private Vendor loja;
  private String sku;
}
