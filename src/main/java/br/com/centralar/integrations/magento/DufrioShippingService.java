package br.com.centralar.integrations.magento;

import br.com.centralar.dtos.DuFrioShippingRate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class DufrioShippingService {

  private static final Pattern NON_DIGITS = Pattern.compile("\\D");

  @Inject @RestClient DufrioProductClient client;

  /**
   * Consulta cotação de frete para um produto + CEP.
   *
   * @param productId ID do produto (ex.: 10820)
   * @param postcode CEP (com ou sem hífen)
   * @param quantity quantidade (default comum: 1)
   */
  // (Opcional) 2 tentativas com backoff, só se você adicionou quarkus-smallrye-fault-tolerance
  @Retry(maxRetries = 2, delay = 300, jitter = 200)
  public List<DuFrioShippingRate> cotarFrete(
      final long productId, final String postcode, final int quantity) {
    final String normalizedCep = normalizeCep(postcode);
    final Map<String, List<DuFrioShippingRate>> raw =
        client.getShippingQuote(productId, productId, quantity, productId, "BR", normalizedCep);

    if (raw == null || raw.isEmpty()) {
      return List.of();
    }

    // Achatar carrier -> lista de opções
    return raw.values().stream()
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .filter(Objects::nonNull)
        .toList();
  }

  public List<DuFrioShippingRate> cotarFrete(final long productId, final String postcode) {
    return cotarFrete(productId, postcode, 1);
  }

  private String normalizeCep(String cep) {
    if (cep == null) return null;
    return NON_DIGITS.matcher(cep).replaceAll("");
  }
}
