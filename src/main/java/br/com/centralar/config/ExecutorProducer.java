package br.com.centralar.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

@ApplicationScoped
public class ExecutorProducer {

  @Produces
  @ApplicationScoped
  @JobExecutor
  ManagedExecutor jobExecutor() {
    // Sem maxAsync ⇒ sem teto artificial
    // Não propagar contexto de request/tx para as threads assíncronas
    return ManagedExecutor.builder()
        .propagated(ThreadContext.NONE)
        .cleared(ThreadContext.ALL_REMAINING)
        .build();
  }
}
