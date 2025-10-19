package br.com.centralar.filters;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class CookieAuthFilter implements ContainerRequestFilter {
  @Override
  public void filter(ContainerRequestContext ctx) {
    var c = ctx.getCookies().get("access_token");
    if (c != null && !c.getValue().isBlank()) {
      // Injeta o header padrão para o mecanismo JWT do Quarkus validar
      ctx.getHeaders().putSingle("Authorization", "Bearer " + c.getValue());
    }
  }
}
