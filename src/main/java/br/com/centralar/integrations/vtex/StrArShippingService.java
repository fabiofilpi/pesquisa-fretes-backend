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
public class StrArShippingService {

  private static final String BASE_URL = "https://www.strar.com.br/api/graphql";
  private static final String OPERATION_NAME = "ClientShippingSimulationQuery";
  private static final String OPERATION_HASH = "c35bad22f67f3eb34fea52bb49efa6b1da6b728d";

  private final HttpClient http = HttpClient.newHttpClient();

  /**
   * Simula frete na Strar.
   *
   * @param country ex.: "BRA"
   * @param postalCode ex.: "13087-500"
   * @param skuId ex.: "23"
   * @param seller ex.: "1"
   * @param quantity ex.: 1
   */
  public ShippingResult cotarFrete(
      String country, String postalCode, String skuId, String seller, int quantity) {
    try {
      // 1) variables = JSON (SEM base64; aqui é JSON url-encodado)
      var variables =
          new JsonObject()
              .put(
                  "items",
                  new JsonArray()
                      .add(
                          new JsonObject()
                              .put("id", skuId) // id/seller como string (conforme exemplo)
                              .put("quantity", quantity) // quantity numérico
                              .put("seller", seller)))
              .put("postalCode", postalCode)
              .put("country", country);

      // 2) URL-encode do JSON
      String variablesEncoded = URLEncoder.encode(variables.encode(), StandardCharsets.UTF_8);

      // Se o servidor exigir **duplo encode** (curl mostra %257B...), habilite:
      // variablesEncoded = URLEncoder.encode(variablesEncoded, StandardCharsets.UTF_8);

      // 3) Montar URI final com operationName + operationHash + variables
      var uri =
          URI.create(
              BASE_URL
                  + "?operationName="
                  + OPERATION_NAME
                  + "&operationHash="
                  + OPERATION_HASH
                  + "&variables="
                  + variablesEncoded);

      var req = HttpRequest.newBuilder(uri).GET().header("Accept", "application/json").build();

      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() / 100 != 2) {
        throw new IllegalStateException(
            "HTTP " + resp.statusCode() + " ao chamar Strar: " + resp.body());
      }

      // 4) Parse do JSON
      var root = new JsonObject(resp.body());
      var shipping = root.getJsonObject("data").getJsonObject("shipping");

      var addrJson = shipping.getJsonObject("address");
      var address =
          new AddressDTO(
              addrJson.getString("city"),
              addrJson.getString("neighborhood"),
              addrJson.getString("state"));

      var logisticsInfo = shipping.getJsonArray("logisticsInfo");
      List<ShippingOptionDTO> options = new ArrayList<>();
      if (logisticsInfo != null && !logisticsInfo.isEmpty()) {
        var slas = logisticsInfo.getJsonObject(0).getJsonArray("slas");
        if (slas != null) {
          for (int i = 0; i < slas.size(); i++) {
            var sla = slas.getJsonObject(i);
            options.add(
                new ShippingOptionDTO(
                    sla.getString("carrier"),
                    sla.getString("deliveryChannel"),
                    sla.getLong("price"),
                    sla.getString("shippingEstimate"),
                    sla.getString("localizedEstimates")));
          }
        }
      }

      return new ShippingResult(address, options);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("Falha ao consultar frete (Strar)", e);
    }
  }

  public record ShippingOptionDTO(
      String carrier, // "Entrega"
      String deliveryChannel, // "delivery"
      long priceInCents, // 0
      String estimate, // "4bd"
      String localizedEstimates // "Up to 4 business days"
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
