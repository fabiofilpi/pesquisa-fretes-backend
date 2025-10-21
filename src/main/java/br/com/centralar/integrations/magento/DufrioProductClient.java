package br.com.centralar.integrations.magento;

import br.com.centralar.dtos.DuFrioShippingRate;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;
import java.util.Map;

@Path("/catalog/product")
@RegisterRestClient(configKey = "dufrio-api")
@Produces(MediaType.APPLICATION_JSON)
public interface DufrioProductClient {

  @GET
  @Path("/shippingQuote")
  Map<String, List<DuFrioShippingRate>> getShippingQuote(
      @QueryParam("product") long product,
      @QueryParam("item") long item,
      @QueryParam("qty") int qty,
      @QueryParam("product_id") long productId,
      @QueryParam("country") String country,
      @QueryParam("postcode") String postcode);
}
