package br.com.centralar.integrations.vtex;

import br.com.centralar.dtos.ShippingOptionDTO;
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
public class FrioPecasShippingService {

  // Endpoint VTEX (Friopeças)
  private static final String BASE_URL = "https://www.friopecas.com.br/_v/segment/graphql/v1";
  private static final String OPERATION = "getShippingEstimates";

  // Persisted query (do seu cURL)
  private static final String HASH =
      "7ce5ad49f177bdecfef578def58ba597a57ae64295229da99c804bfe933d4b42";
  private static final String SENDER = "vtex.store-components@3.x";
  private static final String PROVIDER = "vtex.store-graphql@2.x";

  private final HttpClient http = HttpClient.newHttpClient();

  public List<ShippingOptionDTO> cotarFrete(
      String country, // ex.: "BRA"
      String postalCode, // ex.: "13087-500"
      String skuId, // ex.: "155530"
      String seller, // ex.: "1"
      int quantity // ex.: 1
      ) {
    try {
      // 1) variables => Base64(JSON)
      var variables =
          new JsonObject()
              .put("country", country)
              .put("postalCode", postalCode)
              .put(
                  "items",
                  new JsonArray()
                      .add(
                          new JsonObject()
                              // manter como String, igual ao exemplo VTEX
                              .put("quantity", String.valueOf(quantity))
                              .put("id", skuId)
                              .put("seller", seller)));

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

      // 3) encode do extensions para query param
      String extensionsEncoded = URLEncoder.encode(extensions.encode(), StandardCharsets.UTF_8);

      // Se o ambiente exigir **duplo encode** (como o seu curl mostra %257B...), descomente:
      // extensionsEncoded = URLEncoder.encode(extensionsEncoded, StandardCharsets.UTF_8);

      // 4) Montar URI final
      var uri =
          URI.create(BASE_URL + "?operationName=" + OPERATION + "&extensions=" + extensionsEncoded);

      var req = HttpRequest.newBuilder(uri).GET().header("Accept", "application/json").build();

      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() / 100 != 2) {
        throw new IllegalStateException(
            "HTTP " + resp.statusCode() + " ao chamar VTEX (Friopeças): " + resp.body());
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
      throw new RuntimeException("Falha ao consultar frete (VTEX/Friopeças)", e);
    }
  }
}
