package br.com.centralar.strategies;

import br.com.centralar.enums.Vendor;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class StrategyRegistry {

  @Inject Instance<PesquisaFretesStrategy> strategies;

  private Map<Vendor, PesquisaFretesStrategy> byVendor;

  @PostConstruct
  void init() {
    byVendor = new EnumMap<>(Vendor.class);
    StreamSupport.stream(strategies.spliterator(), false)
        .forEach(s -> byVendor.put(s.getVendor(), s));
  }

  public PesquisaFretesStrategy get(final Vendor vendor) {
    final PesquisaFretesStrategy s = byVendor.get(vendor);
    if (s == null) {
      throw new IllegalStateException("Sem strategy registrada para vendor: " + vendor);
    }
    return s;
  }
}
