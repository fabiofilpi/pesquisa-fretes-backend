package br.com.centralar.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PesquisaFretesUtils {
  public static final int ERROR_MESSAGE_LENGTH = 100;

  private PesquisaFretesUtils() {
    // construtor privado para evitar instância
  }

  public static String truncateMessage(final String input, final int maxLength) {
    if (input == null) {
      return null;
    }
    return input.length() > maxLength ? input.substring(0, maxLength) : input;
  }

  public static double fromCentsToReaisDouble(long cents) {
    BigDecimal reais =
        BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    return reais.doubleValue();
  }

  public static int removeBdAndReturnInt(final String estimate) {
    final var estimateWithoutBd = estimate.replace("bd", "");
    return Integer.parseInt(estimateWithoutBd);
  }
}
