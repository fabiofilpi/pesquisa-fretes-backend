package br.com.centralar.resources;

import br.com.centralar.dtos.AuthRequest;
import br.com.centralar.dtos.AuthResponse;
import br.com.centralar.services.impl.FileAuthService;
import br.com.centralar.services.impl.TokenService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

  // Nome do cookie do access token
  private static final String ACCESS_COOKIE = "access_token";
  @Inject FileAuthService fileAuth;
  @Inject TokenService tokenService;
  @Inject SecurityIdentity identity;

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  @POST
  @PermitAll
  @Path("/login")
  public Response login(AuthRequest req, @Context UriInfo uri) {
    if (req == null || isBlank(req.username) || isBlank(req.password)) {
      throw new BadRequestException("username e password são obrigatórios");
    }

    if (!fileAuth.isValid(req.username, req.password)) {
      throw new NotAuthorizedException("Credenciais inválidas");
    }

    var groups = fileAuth.rolesOf(req.username);
    var token = tokenService.generate(req.username, groups);

    // Cria cookie seguro com SameSite=Strict (ou Lax se tiver SPA em outro domínio)
    NewCookie cookie =
        new NewCookie.Builder(ACCESS_COOKIE)
            .value(token)
            .path("/") // não defina Domain -> vale pro host atual (IP/localhost)
            .httpOnly(true)
            .secure(false) // SEM HTTPS -> false
            .sameSite(NewCookie.SameSite.LAX) // LAX funciona para XHR same-origin via proxy
            .maxAge((int) tokenService.defaultTtlSeconds())
            .build();

    return Response.ok(new AuthResponse("ok", tokenService.defaultTtlSeconds()))
        .cookie(cookie)
        .build();
  }

  @POST
  @PermitAll
  @Path("/logout")
  public Response logout() {
    // Invalida o cookie (maxAge=0)
    final NewCookie expired =
        new NewCookie.Builder(ACCESS_COOKIE)
            .value("")
            .path("/")
            .httpOnly(true)
            .secure(true)
            .sameSite(NewCookie.SameSite.STRICT)
            .maxAge(0)
            .build();

    return Response.ok(new AuthResponse("logout", 0)).cookie(expired).build();
  }

  @GET
  @Path("/me")
  @Authenticated // exige token válido, independe de roles
  @Produces(MediaType.TEXT_PLAIN)
  public String me() {
    return identity.getPrincipal().getName(); // sub/email
  }
}
