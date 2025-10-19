package br.com.centralar.services.impl;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TokenService {
  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String issuer;

  // 1 hora (ajuste à vontade)
  public String generate(String subject, Set<String> groups) {
    long now = Instant.now().getEpochSecond();
    long exp = now + 3600;

    // Como configuramos smallrye.jwt.sign.key.location, basta chamar .sign()
    return Jwt.issuer(issuer).subject(subject).groups(groups).issuedAt(now).expiresAt(exp).sign();
  }

  public long defaultTtlSeconds() {
    return 3600;
  }
}
