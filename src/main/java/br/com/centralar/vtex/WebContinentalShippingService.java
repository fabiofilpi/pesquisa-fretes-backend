package br.com.centralar.vtex;

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
import java.util.Base64;
import java.util.List;

@ApplicationScoped
public class WebContinentalShippingService {

  private static final String BASE_URL = "https://www.webcontinental.com.br/_v/segment/graphql/v1";
  private static final String OPERATION = "getShippingEstimates";

  // Do seu cURL (VTEX store-components@3.x)
  private static final String HASH =
      "7ce5ad49f177bdecfef578def58ba597a57ae64295229da99c804bfe933d4b42";
  private static final String SENDER = "vtex.store-components@3.x";
  private static final String PROVIDER = "vtex.store-graphql@2.x";

  private final HttpClient http = HttpClient.newHttpClient();

  /**
   * Simula frete na Webcontinental.
   *
   * @param country ex.: "BRA"
   * @param postalCode ex.: "13087-500"
   * @param skuId ex.: "4638345"
   * @param seller ex.: "006176" (observe zeros à esquerda → manter como String)
   * @param quantity ex.: 1
   */
  public List<ShippingOptionDTO> cotarFrete(
      String country, String postalCode, String skuId, String seller, int quantity) {
    try {
      // 1) variables => Base64(JSON). Mantém id/seller/quantity como String (conforme VTEX
      // storefront).
      var variables =
          new JsonObject()
              .put("country", country)
              .put("postalCode", postalCode)
              .put(
                  "items",
                  new JsonArray()
                      .add(
                          new JsonObject()
                              .put("quantity", String.valueOf(quantity))
                              .put("id", skuId)
                              .put("seller", seller) // pode ter zeros à esquerda
                          ));

      String variablesBase64 =
          Base64.getEncoder().encodeToString(variables.encode().getBytes(StandardCharsets.UTF_8));

      // 2) extensions JSON
      var extensions =
          new JsonObject()
              .put(
                  "persistedQuery",
                  new JsonObject()
                      .put("version", 1)
                      .put("sha256Hash", HASH)
                      .put("sender", SENDER)
                      .put("provider", PROVIDER))
              .put("variables", variablesBase64);

      // 3) URL-encode do extensions para query param
      String extensionsEncoded = URLEncoder.encode(extensions.encode(), StandardCharsets.UTF_8);

      // Se o CDN exigir **duplo encode** (cURL mostra %257B...), habilite:
      // extensionsEncoded = URLEncoder.encode(extensionsEncoded, StandardCharsets.UTF_8);

      // 4) GET final
      var uri =
          URI.create(BASE_URL + "?operationName=" + OPERATION + "&extensions=" + extensionsEncoded);

      var req = HttpRequest.newBuilder(uri).GET().header("Accept", "application/json").build();

      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() / 100 != 2) {
        throw new IllegalStateException(
            "HTTP " + resp.statusCode() + " ao chamar Webcontinental: " + resp.body());
      }

      // 5) Parse do JSON
      var root = new JsonObject(resp.body());
      var logisticsInfo =
          root.getJsonObject("data").getJsonObject("shipping").getJsonArray("logisticsInfo");

      if (logisticsInfo == null || logisticsInfo.isEmpty()) return List.of();

      var slas = logisticsInfo.getJsonObject(0).getJsonArray("slas");
      if (slas == null || slas.isEmpty()) return List.of();

      List<ShippingOptionDTO> out = new ArrayList<>();
      for (int i = 0; i < slas.size(); i++) {
        var sla = slas.getJsonObject(i);
        out.add(
            new ShippingOptionDTO(
                sla.getString("id"),
                sla.getString("friendlyName"),
                sla.getLong("price"),
                sla.getString("shippingEstimate")));
      }
      return out;

    } catch (Exception e) {
      throw new RuntimeException("Falha ao consultar frete (Webcontinental)", e);
    }
  }

  public record ShippingOptionDTO(
      String id, // ex.: "express"
      String friendlyName, // ex.: "Econômico"
      long priceInCents, // ex.: 3752
      String estimate // ex.: "9bd"
      ) {}
}
