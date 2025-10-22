package br.com.centralar.integrations.vtex;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PoloArShippingService {

  private static final String BASE_URL = "https://www.poloar.com.br/api/graphql";
  // do seu cURL
  private static final String OPERATION_HASH = "d6667f1de2a26b94b9b55f4b25d7d823f82635a0";

  private final HttpClient http = HttpClient.newHttpClient();

  /**
   * Faz a cotação na Polo Ar.
   *
   * @param country ex.: "BRA"
   * @param postalCode ex.: "13087-500"
   * @param skuId ex.: "1263"
   * @param seller ex.: "1"
   * @param quantity ex.: 1
   */
  public ShippingResult cotarFrete(
      String country, String postalCode, String skuId, String seller, int quantity) {
    try {
      // 1) variables = JSON (SEM base64; esse endpoint usa JSON url-encodado)
      // No exemplo, quantity é número; id/seller são strings.
      var variables =
          new JsonObject()
              .put(
                  "items",
                  new JsonArray()
                      .add(
                          new JsonObject()
                              .put("id", skuId)
                              .put("quantity", quantity)
                              .put("seller", seller)))
              .put("postalCode", postalCode)
              .put("country", country);

      // 2) URL-encode do JSON
      String variablesEncoded = URLEncoder.encode(variables.encode(), StandardCharsets.UTF_8);

      // Se notar que o servidor exige **duplo encode** (ex: aparece %257B no cURL),
      // descomente a linha abaixo:
      // variablesEncoded = URLEncoder.encode(variablesEncoded, StandardCharsets.UTF_8);

      // 3) Monta a URI final
      var uri =
          URI.create(
              BASE_URL + "?operationHash=" + OPERATION_HASH + "&variables=" + variablesEncoded);

      var req = HttpRequest.newBuilder(uri).GET().header("Accept", "application/json").build();

      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() / 100 != 2) {
        throw new IllegalStateException(
            "HTTP " + resp.statusCode() + " ao chamar PoloAr: " + resp.body());
      }

      // 4) Parse do JSON
      var root = new JsonObject(resp.body());
      var shipping = root.getJsonObject("data").getJsonObject("shipping");

      // address
      var addrJson = shipping.getJsonObject("address");
      var address =
          new AddressDTO(
              addrJson.getString("city"),
              addrJson.getString("neighborhood"),
              addrJson.getString("state"));

      // opções (slas)
      var logisticsInfo = shipping.getJsonArray("logisticsInfo");
      List<ShippingOptionDTO> options = new ArrayList<>();
      if (logisticsInfo != null && !logisticsInfo.isEmpty()) {
        var slas = logisticsInfo.getJsonObject(0).getJsonArray("slas");
        if (slas != null) {
          for (int i = 0; i < slas.size(); i++) {
            var sla = slas.getJsonObject(i);
            options.add(
                new ShippingOptionDTO(
                    sla.getString("carrier"), // "Entrega"
                    sla.getLong("price"), // 0
                    sla.getString("shippingEstimate"), // "12bd"
                    sla.getString("localizedEstimates") // "Up to 12 business days"
                    ));
          }
        }
      }

      return new ShippingResult(address, options);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("Falha ao consultar frete (PoloAr)", e);
    }
  }

  public record ShippingOptionDTO(
      String carrier,
      long priceInCents,
      String estimate, // ex.: "12bd"
      String localizedEstimates // ex.: "Up to 12 business days"
      ) {}

  public record AddressDTO(String city, String neighborhood, String state) {}

  @Getter
  public static final class ShippingResult {
    public final AddressDTO address;
    public final List<ShippingOptionDTO> options;

    public ShippingResult(AddressDTO address, List<ShippingOptionDTO> options) {
      this.address = address;
      this.options = options;
    }
  }
}
