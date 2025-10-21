// br/com/centralar/integrations/adias/AdiasShippingService.java
package br.com.centralar.integrations.magento;

import br.com.centralar.dtos.AdiasShippingOption;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@ApplicationScoped
public class AdiasShippingService {

  @Inject @RestClient AdiasShippingClient client;

  private static final Pattern NON_DIGITS = Pattern.compile("\\D+");
  private static final Pattern CURRENCY_EXTRACT = Pattern.compile("[^0-9,.-]");

  /**
   * Consulta cotação na Adias.
   *
   * @param productId ex.: 22549
   * @param sku ex.: PRINVHIW09F2GRA
   * @param postcode ex.: 13087-500 (com ou sem hífen)
   * @param quantity ex.: 1
   */
  @Retry(maxRetries = 2, delay = 300, jitter = 200) // remova se não usar fault-tolerance
  public List<AdiasShippingOption> cotarFrete(
      final long productId, final String sku, final String postcode, final int quantity) {
    final String cep = normalizeCep(postcode);
    final String html = client.getShippingHtml(productId, productId, quantity, sku, "BR", cep);
    return parseHtml(html);
  }

  public List<AdiasShippingOption> cotarFrete(
      final long productId, final String sku, final String postcode) {
    return cotarFrete(productId, sku, postcode, 1);
  }

  /* ------------------ helpers ------------------ */

  private List<AdiasShippingOption> parseHtml(String html) {
    final List<AdiasShippingOption> result = new ArrayList<>();
    if (html == null || html.isBlank()) return result;

    final Document doc = Jsoup.parse(html);
    for (Element row : doc.select("table.product-shipping-table tr")) {
      final Element tdDesc = row.selectFirst("td:nth-of-type(1)");
      final Element tdPrice = row.selectFirst("td:nth-of-type(2) .price");

      if (tdDesc == null || tdPrice == null) continue;

      final String description = tdDesc.text().trim();
      final Integer days = extractBusinessDays(description);
      final BigDecimal price = parseBrl(tdPrice.text());

      if (price != null) {
        result.add(
            AdiasShippingOption.builder()
                .description(description)
                .businessDays(days)
                .price(price)
                .build());
      }
    }
    return result;
  }

  /** Extrai "6" de "Previsão de 6 dias úteis para entrega". */
  private Integer extractBusinessDays(String description) {
    if (description == null) return null;
    final var m =
        Pattern.compile(
                "(\\d{1,3})\\s*dias?\\s+úteis", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
            .matcher(description);
    if (m.find()) {
      try {
        return Integer.parseInt(m.group(1));
      } catch (NumberFormatException ignored) {
      }
    }
    // fallback: pega o primeiro número que aparecer
    final var m2 = Pattern.compile("(\\d{1,3})").matcher(description);
    if (m2.find()) {
      try {
        return Integer.parseInt(m2.group(1));
      } catch (NumberFormatException ignored) {
      }
    }
    return null;
  }

  /** Converte "R$ 119,90" -> 119.90 (BigDecimal). */
  private BigDecimal parseBrl(String brlText) {
    if (brlText == null) return null;
    // remove símbolos e espaços não-quebrantes, mantém dígitos/.,,
    String cleaned = CURRENCY_EXTRACT.matcher(brlText).replaceAll("");
    cleaned = cleaned.replace(".", "").replace(",", "."); // pt-BR -> ponto decimal
    if (cleaned.isBlank()) return null;
    try {
      return new BigDecimal(cleaned);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String normalizeCep(String cep) {
    if (cep == null) return null;
    return NON_DIGITS.matcher(cep).replaceAll("");
  }
}
