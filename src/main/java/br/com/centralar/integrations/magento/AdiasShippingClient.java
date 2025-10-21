// br/com/centralar/integrations/adias/AdiasShippingClient.java
package br.com.centralar.integrations.magento;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/intelipost/product/shipping")
@RegisterRestClient(configKey = "adias-api")
public interface AdiasShippingClient {

  @GET
  @Produces(MediaType.TEXT_HTML)
  String getShippingHtml(
      @QueryParam("product") long product,
      @QueryParam("item") long item,
      @QueryParam("qty") int qty,
      @QueryParam("sku") String sku,
      @QueryParam("country") String country,
      @QueryParam("postcode") String postcode);
}
