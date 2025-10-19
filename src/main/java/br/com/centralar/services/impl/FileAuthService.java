package br.com.centralar.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@ApplicationScoped
public class FileAuthService {

  private final Properties users = new Properties();
  private final Properties roles = new Properties();

  public FileAuthService() {
    loadProps("users.properties", users);
    loadProps("roles.properties", roles);
  }

  private void loadProps(String name, Properties target) {
    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
      if (is == null) throw new IllegalStateException(name + " não encontrado no classpath");
      target.load(is);
    } catch (IOException e) {
      throw new IllegalStateException("Erro lendo " + name, e);
    }
  }

  public boolean isValid(String username, String password) {
    String expected = users.getProperty(username);
    return expected != null && Objects.equals(expected, password);
  }

  public Set<String> rolesOf(String username) {
    String csv = roles.getProperty(username);
    if (csv == null || csv.isBlank()) return Set.of();
    String[] arr = csv.split("\\s*,\\s*");
    return new HashSet<>(Arrays.asList(arr));
  }
}
