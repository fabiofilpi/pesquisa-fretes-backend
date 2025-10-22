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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ClimaRioShippingService {

  // Fixos do seu exemplo (persisted query VTEX)
  private static final String BASE_URL = "https://www.climario.com.br/_v/segment/graphql/v1";
  private static final String OPERATION = "getShippingEstimates";
  private static final String HASH =
      "146a0e2e3e069788326718b44934513f58f73b2da7ac3edc0b4c4090bbda64bf";
  private static final String SENDER = "climario.climario-components@1.x";
  private static final String PROVIDER = "vtex.store-graphql@2.x";

  private final HttpClient http = HttpClient.newHttpClient();

  public List<ShippingOptionDTO> cotarFrete(
      String country, // "BRA"
      String postalCode, // "13087-500"
      String skuId, // "31809"
      String seller, // "1"
      int quantity // 1
      ) {
    try {
      // 1) variables (tem que ser BASE64 do JSON)
      var variables =
          new JsonObject()
              .put("country", country)
              .put("postalCode", postalCode)
              .put(
                  "items",
                  new JsonArray()
                      .add(
                          new JsonObject()
                              // A VTEX desse endpoint costuma esperar *strings* aqui
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

      // OBS: alguns ambientes VTEX aparentam **duplo encode** (como no seu curl, que mostra
      // %257B...).
      // Se necessário, habilite a linha abaixo para “double encode”:
      // extensionsEncoded = URLEncoder.encode(extensionsEncoded, StandardCharsets.UTF_8);

      // 4) Monta URI final: ?operationName=...&extensions=...
      var uri =
          URI.create(BASE_URL + "?operationName=" + OPERATION + "&extensions=" + extensionsEncoded);

      var req = HttpRequest.newBuilder(uri).GET().header("Accept", "application/json").build();

      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() / 100 != 2) {
        throw new IllegalStateException(
            "HTTP " + resp.statusCode() + " ao chamar VTEX: " + resp.body());
      }

      // 5) Parse simples com Vert.x JsonObject (já incluso no Quarkus)
      var root = new JsonObject(resp.body());
      var slas =
          root.getJsonObject("data")
              .getJsonObject("shipping")
              .getJsonArray("logisticsInfo")
              .getJsonObject(0)
              .getJsonArray("slas");

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
      log.error(e.getMessage(), e);
      throw new RuntimeException("Falha ao consultar frete (VTEX/Climario)", e);
    }
  }
}
